package com.ruoyi.tdw.service.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.tdw.domain.TdwBids;
import com.ruoyi.tdw.domain.TdwContents;
import com.ruoyi.tdw.domain.TdwDuplicateFile;
import com.ruoyi.tdw.domain.TdwDuplicateResult;
import com.ruoyi.tdw.domain.TdwDuplicateTask;
import com.ruoyi.tdw.domain.TdwOutlines;
import com.ruoyi.tdw.mapper.TdwBidsMapper;
import com.ruoyi.tdw.mapper.TdwContentsMapper;
import com.ruoyi.tdw.mapper.TdwDuplicateFileMapper;
import com.ruoyi.tdw.mapper.TdwDuplicateResultMapper;
import com.ruoyi.tdw.mapper.TdwDuplicateTaskMapper;
import com.ruoyi.tdw.mapper.TdwOutlinesMapper;
import com.ruoyi.tdw.service.ITdwDuplicateService;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TdwDuplicateServiceImpl implements ITdwDuplicateService
{
    @Autowired
    private TdwDuplicateTaskMapper duplicateTaskMapper;

    @Autowired
    private TdwDuplicateFileMapper duplicateFileMapper;

    @Autowired
    private TdwDuplicateResultMapper duplicateResultMapper;

    @Autowired
    private TdwBidsMapper tdwBidsMapper;

    @Autowired
    private TdwOutlinesMapper tdwOutlinesMapper;

    @Autowired
    private TdwContentsMapper tdwContentsMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<TdwDuplicateTask> selectDuplicateTaskList(TdwDuplicateTask query)
    {
        return duplicateTaskMapper.selectDuplicateTaskList(query);
    }

    @Override
    public TdwDuplicateTask selectDuplicateTaskById(Long id)
    {
        return duplicateTaskMapper.selectDuplicateTaskById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TdwDuplicateTask createDuplicateTask(TdwDuplicateTask task, MultipartFile file) throws IOException
    {
        if (task.getBidId() == null) {
            throw new IllegalArgumentException("bidId不能为空");
        }
        if (StringUtils.isBlank(task.getTaskName())) {
            TdwBids bid = tdwBidsMapper.selectTdwBidsById(task.getBidId());
            task.setTaskName((bid == null ? "方案" : bid.getTitle()) + "查重任务");
        }
        task.setSourceType(task.getCompareBidId() == null ? "file" : "bid");
        task.setStatus("waiting");
        task.setTotalCount(0);
        task.setIssueCount(0);
        task.setTotalTextLength(0);
        task.setOverallSimilarity(BigDecimal.ZERO);
        task.setCreateTime(DateUtils.getNowDate());
        task.setUpdateTime(DateUtils.getNowDate());
        task.setDelFlag("0");
        duplicateTaskMapper.insertDuplicateTask(task);
        if (file != null && !file.isEmpty()) {
            uploadCompareFile(task.getId(), file);
        }
        return task;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TdwDuplicateFile uploadCompareFile(Long taskId, MultipartFile file) throws IOException
    {
        TdwDuplicateTask task = duplicateTaskMapper.selectDuplicateTaskById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("查重任务不存在");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("比对文件不能为空");
        }
        String fileUrl = FileUploadUtils.upload(RuoYiConfig.getUploadPath(), file);
        TdwDuplicateFile duplicateFile = new TdwDuplicateFile();
        duplicateFile.setTaskId(taskId);
        duplicateFile.setBidId(task.getBidId());
        duplicateFile.setOriginalName(file.getOriginalFilename());
        duplicateFile.setFileName(FilenameUtils.getName(fileUrl));
        duplicateFile.setFileUrl(fileUrl);
        duplicateFile.setFileSize(file.getSize());
        duplicateFile.setFileType(FilenameUtils.getExtension(file.getOriginalFilename()));
        duplicateFile.setParseStatus("mock_parsed");
        duplicateFile.setCreateTime(DateUtils.getNowDate());
        duplicateFile.setUpdateTime(DateUtils.getNowDate());
        duplicateFile.setDelFlag("0");
        duplicateFileMapper.insertDuplicateFile(duplicateFile);
        return duplicateFile;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<TdwDuplicateResult> runDuplicateTask(Long taskId)
    {
        TdwDuplicateTask task = duplicateTaskMapper.selectDuplicateTaskById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("查重任务不存在");
        }
        Date now = DateUtils.getNowDate();
        task.setStatus("running");
        task.setStartTime(now);
        task.setUpdateTime(now);
        duplicateTaskMapper.updateDuplicateTask(task);

        List<TextSegment> segments = collectBidSegments(task.getBidId());
        String compareName = resolveCompareName(task);
        List<TdwDuplicateResult> results = buildMockResults(task, segments, compareName);

        duplicateResultMapper.deleteByTaskId(taskId);
        if (!results.isEmpty()) {
            duplicateResultMapper.batchInsertDuplicateResult(results);
        }

        task.setStatus("success");
        task.setTotalCount(segments.size());
        task.setIssueCount(results.size());
        task.setTotalTextLength(totalTextLength(segments));
        task.setOverallSimilarity(calcOverallSimilarity(results));
        task.setFinishTime(DateUtils.getNowDate());
        task.setUpdateTime(DateUtils.getNowDate());
        duplicateTaskMapper.updateDuplicateTask(task);
        return results;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteDuplicateTaskByIds(Long[] ids)
    {
        duplicateResultMapper.deleteByTaskIds(ids);
        duplicateFileMapper.deleteByTaskIds(ids);
        return duplicateTaskMapper.deleteDuplicateTaskByIds(ids);
    }

    @Override
    public List<TdwDuplicateFile> selectDuplicateFileList(TdwDuplicateFile query)
    {
        return duplicateFileMapper.selectDuplicateFileList(query);
    }

    @Override
    public List<TdwDuplicateResult> selectDuplicateResultList(TdwDuplicateResult query)
    {
        return duplicateResultMapper.selectDuplicateResultList(query);
    }

    private List<TextSegment> collectBidSegments(Long bidId)
    {
        List<TdwOutlines> outlines = tdwOutlinesMapper.selectOutlinesByBidId(bidId);
        Map<Long, List<TdwOutlines>> childMap = new HashMap<Long, List<TdwOutlines>>();
        for (TdwOutlines outline : outlines) {
            Long key = outline.getParentId();
            if (!childMap.containsKey(key)) {
                childMap.put(key, new ArrayList<TdwOutlines>());
            }
            childMap.get(key).add(outline);
        }
        for (List<TdwOutlines> children : childMap.values()) {
            Collections.sort(children, new Comparator<TdwOutlines>() {
                @Override
                public int compare(TdwOutlines o1, TdwOutlines o2)
                {
                    int sort = Integer.valueOf(o1.getSortNum()).compareTo(o2.getSortNum());
                    return sort != 0 ? sort : o1.getId().compareTo(o2.getId());
                }
            });
        }
        List<TextSegment> segments = new ArrayList<TextSegment>();
        appendOutlineSegments(childMap, null, segments);
        return segments;
    }

    private void appendOutlineSegments(Map<Long, List<TdwOutlines>> childMap, Long parentId, List<TextSegment> segments)
    {
        List<TdwOutlines> children = childMap.get(parentId);
        if (children == null) {
            return;
        }
        for (TdwOutlines outline : children) {
            if (StringUtils.isNotBlank(outline.getTitle())) {
                segments.add(new TextSegment(outline.getId(), null, outline.getTitle(), "outline"));
            }
            List<TdwContents> contents = tdwContentsMapper.selectTdwContentsByOutlineId(outline.getId());
            for (TdwContents content : contents) {
                String text = contentToText(content);
                if (StringUtils.isNotBlank(text)) {
                    segments.add(new TextSegment(outline.getId(), content.getId(), text, "content"));
                }
            }
            appendOutlineSegments(childMap, outline.getId(), segments);
        }
    }

    private String contentToText(TdwContents content)
    {
        Map<String, Object> json = parseJson(content.getContent());
        if (content.getContentType() != null && content.getContentType() == 1) {
            return stringValue(json.get("text"), content.getContent());
        }
        if (content.getContentType() != null && content.getContentType() == 2) {
            StringBuilder sb = new StringBuilder();
            appendValue(sb, json.get("title"));
            appendValue(sb, json.get("headers"));
            appendValue(sb, json.get("rows"));
            return sb.toString();
        }
        if (content.getContentType() != null && content.getContentType() == 3) {
            StringBuilder sb = new StringBuilder();
            appendValue(sb, json.get("title"));
            appendValue(sb, json.get("description"));
            appendValue(sb, json.get("mermaid"));
            return sb.toString();
        }
        return content.getContent();
    }

    private Map<String, Object> parseJson(String value)
    {
        if (StringUtils.isBlank(value)) {
            return new HashMap<String, Object>();
        }
        try {
            return objectMapper.readValue(value, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return new HashMap<String, Object>();
        }
    }

    private String resolveCompareName(TdwDuplicateTask task)
    {
        if (task.getCompareBidId() != null) {
            TdwBids bid = tdwBidsMapper.selectTdwBidsById(task.getCompareBidId());
            return bid == null ? "比对方案#" + task.getCompareBidId() : bid.getTitle();
        }
        TdwDuplicateFile query = new TdwDuplicateFile();
        query.setTaskId(task.getId());
        List<TdwDuplicateFile> files = duplicateFileMapper.selectDuplicateFileList(query);
        if (!files.isEmpty()) {
            return files.get(0).getOriginalName();
        }
        return "Mock比对文件";
    }

    private List<TdwDuplicateResult> buildMockResults(TdwDuplicateTask task, List<TextSegment> segments, String compareName)
    {
        List<TdwDuplicateResult> results = new ArrayList<TdwDuplicateResult>();
        int index = 0;
        for (TextSegment segment : segments) {
            String text = normalize(segment.text);
            if (text.length() < 8) {
                continue;
            }
            if (!looksSimilar(text) && index > 1) {
                continue;
            }
            results.add(newResult(task, segment, compareName, index));
            index++;
            if (results.size() >= 8) {
                break;
            }
        }
        return results;
    }

    private TdwDuplicateResult newResult(TdwDuplicateTask task, TextSegment segment, String compareName, int index)
    {
        TdwDuplicateResult result = new TdwDuplicateResult();
        result.setTaskId(task.getId());
        result.setBidId(task.getBidId());
        result.setOutlineId(segment.outlineId);
        result.setContentId(segment.contentId);
        result.setSourceType(task.getCompareBidId() == null ? "file" : "bid");
        result.setSourceName(compareName);
        result.setSimilarText(limit(segment.text, 220));
        result.setSourceText("Mock来源片段：" + limit(segment.text, 180));
        result.setSimilarity(new BigDecimal(82 + (index % 6) * 2));
        result.setSuggestion("建议调整表达方式，补充项目特有数据和实施细节，降低模板化重复。");
        result.setCreateTime(DateUtils.getNowDate());
        result.setUpdateTime(DateUtils.getNowDate());
        result.setDelFlag("0");
        return result;
    }

    private boolean looksSimilar(String text)
    {
        return text.contains("方案") || text.contains("技术") || text.contains("实施")
                || text.contains("质量") || text.contains("服务") || text.contains("系统");
    }

    private int totalTextLength(List<TextSegment> segments)
    {
        int total = 0;
        for (TextSegment segment : segments) {
            total += normalize(segment.text).length();
        }
        return total;
    }

    private BigDecimal calcOverallSimilarity(List<TdwDuplicateResult> results)
    {
        if (results.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (TdwDuplicateResult result : results) {
            total = total.add(result.getSimilarity());
        }
        return total.divide(new BigDecimal(results.size()), 2, BigDecimal.ROUND_HALF_UP);
    }

    private String normalize(String value)
    {
        return value == null ? "" : value.replaceAll("\\s+", "");
    }

    private String stringValue(Object value, String fallback)
    {
        return value == null ? fallback : String.valueOf(value);
    }

    private void appendValue(StringBuilder sb, Object value)
    {
        if (value != null) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(String.valueOf(value));
        }
    }

    private String limit(String value, int max)
    {
        if (value == null) {
            return "";
        }
        return value.length() <= max ? value : value.substring(0, max);
    }

    private static class TextSegment
    {
        private Long outlineId;
        private Long contentId;
        private String text;
        private String segmentType;

        private TextSegment(Long outlineId, Long contentId, String text, String segmentType)
        {
            this.outlineId = outlineId;
            this.contentId = contentId;
            this.text = text;
            this.segmentType = segmentType;
        }
    }
}
