package com.ruoyi.tdw.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.tdw.domain.TdwBids;
import com.ruoyi.tdw.domain.TdwContents;
import com.ruoyi.tdw.domain.TdwOutlines;
import com.ruoyi.tdw.domain.TdwQualityItem;
import com.ruoyi.tdw.domain.TdwQualityResult;
import com.ruoyi.tdw.domain.TdwQualityTask;
import com.ruoyi.tdw.mapper.TdwBidsMapper;
import com.ruoyi.tdw.mapper.TdwContentsMapper;
import com.ruoyi.tdw.mapper.TdwOutlinesMapper;
import com.ruoyi.tdw.mapper.TdwQualityItemMapper;
import com.ruoyi.tdw.mapper.TdwQualityResultMapper;
import com.ruoyi.tdw.mapper.TdwQualityTaskMapper;
import com.ruoyi.tdw.service.ITdwQualityService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TdwQualityServiceImpl implements ITdwQualityService
{
    @Autowired
    private TdwQualityTaskMapper qualityTaskMapper;

    @Autowired
    private TdwQualityResultMapper qualityResultMapper;

    @Autowired
    private TdwQualityItemMapper qualityItemMapper;

    @Autowired
    private TdwBidsMapper tdwBidsMapper;

    @Autowired
    private TdwOutlinesMapper tdwOutlinesMapper;

    @Autowired
    private TdwContentsMapper tdwContentsMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<TdwQualityTask> selectQualityTaskList(TdwQualityTask query)
    {
        return qualityTaskMapper.selectQualityTaskList(query);
    }

    @Override
    public TdwQualityTask selectQualityTaskById(Long id)
    {
        return qualityTaskMapper.selectQualityTaskById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TdwQualityTask createQualityTask(TdwQualityTask task)
    {
        if (task == null || task.getBidId() == null) {
            throw new IllegalArgumentException("bidId不能为空");
        }
        if (StringUtils.isBlank(task.getTaskName())) {
            TdwBids bid = tdwBidsMapper.selectTdwBidsById(task.getBidId());
            task.setTaskName((bid == null ? "标书" : bid.getTitle()) + "质检任务");
        }
        if (StringUtils.isBlank(task.getSourceModule())) {
            task.setSourceModule("plan");
        }
        task.setStatus("waiting");
        task.setTotalCount(0);
        task.setIssueCount(0);
        task.setCreateTime(DateUtils.getNowDate());
        task.setUpdateTime(DateUtils.getNowDate());
        task.setDelFlag("0");
        qualityTaskMapper.insertQualityTask(task);
        return task;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<TdwQualityResult> runQualityTask(Long taskId)
    {
        TdwQualityTask task = qualityTaskMapper.selectQualityTaskById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("质检任务不存在");
        }
        Date now = DateUtils.getNowDate();
        task.setStatus("running");
        task.setStartTime(now);
        task.setUpdateTime(now);
        qualityTaskMapper.updateQualityTask(task);

        List<TdwOutlines> outlines = tdwOutlinesMapper.selectOutlinesByBidId(task.getBidId());
        Map<Long, List<TdwOutlines>> childMap = buildChildMap(outlines);
        Map<Long, List<TdwContents>> contentMap = buildContentMap(outlines);
        List<TdwQualityResult> results = new ArrayList<TdwQualityResult>();

        for (TdwOutlines outline : outlines) {
            List<TdwContents> contents = contentMap.get(outline.getId());
            if (outline.getLevel() == 3 && (contents == null || contents.isEmpty())) {
                results.add(newResult(task, outline.getId(), null, null, "empty_content", "warning", "内容标题未生成内容", "该三级内容标题下没有任何内容块。", "请生成或手动补充正文、表格或图示内容。"));
            }
            if (outline.getLevel() < 3 && isEmptyBranch(outline, childMap, contentMap)) {
                results.add(newResult(task, outline.getId(), null, null, "empty_outline", "warning", "章节为空", "该章/节下没有可检查的内容。", "请补充下级标题或内容块。"));
            }
            if (contents != null) {
                for (TdwContents content : contents) {
                    inspectContentBlock(task, outline, content, results);
                }
            }
        }

        inspectQualityItems(task, outlines, contentMap, results);

        qualityResultMapper.deleteByTaskId(taskId);
        if (!results.isEmpty()) {
            qualityResultMapper.batchInsertQualityResult(results);
        }
        task.setStatus("success");
        task.setTotalCount(outlines.size());
        task.setIssueCount(results.size());
        task.setFinishTime(DateUtils.getNowDate());
        task.setUpdateTime(DateUtils.getNowDate());
        qualityTaskMapper.updateQualityTask(task);
        return results;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteQualityTaskByIds(Long[] ids)
    {
        qualityResultMapper.deleteByTaskIds(ids);
        return qualityTaskMapper.deleteQualityTaskByIds(ids);
    }

    @Override
    public List<TdwQualityResult> selectQualityResultList(TdwQualityResult query)
    {
        return qualityResultMapper.selectQualityResultList(query);
    }

    private Map<Long, List<TdwOutlines>> buildChildMap(List<TdwOutlines> outlines)
    {
        Map<Long, List<TdwOutlines>> map = new HashMap<Long, List<TdwOutlines>>();
        for (TdwOutlines outline : outlines) {
            if (outline.getParentId() == null) {
                continue;
            }
            if (!map.containsKey(outline.getParentId())) {
                map.put(outline.getParentId(), new ArrayList<TdwOutlines>());
            }
            map.get(outline.getParentId()).add(outline);
        }
        return map;
    }

    private Map<Long, List<TdwContents>> buildContentMap(List<TdwOutlines> outlines)
    {
        Map<Long, List<TdwContents>> map = new HashMap<Long, List<TdwContents>>();
        for (TdwOutlines outline : outlines) {
            map.put(outline.getId(), tdwContentsMapper.selectTdwContentsByOutlineId(outline.getId()));
        }
        return map;
    }

    private boolean isEmptyBranch(TdwOutlines outline, Map<Long, List<TdwOutlines>> childMap, Map<Long, List<TdwContents>> contentMap)
    {
        List<TdwContents> contents = contentMap.get(outline.getId());
        if (contents != null && !contents.isEmpty()) {
            return false;
        }
        List<TdwOutlines> children = childMap.get(outline.getId());
        if (children == null || children.isEmpty()) {
            return true;
        }
        for (TdwOutlines child : children) {
            if (!isEmptyBranch(child, childMap, contentMap)) {
                return false;
            }
        }
        return true;
    }

    private void inspectContentBlock(TdwQualityTask task, TdwOutlines outline, TdwContents content, List<TdwQualityResult> results)
    {
        Map<String, Object> json = parseJson(content.getContent());
        if (content.getContentType() != null && content.getContentType() == 2) {
            Object headers = json.get("headers");
            Object rows = json.get("rows");
            if (!(headers instanceof List) || ((List<?>) headers).isEmpty() || !(rows instanceof List) || ((List<?>) rows).isEmpty()) {
                results.add(newResult(task, outline.getId(), content.getId(), null, "empty_table", "warning", "表格内容为空", "表格块缺少表头或数据行。", "请补充表头和至少一行数据。"));
            }
        }
        if (content.getContentType() != null && content.getContentType() == 3) {
            Object description = json.get("description");
            if (description == null || StringUtils.isBlank(String.valueOf(description))) {
                results.add(newResult(task, outline.getId(), content.getId(), null, "missing_image_desc", "warning", "图片/结构图缺少说明", "图片或结构图内容块未填写说明。", "请补充图示用途、模块关系或关键结论。"));
            }
        }
    }

    private void inspectQualityItems(TdwQualityTask task, List<TdwOutlines> outlines, Map<Long, List<TdwContents>> contentMap, List<TdwQualityResult> results)
    {
        if (task.getFrameworkId() == null) {
            return;
        }
        List<TdwQualityItem> items = qualityItemMapper.selectEnabledItemsByFrameworkId(task.getFrameworkId());
        if (items == null || items.isEmpty()) {
            return;
        }
        String allText = collectAllText(outlines, contentMap);
        Long firstOutlineId = outlines.isEmpty() ? null : outlines.get(0).getId();
        Set<String> emitted = new HashSet<String>();
        for (TdwQualityItem item : items) {
            String keyword = StringUtils.defaultIfBlank(item.getCheckRule(), item.getItemName());
            if (StringUtils.isBlank(keyword)) {
                continue;
            }
            String normalized = keyword.replace("必须", "").replace("应", "").replace("包含", "").trim();
            if (StringUtils.isNotBlank(normalized) && !allText.contains(normalized) && !emitted.contains(normalized)) {
                results.add(newResult(task, firstOutlineId, null, item.getId(), "quality_item", StringUtils.defaultIfBlank(item.getSeverity(), "warning"), "未满足检查项：" + item.getItemName(), "全文未明显覆盖检查项关键要求：" + normalized, "请在相关章节补充响应内容。"));
                emitted.add(normalized);
            }
        }
    }

    private String collectAllText(List<TdwOutlines> outlines, Map<Long, List<TdwContents>> contentMap)
    {
        StringBuilder sb = new StringBuilder();
        for (TdwOutlines outline : outlines) {
            sb.append(outline.getTitle()).append(' ');
            List<TdwContents> contents = contentMap.get(outline.getId());
            if (contents != null) {
                for (TdwContents content : contents) {
                    sb.append(content.getContent()).append(' ');
                }
            }
        }
        return sb.toString();
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

    private TdwQualityResult newResult(TdwQualityTask task, Long outlineId, Long contentId, Long itemId, String issueType, String severity, String title, String desc, String suggestion)
    {
        TdwQualityResult result = new TdwQualityResult();
        result.setTaskId(task.getId());
        result.setBidId(task.getBidId());
        result.setOutlineId(outlineId);
        result.setContentId(contentId);
        result.setItemId(itemId);
        result.setIssueType(issueType);
        result.setSeverity(severity);
        result.setIssueTitle(title);
        result.setIssueDesc(desc);
        result.setSuggestion(suggestion);
        result.setStatus("open");
        result.setCreateTime(DateUtils.getNowDate());
        result.setUpdateTime(DateUtils.getNowDate());
        result.setDelFlag("0");
        return result;
    }
}
