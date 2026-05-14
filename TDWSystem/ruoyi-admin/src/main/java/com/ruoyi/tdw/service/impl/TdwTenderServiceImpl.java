package com.ruoyi.tdw.service.impl;

import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    public TdwTenderParseReport mockParse(Long tenderFileId)
    {
        TdwTenderFile tenderFile = tenderFileMapper.selectById(tenderFileId);
        if (tenderFile == null) {
            throw new IllegalArgumentException("招标文件不存在");
        }
        TdwBids bid = tdwBidsMapper.selectTdwBidsById(tenderFile.getBidId());
        String sourceText = parseUploadedText(tenderFile);
        String projectName = inferProjectName(sourceText, bid == null ? null : bid.getTitle(), tenderFile.getOriginalName());
        String requirementSummary = buildRequirementSummary(sourceText);
        String scoreItems = buildScoreItems(sourceText);

        Map<String, Object> report = new LinkedHashMap<String, Object>();
        report.put("projectName", projectName);
        report.put("sourceFileName", tenderFile.getOriginalName());
        report.put("projectBasicInfo", buildProjectBasicInfo(projectName, sourceText));
        report.put("purchaseRequirement", extractSection(sourceText, "采购需求", "技术标准与要求", "主要商务要求", "项目概况"));
        report.put("serviceOrTechnicalRequirements", extractSection(sourceText, "技术标准与要求", "功能要求", "性能要求", "服务要求"));
        report.put("qualificationRequirements", extractSection(sourceText, "资格", "投标人资格", "资格审查"));
        report.put("scoreItems", buildScoreItemList(sourceText));
        report.put("invalidBidClauses", extractSection(sourceText, "废标", "无效投标", "无效标"));
        report.put("responseDocumentComposition", extractSection(sourceText, "投标文件组成", "响应文件格式", "应提交材料"));
        report.put("contractTerms", extractSection(sourceText, "合同", "履约", "验收", "付款"));
        report.put("rawText", sourceText);
        report.put("sourceFileName", tenderFile.getOriginalName());

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

    private String buildRequirementSummary(String sourceText)
    {
        if (sourceText != null && sourceText.contains("高等学校教学资源管理")) {
            return "围绕高校教学资源管理、个性化推荐、学生行为数据分析和决策分析能力建设，需响应统一用户管理、权限管理、基础信息管理、资源管理、接口管理、智能推荐、可视化分析、教师教研管理和基础服务等核心要求。";
        }
        if (sourceText == null || sourceText.trim().length() == 0) {
            return "Mock解析：需围绕招标文件要求响应技术方案、实施计划、质量保障、服务承诺和商务条款。";
        }
        return truncate(sourceText.replaceAll("\\s+", " "), 500);
    }

    private String buildScoreItems(String sourceText)
    {
        if (sourceText != null && sourceText.contains("技术参数")) {
            return "技术参数（51.0分）；项目实施阶段方案（9.0分）；可视化开发技术方案（6.0分）；质量保障与售后服务响应";
        }
        return "技术方案响应；实施组织与进度；质量与风险控制；售后服务承诺；商务响应";
    }

    private Map<String, Object> buildProjectBasicInfo(String projectName, String sourceText)
    {
        Map<String, Object> info = new LinkedHashMap<String, Object>();
        info.put("项目名称", projectName);
        info.put("方案类型", "服务方案");
        info.put("识别来源", sourceText == null || sourceText.trim().isEmpty() ? "文件名与用户录入信息" : "上传文件正文");
        return info;
    }

    private List<Map<String, Object>> buildScoreItemList(String sourceText)
    {
        List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
        if (sourceText != null && sourceText.contains("技术参数")) {
            items.add(scoreItem("技术参数", "51.0分", "投标产品与招标文件规定的技术参数和要求的满足程度，需完整响应实质性参数与一般参数。"));
            items.add(scoreItem("项目实施阶段方案", "9.0分", "围绕需求分析、实施准备、培训、测试、试运行和验收等阶段形成可执行方案。"));
            items.add(scoreItem("可视化开发技术方案", "6.0分", "结合实时数据、前端展示窗口、系统数据架构和系统应用架构进行响应。"));
            return items;
        }
        items.add(scoreItem("技术方案响应", "重点项", "覆盖采购需求、服务或技术要求和实施路线。"));
        items.add(scoreItem("实施组织与进度", "重点项", "说明团队组织、阶段计划、交付控制和协同机制。"));
        items.add(scoreItem("质量与风险控制", "重点项", "说明质量检查、风险识别、应急处理和售后保障。"));
        return items;
    }

    private Map<String, Object> scoreItem(String name, String score, String requirement)
    {
        Map<String, Object> item = new LinkedHashMap<String, Object>();
        item.put("name", name);
        item.put("score", score);
        item.put("requirement", requirement);
        return item;
    }

    private String extractSection(String sourceText, String... keywords)
    {
        if (sourceText == null || sourceText.trim().length() == 0 || keywords == null) {
            return "";
        }
        String compact = sourceText.replaceAll("\\s+", " ");
        for (String keyword : keywords) {
            int index = compact.indexOf(keyword);
            if (index >= 0) {
                int end = Math.min(compact.length(), index + 700);
                return compact.substring(index, end);
            }
        }
        return "";
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
