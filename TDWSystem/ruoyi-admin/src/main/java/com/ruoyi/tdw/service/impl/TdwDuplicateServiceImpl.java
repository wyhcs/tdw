package com.ruoyi.tdw.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.tdw.ai.prompt.TdwPromptTemplateService;
import com.ruoyi.tdw.ai.service.TdwAiService;
import com.ruoyi.tdw.domain.TdwBids;
import com.ruoyi.tdw.domain.TdwContents;
import com.ruoyi.tdw.domain.TdwDownloadRecord;
import com.ruoyi.tdw.domain.TdwDuplicateFile;
import com.ruoyi.tdw.domain.TdwDuplicateResult;
import com.ruoyi.tdw.domain.TdwDuplicateTask;
import com.ruoyi.tdw.domain.TdwKnowledgeFile;
import com.ruoyi.tdw.domain.TdwOutlines;
import com.ruoyi.tdw.domain.dto.TdwDuplicateRunRequest;
import com.ruoyi.tdw.mapper.TdwBidsMapper;
import com.ruoyi.tdw.mapper.TdwContentsMapper;
import com.ruoyi.tdw.mapper.TdwDuplicateFileMapper;
import com.ruoyi.tdw.mapper.TdwDuplicateResultMapper;
import com.ruoyi.tdw.mapper.TdwDuplicateTaskMapper;
import com.ruoyi.tdw.mapper.TdwOutlinesMapper;
import com.ruoyi.tdw.service.ITdwDownloadService;
import com.ruoyi.tdw.service.ITdwDuplicateService;
import com.ruoyi.tdw.service.ITdwKnowledgeService;
import com.ruoyi.tdw.utils.FileParser;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TdwDuplicateServiceImpl implements ITdwDuplicateService
{
    private static final long MAX_DUPLICATE_FILE_SIZE = 200L * 1024L * 1024L;
    private static final int MAX_COMPARE_FILE_COUNT = 3;
    private static final int MAX_EXTRACT_TEXT_CHARS = 80000;
    private static final int MAX_PROMPT_TEXT_CHARS = 60000;
    private static final String[] DUPLICATE_EXTENSIONS = new String[] { "doc", "docx" };

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
    private ITdwKnowledgeService knowledgeService;

    @Autowired
    private ITdwDownloadService downloadService;

    @Autowired
    private TdwAiService aiService;

    @Autowired
    private TdwPromptTemplateService promptTemplateService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${tdw.duplicate.prompt-path:prompts/duplicate-check.md}")
    private String duplicatePromptPath;

    @Value("${tdw.duplicate.report-docx-template:reports/duplicate/duplicate-report.docx}")
    private String reportDocxTemplate;

    @Value("${tdw.duplicate.report-xlsx-template:reports/duplicate/duplicate-detail.xlsx}")
    private String reportXlsxTemplate;

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
        if ((task == null || task.getBidId() == null) && (file == null || file.isEmpty())) {
            throw new IllegalArgumentException("待查重投标文件不能为空");
        }
        if (task == null) {
            task = new TdwDuplicateTask();
        }
        if (StringUtils.isBlank(task.getTaskName())) {
            task.setTaskName(resolveTaskName(task, file));
        }
        task.setSourceType(StringUtils.defaultIfBlank(task.getSourceType(), "file"));
        task.setCheckScope(StringUtils.defaultIfBlank(task.getCheckScope(), "full"));
        task.setStatus("draft");
        task.setTotalCount(0);
        task.setIssueCount(0);
        task.setRiskCount(0);
        task.setTotalTextLength(0);
        task.setOverallSimilarity(BigDecimal.ZERO);
        task.setTextSimilarity(BigDecimal.ZERO);
        task.setImageSimilarity(BigDecimal.ZERO);
        task.setRiskLevel("none");
        task.setCreateTime(DateUtils.getNowDate());
        task.setUpdateTime(DateUtils.getNowDate());
        task.setDelFlag("0");
        duplicateTaskMapper.insertDuplicateTask(task);
        if (file != null && !file.isEmpty()) {
            TdwDuplicateFile source = saveDuplicateFile(task, file, "source");
            TdwDuplicateTask update = new TdwDuplicateTask();
            update.setId(task.getId());
            update.setTotalTextLength(length(source.getExtractedText()));
            update.setUpdateTime(DateUtils.getNowDate());
            duplicateTaskMapper.updateDuplicateTask(update);
            task.setSourceFileName(source.getOriginalName());
            task.setSourceFileSize(source.getFileSize());
            task.setSourceFileType(source.getFileType());
            task.setSourceFileUrl(source.getFileUrl());
            task.setSourceTextLength(length(source.getExtractedText()));
            task.setTotalTextLength(length(source.getExtractedText()));
        }
        return task;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TdwDuplicateFile uploadCompareFile(Long taskId, MultipartFile file) throws IOException
    {
        List<TdwDuplicateFile> files = uploadCompareFiles(taskId, new MultipartFile[] { file });
        return files.isEmpty() ? null : files.get(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<TdwDuplicateFile> uploadCompareFiles(Long taskId, MultipartFile[] files) throws IOException
    {
        TdwDuplicateTask task = requireTask(taskId);
        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("对比文件不能为空");
        }
        int existingCount = duplicateFileMapper.countCompareFilesByTaskId(taskId);
        if (existingCount + files.length > MAX_COMPARE_FILE_COUNT) {
            throw new IllegalArgumentException("对比文件最多上传3份");
        }
        List<TdwDuplicateFile> savedFiles = new ArrayList<TdwDuplicateFile>();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            savedFiles.add(saveDuplicateFile(task, file, "compare"));
        }
        return savedFiles;
    }

    @Override
    public List<TdwDuplicateResult> runDuplicateTask(Long taskId)
    {
        return runDuplicateTask(taskId, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<TdwDuplicateResult> runDuplicateTask(Long taskId, TdwDuplicateRunRequest request)
    {
        TdwDuplicateTask task = requireTask(taskId);
        Date startedAt = DateUtils.getNowDate();
        List<Long> libraryIds = request == null ? parseLongList(task.getCompareLibraryIds()) : request.getCompareLibraryIds();
        String checkScope = request == null ? task.getCheckScope() : request.getCheckScope();
        if (libraryIds != null) {
            task.setCompareLibraryIds(joinIds(libraryIds));
        }
        task.setCheckScope(normalizeScope(checkScope));
        task.setStatus("running");
        task.setStartTime(startedAt);
        task.setFinishTime(null);
        task.setErrorMessage("");
        task.setUpdateTime(startedAt);
        duplicateTaskMapper.updateDuplicateTask(task);

        try {
            TdwDuplicateFile sourceFile = duplicateFileMapper.selectSourceFileByTaskId(taskId);
            SourceMaterial source = resolveSourceMaterial(task, sourceFile);
            List<TdwDuplicateFile> compareFiles = duplicateFileMapper.selectCompareFilesByTaskId(taskId);
            String libraryContext = buildLibraryContext(libraryIds);
            if (compareFiles.isEmpty() && StringUtils.isBlank(libraryContext)) {
                throw new IllegalArgumentException("请至少上传1份对比文件或选择对比文库");
            }

            Map<String, Object> report = callDuplicateModel(task, source, compareFiles, libraryContext);
            normalizeReport(report, task, source, compareFiles, libraryContext);
            List<TdwDuplicateResult> results = buildResults(task, report, compareFiles);

            duplicateResultMapper.deleteByTaskId(taskId);
            if (!results.isEmpty()) {
                duplicateResultMapper.batchInsertDuplicateResult(results);
            }

            task.setStatus("success");
            task.setTotalCount(compareFiles.size() + (StringUtils.isBlank(libraryContext) ? 0 : 1));
            task.setIssueCount(results.size());
            task.setRiskCount(intValue(report.get("riskCount"), results.size()));
            task.setTotalTextLength(length(source.text));
            task.setOverallSimilarity(decimalValue(report.get("overallSimilarity"), decimalValue(report.get("textSimilarity"), BigDecimal.ZERO)));
            task.setTextSimilarity(decimalValue(report.get("textSimilarity"), task.getOverallSimilarity()));
            task.setImageSimilarity(decimalValue(report.get("imageSimilarity"), BigDecimal.ZERO));
            task.setRiskLevel(StringUtils.defaultIfBlank(stringValue(report.get("riskLevel")), riskLevel(task.getTextSimilarity(), task.getRiskCount())));
            task.setReportJson(objectMapper.writeValueAsString(report));
            task.setFinishTime(DateUtils.getNowDate());
            task.setUpdateTime(DateUtils.getNowDate());
            duplicateTaskMapper.updateDuplicateTask(task);
            return results;
        } catch (RuntimeException e) {
            markTaskFailed(task, e.getMessage());
            throw e;
        } catch (Exception e) {
            markTaskFailed(task, e.getMessage());
            throw new IllegalStateException("方案查重失败：" + e.getMessage(), e);
        }
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
    @Transactional(rollbackFor = Exception.class)
    public int deleteDuplicateFileByIds(Long[] ids)
    {
        return duplicateFileMapper.deleteDuplicateFileByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateCompareLibraries(Long taskId, List<Long> compareLibraryIds)
    {
        requireTask(taskId);
        TdwDuplicateTask update = new TdwDuplicateTask();
        update.setId(taskId);
        update.setCompareLibraryIds(joinIds(compareLibraryIds));
        update.setUpdateTime(DateUtils.getNowDate());
        return duplicateTaskMapper.updateDuplicateTask(update);
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

    @Override
    public Map<String, Object> selectDuplicateReport(Long taskId) throws IOException
    {
        TdwDuplicateTask task = requireTask(taskId);
        TdwDuplicateFile fileQuery = new TdwDuplicateFile();
        fileQuery.setTaskId(taskId);
        List<TdwDuplicateFile> files = duplicateFileMapper.selectDuplicateFileList(fileQuery);
        TdwDuplicateResult resultQuery = new TdwDuplicateResult();
        resultQuery.setTaskId(taskId);
        List<TdwDuplicateResult> results = duplicateResultMapper.selectDuplicateResultList(resultQuery);

        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("task", task);
        payload.put("files", files);
        payload.put("results", results);
        payload.put("report", parseStoredReport(task.getReportJson()));
        return payload;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TdwDownloadRecord exportDuplicateReport(Long taskId) throws IOException
    {
        TdwDuplicateTask task = requireTask(taskId);
        String baseName = safeFileName(StringUtils.defaultIfBlank(task.getSourceFileName(), task.getTaskName()));
        String zipName = baseName + "_查重报告_" + taskId + "_" + System.currentTimeMillis() + ".zip";
        File dir = new File(RuoYiConfig.getProfile() + File.separator + "download" + File.separator + "bid");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File zipFile = new File(dir, zipName);
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            addResourceToZip(zos, reportDocxTemplate, baseName + "_查重报告.docx");
            addResourceToZip(zos, reportXlsxTemplate, baseName + "_查重明细.xlsx");
        }
        String fileUrl = Constants.RESOURCE_PREFIX + "/download/bid/" + zipName;
        return downloadService.recordGeneratedFile("duplicate", taskId, zipName, "zip", fileUrl, zipFile.length(), "方案查重报告ZIP导出");
    }

    private TdwDuplicateTask requireTask(Long taskId)
    {
        TdwDuplicateTask task = duplicateTaskMapper.selectDuplicateTaskById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("查重项目不存在");
        }
        return task;
    }

    private String resolveTaskName(TdwDuplicateTask task, MultipartFile file)
    {
        if (file != null && StringUtils.isNotBlank(file.getOriginalFilename())) {
            return FileParser.extractTitleFromFileName(file.getOriginalFilename());
        }
        if (task.getBidId() != null) {
            TdwBids bid = tdwBidsMapper.selectTdwBidsById(task.getBidId());
            if (bid != null && StringUtils.isNotBlank(bid.getTitle())) {
                return bid.getTitle();
            }
        }
        return "新建查重项目";
    }

    private TdwDuplicateFile saveDuplicateFile(TdwDuplicateTask task, MultipartFile file, String role) throws IOException
    {
        validateDuplicateFile(file, role);
        String extension = FilenameUtils.getExtension(file.getOriginalFilename()).toLowerCase();
        String extractedText = "";
        try {
            extractedText = FileParser.parseFileText(file, MAX_EXTRACT_TEXT_CHARS);
        } catch (Exception e) {
            extractedText = "";
        }

        String fileUrl = storeUploadFile(file, extension);
        TdwDuplicateFile duplicateFile = new TdwDuplicateFile();
        duplicateFile.setTaskId(task.getId());
        duplicateFile.setBidId(task.getBidId());
        duplicateFile.setFileRole(role);
        duplicateFile.setOriginalName(file.getOriginalFilename());
        duplicateFile.setFileName(FilenameUtils.getName(fileUrl));
        duplicateFile.setFileUrl(fileUrl);
        duplicateFile.setFileSize(file.getSize());
        duplicateFile.setFileType(extension);
        duplicateFile.setParseStatus(StringUtils.isBlank(extractedText) ? "uploaded" : "parsed");
        duplicateFile.setExtractedText(extractedText);
        duplicateFile.setEnabled("1");
        duplicateFile.setCreateTime(DateUtils.getNowDate());
        duplicateFile.setUpdateTime(DateUtils.getNowDate());
        duplicateFile.setDelFlag("0");
        duplicateFileMapper.insertDuplicateFile(duplicateFile);
        return duplicateFile;
    }

    private void validateDuplicateFile(MultipartFile file, String role)
    {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("source".equals(role) ? "待查重投标文件不能为空" : "对比文件不能为空");
        }
        if (file.getSize() > MAX_DUPLICATE_FILE_SIZE) {
            throw new IllegalArgumentException("单个文件不能超过200MB");
        }
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (StringUtils.isBlank(extension) || !Arrays.asList(DUPLICATE_EXTENSIONS).contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("仅支持doc/docx格式文件");
        }
    }

    private String storeUploadFile(MultipartFile file, String extension) throws IOException
    {
        String datePath = DateUtils.datePath();
        String storedName = datePath + "/" + IdUtils.fastSimpleUUID() + "." + extension;
        File target = new File(RuoYiConfig.getUploadPath(), storedName);
        if (!target.getParentFile().exists()) {
            target.getParentFile().mkdirs();
        }
        file.transferTo(target);
        return Constants.RESOURCE_PREFIX + "/upload/" + storedName.replace("\\", "/");
    }

    private SourceMaterial resolveSourceMaterial(TdwDuplicateTask task, TdwDuplicateFile sourceFile)
    {
        if (sourceFile != null) {
            return new SourceMaterial(sourceFile.getOriginalName(), sourceFile.getExtractedText(), sourceFile);
        }
        if (task.getBidId() != null) {
            TdwBids bid = tdwBidsMapper.selectTdwBidsById(task.getBidId());
            return new SourceMaterial(bid == null ? task.getTaskName() : bid.getTitle(), collectBidText(task.getBidId()), null);
        }
        throw new IllegalArgumentException("待查重投标文件不存在");
    }

    private Map<String, Object> callDuplicateModel(TdwDuplicateTask task, SourceMaterial source, List<TdwDuplicateFile> compareFiles,
                                                   String libraryContext) throws IOException
    {
        Map<String, String> variables = new LinkedHashMap<String, String>();
        variables.put("projectName", StringUtils.defaultIfBlank(task.getTaskName(), source.name));
        variables.put("checkScope", scopeText(task.getCheckScope()));
        variables.put("sourceFileName", source.name);
        variables.put("sourceText", truncate(source.text, MAX_PROMPT_TEXT_CHARS));
        variables.put("compareDocuments", buildCompareDocuments(compareFiles));
        variables.put("libraryContext", truncate(libraryContext, MAX_PROMPT_TEXT_CHARS / 2));
        variables.put("compareFileNames", joinCompareNames(compareFiles));
        String prompt = promptTemplateService.render(duplicatePromptPath, variables);
        String answer = aiService.extractText(prompt, "", "duplicate-check");
        Map<String, Object> report = parseReportJson(answer);
        report.put("modelRawAnswer", answer);
        return report;
    }

    private Map<String, Object> parseReportJson(String answer) throws IOException
    {
        String json = extractJson(answer);
        if (StringUtils.isBlank(json)) {
            throw new IllegalStateException("大模型未返回查重报告JSON");
        }
        return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
    }

    private String extractJson(String answer)
    {
        String value = StringUtils.defaultString(answer).trim();
        if (value.startsWith("```")) {
            value = value.replaceFirst("^```(?:json|JSON)?\\s*", "");
            value = value.replaceFirst("\\s*```$", "");
        }
        int start = value.indexOf('{');
        int end = value.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return value.substring(start, end + 1);
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    private void normalizeReport(Map<String, Object> report, TdwDuplicateTask task, SourceMaterial source,
                                 List<TdwDuplicateFile> compareFiles, String libraryContext)
    {
        BigDecimal fallbackSimilarity = maxLocalSimilarity(source.text, compareFiles, libraryContext);
        putDefault(report, "overallSimilarity", fallbackSimilarity);
        putDefault(report, "textSimilarity", fallbackSimilarity);
        putDefault(report, "imageSimilarity", BigDecimal.ZERO);
        putDefault(report, "riskCount", compareFiles.isEmpty() && StringUtils.isBlank(libraryContext) ? 0 : 1);
        putDefault(report, "riskLevel", riskLevel(decimalValue(report.get("textSimilarity"), fallbackSimilarity), intValue(report.get("riskCount"), 0)));
        putDefault(report, "checkScope", scopeText(task.getCheckScope()));
        putDefault(report, "sourceFileName", source.name);
        putDefault(report, "compareFileNames", joinCompareNames(compareFiles));

        Object duplicateSources = report.get("duplicateSources");
        if (!(duplicateSources instanceof List) || ((List<Object>) duplicateSources).isEmpty()) {
            report.put("duplicateSources", buildDuplicateSources(source.text, compareFiles, libraryContext));
        }
        Object topRisks = report.get("topRisks");
        if (!(topRisks instanceof List) || ((List<Object>) topRisks).isEmpty()) {
            report.put("topRisks", buildTopRisks(source.text, compareFiles, libraryContext, decimalValue(report.get("textSimilarity"), fallbackSimilarity)));
        }
        Object attrRisks = report.get("attributeRisks");
        if (!(attrRisks instanceof List) || ((List<Object>) attrRisks).isEmpty()) {
            report.put("attributeRisks", buildAttributeRisks(task, source));
        }
    }

    private void putDefault(Map<String, Object> report, String key, Object value)
    {
        if (!report.containsKey(key) || report.get(key) == null || StringUtils.isBlank(String.valueOf(report.get(key)))) {
            report.put(key, value);
        }
    }

    @SuppressWarnings("unchecked")
    private List<TdwDuplicateResult> buildResults(TdwDuplicateTask task, Map<String, Object> report, List<TdwDuplicateFile> compareFiles)
    {
        List<TdwDuplicateResult> results = new ArrayList<TdwDuplicateResult>();
        Object topRisks = report.get("topRisks");
        if (topRisks instanceof List) {
            for (Object item : (List<Object>) topRisks) {
                if (!(item instanceof Map)) {
                    continue;
                }
                Map<String, Object> risk = (Map<String, Object>) item;
                results.add(newResult(task, risk, compareFiles));
            }
        }
        if (results.isEmpty()) {
            Object duplicateSources = report.get("duplicateSources");
            if (duplicateSources instanceof List) {
                for (Object item : (List<Object>) duplicateSources) {
                    if (!(item instanceof Map)) {
                        continue;
                    }
                    Map<String, Object> source = (Map<String, Object>) item;
                    results.add(newResult(task, source, compareFiles));
                }
            }
        }
        return results;
    }

    private TdwDuplicateResult newResult(TdwDuplicateTask task, Map<String, Object> payload, List<TdwDuplicateFile> compareFiles)
    {
        TdwDuplicateResult result = new TdwDuplicateResult();
        result.setTaskId(task.getId());
        result.setBidId(task.getBidId());
        result.setSourceType("file");
        result.setSourceName(resolveResultSourceName(payload, compareFiles));
        result.setSimilarText(truncate(firstString(payload, "originalText", "sourceText", "riskText", "similarText"), 500));
        result.setSourceText(truncate(firstString(payload, "matchedText", "compareText", "evidenceText"), 500));
        result.setSimilarity(decimalValue(firstValue(payload, "similarity", "similarityRate", "textSimilarity"), task.getTextSimilarity()));
        result.setSuggestion(StringUtils.defaultIfBlank(firstString(payload, "suggestion", "riskAnalysis", "analysis"), "建议调整重复表达，补充项目专属数据、实施细节和差异化证据。"));
        result.setCreateTime(DateUtils.getNowDate());
        result.setUpdateTime(DateUtils.getNowDate());
        result.setDelFlag("0");
        return result;
    }

    private String resolveResultSourceName(Map<String, Object> payload, List<TdwDuplicateFile> compareFiles)
    {
        String name = firstString(payload, "compareFile", "sourceName", "fileName");
        if (StringUtils.isNotBlank(name)) {
            return name;
        }
        return compareFiles.isEmpty() ? "对比文库" : compareFiles.get(0).getOriginalName();
    }

    private String buildLibraryContext(List<Long> libraryIds)
    {
        if (libraryIds == null || libraryIds.isEmpty()) {
            return "";
        }
        List<Long> fileIds = new ArrayList<Long>();
        for (Long libraryId : libraryIds) {
            if (libraryId == null) {
                continue;
            }
            TdwKnowledgeFile query = new TdwKnowledgeFile();
            query.setKnowledgeId(libraryId);
            List<TdwKnowledgeFile> files = knowledgeService.selectKnowledgeFileList(query);
            for (TdwKnowledgeFile file : files) {
                fileIds.add(file.getKnowledgeFileId());
            }
        }
        return knowledgeService.buildKnowledgeContext(fileIds, Collections.<Long>emptyList());
    }

    private String buildCompareDocuments(List<TdwDuplicateFile> compareFiles)
    {
        if (compareFiles == null || compareFiles.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        int perFileLimit = Math.max(12000, MAX_PROMPT_TEXT_CHARS / Math.max(compareFiles.size(), 1));
        for (int i = 0; i < compareFiles.size(); i++) {
            TdwDuplicateFile file = compareFiles.get(i);
            builder.append("【对比文件").append(i + 1).append("：").append(file.getOriginalName()).append("】\n");
            builder.append(truncate(file.getExtractedText(), perFileLimit)).append("\n\n");
        }
        return builder.toString().trim();
    }

    private List<Map<String, Object>> buildDuplicateSources(String sourceText, List<TdwDuplicateFile> compareFiles, String libraryContext)
    {
        List<Map<String, Object>> sources = new ArrayList<Map<String, Object>>();
        for (TdwDuplicateFile file : compareFiles) {
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            item.put("sourceName", file.getOriginalName());
            item.put("textSimilarity", similarity(sourceText, file.getExtractedText()));
            item.put("duplicateImages", 0);
            item.put("similarImages", 0);
            item.put("riskLevel", riskLevel(decimalValue(item.get("textSimilarity"), BigDecimal.ZERO), 0));
            sources.add(item);
        }
        if (StringUtils.isNotBlank(libraryContext)) {
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            item.put("sourceName", "对比文库");
            item.put("textSimilarity", similarity(sourceText, libraryContext));
            item.put("duplicateImages", 0);
            item.put("similarImages", 0);
            item.put("riskLevel", riskLevel(decimalValue(item.get("textSimilarity"), BigDecimal.ZERO), 0));
            sources.add(item);
        }
        return sources;
    }

    private List<Map<String, Object>> buildTopRisks(String sourceText, List<TdwDuplicateFile> compareFiles, String libraryContext, BigDecimal similarity)
    {
        List<Map<String, Object>> risks = new ArrayList<Map<String, Object>>();
        if (!compareFiles.isEmpty()) {
            TdwDuplicateFile compare = compareFiles.get(0);
            risks.add(topRisk(compare.getOriginalName(), sourceText, compare.getExtractedText(), similarity));
        } else if (StringUtils.isNotBlank(libraryContext)) {
            risks.add(topRisk("对比文库", sourceText, libraryContext, similarity));
        }
        return risks;
    }

    private Map<String, Object> topRisk(String sourceName, String sourceText, String compareText, BigDecimal similarity)
    {
        Map<String, Object> item = new LinkedHashMap<String, Object>();
        item.put("similarity", similarity);
        item.put("compareFile", sourceName);
        item.put("location", "文档正文");
        item.put("originalText", snippet(sourceText));
        item.put("matchedText", snippet(compareText));
        item.put("riskAnalysis", "检测对象与对比来源存在较高相似表达，建议补充项目专属参数、组织安排和交付细节。");
        item.put("suggestion", "对相似段落进行改写，并加入本项目独有的实施方法、数据依据和风险控制措施。");
        return item;
    }

    private List<Map<String, Object>> buildAttributeRisks(TdwDuplicateTask task, SourceMaterial source)
    {
        List<Map<String, Object>> risks = new ArrayList<Map<String, Object>>();
        risks.add(attributeRisk("创建时间", task.getCreateTime() == null ? "" : DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, task.getCreateTime()), "low", "仅作为辅助核验信息，不单独作为废标依据。"));
        risks.add(attributeRisk("文件名称", source.name, "low", "文件名称与项目命名一致性正常，建议最终版导出前复核。"));
        risks.add(attributeRisk("编辑软件", "未知", "low", "上传文件未解析到稳定的软件版本信息。"));
        return risks;
    }

    private Map<String, Object> attributeRisk(String field, String value, String level, String analysis)
    {
        Map<String, Object> risk = new LinkedHashMap<String, Object>();
        risk.put("field", field);
        risk.put("detectedValue", value);
        risk.put("riskLevel", level);
        risk.put("riskAnalysis", analysis);
        return risk;
    }

    private BigDecimal maxLocalSimilarity(String sourceText, List<TdwDuplicateFile> compareFiles, String libraryContext)
    {
        BigDecimal max = BigDecimal.ZERO;
        for (TdwDuplicateFile file : compareFiles) {
            BigDecimal value = similarity(sourceText, file.getExtractedText());
            if (value.compareTo(max) > 0) {
                max = value;
            }
        }
        if (StringUtils.isNotBlank(libraryContext)) {
            BigDecimal value = similarity(sourceText, libraryContext);
            if (value.compareTo(max) > 0) {
                max = value;
            }
        }
        return max;
    }

    private BigDecimal similarity(String left, String right)
    {
        Set<String> a = shingles(left);
        Set<String> b = shingles(right);
        if (a.isEmpty() || b.isEmpty()) {
            return BigDecimal.ZERO;
        }
        Set<String> intersection = new HashSet<String>(a);
        intersection.retainAll(b);
        Set<String> union = new HashSet<String>(a);
        union.addAll(b);
        return new BigDecimal(intersection.size() * 100.0 / union.size()).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private Set<String> shingles(String text)
    {
        String value = normalize(text);
        Set<String> shingles = new HashSet<String>();
        if (value.length() < 4) {
            if (value.length() > 0) {
                shingles.add(value);
            }
            return shingles;
        }
        for (int i = 0; i <= value.length() - 4; i++) {
            shingles.add(value.substring(i, i + 4));
        }
        return shingles;
    }

    private String collectBidText(Long bidId)
    {
        List<TextSegment> segments = collectBidSegments(bidId);
        StringBuilder builder = new StringBuilder();
        for (TextSegment segment : segments) {
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(segment.text);
        }
        return builder.toString();
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
                segments.add(new TextSegment(outline.getTitle()));
            }
            List<TdwContents> contents = tdwContentsMapper.selectTdwContentsByOutlineId(outline.getId());
            for (TdwContents content : contents) {
                String text = contentToText(content);
                if (StringUtils.isNotBlank(text)) {
                    segments.add(new TextSegment(text));
                }
            }
            appendOutlineSegments(childMap, outline.getId(), segments);
        }
    }

    private String contentToText(TdwContents content)
    {
        Map<String, Object> json = parseContentJson(content.getContent());
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

    private Map<String, Object> parseContentJson(String value)
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

    private Map<String, Object> parseStoredReport(String reportJson) throws IOException
    {
        if (StringUtils.isBlank(reportJson)) {
            return new LinkedHashMap<String, Object>();
        }
        return objectMapper.readValue(reportJson, new TypeReference<Map<String, Object>>() {});
    }

    private void addResourceToZip(ZipOutputStream zos, String resourcePath, String entryName) throws IOException
    {
        ClassPathResource resource = new ClassPathResource(resourcePath);
        zos.putNextEntry(new ZipEntry(entryName));
        if (resource.exists()) {
            try (InputStream input = resource.getInputStream()) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = input.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }
            }
        } else {
            zos.write(("查重报告模板缺失：" + resourcePath).getBytes("UTF-8"));
        }
        zos.closeEntry();
    }

    private void markTaskFailed(TdwDuplicateTask task, String message)
    {
        TdwDuplicateTask update = new TdwDuplicateTask();
        update.setId(task.getId());
        update.setStatus("fail");
        update.setErrorMessage(StringUtils.left(StringUtils.defaultString(message), 1000));
        update.setFinishTime(DateUtils.getNowDate());
        update.setUpdateTime(DateUtils.getNowDate());
        duplicateTaskMapper.updateDuplicateTask(update);
    }

    private List<Long> parseLongList(String value)
    {
        List<Long> result = new ArrayList<Long>();
        if (StringUtils.isBlank(value)) {
            return result;
        }
        String[] parts = value.split(",");
        for (String part : parts) {
            try {
                result.add(Long.valueOf(part.trim()));
            } catch (Exception ignored) {
            }
        }
        return result;
    }

    private String joinIds(List<Long> values)
    {
        if (values == null || values.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (Long value : values) {
            if (value == null) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(',');
            }
            builder.append(value);
        }
        return builder.toString();
    }

    private String joinCompareNames(List<TdwDuplicateFile> compareFiles)
    {
        List<String> names = new ArrayList<String>();
        for (TdwDuplicateFile file : compareFiles) {
            names.add(file.getOriginalName());
        }
        return StringUtils.join(names, "、");
    }

    private String normalizeScope(String scope)
    {
        String value = StringUtils.defaultIfBlank(scope, "full");
        if ("image".equals(value) || "text".equals(value) || "full".equals(value)) {
            return value;
        }
        return "full";
    }

    private String scopeText(String scope)
    {
        String value = normalizeScope(scope);
        if ("image".equals(value)) {
            return "图片";
        }
        if ("text".equals(value)) {
            return "文字";
        }
        return "全文档";
    }

    private String riskLevel(BigDecimal similarity, int riskCount)
    {
        BigDecimal value = similarity == null ? BigDecimal.ZERO : similarity;
        if (value.compareTo(new BigDecimal("70")) >= 0 || riskCount >= 3) {
            return "high";
        }
        if (value.compareTo(new BigDecimal("35")) >= 0 || riskCount > 0) {
            return "middle";
        }
        return "low";
    }

    private Object firstValue(Map<String, Object> map, String... keys)
    {
        for (String key : keys) {
            Object value = map.get(key);
            if (value != null && StringUtils.isNotBlank(String.valueOf(value))) {
                return value;
            }
        }
        return null;
    }

    private String firstString(Map<String, Object> map, String... keys)
    {
        Object value = firstValue(map, keys);
        return value == null ? "" : String.valueOf(value);
    }

    private BigDecimal decimalValue(Object value, BigDecimal fallback)
    {
        if (value == null) {
            return fallback;
        }
        try {
            String text = String.valueOf(value).replace("%", "").trim();
            return new BigDecimal(text).setScale(2, BigDecimal.ROUND_HALF_UP);
        } catch (Exception e) {
            return fallback;
        }
    }

    private int intValue(Object value, int fallback)
    {
        if (value == null) {
            return fallback;
        }
        try {
            return new BigDecimal(String.valueOf(value).replace("%", "").trim()).intValue();
        } catch (Exception e) {
            return fallback;
        }
    }

    private String stringValue(Object value)
    {
        return value == null ? "" : String.valueOf(value);
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

    private String snippet(String text)
    {
        return truncate(StringUtils.defaultString(text).replaceAll("\\s+", " ").trim(), 140);
    }

    private String truncate(String value, int max)
    {
        String text = StringUtils.defaultString(value);
        return text.length() <= max ? text : text.substring(0, max);
    }

    private String normalize(String value)
    {
        return StringUtils.defaultString(value).replaceAll("\\s+", "");
    }

    private int length(String value)
    {
        return StringUtils.defaultString(value).length();
    }

    private String safeFileName(String value)
    {
        return StringUtils.defaultIfBlank(value, "duplicate").replaceAll("[\\\\/:*?\"<>|\\s]+", "_");
    }

    private static class SourceMaterial
    {
        private final String name;
        private final String text;
        private final TdwDuplicateFile file;

        private SourceMaterial(String name, String text, TdwDuplicateFile file)
        {
            this.name = StringUtils.defaultIfBlank(name, "待查重文档");
            this.text = StringUtils.defaultString(text);
            this.file = file;
        }
    }

    private static class TextSegment
    {
        private final String text;

        private TextSegment(String text)
        {
            this.text = text;
        }
    }
}
