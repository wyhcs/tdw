package com.ruoyi.tdw.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import com.ruoyi.tdw.ai.prompt.TdwPromptTemplateService;
import com.ruoyi.tdw.ai.service.TdwAiService;
import com.ruoyi.tdw.domain.TdwContents;
import com.ruoyi.tdw.domain.TdwOutlines;
import com.ruoyi.tdw.domain.TdwBids;
import com.ruoyi.tdw.domain.dto.TdwContentGenerateRequest;
import com.ruoyi.tdw.domain.dto.TdwContentSelectionAiRequest;
import com.ruoyi.tdw.domain.dto.TdwContentSelectionAiResult;
import com.ruoyi.tdw.domain.dto.TdwContentSortRequest;
import com.ruoyi.tdw.domain.dto.TdwRichContentSaveRequest;
import com.ruoyi.tdw.mapper.TdwBidsMapper;
import com.ruoyi.tdw.mapper.TdwContentsMapper;
import com.ruoyi.tdw.mapper.TdwOutlinesMapper;
import com.ruoyi.tdw.mapper.TdwTenderParseReportMapper;
import com.ruoyi.tdw.service.ITdwContentsService;
import com.ruoyi.tdw.service.ITdwKnowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
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
    private static final String CONTENT_PROMPT_ROOT = "prompts/ai-plan/content/";

    @Autowired
    private TdwContentsMapper tdwContentsMapper;

    @Autowired
    private TdwOutlinesMapper outlinesMapper;

    @Autowired
    private TdwBidsMapper bidsMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TdwAiService tdwAiService;

    @Autowired
    private ITdwKnowledgeService tdwKnowledgeService;

    @Autowired
    private TdwTenderParseReportMapper tenderParseReportMapper;

    @Autowired
    private TdwPromptTemplateService promptTemplateService;

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
    public List<TdwContents> selectTdwContentsByOutlineIds(List<Long> outlineIds)
    {
        if (outlineIds == null || outlineIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> ids = new ArrayList<>();
        Set<Long> uniqueIds = new LinkedHashSet<>(outlineIds);
        for (Long outlineId : uniqueIds) {
            if (outlineId != null) {
                ids.add(outlineId);
            }
        }
        if (ids.isEmpty()) {
            return new ArrayList<>();
        }
        return tdwContentsMapper.selectTdwContentsByOutlineIds(ids);
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

        System.out.println("@@@@@@@@@@");
        System.out.println(scope);
        System.out.println(mode);
        if (!"full".equals(scope) && !"selected".equals(scope)) {
            throw new IllegalArgumentException("scope只能是full或selected");
        }
        if (!"overwrite".equals(mode) && !"append".equals(mode) && !"keep".equals(mode)) {
            throw new IllegalArgumentException("mode只能是overwrite、append或keep");
        }

        List<TdwOutlines> targetOutlines = resolveTargetOutlines(request, scope);
        List<TdwContents> result = new ArrayList<>();
        for (TdwOutlines outline : targetOutlines) {
            List<TdwContents> generated = generateBlocks(outline, request);
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
        System.out.println(result);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TdwContents saveRichContent(TdwRichContentSaveRequest request) throws JsonProcessingException
    {
        if (request == null || request.getOutlineId() == null) {
            throw new IllegalArgumentException("outlineId不能为空");
        }
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("html", safe(request.getHtml()));
        root.put("text", safe(request.getText()));
        Map<String, Object> format = new LinkedHashMap<>();
        format.put("fontSize", 14);
        format.put("bold", false);
        root.put("format", format);

        String contentJson = objectMapper.writeValueAsString(root);
        List<TdwContents> contents = tdwContentsMapper.selectTdwContentsByOutlineId(request.getOutlineId());
        TdwContents target = null;
        for (TdwContents content : contents) {
            if (content.getContentType() != null && content.getContentType() == CONTENT_TYPE_TEXT) {
                target = content;
                break;
            }
        }
        if (target == null) {
            target = new TdwContents();
            target.setOutlineId(request.getOutlineId());
            target.setContentType(CONTENT_TYPE_TEXT);
            target.setContent(contentJson);
            target.setSortOrder(1);
            target.setCreateTime(DateUtils.getNowDate());
            target.setUpdateTime(DateUtils.getNowDate());
            tdwContentsMapper.insertTdwContents(target);
        } else {
            target.setOutlineId(request.getOutlineId());
            target.setContentType(CONTENT_TYPE_TEXT);
            target.setContent(contentJson);
            target.setSortOrder(1);
            target.setUpdateTime(DateUtils.getNowDate());
            tdwContentsMapper.updateTdwContents(target);
        }

        for (TdwContents content : contents) {
            if (!content.getId().equals(target.getId())) {
                tdwContentsMapper.deleteTdwContentsById(content.getId());
            }
        }
        normalizeSortOrder(request.getOutlineId());
        return tdwContentsMapper.selectTdwContentsById(target.getId());
    }

    @Override
    public TdwContentSelectionAiResult handleSelectionAi(TdwContentSelectionAiRequest request) throws JsonProcessingException
    {
        if (request == null || request.getSelectedText() == null || request.getSelectedText().trim().length() == 0) {
            throw new IllegalArgumentException("选中文本不能为空");
        }
        String action = normalizeOption(request.getAction(), "rewrite");
        String selectedText = request.getSelectedText().trim();
        GenerateContentAiRequest aiRequest = new GenerateContentAiRequest();
        aiRequest.setBidId(request.getBidId());
        aiRequest.setOutlineId(request.getOutlineId());
        aiRequest.setExistingContent(selectedText);
        aiRequest.setRequirement(request.getRequirement());
        if (request.getOutlineId() != null) {
            TdwOutlines outline = outlinesMapper.selectTdwOutlinesById(request.getOutlineId());
            if (outline != null) {
                aiRequest.setOutlineTitle(outline.getTitle());
                aiRequest.setOutlineLevel(outline.getLevel());
                aiRequest.setWordLimit(outline.getWordLimit());
            }
        }

        TdwContentSelectionAiResult result = new TdwContentSelectionAiResult();
        result.setAction(action);
        if ("expand".equals(action)) {
            result.setText(tdwAiService.expandContent(aiRequest));
            return result;
        }
        if ("shorten".equals(action)) {
            result.setText(tdwAiService.shortenContent(aiRequest));
            return result;
        }
        if ("rewrite".equals(action)) {
            result.setText(tdwAiService.optimizeContent(aiRequest));
            return result;
        }
        if ("summarytable".equals(action)) {
            return buildSummaryTableResult(action, selectedText);
        }
        if ("summarydiagram".equals(action)) {
            return buildSummaryDiagramResult(action, selectedText);
        }
        throw new IllegalArgumentException("action只能是expand、shorten、rewrite、summaryDiagram、summaryTable");
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

    private List<TdwContents> generateBlocks(TdwOutlines outline, TdwContentGenerateRequest request) throws JsonProcessingException
    {
        List<TdwContents> blocks = new ArrayList<>();
        TdwBids bid = resolveBid(outline, request);
        String projectType = bid == null ? "" : safe(bid.getCategory());
        String projectName = bid == null ? "" : safe(bid.getTitle());
        String writingStyle = normalizeContentStyleName(request.getWritingStyle(), request.getRequirement());
        String promptPath = contentPromptPath(projectType, writingStyle);
        String knowledgeContext = tdwKnowledgeService.buildKnowledgeContext(request.getKnowledgeFileIds(), request.getKnowledgeChunkIds());
        String tenderParseResult = "";
        if (request.getTenderParseReportId() != null) {
            com.ruoyi.tdw.domain.TdwTenderParseReport report = tenderParseReportMapper.selectById(request.getTenderParseReportId());
            if (report != null) {
                tenderParseResult = safe(report.getReportContent());
            }
        }

        GenerateContentAiRequest aiRequest = new GenerateContentAiRequest();
        aiRequest.setBidId(resolveBidId(outline, request));
        aiRequest.setOutlineId(outline.getId());
        aiRequest.setOutlineTitle(outline.getTitle());
        aiRequest.setOutlineLevel(outline.getLevel());
        aiRequest.setWordLimit(outline.getWordLimit());
        aiRequest.setProjectName(projectName);
        aiRequest.setProjectType(projectType);
        aiRequest.setWritingStyle(writingStyle);
        aiRequest.setPromptKey(promptPath);
        aiRequest.setRequirement(request.getRequirement());
        aiRequest.setIncludeTable(request.getIncludeTable());
        aiRequest.setIncludeDiagram(request.getIncludeDiagram());
        aiRequest.setKnowledgeFileIds(request.getKnowledgeFileIds());
        aiRequest.setKnowledgeChunkIds(request.getKnowledgeChunkIds());
        aiRequest.setKnowledgeContext(knowledgeContext);
        aiRequest.setTenderParseResult(tenderParseResult);
        aiRequest.setFinalPrompt(buildContentPrompt(promptPath, bid, outline, request, writingStyle, tenderParseResult, knowledgeContext));

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

    private TdwBids resolveBid(TdwOutlines outline, TdwContentGenerateRequest request)
    {
        Long bidId = resolveBidId(outline, request);
        return bidId == null ? null : bidsMapper.selectTdwBidsById(bidId);
    }

    private Long resolveBidId(TdwOutlines outline, TdwContentGenerateRequest request)
    {
        if (request != null && request.getBidId() != null) {
            return request.getBidId();
        }
        return outline == null ? null : outline.getBidId();
    }

    private String buildContentPrompt(String promptPath, TdwBids bid, TdwOutlines outline,
                                      TdwContentGenerateRequest request, String writingStyle,
                                      String tenderParseResult, String knowledgeContext)
    {
        Map<String, String> variables = new HashMap<>();
        variables.put("projectName", bid == null ? "" : safe(bid.getTitle()));
        variables.put("projectType", bid == null ? "" : safe(bid.getCategory()));
        variables.put("writingStyle", safe(writingStyle));
        variables.put("outlineTitle", outline == null ? "" : safe(outline.getTitle()));
        variables.put("outlineRequirement", buildOutlineRequirementText(outline));
        variables.put("wordLimit", outline == null ? "" : String.valueOf(outline.getWordLimit()));
        variables.put("requirement", request == null ? "" : safe(request.getRequirement()));
        variables.put("tenderParseResult", safe(tenderParseResult));
        variables.put("knowledgeContext", safe(knowledgeContext));
        variables.put("includeTable", request != null && Boolean.TRUE.equals(request.getIncludeTable()) ? "是" : "否");
        variables.put("includeDiagram", request != null && Boolean.TRUE.equals(request.getIncludeDiagram()) ? "是" : "否");
        return promptTemplateService.render(promptPath, variables);
    }

    private String buildOutlineRequirementText(TdwOutlines outline)
    {
        if (outline == null) {
            return "";
        }
        List<String> parts = new ArrayList<>();
        addIfNotBlank(parts, outline.getRequirementDesc());
        addIfNotBlank(parts, outline.getWritingDirection());
        addIfNotBlank(parts, outline.getWritingRequirement());
        return String.join("\n", parts);
    }

    private void addIfNotBlank(List<String> parts, String value)
    {
        if (value != null && value.trim().length() > 0) {
            parts.add(value.trim());
        }
    }

    private String contentPromptPath(String projectType, String writingStyle)
    {
        String categoryKey = normalizeProjectTypeKey(projectType);
        String styleKey = normalizeContentStyleKey(writingStyle);
        String path = CONTENT_PROMPT_ROOT + categoryKey + "/" + styleKey + ".md";
        if (resourceExists(path)) {
            return path;
        }
        String fallbackStylePath = CONTENT_PROMPT_ROOT + "other/" + styleKey + ".md";
        if (resourceExists(fallbackStylePath)) {
            return fallbackStylePath;
        }
        return CONTENT_PROMPT_ROOT + "other/general.md";
    }

    private boolean resourceExists(String path)
    {
        return new ClassPathResource(path).exists();
    }

    private String normalizeProjectTypeKey(String projectType)
    {
        String value = safe(projectType).trim().toLowerCase();
        if (value.contains("服务")) {
            return "service";
        }
        if (value.contains("货物") || value.contains("采购") || value.contains("商品")) {
            return "goods";
        }
        if (value.contains("工程") || value.contains("施工") || value.contains("建设")) {
            return "engineering";
        }
        if (value.contains("监理")) {
            return "supervision";
        }
        if (value.contains("it") || value.contains("软件") || value.contains("信息") || value.contains("系统")
                || value.contains("平台") || value.contains("数字化")) {
            return "it";
        }
        return "other";
    }

    private String normalizeContentStyleName(String writingStyle, String requirement)
    {
        String value = writingStyle == null || writingStyle.trim().length() == 0 ? requirement : writingStyle;
        String key = normalizeContentStyleKey(value);
        if ("data".equals(key)) {
            return "数据型";
        }
        if ("concise".equals(key)) {
            return "简练型";
        }
        if ("practical".equals(key)) {
            return "实用型";
        }
        return "通用型";
    }

    private String normalizeContentStyleKey(String writingStyle)
    {
        String value = safe(writingStyle).trim().toLowerCase();
        if (value.contains("data") || value.contains("数据")) {
            return "data";
        }
        if (value.contains("concise") || value.contains("简练") || value.contains("简洁") || value.contains("简约")) {
            return "concise";
        }
        if (value.contains("practical") || value.contains("实用")) {
            return "practical";
        }
        return "general";
    }

    private TdwContents buildContent(Long outlineId, Integer contentType, String content)
    {
        TdwContents tdwContents = new TdwContents();
        tdwContents.setOutlineId(outlineId);
        tdwContents.setContentType(contentType);
        tdwContents.setContent(content);
        return tdwContents;
    }

    private TdwContentSelectionAiResult buildSummaryTableResult(String action, String selectedText)
    {
        TdwContentSelectionAiResult result = new TdwContentSelectionAiResult();
        result.setAction(action);
        result.setTitle("局部内容总结表");
        List<String> headers = Arrays.asList("归纳维度", "关键内容", "标书响应建议");
        List<List<String>> rows = new ArrayList<>();
        List<String> sentences = splitSentences(selectedText, 3);
        if (sentences.isEmpty()) {
            sentences.add(selectedText);
        }
        for (int i = 0; i < Math.min(3, sentences.size()); i++) {
            rows.add(Arrays.asList("要点" + (i + 1), sentences.get(i), "保持与招标需求一致，补充可执行措施与验收依据"));
        }
        result.setHeaders(headers);
        result.setRows(rows);
        result.setText("已将选中内容整理为" + rows.size() + "行总结表。");
        result.setHtml(buildTableHtml(headers, rows));
        return result;
    }

    private TdwContentSelectionAiResult buildSummaryDiagramResult(String action, String selectedText)
    {
        TdwContentSelectionAiResult result = new TdwContentSelectionAiResult();
        result.setAction(action);
        result.setTitle(buildSummaryTitle(selectedText));
        result.setDescription("根据选中内容自动归纳的结构图，可在前端点击图片右上角进行编辑。");
        List<String> keywords = splitSentences(selectedText, 4);
        while (keywords.size() < 4) {
            keywords.add("响应要点" + (keywords.size() + 1));
        }
        result.setKeywords(keywords);
        result.setText("已生成智能总结图。");
        return result;
    }

    private String buildTableHtml(List<String> headers, List<List<String>> rows)
    {
        StringBuilder html = new StringBuilder();
        html.append("<table>");
        html.append("<thead><tr>");
        for (String header : headers) {
            html.append("<th>").append(escapeHtml(header)).append("</th>");
        }
        html.append("</tr></thead><tbody>");
        for (List<String> row : rows) {
            html.append("<tr>");
            for (String cell : row) {
                html.append("<td>").append(escapeHtml(cell)).append("</td>");
            }
            html.append("</tr>");
        }
        html.append("</tbody></table>");
        return html.toString();
    }

    private List<String> splitSentences(String text, int limit)
    {
        List<String> result = new ArrayList<>();
        if (text == null) {
            return result;
        }
        String[] sentences = text.replace("\r", "\n").split("[。；;\\n]+");
        for (String sentence : sentences) {
            String value = sentence == null ? "" : sentence.trim();
            if (value.length() == 0) {
                continue;
            }
            result.add(value.length() > 80 ? value.substring(0, 80) : value);
            if (result.size() >= limit) {
                break;
            }
        }
        return result;
    }

    private String buildSummaryTitle(String selectedText)
    {
        List<String> sentences = splitSentences(selectedText, 1);
        if (sentences.isEmpty()) {
            return "智能总结图";
        }
        String title = sentences.get(0);
        return title.length() > 18 ? title.substring(0, 18) + "..." : title;
    }

    private String escapeHtml(String value)
    {
        return safe(value).replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String safe(String value)
    {
        return value == null ? "" : value;
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
