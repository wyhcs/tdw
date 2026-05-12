package com.ruoyi.tdw.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.tdw.ai.dto.GenerateContentAiRequest;
import com.ruoyi.tdw.ai.dto.GenerateContentAiResponse;
import com.ruoyi.tdw.ai.service.TdwAiService;
import com.ruoyi.tdw.domain.TdwContents;
import com.ruoyi.tdw.domain.TdwOutlines;
import com.ruoyi.tdw.domain.dto.TdwContentGenerateRequest;
import com.ruoyi.tdw.domain.dto.TdwContentSortRequest;
import com.ruoyi.tdw.mapper.TdwContentsMapper;
import com.ruoyi.tdw.mapper.TdwOutlinesMapper;
import com.ruoyi.tdw.mapper.TdwTenderParseReportMapper;
import com.ruoyi.tdw.service.ITdwContentsService;
import com.ruoyi.tdw.service.ITdwKnowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 内容块 Service，支持文本、表格、图片/结构图混排。
 *
 * @author ruoyi
 * @date 2026-03-28
 */
@Service
public class TdwContentsServiceImpl implements ITdwContentsService
{
    private static final int CONTENT_TYPE_TEXT = 1;
    private static final int CONTENT_TYPE_TABLE = 2;
    private static final int CONTENT_TYPE_DIAGRAM = 3;

    @Autowired
    private TdwContentsMapper tdwContentsMapper;

    @Autowired
    private TdwOutlinesMapper outlinesMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TdwAiService tdwAiService;

    @Autowired
    private ITdwKnowledgeService tdwKnowledgeService;

    @Autowired
    private TdwTenderParseReportMapper tenderParseReportMapper;

    @Override
    public TdwContents selectTdwContentsById(Long id)
    {
        return tdwContentsMapper.selectTdwContentsById(id);
    }

    @Override
    public List<TdwContents> selectTdwContentsList(TdwContents tdwContents)
    {
        return tdwContentsMapper.selectTdwContentsList(tdwContents);
    }

    @Override
    public List<TdwContents> selectTdwContentsByOutlineId(Long outlineId)
    {
        if (outlineId == null) {
            throw new IllegalArgumentException("outlineId不能为空");
        }
        return tdwContentsMapper.selectTdwContentsByOutlineId(outlineId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertTdwContents(TdwContents tdwContents)
    {
        validateContentBlock(tdwContents, true);
        if (tdwContents.getSortOrder() == null || tdwContents.getSortOrder() <= 0) {
            tdwContents.setSortOrder(nextSortOrder(tdwContents.getOutlineId()));
        }
        tdwContents.setCreateTime(DateUtils.getNowDate());
        tdwContents.setUpdateTime(DateUtils.getNowDate());
        return tdwContentsMapper.insertTdwContents(tdwContents);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateTdwContents(TdwContents tdwContents)
    {
        validateContentBlock(tdwContents, false);
        tdwContents.setUpdateTime(DateUtils.getNowDate());
        int rows = tdwContentsMapper.updateTdwContents(tdwContents);
        TdwContents updated = tdwContentsMapper.selectTdwContentsById(tdwContents.getId());
        if (updated != null) {
            normalizeSortOrder(updated.getOutlineId());
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteTdwContentsByIds(Long[] ids)
    {
        if (ids == null || ids.length == 0) {
            return 0;
        }
        Set<Long> outlineIds = new LinkedHashSet<>();
        for (Long id : ids) {
            TdwContents content = tdwContentsMapper.selectTdwContentsById(id);
            if (content != null) {
                outlineIds.add(content.getOutlineId());
            }
        }
        int rows = tdwContentsMapper.deleteTdwContentsByIds(ids);
        for (Long outlineId : outlineIds) {
            normalizeSortOrder(outlineId);
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteTdwContentsById(Long id)
    {
        TdwContents content = tdwContentsMapper.selectTdwContentsById(id);
        int rows = tdwContentsMapper.deleteTdwContentsById(id);
        if (content != null) {
            normalizeSortOrder(content.getOutlineId());
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int sortContents(TdwContentSortRequest request)
    {
        if (request == null || request.getOutlineId() == null) {
            throw new IllegalArgumentException("outlineId不能为空");
        }
        if (request.getContentIds() == null || request.getContentIds().isEmpty()) {
            throw new IllegalArgumentException("内容块排序列表不能为空");
        }

        List<TdwContents> contents = tdwContentsMapper.selectTdwContentsByOutlineId(request.getOutlineId());
        Set<Long> exists = new LinkedHashSet<>();
        for (TdwContents content : contents) {
            exists.add(content.getId());
        }
        Set<Long> input = new LinkedHashSet<>(request.getContentIds());
        if (exists.size() != input.size() || !exists.equals(input)) {
            throw new IllegalArgumentException("排序列表必须包含当前大纲下全部内容块");
        }
        for (int i = 0; i < request.getContentIds().size(); i++) {
            tdwContentsMapper.updateSortOrder(request.getContentIds().get(i), i + 1);
        }
        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<TdwContents> generateContentBlocks(TdwContentGenerateRequest request) throws JsonProcessingException
    {
        if (request == null) {
            throw new IllegalArgumentException("生成参数不能为空");
        }
        String defaultScope = request.getBidId() != null && request.getOutlineId() == null ? "full" : "selected";
        String scope = normalizeOption(request.getScope(), defaultScope);
        String mode = normalizeOption(request.getMode(), "append");
        if (!"full".equals(scope) && !"selected".equals(scope)) {
            throw new IllegalArgumentException("scope只能是full或selected");
        }
        if (!"overwrite".equals(mode) && !"append".equals(mode) && !"keep".equals(mode)) {
            throw new IllegalArgumentException("mode只能是overwrite、append或keep");
        }

        List<TdwOutlines> targetOutlines = resolveTargetOutlines(request, scope);
        List<TdwContents> result = new ArrayList<>();
        for (TdwOutlines outline : targetOutlines) {
            List<TdwContents> generated = mockGenerateBlocks(outline, request);
            if (!"keep".equals(mode)) {
                if ("overwrite".equals(mode)) {
                    tdwContentsMapper.deleteByOutlineId(outline.getId());
                }
                int startSortOrder = "overwrite".equals(mode) ? 1 : nextSortOrder(outline.getId());
                for (int i = 0; i < generated.size(); i++) {
                    TdwContents content = generated.get(i);
                    content.setSortOrder(startSortOrder + i);
                    content.setCreateTime(DateUtils.getNowDate());
                    content.setUpdateTime(DateUtils.getNowDate());
                    tdwContentsMapper.insertTdwContents(content);
                }
            }
            result.addAll(generated);
        }
        return result;
    }

    @Override
    public String generateContent(Long id) throws JsonProcessingException
    {
        return generateContent(id, null, null);
    }

    @Override
    public String generateContent(Long id, List<Long> knowledgeFileIds, List<Long> knowledgeChunkIds) throws JsonProcessingException
    {
        TdwContentGenerateRequest request = new TdwContentGenerateRequest();
        request.setBidId(id);
        request.setScope("full");
        request.setMode("append");
        request.setIncludeTable(false);
        request.setIncludeDiagram(false);
        request.setKnowledgeFileIds(knowledgeFileIds);
        request.setKnowledgeChunkIds(knowledgeChunkIds);
        generateContentBlocks(request);
        return "success";
    }

    @Override
    public List<TdwContents> getContent(Long id)
    {
        return tdwContentsMapper.getContent(id);
    }

    private void validateContentBlock(TdwContents tdwContents, boolean requireOutlineId)
    {
        if (tdwContents == null) {
            throw new IllegalArgumentException("内容块不能为空");
        }
        if (tdwContents.getId() == null && !requireOutlineId) {
            throw new IllegalArgumentException("内容块ID不能为空");
        }
        if (requireOutlineId && tdwContents.getOutlineId() == null) {
            throw new IllegalArgumentException("outlineId不能为空");
        }
        if (tdwContents.getContentType() != null && !isValidContentType(tdwContents.getContentType())) {
            throw new IllegalArgumentException("contentType只能是1文本、2表格、3图片/结构图");
        }
        if (requireOutlineId && tdwContents.getContentType() == null) {
            throw new IllegalArgumentException("contentType不能为空");
        }
        if (tdwContents.getContent() != null && tdwContents.getContent().trim().length() > 0) {
            try {
                objectMapper.readTree(tdwContents.getContent());
            } catch (Exception e) {
                throw new IllegalArgumentException("content必须是合法JSON");
            }
        } else if (requireOutlineId) {
            throw new IllegalArgumentException("content不能为空");
        }
    }

    private boolean isValidContentType(int contentType)
    {
        return contentType == CONTENT_TYPE_TEXT || contentType == CONTENT_TYPE_TABLE || contentType == CONTENT_TYPE_DIAGRAM;
    }

    private int nextSortOrder(Long outlineId)
    {
        Integer maxSortOrder = tdwContentsMapper.selectMaxSortOrder(outlineId);
        return (maxSortOrder == null ? 0 : maxSortOrder) + 1;
    }

    private void normalizeSortOrder(Long outlineId)
    {
        if (outlineId == null) {
            return;
        }
        List<TdwContents> contents = tdwContentsMapper.selectTdwContentsByOutlineId(outlineId);
        for (int i = 0; i < contents.size(); i++) {
            tdwContentsMapper.updateSortOrder(contents.get(i).getId(), i + 1);
        }
    }

    private String normalizeOption(String value, String defaultValue)
    {
        if (value == null || value.trim().length() == 0) {
            return defaultValue;
        }
        return value.trim().toLowerCase();
    }

    private List<TdwOutlines> resolveTargetOutlines(TdwContentGenerateRequest request, String scope)
    {
        if ("full".equals(scope)) {
            if (request.getBidId() == null) {
                throw new IllegalArgumentException("全文生成时bidId不能为空");
            }
            return outlinesMapper.selectTdwOutlinesByLevel(request.getBidId(), 3);
        }

        if (request.getOutlineId() == null) {
            throw new IllegalArgumentException("选中节点生成时outlineId不能为空");
        }
        TdwOutlines outline = outlinesMapper.selectTdwOutlinesById(request.getOutlineId());
        if (outline == null) {
            throw new IllegalArgumentException("大纲节点不存在");
        }
        if (outline.getLevel() == 3) {
            List<TdwOutlines> result = new ArrayList<>();
            result.add(outline);
            return result;
        }
        return outlinesMapper.selectContentTitleOutlinesByAncestor(request.getOutlineId());
    }

    private List<TdwContents> mockGenerateBlocks(TdwOutlines outline, TdwContentGenerateRequest request) throws JsonProcessingException
    {
        List<TdwContents> blocks = new ArrayList<>();
        GenerateContentAiRequest aiRequest = new GenerateContentAiRequest();
        aiRequest.setBidId(request.getBidId());
        aiRequest.setOutlineId(outline.getId());
        aiRequest.setOutlineTitle(outline.getTitle());
        aiRequest.setOutlineLevel(outline.getLevel());
        aiRequest.setWordLimit(outline.getWordLimit());
        aiRequest.setRequirement(request.getRequirement());
        aiRequest.setIncludeTable(request.getIncludeTable());
        aiRequest.setIncludeDiagram(request.getIncludeDiagram());
        aiRequest.setKnowledgeFileIds(request.getKnowledgeFileIds());
        aiRequest.setKnowledgeChunkIds(request.getKnowledgeChunkIds());
        aiRequest.setKnowledgeContext(tdwKnowledgeService.buildKnowledgeContext(request.getKnowledgeFileIds(), request.getKnowledgeChunkIds()));
        if (request.getTenderParseReportId() != null) {
            com.ruoyi.tdw.domain.TdwTenderParseReport report = tenderParseReportMapper.selectById(request.getTenderParseReportId());
            if (report != null) {
                aiRequest.setTenderParseResult(report.getReportContent());
            }
        }

        GenerateContentAiResponse aiResponse = tdwAiService.generateContentBlocks(aiRequest);
        if (aiResponse == null || aiResponse.getBlocks() == null || aiResponse.getBlocks().isEmpty()) {
            return blocks;
        }
        for (GenerateContentAiResponse.ContentBlock block : aiResponse.getBlocks()) {
            Integer contentType = block.getContentType() == null ? CONTENT_TYPE_TEXT : block.getContentType();
            blocks.add(buildContent(outline.getId(), contentType, objectMapper.writeValueAsString(block.getContent())));
        }
        return blocks;
    }

    private TdwContents buildContent(Long outlineId, Integer contentType, String content)
    {
        TdwContents tdwContents = new TdwContents();
        tdwContents.setOutlineId(outlineId);
        tdwContents.setContentType(contentType);
        tdwContents.setContent(content);
        return tdwContents;
    }

    private String buildTextJson(TdwOutlines outline, TdwContentGenerateRequest request) throws JsonProcessingException
    {
        Map<String, Object> root = new LinkedHashMap<>();
        String requirement = request.getRequirement() == null ? "" : request.getRequirement().trim();
        root.put("text", "围绕“" + outline.getTitle() + "”生成的正文内容。" + (requirement.length() == 0 ? "" : " 补充要求：" + requirement));
        Map<String, Object> format = new LinkedHashMap<>();
        format.put("fontSize", 14);
        format.put("bold", false);
        root.put("format", format);
        return objectMapper.writeValueAsString(root);
    }

    private String buildTableJson(TdwOutlines outline) throws JsonProcessingException
    {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("title", outline.getTitle() + "要点表");

        List<String> headers = new ArrayList<>();
        headers.add("序号");
        headers.add("内容");
        headers.add("说明");
        root.put("headers", headers);

        List<List<String>> rows = new ArrayList<>();
        List<String> row1 = new ArrayList<>();
        row1.add("1");
        row1.add("总体思路");
        row1.add("结合招标要求形成可执行方案");
        rows.add(row1);
        List<String> row2 = new ArrayList<>();
        row2.add("2");
        row2.add("实施要点");
        row2.add("明确任务分工、进度安排和质量控制");
        rows.add(row2);
        root.put("rows", rows);
        return objectMapper.writeValueAsString(root);
    }

    private String buildDiagramJson(TdwOutlines outline) throws JsonProcessingException
    {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("title", outline.getTitle() + "结构图");
        root.put("description", "展示“" + outline.getTitle() + "”相关模块之间的关系");
        root.put("imageUrl", "");
        root.put("diagramType", "architecture");
        root.put("mermaid", "graph TD; A[输入资料] --> B[" + outline.getTitle() + "]; B --> C[成果输出];");
        return objectMapper.writeValueAsString(root);
    }
}
