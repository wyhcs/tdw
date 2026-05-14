package com.ruoyi.tdw.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.tdw.ai.prompt.TdwPromptTemplateService;
import com.ruoyi.tdw.domain.TdwAiRecord;
import com.ruoyi.tdw.domain.TdwAiTask;
import com.ruoyi.tdw.domain.TdwBids;
import com.ruoyi.tdw.domain.TdwOutlineClosure;
import com.ruoyi.tdw.domain.TdwOutlines;
import com.ruoyi.tdw.domain.TdwPlanSetting;
import com.ruoyi.tdw.domain.dto.TdwPlanAddNodeRequest;
import com.ruoyi.tdw.domain.dto.TdwPlanAiTextResult;
import com.ruoyi.tdw.domain.dto.TdwPlanDeleteNodeRequest;
import com.ruoyi.tdw.domain.dto.TdwPlanSortRequest;
import com.ruoyi.tdw.domain.dto.TdwPlanTitleRequest;
import com.ruoyi.tdw.domain.dto.TdwPlanWordLimitRequest;
import com.ruoyi.tdw.domain.dto.TdwPlanWordPresetRequest;
import com.ruoyi.tdw.domain.dto.TdwPlanWritingAiRequest;
import com.ruoyi.tdw.domain.dto.TdwPlanWritingRuleRequest;
import com.ruoyi.tdw.mapper.TdwAiRecordMapper;
import com.ruoyi.tdw.mapper.TdwAiTaskMapper;
import com.ruoyi.tdw.mapper.TdwBidsMapper;
import com.ruoyi.tdw.mapper.TdwOutlineClosureMapper;
import com.ruoyi.tdw.mapper.TdwOutlinesMapper;
import com.ruoyi.tdw.mapper.TdwPlanSettingMapper;
import com.ruoyi.tdw.service.ITdwOutlinesService;
import com.ruoyi.tdw.service.ITdwPlanOutlineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AI方案目录生成后的编辑业务。
 */
@Service
public class TdwPlanOutlineServiceImpl implements ITdwPlanOutlineService
{
    private static final int DEFAULT_WORD_LIMIT = 300;
    private static final int DEFAULT_PAGE_WORDS = 380;
    private static final Pattern CHAPTER_PREFIX = Pattern.compile("^第[一二三四五六七八九十百千万零〇两]+章\\s*");
    private static final Pattern SECTION_PREFIX = Pattern.compile("^第[一二三四五六七八九十百千万零〇两]+节\\s*");
    private static final Pattern PARAGRAPH_PREFIX = Pattern.compile("^[（(][一二三四五六七八九十百千万零〇两]+[）)]\\s*");

    @Autowired
    private TdwBidsMapper bidsMapper;

    @Autowired
    private TdwOutlinesMapper outlinesMapper;

    @Autowired
    private TdwOutlineClosureMapper closureMapper;

    @Autowired
    private TdwPlanSettingMapper planSettingMapper;

    @Autowired
    private TdwAiTaskMapper aiTaskMapper;

    @Autowired
    private TdwAiRecordMapper aiRecordMapper;

    @Autowired
    private ITdwOutlinesService outlinesService;

    @Autowired
    private TdwPromptTemplateService promptTemplateService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Map<String, Object> getOverview(Long bidId)
    {
        requireBid(bidId);
        TdwPlanSetting setting = getOrDefaultSetting(bidId);
        List<TdwOutlines> tree = outlinesService.selectOutlineTree(bidId);
        return buildOverview(bidId, setting, tree);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> applyWordPreset(Long bidId, TdwPlanWordPresetRequest request)
    {
        requireBid(bidId);
        int wordLimit = normalizeWordLimit(request == null ? null : request.getWordLimit());
        if (request != null && request.getPreset() != null && request.getPreset().trim().length() > 0) {
            wordLimit = presetToWordLimit(request.getPreset(), wordLimit);
        }
        List<Long> leafIds = outlinesMapper.selectTdwOutlinesByLevel(bidId, 3).stream()
                .map(TdwOutlines::getId)
                .collect(Collectors.toList());
        if (!leafIds.isEmpty()) {
            outlinesMapper.updateOutlineWordLimitByIds(leafIds, wordLimit);
        }

        TdwPlanSetting setting = new TdwPlanSetting();
        setting.setBidId(bidId);
        setting.setWordPreset(request == null ? String.valueOf(wordLimit) : safe(request.getPreset(), String.valueOf(wordLimit)));
        setting.setPageWords(request == null || request.getPageWords() == null ? DEFAULT_PAGE_WORDS : request.getPageWords());
        planSettingMapper.upsertTdwPlanSetting(setting);
        normalizeTitles(bidId);
        return getOverview(bidId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateNodeWordLimit(Long bidId, Long outlineId, TdwPlanWordLimitRequest request)
    {
        TdwOutlines outline = requireOutlineInBid(bidId, outlineId);
        outlinesMapper.updateOutlineWordLimit(outline.getId(), normalizeWordLimit(request == null ? null : request.getWordLimit()));
        return getOverview(bidId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchUpdateWordLimit(Long bidId, Long outlineId, TdwPlanWordLimitRequest request)
    {
        TdwOutlines outline = requireOutlineInBid(bidId, outlineId);
        List<TdwOutlines> leaves = outline.getLevel() == 3
                ? Arrays.asList(outline)
                : outlinesMapper.selectContentTitleOutlinesByAncestor(outline.getId());
        List<Long> ids = leaves.stream().map(TdwOutlines::getId).collect(Collectors.toList());
        if (!ids.isEmpty()) {
            outlinesMapper.updateOutlineWordLimitByIds(ids, normalizeWordLimit(request == null ? null : request.getWordLimit()));
        }
        return getOverview(bidId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateTitle(Long bidId, Long outlineId, TdwPlanTitleRequest request)
    {
        TdwOutlines outline = requireOutlineInBid(bidId, outlineId);
        if (request == null || request.getTitle() == null || request.getTitle().trim().length() == 0) {
            throw new IllegalArgumentException("标题不能为空");
        }
        String titleText = cleanTitleText(request.getTitle());
        String prefix = titlePrefix(outline.getLevel(), outline.getSortNum());
        outlinesMapper.updateOutlineNumbering(outline.getId(), prefix + " " + titleText, prefix, titleText, outline.getSortNum());
        return getOverview(bidId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateWritingDirection(Long bidId, Long outlineId, TdwPlanWritingRuleRequest request)
    {
        TdwOutlines outline = requireOutlineInBid(bidId, outlineId);
        outlinesMapper.updateWritingDirection(outline.getId(), cleanLongText(request == null ? null : request.getContent(), 10000),
                request == null ? null : request.getMode());
        return getOverview(bidId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateWritingRequirement(Long bidId, Long outlineId, TdwPlanWritingRuleRequest request)
    {
        TdwOutlines outline = requireOutlineInBid(bidId, outlineId);
        outlinesMapper.updateWritingRequirement(outline.getId(), cleanLongText(request == null ? null : request.getContent(), 10000),
                request == null ? null : request.getMode());
        return getOverview(bidId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateGlobalWritingRequirement(Long bidId, TdwPlanWritingRuleRequest request)
    {
        requireBid(bidId);
        TdwPlanSetting current = getOrDefaultSetting(bidId);
        current.setGlobalWritingRequirement(cleanLongText(request == null ? null : request.getContent(), 10000));
        planSettingMapper.upsertTdwPlanSetting(current);
        return getOverview(bidId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> addSibling(Long bidId, Long outlineId, TdwPlanAddNodeRequest request)
    {
        TdwOutlines current = requireOutlineInBid(bidId, outlineId);
        validateAddRequest(request, current.getLevel() == 1, current.getLevel() < 3);
        if (current.getLevel() == 3) {
            return addParagraph(bidId, outlineId, request);
        }
        insertSubtreeAfter(current, request);
        normalizeTitles(bidId);
        return getOverview(bidId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> addChild(Long bidId, Long outlineId, TdwPlanAddNodeRequest request)
    {
        TdwOutlines current = requireOutlineInBid(bidId, outlineId);
        if (current.getLevel() >= 3) {
            throw new IllegalArgumentException("段级标题不能新增子级");
        }
        validateAddRequest(request, false, current.getLevel() == 1);
        appendChildSubtree(current, request);
        normalizeTitles(bidId);
        return getOverview(bidId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> addParagraph(Long bidId, Long outlineId, TdwPlanAddNodeRequest request)
    {
        TdwOutlines current = requireOutlineInBid(bidId, outlineId);
        validateAddRequest(request, false, false);
        if (current.getLevel() == 3) {
            insertParagraphAfter(current, request);
        } else {
            appendParagraph(current, request);
        }
        normalizeTitles(bidId);
        return getOverview(bidId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> deleteNodes(Long bidId, TdwPlanDeleteNodeRequest request)
    {
        requireBid(bidId);
        if (request == null || request.getIds() == null || request.getIds().isEmpty()) {
            throw new IllegalArgumentException("请选择要删除的目录节点");
        }
        List<Long> rootDeleteIds = filterCoveredNodes(bidId, request.getIds());
        for (Long id : rootDeleteIds) {
            TdwOutlines outline = requireOutlineInBid(bidId, id);
            outlinesService.deleteOutlineById(outline.getId());
        }
        normalizeTitles(bidId);
        return getOverview(bidId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> sortNodes(Long bidId, TdwPlanSortRequest request)
    {
        requireBid(bidId);
        if (request == null || request.getOrderedIds() == null || request.getOrderedIds().isEmpty()) {
            throw new IllegalArgumentException("排序节点不能为空");
        }
        Long parentId = request.getParentId();
        List<TdwOutlines> siblings = outlinesMapper.selectSiblings(bidId, parentId);
        Set<Long> expected = siblings.stream().map(TdwOutlines::getId).collect(Collectors.toSet());
        Set<Long> actual = new LinkedHashSet<Long>(request.getOrderedIds());
        if (!expected.equals(actual)) {
            throw new IllegalArgumentException("排序节点必须属于同一个层级");
        }
        for (int i = 0; i < request.getOrderedIds().size(); i++) {
            outlinesMapper.updateSortNum(request.getOrderedIds().get(i), i + 1);
        }
        normalizeTitles(bidId);
        return getOverview(bidId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> finalizeOutline(Long bidId)
    {
        TdwBids bid = requireBid(bidId);
        bid.setStatus(1L);
        bidsMapper.updateTdwBids(bid);
        normalizeTitles(bidId);
        return getOverview(bidId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TdwPlanAiTextResult generateWritingDirection(Long bidId, Long outlineId, TdwPlanWritingAiRequest request)
    {
        return generateAiText(bidId, outlineId, request, "writing_direction_generate",
                "prompts/ai-plan/writing-direction.md", "编写方向生成");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TdwPlanAiTextResult generateWritingRequirement(Long bidId, Long outlineId, TdwPlanWritingAiRequest request)
    {
        return generateAiText(bidId, outlineId, request, "writing_requirement_generate",
                "prompts/ai-plan/writing-requirement.md", "编写要求生成");
    }

    private Map<String, Object> buildOverview(Long bidId, TdwPlanSetting setting, List<TdwOutlines> tree)
    {
        TdwBids bid = bidsMapper.selectTdwBidsById(bidId);
        List<TdwOutlines> rows = flatten(tree);
        int targetWords = rows.stream()
                .filter(item -> item.getLevel() == 3)
                .mapToInt(item -> item.getWordLimit() <= 0 ? DEFAULT_WORD_LIMIT : item.getWordLimit())
                .sum();
        int generatedWords = rows.stream().mapToInt(TdwOutlines::getContentWords).sum();
        int pageWords = setting.getPageWords() == null || setting.getPageWords() <= 0 ? DEFAULT_PAGE_WORDS : setting.getPageWords();

        Map<String, Object> stats = new LinkedHashMap<String, Object>();
        stats.put("targetWords", targetWords);
        stats.put("generatedWords", generatedWords);
        stats.put("estimatePages", ceilDiv(targetWords, pageWords));
        stats.put("currentPages", ceilDiv(generatedWords, pageWords));
        stats.put("outlineCount", rows.size());
        stats.put("leafCount", rows.stream().filter(item -> item.getLevel() == 3).count());
        stats.put("pageWords", pageWords);

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("bid", bid);
        result.put("setting", setting);
        result.put("tree", tree);
        result.put("rows", rows);
        result.put("stats", stats);
        return result;
    }

    private List<TdwOutlines> flatten(List<TdwOutlines> tree)
    {
        List<TdwOutlines> rows = new ArrayList<TdwOutlines>();
        appendRows(tree, rows);
        return rows;
    }

    private void appendRows(List<TdwOutlines> nodes, List<TdwOutlines> rows)
    {
        if (nodes == null) {
            return;
        }
        for (TdwOutlines node : nodes) {
            rows.add(node);
            appendRows(node.getChildren(), rows);
        }
    }

    private void insertSubtreeAfter(TdwOutlines current, TdwPlanAddNodeRequest request)
    {
        int targetSort = current.getSortNum() + 1;
        if (current.getLevel() == 1) {
            outlinesMapper.updateOutlineLeval1(current.getBidId(), targetSort);
            TdwOutlines chapter = insertNode(current.getBidId(), null, 1, targetSort, request.getTitle(), request);
            appendGeneratedSections(chapter, request);
        } else {
            outlinesMapper.updateOutlineLeval2And3(current.getParentId(), targetSort);
            TdwOutlines section = insertNode(current.getBidId(), current.getParentId(), 2, targetSort, request.getTitle(), request);
            appendGeneratedParagraphs(section, request);
        }
    }

    private void appendChildSubtree(TdwOutlines current, TdwPlanAddNodeRequest request)
    {
        int targetSort = nextSort(current.getBidId(), current.getId());
        if (current.getLevel() == 1) {
            TdwOutlines section = insertNode(current.getBidId(), current.getId(), 2, targetSort, request.getTitle(), request);
            appendGeneratedParagraphs(section, request);
        } else {
            insertNode(current.getBidId(), current.getId(), 3, targetSort, request.getTitle(), request);
        }
    }

    private void appendParagraph(TdwOutlines parent, TdwPlanAddNodeRequest request)
    {
        insertNode(parent.getBidId(), parent.getId(), 3, nextSort(parent.getBidId(), parent.getId()), request.getTitle(), request);
    }

    private void insertParagraphAfter(TdwOutlines current, TdwPlanAddNodeRequest request)
    {
        int targetSort = current.getSortNum() + 1;
        outlinesMapper.updateOutlineLeval2And3(current.getParentId(), targetSort);
        insertNode(current.getBidId(), current.getParentId(), 3, targetSort, request.getTitle(), request);
    }

    private void appendGeneratedSections(TdwOutlines chapter, TdwPlanAddNodeRequest request)
    {
        int sectionCount = request.getSectionCount() == null ? 1 : Math.max(1, request.getSectionCount());
        for (int i = 1; i <= sectionCount; i++) {
            TdwOutlines section = insertNode(chapter.getBidId(), chapter.getId(), 2, i, "新增节标题", request);
            appendGeneratedParagraphs(section, request);
        }
    }

    private void appendGeneratedParagraphs(TdwOutlines section, TdwPlanAddNodeRequest request)
    {
        int paragraphCount = request.getParagraphCount() == null ? 1 : Math.max(1, request.getParagraphCount());
        for (int i = 1; i <= paragraphCount; i++) {
            insertNode(section.getBidId(), section.getId(), 3, i, "新增段标题", request);
        }
    }

    private TdwOutlines insertNode(Long bidId, Long parentId, int level, int sortNum, String titleText, TdwPlanAddNodeRequest request)
    {
        TdwOutlines outline = new TdwOutlines();
        outline.setBidId(bidId);
        outline.setParentId(parentId);
        outline.setLevel(level);
        outline.setTitleText(cleanTitleText(titleText));
        outline.setTitle(composeTitle(level, sortNum, outline.getTitleText()));
        outline.setTitlePrefix(titlePrefix(level, sortNum));
        outline.setSortNum(sortNum);
        outline.setWordLimit(normalizeWordLimit(request == null ? null : request.getWordLimit()));
        outline.setRequirementDesc(cleanLongText(request == null ? null : request.getRequirementDesc(), 1000));
        outlinesMapper.insertTdwOutlines(outline);
        insertClosure(outline.getId(), parentId);
        return outline;
    }

    private void insertClosure(Long outlineId, Long parentId)
    {
        List<TdwOutlineClosure> closures = new ArrayList<TdwOutlineClosure>();
        closures.add(new TdwOutlineClosure(outlineId, outlineId, 0L));
        if (parentId != null) {
            List<TdwOutlineClosure> parentClosures = closureMapper.selectAncestorByParentId(parentId);
            if (parentClosures == null || parentClosures.isEmpty()) {
                parentClosures = new ArrayList<TdwOutlineClosure>();
                parentClosures.add(new TdwOutlineClosure(parentId, parentId, 0L));
            }
            for (TdwOutlineClosure closure : parentClosures) {
                closures.add(new TdwOutlineClosure(closure.getAncestor(), outlineId, closure.getDepth() + 1L));
            }
        }
        closureMapper.batchInsert(closures);
    }

    private List<Long> filterCoveredNodes(Long bidId, List<Long> ids)
    {
        Set<Long> selected = new HashSet<Long>(ids);
        List<TdwOutlines> outlines = new ArrayList<TdwOutlines>();
        for (Long id : selected) {
            outlines.add(requireOutlineInBid(bidId, id));
        }
        outlines.sort((a, b) -> Integer.compare(a.getLevel(), b.getLevel()));
        List<Long> result = new ArrayList<Long>();
        for (TdwOutlines outline : outlines) {
            boolean covered = false;
            List<TdwOutlineClosure> ancestors = closureMapper.selectAncestorByParentId(outline.getId());
            for (TdwOutlineClosure ancestor : ancestors) {
                if (ancestor.getDepth() != null && ancestor.getDepth() > 0 && selected.contains(ancestor.getAncestor())) {
                    covered = true;
                    break;
                }
            }
            if (!covered) {
                result.add(outline.getId());
            }
        }
        return result;
    }

    private void normalizeTitles(Long bidId)
    {
        List<TdwOutlines> tree = outlinesService.selectOutlineTree(bidId);
        normalizeLevel(tree, 1);
    }

    private void normalizeLevel(List<TdwOutlines> nodes, int level)
    {
        if (nodes == null) {
            return;
        }
        nodes.sort((a, b) -> Integer.compare(a.getSortNum(), b.getSortNum()));
        for (int i = 0; i < nodes.size(); i++) {
            TdwOutlines node = nodes.get(i);
            int sortNum = i + 1;
            String titleText = cleanTitleText(node.getTitleText());
            if (titleText.length() == 0) {
                titleText = stripTitlePrefix(node.getLevel(), node.getTitle());
            }
            String prefix = titlePrefix(node.getLevel(), sortNum);
            outlinesMapper.updateOutlineNumbering(node.getId(), prefix + " " + titleText, prefix, titleText, sortNum);
            normalizeLevel(node.getChildren(), level + 1);
        }
    }

    private TdwPlanAiTextResult generateAiText(Long bidId, Long outlineId, TdwPlanWritingAiRequest request,
                                               String taskType, String promptPath, String taskName)
    {
        TdwBids bid = requireBid(bidId);
        TdwOutlines outline = requireOutlineInBid(bidId, outlineId);
        List<TdwOutlines> contextNodes = outline.getLevel() == 3
                ? Arrays.asList(outline)
                : outlinesMapper.selectContentTitleOutlinesByAncestor(outline.getId());
        String outlineText = request != null && request.getOutlineText() != null && request.getOutlineText().trim().length() > 0
                ? request.getOutlineText().trim()
                : buildOutlineText(outline, contextNodes);
        String mode = request == null ? "" : safe(request.getMode(), "");
        String modeInstruction = resolveModeInstruction(taskType, mode);
        String manualInstruction = request == null ? "" : safe(request.getManualInstruction(), "");

        Map<String, String> variables = new HashMap<String, String>();
        variables.put("projectName", safe(bid.getTitle(), ""));
        variables.put("outlineTitle", outline.getTitle());
        variables.put("outlineText", outlineText);
        variables.put("mode", mode);
        variables.put("modeInstruction", modeInstruction);
        variables.put("manualInstruction", manualInstruction);
        String finalPrompt = promptTemplateService.render(promptPath, variables);

        Date start = new Date();
        TdwAiTask task = new TdwAiTask();
        task.setModuleType("plan");
        task.setBizId(bidId);
        task.setTargetType("outline");
        task.setTargetId(outlineId);
        task.setTaskType(taskType);
        task.setTaskName(taskName);
        task.setPromptKey(promptPath);
        task.setProviderName("mock");
        task.setModelName("mock-plan-writer");
        task.setStatus("running");
        task.setStreamStatus("streaming");
        task.setProgress(5);
        task.setInputChars(finalPrompt.length());
        task.setRetryCount(0);
        task.setStartedTime(start);
        aiTaskMapper.insertTdwAiTask(task);

        String output = mockAiText(taskType, mode, outline, contextNodes, manualInstruction);

        TdwAiRecord record = new TdwAiRecord();
        record.setTaskId(task.getId());
        record.setModuleType("plan");
        record.setBizId(bidId);
        record.setTargetType("outline");
        record.setTargetId(outlineId);
        record.setTaskType(taskType);
        record.setPromptKey(promptPath);
        record.setPromptVersion("v1");
        record.setSystemPrompt("");
        record.setUserPrompt(manualInstruction);
        record.setFinalPrompt(finalPrompt);
        record.setInputText(outlineText);
        record.setOutputText(output);
        record.setProviderName("mock");
        record.setModelName("mock-plan-writer");
        record.setTokensInput(estimateTokens(finalPrompt));
        record.setTokensOutput(estimateTokens(output));
        record.setElapsedMs(System.currentTimeMillis() - start.getTime());
        record.setStatus("success");
        record.setRequestJson(toJson(variables));
        record.setResponseJson(toJson(output));
        aiRecordMapper.insertTdwAiRecord(record);

        TdwAiTask finish = new TdwAiTask();
        finish.setId(task.getId());
        finish.setStatus("success");
        finish.setStreamStatus("done");
        finish.setProgress(100);
        finish.setCurrentOutput(output);
        finish.setResultType("ai_record");
        finish.setResultId(record.getId());
        finish.setOutputChars(output.length());
        finish.setTokensInput(record.getTokensInput());
        finish.setTokensOutput(record.getTokensOutput());
        finish.setFinishedTime(new Date());
        finish.setElapsedMs(record.getElapsedMs());
        aiTaskMapper.updateTdwAiTask(finish);

        TdwPlanAiTextResult result = new TdwPlanAiTextResult();
        result.setTaskId(task.getId());
        result.setRecordId(record.getId());
        result.setContent(output);
        return result;
    }

    private String mockAiText(String taskType, String mode, TdwOutlines outline, List<TdwOutlines> contextNodes, String manualInstruction)
    {
        List<String> titles = contextNodes.stream()
                .map(TdwOutlines::getTitleText)
                .filter(Objects::nonNull)
                .filter(item -> item.trim().length() > 0)
                .collect(Collectors.toList());
        if (titles.isEmpty()) {
            titles.add(stripTitlePrefix(outline.getLevel(), outline.getTitle()));
        }
        StringBuilder builder = new StringBuilder();
        int index = 1;
        for (String title : titles) {
            String clean = cleanTitleText(title);
            if ("writing_direction_generate".equals(taskType)) {
                builder.append(index).append(". ").append(directionLine(clean, mode));
            } else {
                builder.append(index).append(". ").append(requirementLine(clean, mode));
            }
            builder.append("\n");
            index++;
            if (index > 8) {
                break;
            }
        }
        if (manualInstruction != null && manualInstruction.trim().length() > 0) {
            builder.append("补充约束：").append(manualInstruction.trim());
        }
        return builder.toString().trim();
    }

    private String directionLine(String title, String mode)
    {
        if ("four_title".equals(mode)) {
            return trimTo(title, 4) + "：围绕" + title + "形成可执行、可验收的工作项。";
        }
        if ("six_title".equals(mode)) {
            return trimTo(title, 6) + "：说明实施目标、关键动作、交付成果和验证方式。";
        }
        if ("verb_object".equals(mode)) {
            return "梳理" + title + "交付物，明确责任边界、执行步骤、验收依据和风险闭环。";
        }
        if ("free".equals(mode)) {
            return title + "：结合评分标准展开背景依据、执行路径、资源安排、质量控制与成果输出。";
        }
        return title + "：聚焦目标、路径、交付物和评价依据，保持内容可执行、可验证、可追踪。";
    }

    private String requirementLine(String title, String mode)
    {
        if ("simple".equals(mode)) {
            return "使用通俗、直接的语言说明" + title + "，避免复杂句式和抽象表述。";
        }
        if ("structure".equals(mode)) {
            return "围绕" + title + "采用1、2、3、4数字序列组织内容，逐项展开。";
        }
        if ("no_pronoun".equals(mode)) {
            return "描述" + title + "时避免使用你、我、他、我们等人称代词。";
        }
        return "内容应紧扣" + title + "，表达清晰、层级稳定，避免偏离评分项。";
    }

    private String resolveModeInstruction(String taskType, String mode)
    {
        if ("writing_direction_generate".equals(taskType)) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("four_title", "请将标题下的所有内容输出为四字格式（标题：内容）");
            map.put("six_title", "请将标题下的所有内容输出为六字格式（标题：内容）");
            map.put("free", "请将标题下的每条内容，字数不限，自由决定长度。（标题：内容）");
            map.put("verb_object", "用“动词 + 具体交付物”的方式描述每一个工作项，让任务可执行、可验收、可追踪。");
            return safe(map.get(mode), "");
        }
        Map<String, String> map = new HashMap<String, String>();
        map.put("simple", "要求使用通俗、直接的词语，避免采用复杂句式或抽象术语。");
        map.put("structure", "要求按照特定编号体系组织内容，如使用数字序列（1、2、3、4）");
        map.put("no_pronoun", "禁止使用特定人称代词，如“你”、“我”、“他”或“我们”。");
        return safe(map.get(mode), "");
    }

    private String buildOutlineText(TdwOutlines outline, List<TdwOutlines> contextNodes)
    {
        StringBuilder builder = new StringBuilder();
        builder.append(outline.getTitle()).append("\n");
        for (TdwOutlines node : contextNodes) {
            builder.append(node.getTitle()).append("\n");
        }
        return builder.toString().trim();
    }

    private void validateAddRequest(TdwPlanAddNodeRequest request, boolean needSectionCount, boolean needParagraphCount)
    {
        if (request == null || request.getTitle() == null || request.getTitle().trim().length() == 0) {
            throw new IllegalArgumentException("标题不能为空");
        }
        if (needSectionCount && (request.getSectionCount() == null || request.getSectionCount() <= 0)) {
            throw new IllegalArgumentException("要求节数不能为空");
        }
        if (needParagraphCount && (request.getParagraphCount() == null || request.getParagraphCount() <= 0)) {
            throw new IllegalArgumentException("每节段数不能为空");
        }
        normalizeWordLimit(request.getWordLimit());
        if (request.getRequirementDesc() != null && request.getRequirementDesc().length() > 1000) {
            throw new IllegalArgumentException("需求描述不能超过1000字");
        }
    }

    private TdwBids requireBid(Long bidId)
    {
        if (bidId == null) {
            throw new IllegalArgumentException("方案ID不能为空");
        }
        TdwBids bid = bidsMapper.selectTdwBidsById(bidId);
        if (bid == null) {
            throw new IllegalArgumentException("方案不存在");
        }
        return bid;
    }

    private TdwOutlines requireOutlineInBid(Long bidId, Long outlineId)
    {
        requireBid(bidId);
        if (outlineId == null) {
            throw new IllegalArgumentException("目录节点ID不能为空");
        }
        TdwOutlines outline = outlinesMapper.selectTdwOutlinesById(outlineId);
        if (outline == null || !Objects.equals(outline.getBidId(), bidId)) {
            throw new IllegalArgumentException("目录节点不存在");
        }
        return outline;
    }

    private TdwPlanSetting getOrDefaultSetting(Long bidId)
    {
        TdwPlanSetting setting = planSettingMapper.selectByBidId(bidId);
        if (setting == null) {
            setting = new TdwPlanSetting();
            setting.setBidId(bidId);
            setting.setWordPreset("free");
            setting.setPageWords(DEFAULT_PAGE_WORDS);
        }
        return setting;
    }

    private int nextSort(Long bidId, Long parentId)
    {
        Integer maxSort = outlinesMapper.selectMaxSortNum(bidId, parentId);
        return maxSort == null ? 1 : maxSort + 1;
    }

    private int normalizeWordLimit(Integer wordLimit)
    {
        int value = wordLimit == null ? DEFAULT_WORD_LIMIT : wordLimit;
        if (value <= 0) {
            throw new IllegalArgumentException("字数必须大于0");
        }
        return Math.min(value, 100000);
    }

    private int presetToWordLimit(String preset, int fallback)
    {
        try {
            return normalizeWordLimit(Integer.parseInt(preset.replace("字", "").trim()));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private String cleanTitleText(String title)
    {
        if (title == null) {
            return "";
        }
        return PARAGRAPH_PREFIX.matcher(SECTION_PREFIX.matcher(CHAPTER_PREFIX.matcher(title.trim()).replaceFirst("")).replaceFirst("")).replaceFirst("").trim();
    }

    private String stripTitlePrefix(int level, String title)
    {
        if (title == null) {
            return "";
        }
        if (level == 1) {
            return CHAPTER_PREFIX.matcher(title.trim()).replaceFirst("").trim();
        }
        if (level == 2) {
            return SECTION_PREFIX.matcher(title.trim()).replaceFirst("").trim();
        }
        return PARAGRAPH_PREFIX.matcher(title.trim()).replaceFirst("").trim();
    }

    private String composeTitle(int level, int sortNum, String titleText)
    {
        return titlePrefix(level, sortNum) + " " + cleanTitleText(titleText);
    }

    private String titlePrefix(int level, int sortNum)
    {
        String number = toChineseNumber(sortNum);
        if (level == 1) {
            return "第" + number + "章";
        }
        if (level == 2) {
            return "第" + number + "节";
        }
        return "（" + number + "）";
    }

    private String toChineseNumber(int number)
    {
        if (number <= 0) {
            return "一";
        }
        String[] digits = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
        if (number < 10) {
            return digits[number];
        }
        if (number == 10) {
            return "十";
        }
        if (number < 20) {
            return "十" + digits[number % 10];
        }
        if (number < 100) {
            return digits[number / 10] + "十" + (number % 10 == 0 ? "" : digits[number % 10]);
        }
        if (number < 1000) {
            int hundred = number / 100;
            int rest = number % 100;
            return digits[hundred] + "百" + (rest == 0 ? "" : (rest < 10 ? "零" : "") + toChineseNumber(rest));
        }
        return String.valueOf(number);
    }

    private String cleanLongText(String text, int maxLength)
    {
        if (text == null) {
            return null;
        }
        String value = text.trim();
        if (value.length() > maxLength) {
            return value.substring(0, maxLength);
        }
        return value;
    }

    private int ceilDiv(int value, int divisor)
    {
        if (value <= 0) {
            return 0;
        }
        return (value + divisor - 1) / divisor;
    }

    private int estimateTokens(String text)
    {
        return text == null ? 0 : Math.max(1, text.length() / 2);
    }

    private String trimTo(String text, int length)
    {
        if (text == null) {
            return "";
        }
        return text.length() <= length ? text : text.substring(0, length);
    }

    private String toJson(Object value)
    {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    private String safe(String value, String fallback)
    {
        return value == null ? fallback : value;
    }
}
