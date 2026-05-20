package com.ruoyi.tdw.service.impl;

import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.tdw.ai.prompt.TdwPromptTemplateService;
import com.ruoyi.tdw.ai.service.TdwAiService;
import com.ruoyi.tdw.domain.TdwBids;
import com.ruoyi.tdw.domain.TdwTenderFile;
import com.ruoyi.tdw.domain.TdwTenderParseReport;
import com.ruoyi.tdw.domain.dto.TdwServicePlanParseRequest;
import com.ruoyi.tdw.domain.dto.TdwServicePlanParseResult;
import com.ruoyi.tdw.mapper.TdwBidsMapper;
import com.ruoyi.tdw.mapper.TdwTenderFileMapper;
import com.ruoyi.tdw.mapper.TdwTenderParseReportMapper;
import com.ruoyi.tdw.service.ITdwTenderService;
import com.ruoyi.tdw.utils.FileParser;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TdwTenderServiceImpl implements ITdwTenderService
{
    private static final String PROMPT_ROOT = "prompts/ai-plan/";
    private static final String TENDER_INTERPRETATION_PROMPT = "prompts/tender-parse-interpretation-prompts.json";

    @Autowired
    private TdwBidsMapper tdwBidsMapper;

    @Autowired
    private TdwTenderFileMapper tenderFileMapper;

    @Autowired
    private TdwTenderParseReportMapper parseReportMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TdwAiService aiService;

    @Autowired
    private TdwPromptTemplateService promptTemplateService;

    @Override
    public List<TdwBids> selectTenderBidList(TdwBids query)
    {
        return tenderFileMapper.selectTenderBidList(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createTenderProject(String title, String category, String note, MultipartFile file) throws IOException
    {
        if (title == null || title.trim().length() == 0) {
            throw new IllegalArgumentException("项目名称不能为空");
        }
        TdwBids bid = new TdwBids();
        bid.setTitle(title.trim());
        bid.setCategory(category);
        bid.setNote(note);
        bid.setStatus(1L);
        bid.setCreatedTime(new Date());
        tdwBidsMapper.insertTdwBids(bid);

        TdwTenderFile tenderFile = null;
        if (file != null && !file.isEmpty()) {
            tenderFile = uploadTenderFile(bid.getId(), file, "outline_source");
        }
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("bid", bid);
        result.put("file", tenderFile);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TdwTenderFile uploadTenderFile(Long bidId, MultipartFile file) throws IOException
    {
        return uploadTenderFile(bidId, file, "outline_source");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TdwTenderFile uploadTenderFile(Long bidId, MultipartFile file, String fileStage) throws IOException
    {
        if (bidId == null) {
            throw new IllegalArgumentException("bidId不能为空");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("招标文件不能为空");
        }
        String fileUrl = FileUploadUtils.upload(RuoYiConfig.getUploadPath(), file);
        TdwTenderFile tenderFile = new TdwTenderFile();
        tenderFile.setBidId(bidId);
        tenderFile.setOriginalName(file.getOriginalFilename());
        tenderFile.setFileName(FilenameUtils.getName(fileUrl));
        tenderFile.setFileUrl(fileUrl);
        tenderFile.setFileSize(file.getSize());
        tenderFile.setFileType(FilenameUtils.getExtension(file.getOriginalFilename()));
        tenderFile.setFileStage(normalizeFileStage(fileStage));
        tenderFile.setParseStatus("uploaded");
        tenderFile.setCreateTime(DateUtils.getNowDate());
        tenderFile.setUpdateTime(DateUtils.getNowDate());
        tenderFileMapper.insertTenderFile(tenderFile);
        return tenderFile;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TdwTenderParseReport parseTenderInterpretation(Long tenderFileId)
    {
        return createTenderInterpretationReport(tenderFileId);
    }

    private TdwTenderParseReport createTenderInterpretationReport(Long tenderFileId)
    {
        TdwTenderFile tenderFile = tenderFileMapper.selectById(tenderFileId);
        if (tenderFile == null) {
            throw new IllegalArgumentException("招标文件不存在");
        }
        TdwTenderParseReport existingReport = parseReportMapper.selectByTenderFileId(tenderFileId);
        if (existingReport != null && "success".equals(existingReport.getParseStatus())) {
            return existingReport;
        }
        tenderFileMapper.updateParseStatus(tenderFileId, "parsing", null);
        TdwBids bid = tdwBidsMapper.selectTdwBidsById(tenderFile.getBidId());
        String sourceText = parseUploadedText(tenderFile);
        String projectName = inferProjectName(sourceText, bid == null ? null : bid.getTitle(), tenderFile.getOriginalName());

        Map<String, Object> report = generateTenderInterpretationReport(sourceText, tenderFile.getOriginalName());
        projectName = defaultString(extractReportNodeText(report, "name", 120), projectName);
        String requirementSummary = defaultString(extractReportNodeText(report, "PROJECT_INTERPRETATION", 1000),
                truncate(sourceText == null ? "" : sourceText.replaceAll("\\s+", " ").trim(), 500));
        String scoreItems = defaultString(extractReportNodeText(report, "EVALUATION_CRITERIA", 1200),
                extractReportNodeText(report, "EVALUATION_FRAMEWORK", 1200));

        TdwTenderParseReport parseReport = new TdwTenderParseReport();
        parseReport.setBidId(tenderFile.getBidId());
        parseReport.setTenderFileId(tenderFile.getId());
        parseReport.setProjectName(projectName);
        parseReport.setRequirementSummary(requirementSummary);
        parseReport.setScoreItems(scoreItems);
        try {
            parseReport.setReportContent(objectMapper.writeValueAsString(report));
        } catch (Exception e) {
            parseReport.setReportContent("{}");
        }
        parseReport.setParseStatus("success");
        parseReport.setCreateTime(DateUtils.getNowDate());
        parseReport.setUpdateTime(DateUtils.getNowDate());
        parseReportMapper.insertParseReport(parseReport);
        tenderFileMapper.updateParseStatus(tenderFile.getId(), "success", parseReport.getId());
        return parseReport;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TdwServicePlanParseResult parseServicePlan(TdwServicePlanParseRequest request)
    {
        if (request == null || request.getTenderFileId() == null) {
            throw new IllegalArgumentException("招标文件不能为空");
        }
        TdwTenderFile tenderFile = tenderFileMapper.selectById(request.getTenderFileId());
        if (tenderFile == null) {
            throw new IllegalArgumentException("招标文件不存在");
        }
        TdwBids bid = tdwBidsMapper.selectTdwBidsById(tenderFile.getBidId());
        String sourceText = parseUploadedText(tenderFile);
        String projectType = defaultString(request.getCategory(), "服务方案");

        Map<String, String> variables = promptVariables(projectType, tenderFile.getOriginalName(), sourceText, "", "", "");

        String projectName = extractWithPrompt(promptPath(projectType, "project-name"), variables, sourceText, "service-project-name");
        if (projectName == null || projectName.trim().length() == 0) {
            projectName = inferProjectName(sourceText, bid == null ? null : bid.getTitle(), tenderFile.getOriginalName());
        }
        projectName = projectName == null ? "" : projectName.trim();

        Map<String, String> extractedFields = extractInitialPlanFields(projectType, tenderFile.getOriginalName(), sourceText);
        String purchaseRequirement = defaultString(extractedFields.get("procurementNeed"), "");
        String otherAttachment = "";
        String purchasePrompt = "service".equals(normalizeCategoryKey(projectType))
                ? promptPath(projectType, "purchase-requirement-from-tender") : promptPath(projectType, "field-from-tender");
        String fullScoreItems = extractWithPrompt(promptPath(projectType, "score-items-full"), variables, sourceText, "service-score-full");

        variables.put("scoreItemsText", fullScoreItems);
        String technicalScoreItems = extractWithPrompt(promptPath(projectType, "score-items-technical"), variables, fullScoreItems, "service-score-technical");

        Map<String, Object> report = new LinkedHashMap<String, Object>();
        report.put("projectName", projectName);
        report.put("schemeType", projectType);
        report.put("sourceFileName", tenderFile.getOriginalName());
        report.put("purchaseRequirementSourceFileName", tenderFile.getOriginalName());
        report.put("otherAttachmentSourceFileName", "");
        report.put("purchaseRequirement", purchaseRequirement);
        report.put("otherAttachment", otherAttachment);
        report.put("extractedFields", extractedFields);
        report.put("fullScoreItems", fullScoreItems);
        report.put("technicalScoreItems", technicalScoreItems);
        report.put("promptTemplates", servicePromptTemplates(projectType, purchasePrompt));

        TdwTenderParseReport parseReport = new TdwTenderParseReport();
        parseReport.setBidId(tenderFile.getBidId());
        parseReport.setTenderFileId(tenderFile.getId());
        parseReport.setProjectName(projectName);
        parseReport.setRequirementSummary(purchaseRequirement);
        parseReport.setScoreItems(fullScoreItems);
        try {
            parseReport.setReportContent(objectMapper.writeValueAsString(report));
        } catch (Exception e) {
            parseReport.setReportContent("{}");
        }
        parseReport.setParseStatus("success");
        parseReport.setCreateTime(DateUtils.getNowDate());
        parseReport.setUpdateTime(DateUtils.getNowDate());
        parseReportMapper.insertParseReport(parseReport);
        tenderFileMapper.updateParseStatus(tenderFile.getId(), "success", parseReport.getId());

        if (projectName.length() > 0) {
            TdwBids update = new TdwBids();
            update.setId(tenderFile.getBidId());
            update.setTitle(projectName);
            tdwBidsMapper.updateTdwBids(update);
        }

        TdwServicePlanParseResult result = new TdwServicePlanParseResult();
        result.setReportId(parseReport.getId());
        result.setBidId(tenderFile.getBidId());
        result.setTenderFileId(tenderFile.getId());
        result.setProjectName(projectName);
        result.setPurchaseRequirement(purchaseRequirement);
        result.setOtherAttachment(otherAttachment);
        result.setExtractedFields(extractedFields);
        result.setFullScoreItems(fullScoreItems);
        result.setTechnicalScoreItems(technicalScoreItems);
        return result;
    }

    @Override
    public String extractServicePurchaseRequirementFromTenderFile(Long fileId, String category)
    {
        return extractPlanFieldFromTenderFile(fileId, category, "procurementNeed");
    }

    @Override
    public String extractServicePurchaseRequirementFromFile(Long fileId, String category)
    {
        return extractPlanFieldFromFile(fileId, category, "procurementNeed");
    }

    @Override
    public String extractPlanFieldFromTenderFile(Long fileId, String category, String fieldKey)
    {
        TdwTenderFile file = requireTenderFile(fileId);
        String sourceText = parseUploadedText(file);
        String projectType = defaultString(category, "服务方案");
        return extractPlanField(projectType, file.getOriginalName(), sourceText, fieldKey, false);
    }

    @Override
    public String extractPlanFieldFromFile(Long fileId, String category, String fieldKey)
    {
        TdwTenderFile file = requireTenderFile(fileId);
        String sourceText = parseUploadedText(file);
        String projectType = defaultString(category, "服务方案");
        return extractPlanField(projectType, file.getOriginalName(), sourceText, fieldKey, true);
    }

    @Override
    public String extractServiceOtherAttachmentFromFile(Long fileId, String category)
    {
        TdwTenderFile file = requireTenderFile(fileId);
        String sourceText = parseUploadedText(file);
        String projectType = defaultString(category, "服务方案");
        Map<String, String> variables = promptVariables(projectType, file.getOriginalName(), sourceText, "", sourceText, "");
        return extractWithPrompt(promptPath(projectType, "extra-attachment"), variables, sourceText, "service-extra-attachment");
    }

    @Override
    public String extractServiceScoreItemsFromTenderFile(Long fileId, String category)
    {
        TdwTenderFile file = requireTenderFile(fileId);
        String sourceText = parseUploadedText(file);
        String projectType = defaultString(category, "服务方案");
        Map<String, String> variables = promptVariables(projectType, file.getOriginalName(), sourceText, "", "", "");
        return extractWithPrompt(promptPath(projectType, "score-items-full"), variables, sourceText, "service-score-full");
    }

    @Override
    public String extractServiceScoreItemsFromPurchaseRequirement(String purchaseRequirement, String category)
    {
        String projectType = defaultString(category, "服务方案");
        String sourceText = defaultString(purchaseRequirement, "");
        Map<String, String> variables = promptVariables(projectType, "", "", sourceText, "", "");
        return extractWithPrompt(promptPath(projectType, "score-items-from-purchase"), variables, sourceText, "service-score-from-purchase");
    }

    @Override
    public String extractTechnicalScoreItems(String fullScoreItems, String category)
    {
        String projectType = defaultString(category, "服务方案");
        Map<String, String> variables = promptVariables(projectType, "", "", "", "", defaultString(fullScoreItems, ""));
        return extractWithPrompt(promptPath(projectType, "score-items-technical"), variables, defaultString(fullScoreItems, ""), "service-score-technical");
    }

    private Map<String, String> extractInitialPlanFields(String projectType, String fileName, String sourceText)
    {
        Map<String, String> fields = new LinkedHashMap<String, String>();
        for (String fieldKey : initialFieldKeys(projectType)) {
            fields.put(fieldKey, extractPlanField(projectType, fileName, sourceText, fieldKey, false));
        }
        return fields;
    }

    private String extractPlanField(String projectType, String fileName, String sourceText, String fieldKey, boolean fromUpload)
    {
        String normalizedFieldKey = defaultString(fieldKey, "procurementNeed");
        Map<String, String> variables = promptVariables(projectType, fileName, fromUpload ? "" : sourceText,
                "procurementNeed".equals(normalizedFieldKey) ? sourceText : "", "otherAttachment".equals(normalizedFieldKey) ? sourceText : "", "");
        variables.put("fieldKey", normalizedFieldKey);
        variables.put("fieldLabel", fieldLabel(normalizedFieldKey));
        variables.put("fieldSourceText", defaultString(sourceText, ""));
        String task = fromUpload ? "field-from-upload" : "field-from-tender";
        if (normalizeCategoryKey(projectType).equals("service") && "procurementNeed".equals(normalizedFieldKey)) {
            task = fromUpload ? "purchase-requirement-from-upload" : "purchase-requirement-from-tender";
        } else if ("otherAttachment".equals(normalizedFieldKey)) {
            task = "extra-attachment";
        }
        return extractWithPrompt(promptPath(projectType, task), variables, sourceText,
                normalizeCategoryKey(projectType) + "-field-" + normalizedFieldKey);
    }

    private List<String> initialFieldKeys(String category)
    {
        String key = normalizeCategoryKey(category);
        List<String> fields = new ArrayList<String>();
        if ("goods".equals(key)) {
            fields.add("procurementNeed");
            fields.add("goodsInfo");
        } else if ("engineering".equals(key)) {
            fields.add("engineeringOverview");
            fields.add("engineeringList");
            fields.add("drawingParse");
        } else if ("supervision".equals(key)) {
            fields.add("supervisionOverview");
            fields.add("engineeringOverview");
            fields.add("drawingParse");
        } else if ("it".equals(key)) {
            fields.add("projectOverview");
            fields.add("technicalRequirement");
            fields.add("equipmentList");
        } else {
            fields.add("procurementNeed");
        }
        return fields;
    }

    private String fieldLabel(String fieldKey)
    {
        if ("goodsInfo".equals(fieldKey)) return "货物信息";
        if ("engineeringOverview".equals(fieldKey)) return "工程概况";
        if ("engineeringList".equals(fieldKey)) return "工程量清单";
        if ("drawingParse".equals(fieldKey)) return "图纸解析";
        if ("supervisionOverview".equals(fieldKey)) return "监理概况";
        if ("projectOverview".equals(fieldKey)) return "项目概况";
        if ("technicalRequirement".equals(fieldKey)) return "技术要求（采购需求）";
        if ("equipmentList".equals(fieldKey)) return "设备清单";
        if ("otherAttachment".equals(fieldKey)) return "其他附件";
        return "采购需求";
    }

    private String extractWithPrompt(String promptPath, Map<String, String> variables, String inputText, String taskType)
    {
        String prompt = promptTemplateService.render(promptPath, variables);
        return defaultString(aiService.extractText(prompt, inputText, taskType), "").trim();
    }

    private TdwTenderFile requireTenderFile(Long fileId)
    {
        if (fileId == null) {
            throw new IllegalArgumentException("文件不能为空");
        }
        TdwTenderFile file = tenderFileMapper.selectById(fileId);
        if (file == null) {
            throw new IllegalArgumentException("文件不存在");
        }
        return file;
    }

    private Map<String, String> promptVariables(String projectType, String fileName, String documentText,
                                                String purchaseRequirementText, String otherAttachmentText,
                                                String scoreItemsText)
    {
        Map<String, String> variables = new HashMap<String, String>();
        variables.put("documentText", defaultString(documentText, ""));
        variables.put("purchaseRequirementText", defaultString(purchaseRequirementText, ""));
        variables.put("otherAttachmentText", defaultString(otherAttachmentText, ""));
        variables.put("scoreItemsText", defaultString(scoreItemsText, ""));
        variables.put("projectType", defaultString(projectType, "服务方案"));
        variables.put("fileName", defaultString(fileName, ""));
        return variables;
    }

    private Map<String, String> servicePromptTemplates(String projectType, String purchasePrompt)
    {
        Map<String, String> templates = new LinkedHashMap<String, String>();
        templates.put("projectName", promptPath(projectType, "project-name"));
        templates.put("purchaseRequirement", purchasePrompt);
        templates.put("purchaseRequirementUpload", promptPath(projectType, "purchase-requirement-from-upload"));
        templates.put("otherAttachmentUpload", promptPath(projectType, "extra-attachment"));
        templates.put("fullScoreItems", promptPath(projectType, "score-items-full"));
        templates.put("scoreItemsFromPurchase", promptPath(projectType, "score-items-from-purchase"));
        templates.put("technicalScoreItems", promptPath(projectType, "score-items-technical"));
        return templates;
    }

    private String promptPath(String category, String task)
    {
        String path = PROMPT_ROOT + normalizeCategoryKey(category) + "/" + task + ".md";
        if (new ClassPathResource(path).exists()) {
            return path;
        }
        return PROMPT_ROOT + "service/" + task + ".md";
    }

    private String normalizeCategoryKey(String category)
    {
        String value = defaultString(category, "服务");
        if (value.contains("服务")) {
            return "service";
        }
        if (value.contains("货物")) {
            return "goods";
        }
        if (value.contains("工程")) {
            return "engineering";
        }
        if (value.contains("监理")) {
            return "supervision";
        }
        if (value.contains("IT") || value.contains("信息")) {
            return "it";
        }
        return "other";
    }

    private String defaultString(String value, String fallback)
    {
        return value == null || value.trim().length() == 0 ? fallback : value;
    }

    private String parseUploadedText(TdwTenderFile tenderFile)
    {
        try {
            File file = resolveUploadedFile(tenderFile.getFileUrl());
            return FileParser.parseFileText(file, tenderFile.getOriginalName(), 16000);
        } catch (Exception e) {
            return "";
        }
    }

    private String normalizeFileStage(String fileStage)
    {
        if (fileStage == null || fileStage.trim().length() == 0) {
            return "outline_source";
        }
        String stage = fileStage.trim();
        if (!"outline_source".equals(stage)
                && !"content_prompt".equals(stage)
                && !"extra_material".equals(stage)
                && !"purchase_requirement".equals(stage)
                && !"other_attachment".equals(stage)) {
            return "extra_material";
        }
        return stage;
    }

    private File resolveUploadedFile(String fileUrl)
    {
        if (fileUrl == null || fileUrl.trim().length() == 0) {
            return null;
        }
        String normalized = fileUrl.replace("\\", "/");
        String marker = "/profile/upload/";
        int index = normalized.indexOf(marker);
        if (index >= 0) {
            String relativePath = normalized.substring(index + marker.length()).replace("/", File.separator);
            return new File(RuoYiConfig.getUploadPath(), relativePath);
        }
        String profileMarker = "/profile/";
        index = normalized.indexOf(profileMarker);
        if (index >= 0) {
            String relativePath = normalized.substring(index + profileMarker.length()).replace("/", File.separator);
            return new File(RuoYiConfig.getProfile(), relativePath);
        }
        return new File(normalized);
    }

    private String inferProjectName(String sourceText, String fallbackTitle, String originalName)
    {
        if (sourceText != null && sourceText.contains("高等学校教学资源管理、推荐和分析智慧平台开发")) {
            return "高等学校教学资源管理、推荐和分析智慧平台开发";
        }
        if (fallbackTitle != null && fallbackTitle.trim().length() > 0) {
            return fallbackTitle.trim();
        }
        return FilenameUtils.getBaseName(originalName == null ? "AI方案项目" : originalName);
    }

    private Map<String, Object> generateTenderInterpretationReport(String sourceText, String originalName) {
        try {
            String answer = aiService.extractText(buildTenderInterpretationPrompt(sourceText), sourceText, "tender-interpretation");
            Map<String, Object> aiReport = parseTenderInterpretationAiJson(answer);
            if (isTenderInterpretationReport(aiReport)) {
                Map<String, Object> report = normalizeTenderInterpretationReport(aiReport);
                attachTenderInterpretationMetadata(report, originalName);
                return report;
            }
            throw new IllegalStateException("大模型未返回合法的招标解读JSON");
        } catch (Exception e) {
            throw new IllegalStateException("大模型解析报告失败：" + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private String buildTenderInterpretationPrompt(String sourceText)
    {
        Map<String, Object> config;
        try {
            config = loadTenderInterpretationPromptConfig();
        } catch (Exception e) {
            config = new LinkedHashMap<String, Object>();
        }
        String systemPrompt = defaultString((String) config.get("systemPrompt"), "");
        String userPrompt = "";
        Object singlePassValue = config.get("singlePass");
        if (singlePassValue instanceof Map) {
            userPrompt = defaultString((String) ((Map<String, Object>) singlePassValue).get("userPrompt"), "");
        }
        if (userPrompt.length() == 0) {
            userPrompt = defaultString((String) config.get("commonUserTemplate"), "");
        }
        String prompt = (systemPrompt + "\n\n" + userPrompt).trim();
        if (prompt.length() == 0) {
            prompt = "请根据招标文件全文输出招标解读JSON，结构包含项目解读、项目基础信息、形式审查项、资格审查项、响应审查项、评审框架、无效标与废标项、关键字抓取项。\n\n【招标文件全文】\n{{tenderText}}";
        }
        return prompt.replace("{{tenderText}}", defaultString(sourceText, ""));
    }

    private Map<String, Object> parseTenderInterpretationAiJson(String answer) throws IOException
    {
        String value = defaultString(answer, "").trim();
        if (value.startsWith("```")) {
            value = value.replaceFirst("^```(?:json|JSON)?\\s*", "");
            value = value.replaceFirst("\\s*```$", "");
        }
        int start = value.indexOf('{');
        int end = value.lastIndexOf('}');
        if (start >= 0 && end > start) {
            value = value.substring(start, end + 1);
        }
        return objectMapper.readValue(value, new TypeReference<LinkedHashMap<String, Object>>() {});
    }

    private boolean isTenderInterpretationReport(Map<String, Object> report)
    {
        return report != null
                && "TENDER_INTERPRETATION_REPORT".equals(String.valueOf(report.get("k")))
                && report.get("v") instanceof List;
    }

    private Map<String, Object> normalizeTenderInterpretationReport(Map<String, Object> aiReport)
    {
        Map<String, Object> report = loadTenderInterpretationSchema();
        mergeTenderInterpretationNode(report, aiReport);
        return report;
    }

    @SuppressWarnings("unchecked")
    private void mergeTenderInterpretationNode(Map<String, Object> target, Map<String, Object> source)
    {
        if (target == null || source == null) {
            return;
        }
        Object targetValue = target.get("v");
        Object sourceValue = source.get("v");
        if (targetValue instanceof List && sourceValue instanceof List && isNodeList((List<Object>) targetValue)) {
            for (Object targetItem : (List<Object>) targetValue) {
                if (!(targetItem instanceof Map)) {
                    continue;
                }
                Map<String, Object> targetNode = (Map<String, Object>) targetItem;
                Map<String, Object> sourceNode = findNodeByKey((List<Object>) sourceValue, String.valueOf(targetNode.get("k")));
                if (sourceNode != null) {
                    mergeTenderInterpretationNode(targetNode, sourceNode);
                }
            }
        } else if (sourceValue != null) {
            target.put("v", sourceValue);
        }
    }

    @SuppressWarnings("unchecked")
    private boolean isNodeList(List<Object> values)
    {
        if (values == null || values.isEmpty()) {
            return false;
        }
        for (Object value : values) {
            if (value instanceof Map && ((Map<String, Object>) value).containsKey("k")) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> findNodeByKey(List<Object> nodes, String key)
    {
        if (nodes == null) {
            return null;
        }
        for (Object node : nodes) {
            if (node instanceof Map && key.equals(String.valueOf(((Map<String, Object>) node).get("k")))) {
                return (Map<String, Object>) node;
            }
        }
        return null;
    }

    private void attachTenderInterpretationMetadata(Map<String, Object> report, String originalName)
    {
        report.put("sourceFileName", originalName);
        report.put("promptConfigCode", "tender.parse.interpretation.prompts");
        report.put("promptResource", TENDER_INTERPRETATION_PROMPT);
    }

    private String extractReportNodeText(Map<String, Object> report, String key, int maxLength)
    {
        Map<String, Object> node = findNodeDeep(report, key);
        if (node == null) {
            return "";
        }
        return truncate(flattenReportValue(node.get("v")).trim(), maxLength);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> findNodeDeep(Map<String, Object> node, String key)
    {
        if (node == null) {
            return null;
        }
        if (key.equals(String.valueOf(node.get("k")))) {
            return node;
        }
        Object value = node.get("v");
        if (!(value instanceof List)) {
            return null;
        }
        for (Object child : (List<Object>) value) {
            if (child instanceof Map) {
                Map<String, Object> matched = findNodeDeep((Map<String, Object>) child, key);
                if (matched != null) {
                    return matched;
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private String flattenReportValue(Object value)
    {
        if (value == null) {
            return "";
        }
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) value;
            String name = map.get("n") == null ? "" : String.valueOf(map.get("n"));
            String childValue = flattenReportValue(map.get("v"));
            return name.length() == 0 ? childValue : name + "：" + childValue;
        }
        if (value instanceof List) {
            List<String> items = new ArrayList<String>();
            for (Object item : (List<Object>) value) {
                String text = flattenReportValue(item).trim();
                if (text.length() > 0) {
                    items.add(text);
                }
            }
            return String.join("\n", items);
        }
        return String.valueOf(value);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadTenderInterpretationSchema()
    {
        try {
            Map<String, Object> config = loadTenderInterpretationPromptConfig();
            Map<String, Object> singlePass = (Map<String, Object>) config.get("singlePass");
            Object schema = singlePass == null ? config.get("rootOutputSchema") : singlePass.get("outputSchema");
            Map<String, Object> converted = objectMapper.convertValue(schema, new TypeReference<LinkedHashMap<String, Object>>() {});
            return converted == null ? defaultTenderInterpretationSchema() : converted;
        } catch (Exception e) {
            return defaultTenderInterpretationSchema();
        }
    }

    private Map<String, Object> loadTenderInterpretationPromptConfig() throws IOException
    {
        ClassPathResource resource = new ClassPathResource(TENDER_INTERPRETATION_PROMPT);
        return objectMapper.readValue(resource.getInputStream(), new TypeReference<LinkedHashMap<String, Object>>() {});
    }

    private Map<String, Object> defaultTenderInterpretationSchema()
    {
        Map<String, Object> root = node("TENDER_INTERPRETATION_REPORT", "招标解读", new ArrayList<Object>());
        List<Object> sections = childList(root);
        sections.add(node("PROJECT_INTERPRETATION", "项目解读", new ArrayList<Object>()));
        sections.add(node("PROJECT_INFO", "项目基础信息", projectInfoFields()));
        sections.add(node("FORM_REVIEW", "形式审查项", reviewFields(
                node("DOC_CATALOG", "投标文件目录", new ArrayList<Object>()),
                node("BLIND_BID_FORMAT", "暗标格式", new ArrayList<Object>()),
                node("OTHER_FORM_REQUIREMENTS", "其他要求", new ArrayList<Object>()),
                node("PACKAGING_REQUIREMENTS", "封装要求", new ArrayList<Object>()))));
        sections.add(node("QUALIFICATION_REVIEW", "资格审查项", reviewFields(
                node("QUALIFICATION_REQUIREMENTS", "资质要求", new ArrayList<Object>()),
                node("PERFORMANCE_REQUIREMENTS", "业绩要求", new ArrayList<Object>()),
                node("FINANCIAL_REQUIREMENTS", "财务要求", new ArrayList<Object>()))));
        sections.add(node("RESPONSE_REVIEW", "响应审查项", reviewFields(
                node("TECHNICAL_REQUIREMENTS", "技术要求", new ArrayList<Object>()),
                node("PARAMETER_REQUIREMENTS", "参数要求", new ArrayList<Object>()),
                node("SUBSTANTIVE_TERMS", "实质性条款", new ArrayList<Object>()))));
        sections.add(node("EVALUATION_FRAMEWORK", "评审框架", reviewFields(
                node("EVALUATION_METHOD", "评审方式", new ArrayList<Object>()),
                node("EVALUATION_CRITERIA", "评审标准", new ArrayList<Object>()))));
        sections.add(node("INVALID_AND_WASTE_BID", "无效标与废标项", reviewFields(
                node("WASTE_BID_ITEMS", "废标项", new ArrayList<Object>()))));
        sections.add(node("KEYWORD_EXTRACTION", "关键字抓取项", reviewFields(
                node("KW_QUALIFICATION", "资格", new ArrayList<Object>()),
                node("KW_WASTE_BID", "废标", new ArrayList<Object>()),
                node("KW_INVALID", "无效", new ArrayList<Object>()),
                node("KW_RESPONSE", "响应", new ArrayList<Object>()),
                node("KW_COMMITMENT", "承诺", new ArrayList<Object>()))));
        return root;
    }

    private List<Object> projectInfoFields()
    {
        List<Object> fields = new ArrayList<Object>();
        fields.add(node("name", "项目名称", ""));
        fields.add(node("no", "招标编号", ""));
        fields.add(node("agencyNo", "委托代理编号", ""));
        fields.add(node("overview", "项目概况", ""));
        fields.add(node("tenderer", "招标人/采购人", ""));
        fields.add(node("funding", "资金来源及落实情况", ""));
        fields.add(node("lot_division", "标段/包号划分", new ArrayList<Object>()));
        fields.add(node("bid_deadline", "投标文件递交截止时间与地点", ""));
        fields.add(node("opening", "开标时间与地点", ""));
        fields.add(node("validity", "投标有效期", ""));
        fields.add(node("bid_bond", "投标保证金", ""));
        fields.add(node("perf_bond", "履约保证金", ""));
        fields.add(node("prebid_meeting", "现场踏勘/答疑会", ""));
        fields.add(node("proc_method", "采购方式", ""));
        fields.add(node("scope", "招标范围", new ArrayList<Object>()));
        fields.add(node("control_price", "招标控制价", ""));
        fields.add(node("bid_discount_rate", "投标竞争下浮率", ""));
        fields.add(node("allow_joint", "是否接受联合体投标", ""));
        fields.add(node("allow_subcontract", "是否允许分包", ""));
        fields.add(node("eval_method", "评标方法", ""));
        return fields;
    }

    private List<Object> reviewFields(Map<String, Object>... nodes)
    {
        List<Object> fields = new ArrayList<Object>();
        for (Map<String, Object> node : nodes) {
            fields.add(node);
        }
        return fields;
    }

    private Map<String, Object> node(String key, String name, Object value)
    {
        Map<String, Object> node = new LinkedHashMap<String, Object>();
        node.put("k", key);
        node.put("n", name);
        node.put("v", value);
        return node;
    }

    @SuppressWarnings("unchecked")
    private List<Object> childList(Map<String, Object> node)
    {
        Object value = node.get("v");
        if (value instanceof List) {
            return (List<Object>) value;
        }
        List<Object> list = new ArrayList<Object>();
        node.put("v", list);
        return list;
    }

    private String truncate(String text, int maxLength)
    {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }

    @Override
    public List<TdwTenderFile> selectFilesByBidId(Long bidId)
    {
        return tenderFileMapper.selectByBidId(bidId);
    }

    @Override
    public TdwTenderParseReport selectReportById(Long id)
    {
        return parseReportMapper.selectById(id);
    }

    @Override
    public TdwTenderParseReport selectReportByFileId(Long tenderFileId)
    {
        return parseReportMapper.selectByTenderFileId(tenderFileId);
    }

    @Override
    public TdwTenderParseReport selectLatestReportByBidId(Long bidId)
    {
        return parseReportMapper.selectLatestByBidId(bidId);
    }
}
