package com.ruoyi.tdw.service.impl;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.framework.manager.AsyncManager;
import com.ruoyi.tdw.ai.service.TdwAiService;
import com.ruoyi.tdw.domain.TdwBids;
import com.ruoyi.tdw.domain.TdwQualityFramework;
import com.ruoyi.tdw.domain.TdwQualityItem;
import com.ruoyi.tdw.domain.TdwQualityResult;
import com.ruoyi.tdw.domain.TdwQualityTask;
import com.ruoyi.tdw.domain.TdwTenderFile;
import com.ruoyi.tdw.mapper.TdwBidsMapper;
import com.ruoyi.tdw.mapper.TdwQualityFrameworkMapper;
import com.ruoyi.tdw.mapper.TdwQualityItemMapper;
import com.ruoyi.tdw.mapper.TdwQualityResultMapper;
import com.ruoyi.tdw.mapper.TdwQualityTaskMapper;
import com.ruoyi.tdw.mapper.TdwTenderFileMapper;
import com.ruoyi.tdw.service.ITdwQualityService;
import com.ruoyi.tdw.utils.FileParser;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TdwQualityServiceImpl implements ITdwQualityService
{
    private static final String PROJECT_CATEGORY = "quality";
    private static final String TENDER_STAGE = "quality_tender";
    private static final String BID_STAGE = "quality_bid";
    private static final String PROMPT_RESOURCE = "prompts/quality-inspection-prompts.json";
    private static final int MAX_MODEL_DOC_CHARS = 120000;
    private static final Pattern QUOTED_TEXT_PATTERN = Pattern.compile("[“\"]([^”\"]{2,80})[”\"]");
    @Autowired
    private TdwQualityTaskMapper qualityTaskMapper;

    @Autowired
    private TdwQualityResultMapper qualityResultMapper;

    @Autowired
    private TdwQualityItemMapper qualityItemMapper;

    @Autowired
    private TdwQualityFrameworkMapper qualityFrameworkMapper;

    @Autowired
    private TdwBidsMapper tdwBidsMapper;

    @Autowired
    private TdwTenderFileMapper tenderFileMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TdwAiService aiService;

    @Override
    public List<TdwBids> selectQualityProjectList(TdwBids query)
    {
        TdwBids safeQuery = query == null ? new TdwBids() : query;
        safeQuery.setCategory(PROJECT_CATEGORY);
        return tdwBidsMapper.selectTdwBidsList(safeQuery);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createQualityProject(MultipartFile file) throws IOException
    {
        validateUploadFile(file, new String[] { "doc", "docx", "pdf" }, 50L * 1024L * 1024L, "招标文件");
        TdwBids bid = new TdwBids();
        bid.setTitle(FileParser.extractTitleFromFileName(file.getOriginalFilename()));
        bid.setCategory(PROJECT_CATEGORY);
        bid.setStatus(1L);
        bid.setCreatedTime(new Date());
        bid.setNote("AI质检项目");
        tdwBidsMapper.insertTdwBids(bid);

        TdwTenderFile tenderFile = uploadQualityFile(bid.getId(), file, TENDER_STAGE);
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("bid", bid);
        result.put("tenderFile", tenderFile);
        return result;
    }

    @Override
    public Map<String, Object> selectQualityProjectDetail(Long bidId)
    {
        if (bidId == null) {
            throw new IllegalArgumentException("项目ID不能为空");
        }
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        TdwBids bid = tdwBidsMapper.selectTdwBidsById(bidId);
        TdwTenderFile tenderFile = tenderFileMapper.selectLatestByBidIdAndStage(bidId, TENDER_STAGE);
        TdwQualityFramework framework = qualityFrameworkMapper.selectLatestByBidId(bidId);
        List<TdwQualityItem> items = new ArrayList<TdwQualityItem>();
        if (framework != null) {
            TdwQualityItem query = new TdwQualityItem();
            query.setFrameworkId(framework.getId());
            items = qualityItemMapper.selectQualityItemList(query);
        }
        TdwQualityTask taskQuery = new TdwQualityTask();
        taskQuery.setBidId(bidId);
        List<TdwQualityTask> tasks = qualityTaskMapper.selectQualityTaskList(taskQuery);
        result.put("bid", bid);
        result.put("tenderFile", tenderFile);
        result.put("framework", framework);
        result.put("items", items);
        result.put("tasks", tasks);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TdwQualityFramework uploadQualityFramework(Long bidId, MultipartFile file) throws IOException
    {
        ensureQualityProject(bidId);
        validateUploadFile(file, new String[] { "xls", "xlsx" }, 50L * 1024L * 1024L, "质检框架");
        String fileUrl = FileUploadUtils.upload(RuoYiConfig.getUploadPath(), file);

        TdwQualityFramework framework = newFramework(bidId,
                FilenameUtils.getBaseName(file.getOriginalFilename()),
                "用户上传的质检框架");
        framework.setFileName(FilenameUtils.getName(fileUrl));
        framework.setOriginalName(file.getOriginalFilename());
        framework.setFileUrl(fileUrl);
        framework.setFileSize(file.getSize());
        framework.setStatus("parsing");
        qualityFrameworkMapper.insertQualityFramework(framework);

        List<TdwQualityItem> items = parseWorkbookItems(framework.getId(), file);
        if (items.isEmpty()) {
            throw new IllegalArgumentException("质检框架中未识别到检查项");
        }
        saveFrameworkItems(framework, items);
        return qualityFrameworkMapper.selectQualityFrameworkById(framework.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TdwQualityFramework extractQualityFramework(Long bidId)
    {
        TdwBids bid = ensureQualityProject(bidId);
        TdwTenderFile tenderFile = tenderFileMapper.selectLatestByBidIdAndStage(bidId, TENDER_STAGE);
        if (tenderFile == null) {
            throw new IllegalArgumentException("请先上传招标文件");
        }
        String tenderText = parseStoredText(tenderFile, MAX_MODEL_DOC_CHARS);
        if (StringUtils.isBlank(tenderText)) {
            throw new IllegalArgumentException("招标文件内容解析为空，无法智能提取质检框架");
        }

        TdwQualityFramework framework = newFramework(bidId, bid.getTitle() + "质检框架", "大模型智能提取");
        framework.setStatus("extracting");
        qualityFrameworkMapper.insertQualityFramework(framework);

        JsonNode config = loadPromptConfig();
        Map<String, String> variables = new HashMap<String, String>();
        variables.put("projectName", StringUtils.defaultString(bid.getTitle()));
        variables.put("tenderFileName", StringUtils.defaultString(tenderFile.getOriginalName()));
        variables.put("tenderText", truncate(tenderText, MAX_MODEL_DOC_CHARS));
        variables.put("reviewFramework", reviewFrameworkKnowledge(config));
        String prompt = renderStagePrompt(config, "extract_framework", variables);
        String answer = aiService.extractText(prompt, "", "quality-framework-extract");
        List<TdwQualityItem> items = parseExtractedItems(framework.getId(), answer);
        if (items.isEmpty()) {
            throw new IllegalStateException("大模型未返回可用的质检检查项，请检查提示词或模型响应");
        }
        saveFrameworkItems(framework, items);
        return qualityFrameworkMapper.selectQualityFrameworkById(framework.getId());
    }

    @Override
    public List<TdwQualityFramework> selectQualityFrameworkList(TdwQualityFramework query)
    {
        return qualityFrameworkMapper.selectQualityFrameworkList(query);
    }

    @Override
    public List<TdwQualityItem> selectQualityItemList(TdwQualityItem query)
    {
        return qualityItemMapper.selectQualityItemList(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TdwQualityItem createQualityItem(TdwQualityItem item)
    {
        if (item == null || item.getFrameworkId() == null) {
            throw new IllegalArgumentException("质检框架ID不能为空");
        }
        if (StringUtils.isBlank(item.getItemName())) {
            throw new IllegalArgumentException("检查项不能为空");
        }
        item.setItemType(normalizeItemType(item.getItemType(), item.getItemName()));
        item.setCheckRule(StringUtils.defaultIfBlank(item.getCheckRule(), item.getItemName()));
        item.setSeverity(StringUtils.defaultIfBlank(item.getSeverity(), "warning"));
        item.setSortNum(qualityItemMapper.selectMaxSortNum(item.getFrameworkId()) + 1);
        item.setEnabled(StringUtils.defaultIfBlank(item.getEnabled(), "1"));
        item.setCreateTime(DateUtils.getNowDate());
        item.setUpdateTime(DateUtils.getNowDate());
        item.setDelFlag("0");
        qualityItemMapper.insertQualityItem(item);
        qualityFrameworkMapper.updateItemCount(item.getFrameworkId());
        return item;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateQualityItem(TdwQualityItem item)
    {
        if (item == null || item.getId() == null) {
            throw new IllegalArgumentException("检查项ID不能为空");
        }
        if (StringUtils.isNotBlank(item.getItemType()) || StringUtils.isNotBlank(item.getItemName())) {
            item.setItemType(normalizeItemType(item.getItemType(), item.getItemName()));
        }
        item.setUpdateTime(DateUtils.getNowDate());
        int rows = qualityItemMapper.updateQualityItem(item);
        if (item.getFrameworkId() != null) {
            qualityFrameworkMapper.updateItemCount(item.getFrameworkId());
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteQualityItemByIds(Long[] ids)
    {
        return qualityItemMapper.deleteQualityItemByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createQualityVersion(Long bidId, Long frameworkId, MultipartFile file) throws IOException
    {
        ensureQualityProject(bidId);
        TdwQualityFramework framework = frameworkId == null
                ? qualityFrameworkMapper.selectLatestByBidId(bidId)
                : qualityFrameworkMapper.selectQualityFrameworkById(frameworkId);
        if (framework == null) {
            throw new IllegalArgumentException("请先生成或上传质检框架");
        }
        validateUploadFile(file, new String[] { "doc", "docx" }, 300L * 1024L * 1024L, "投标文件");
        TdwTenderFile bidFile = uploadQualityFile(bidId, file, BID_STAGE);

        TdwQualityTask task = new TdwQualityTask();
        task.setTaskName(FileParser.extractTitleFromFileName(file.getOriginalFilename()) + "质检版本");
        task.setBidId(bidId);
        task.setFrameworkId(framework.getId());
        task.setBidFileId(bidFile.getId());
        task.setSourceModule("quality");
        createQualityTask(task);
        submitQualityTask(task.getId());

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("task", qualityTaskMapper.selectQualityTaskById(task.getId()));
        result.put("bidFile", bidFile);
        result.put("results", new ArrayList<TdwQualityResult>());
        return result;
    }

    private void submitQualityTask(final Long taskId)
    {
        final Runnable submitter = new Runnable()
        {
            @Override
            public void run()
            {
                AsyncManager.me().execute(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        executeQualityTaskSafely(taskId);
                    }
                });
            }
        };
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization()
            {
                @Override
                public void afterCommit()
                {
                    submitter.run();
                }
            });
        } else {
            submitter.run();
        }
    }

    private void executeQualityTaskSafely(Long taskId)
    {
        try {
            runQualityTask(taskId);
        } catch (Exception e) {
            markQualityTaskFailed(taskId, e.getMessage());
        }
    }

    private void markQualityTaskFailed(Long taskId, String reason)
    {
        TdwQualityTask task = qualityTaskMapper.selectQualityTaskById(taskId);
        if (task == null) {
            return;
        }
        Date now = DateUtils.getNowDate();
        task.setStatus("fail");
        task.setFinishTime(now);
        task.setUpdateTime(now);
        task.setRemark(truncate(StringUtils.defaultIfBlank(reason, "大模型质检任务执行失败"), 480));
        qualityTaskMapper.updateQualityTask(task);
    }

    @Override
    public void exportQualityFramework(Long frameworkId, HttpServletResponse response) throws IOException
    {
        TdwQualityFramework framework = qualityFrameworkMapper.selectQualityFrameworkById(frameworkId);
        if (framework == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "质检框架不存在");
            return;
        }
        TdwQualityItem query = new TdwQualityItem();
        query.setFrameworkId(frameworkId);
        List<TdwQualityItem> items = qualityItemMapper.selectQualityItemList(query);
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("质检框架");
        Row header = sheet.createRow(0);
        String[] columns = new String[] { "审查分类", "检查项", "检查规则", "风险等级", "是否启用" };
        for (int i = 0; i < columns.length; i++) {
            header.createCell(i).setCellValue(columns[i]);
            sheet.setColumnWidth(i, i == 1 || i == 2 ? 12000 : 4200);
        }
        int rowIndex = 1;
        for (TdwQualityItem item : items) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(itemTypeText(item.getItemType()));
            row.createCell(1).setCellValue(StringUtils.defaultString(item.getItemName()));
            row.createCell(2).setCellValue(StringUtils.defaultString(item.getCheckRule()));
            row.createCell(3).setCellValue(StringUtils.defaultString(item.getSeverity()));
            row.createCell(4).setCellValue("1".equals(item.getEnabled()) ? "是" : "否");
        }
        String fileName = FilenameUtils.getBaseName(framework.getFrameworkName()) + "_质检框架.xlsx";
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));
        workbook.write(response.getOutputStream());
        workbook.close();
        response.flushBuffer();
    }

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
            task.setTaskName((bid == null ? "投标文件" : bid.getTitle()) + "质检版本");
        }
        if (StringUtils.isBlank(task.getSourceModule())) {
            task.setSourceModule("quality");
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

        TdwTenderFile tenderFile = tenderFileMapper.selectLatestByBidIdAndStage(task.getBidId(), TENDER_STAGE);
        TdwTenderFile bidFile = task.getBidFileId() == null ? null : tenderFileMapper.selectById(task.getBidFileId());
        if (tenderFile == null || bidFile == null) {
            throw new IllegalArgumentException("招标文件或投标文件不存在");
        }
        String tenderText = parseStoredText(tenderFile, MAX_MODEL_DOC_CHARS);
        String bidText = parseStoredText(bidFile, MAX_MODEL_DOC_CHARS);
        if (StringUtils.isBlank(bidText)) {
            throw new IllegalArgumentException("投标文件内容解析为空，无法执行质检");
        }

        List<TdwQualityItem> items = qualityItemMapper.selectEnabledItemsByFrameworkId(task.getFrameworkId());
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("当前质检框架没有启用的检查项");
        }

        List<TdwQualityResult> results = new ArrayList<TdwQualityResult>();
        int issueCount = 0;
        JsonNode config = loadPromptConfig();
        String frameworkKnowledge = reviewFrameworkKnowledge(config);
        for (TdwQualityItem item : items) {
            TdwQualityResult result = inspectByModel(task, item, tenderText, bidText, config, frameworkKnowledge);
            if (!"pass".equals(result.getStatus())) {
                issueCount++;
            }
            results.add(result);
        }

        qualityResultMapper.deleteByTaskId(taskId);
        if (!results.isEmpty()) {
            qualityResultMapper.batchInsertQualityResult(results);
        }
        task.setStatus("success");
        task.setTotalCount(items.size());
        task.setIssueCount(issueCount);
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

    private TdwQualityResult inspectByModel(TdwQualityTask task, TdwQualityItem item, String tenderText, String bidText,
                                            JsonNode config, String frameworkKnowledge)
    {
        Map<String, String> variables = new HashMap<String, String>();
        variables.put("reviewFramework", frameworkKnowledge);
        variables.put("itemType", itemTypeText(item.getItemType()));
        try {
            variables.put("checkItemJson", objectMapper.writeValueAsString(itemToPromptMap(item)));
        } catch (Exception e) {
            throw new IllegalStateException("构造质检检查项提示词失败：" + e.getMessage(), e);
        }
        variables.put("tenderText", truncate(tenderText, MAX_MODEL_DOC_CHARS));
        variables.put("bidText", truncate(bidText, MAX_MODEL_DOC_CHARS));
        String prompt = renderStagePrompt(config, "review_item", variables);
        String answer = aiService.extractText(prompt, "", "quality-review-" + StringUtils.defaultString(item.getItemType()));
        Map<String, String> parsed = parseInspectionAnswer(answer);
        if (parsed == null || parsed.isEmpty()) {
            throw new IllegalStateException("大模型未返回合法的质检 JSON：" + truncate(answer, 300));
        }
        return newResult(task, item, parsed);
    }

    private TdwQualityResult newResult(TdwQualityTask task, TdwQualityItem item, Map<String, String> parsed)
    {
        String status = normalizeResultStatus(parsed.get("resultStatus"));
        String label = StringUtils.defaultIfBlank(parsed.get("resultLabel"), resultLabel(status));
        String analysis = StringUtils.defaultIfBlank(parsed.get("analysis"), "未返回详细分析。");
        String evidence = StringUtils.defaultIfBlank(parsed.get("evidence"), "未定位到明确依据。");
        String suggestion = StringUtils.defaultIfBlank(parsed.get("suggestion"), "请人工复核该检查项。");

        TdwQualityResult result = new TdwQualityResult();
        result.setTaskId(task.getId());
        result.setBidId(task.getBidId());
        result.setItemId(item.getId());
        result.setIssueType(item.getItemType());
        result.setSeverity("pass".equals(status) ? "success" : "fail".equals(status) ? "error" : "warning");
        result.setIssueTitle(item.getItemName());
        result.setIssueDesc("检查结果：" + label);
        result.setSuggestion("结果分析：" + analysis + "\n整改建议：" + suggestion + "\n依据内容：" + evidence);
        result.setStatus(status);
        result.setCreateTime(DateUtils.getNowDate());
        result.setUpdateTime(DateUtils.getNowDate());
        result.setDelFlag("0");
        return result;
    }

    private TdwTenderFile uploadQualityFile(Long bidId, MultipartFile file, String stage) throws IOException
    {
        String fileUrl = FileUploadUtils.upload(RuoYiConfig.getUploadPath(), file);
        TdwTenderFile tenderFile = new TdwTenderFile();
        tenderFile.setBidId(bidId);
        tenderFile.setOriginalName(file.getOriginalFilename());
        tenderFile.setFileName(FilenameUtils.getName(fileUrl));
        tenderFile.setFileUrl(fileUrl);
        tenderFile.setFileSize(file.getSize());
        tenderFile.setFileType(FilenameUtils.getExtension(file.getOriginalFilename()));
        tenderFile.setFileStage(stage);
        tenderFile.setParseStatus("uploaded");
        tenderFile.setCreateTime(DateUtils.getNowDate());
        tenderFile.setUpdateTime(DateUtils.getNowDate());
        tenderFileMapper.insertTenderFile(tenderFile);
        return tenderFile;
    }

    private TdwQualityFramework newFramework(Long bidId, String frameworkName, String description)
    {
        TdwQualityFramework framework = new TdwQualityFramework();
        framework.setBidId(bidId);
        framework.setFrameworkName(StringUtils.defaultIfBlank(frameworkName, "质检框架"));
        framework.setDescription(description);
        framework.setStatus("success");
        framework.setItemCount(0);
        framework.setCreateTime(DateUtils.getNowDate());
        framework.setUpdateTime(DateUtils.getNowDate());
        framework.setDelFlag("0");
        return framework;
    }

    private void saveFrameworkItems(TdwQualityFramework framework, List<TdwQualityItem> items)
    {
        int sortNum = 1;
        for (TdwQualityItem item : items) {
            item.setFrameworkId(framework.getId());
            item.setItemType(normalizeItemType(item.getItemType(), item.getItemName()));
            item.setSeverity(StringUtils.defaultIfBlank(item.getSeverity(), "warning"));
            item.setCheckRule(StringUtils.defaultIfBlank(item.getCheckRule(), item.getItemName()));
            item.setSortNum(sortNum++);
            item.setEnabled(StringUtils.defaultIfBlank(item.getEnabled(), "1"));
            item.setCreateTime(DateUtils.getNowDate());
            item.setUpdateTime(DateUtils.getNowDate());
            item.setDelFlag("0");
            qualityItemMapper.insertQualityItem(item);
        }
        framework.setStatus("success");
        framework.setItemCount(items.size());
        framework.setUpdateTime(DateUtils.getNowDate());
        qualityFrameworkMapper.updateQualityFramework(framework);
        qualityFrameworkMapper.updateItemCount(framework.getId());
    }

    private List<TdwQualityItem> parseWorkbookItems(Long frameworkId, MultipartFile file) throws IOException
    {
        List<TdwQualityItem> items = new ArrayList<TdwQualityItem>();
        DataFormatter formatter = new DataFormatter();
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getNumberOfSheets() == 0 ? null : workbook.getSheetAt(0);
            if (sheet == null) {
                return items;
            }
            Map<String, Integer> header = new HashMap<String, Integer>();
            for (Row row : sheet) {
                List<String> values = rowValues(row, formatter);
                if (values.isEmpty()) {
                    continue;
                }
                if (header.isEmpty() && looksLikeHeader(values)) {
                    header = headerIndex(values);
                    continue;
                }
                TdwQualityItem item = workbookRowToItem(frameworkId, row, formatter, header, values);
                if (item != null) {
                    items.add(item);
                }
            }
        }
        return items;
    }

    private TdwQualityItem workbookRowToItem(Long frameworkId, Row row, DataFormatter formatter,
                                             Map<String, Integer> header, List<String> values)
    {
        String category = valueByHeader(row, formatter, header, "category");
        String itemName = valueByHeader(row, formatter, header, "item");
        String rule = valueByHeader(row, formatter, header, "rule");
        String severity = valueByHeader(row, formatter, header, "severity");
        String enabled = valueByHeader(row, formatter, header, "enabled");
        if (StringUtils.isBlank(itemName)) {
            if (values.size() == 1) {
                itemName = values.get(0);
            } else if (values.size() >= 2) {
                category = StringUtils.defaultIfBlank(category, values.get(0));
                itemName = values.get(1);
                if (values.size() >= 3) {
                    rule = values.get(2);
                }
            }
        }
        if (StringUtils.isBlank(itemName) || looksLikeHeaderValue(itemName)) {
            return null;
        }
        TdwQualityItem item = new TdwQualityItem();
        item.setFrameworkId(frameworkId);
        item.setItemName(toQuestion(itemName));
        item.setItemType(normalizeItemType(category, itemName));
        item.setCheckRule(StringUtils.defaultIfBlank(rule, itemName));
        item.setSeverity(normalizeSeverity(severity));
        item.setEnabled("否".equals(enabled) || "0".equals(enabled) ? "0" : "1");
        return item;
    }

    private List<String> rowValues(Row row, DataFormatter formatter)
    {
        List<String> values = new ArrayList<String>();
        int last = row == null ? 0 : row.getLastCellNum();
        for (int i = 0; i < last; i++) {
            String value = cellText(row.getCell(i), formatter);
            if (StringUtils.isNotBlank(value)) {
                values.add(value);
            }
        }
        return values;
    }

    private boolean looksLikeHeader(List<String> values)
    {
        String text = StringUtils.join(values, "|");
        return text.contains("检查项") || text.contains("审查项") || text.contains("检查规则") || text.contains("审查分类");
    }

    private Map<String, Integer> headerIndex(List<String> values)
    {
        Map<String, Integer> header = new HashMap<String, Integer>();
        for (int i = 0; i < values.size(); i++) {
            String value = values.get(i);
            if (value.contains("分类") || value.contains("类别") || value.contains("类型")) header.put("category", i);
            if (value.contains("检查项") || value.contains("审查项") || value.contains("条目")) header.put("item", i);
            if (value.contains("规则") || value.contains("要求") || value.contains("依据")) header.put("rule", i);
            if (value.contains("等级") || value.contains("风险")) header.put("severity", i);
            if (value.contains("启用")) header.put("enabled", i);
        }
        return header;
    }

    private String valueByHeader(Row row, DataFormatter formatter, Map<String, Integer> header, String key)
    {
        Integer index = header.get(key);
        return index == null ? "" : cellText(row.getCell(index), formatter);
    }

    private String cellText(Cell cell, DataFormatter formatter)
    {
        return cell == null ? "" : StringUtils.defaultString(formatter.formatCellValue(cell)).trim();
    }

    private boolean looksLikeHeaderValue(String value)
    {
        return value != null && (value.contains("检查项") || value.contains("审查项"));
    }

    private List<TdwQualityItem> parseExtractedItems(Long frameworkId, String answer)
    {
        List<TdwQualityItem> items = new ArrayList<TdwQualityItem>();
        String json = extractJson(answer);
        if (StringUtils.isBlank(json)) {
            throw new IllegalStateException("大模型质检框架响应为空或不包含 JSON");
        }
        try {
            JsonNode root = objectMapper.readTree(json);
            collectExtractedItems(root, "", items);
        } catch (Exception e) {
            throw new IllegalStateException("大模型质检框架响应 JSON 解析失败：" + e.getMessage(), e);
        }
        for (TdwQualityItem item : items) {
            item.setFrameworkId(frameworkId);
        }
        return items;
    }

    private void collectExtractedItems(JsonNode node, String inheritedType, List<TdwQualityItem> items)
    {
        if (node == null || node.isNull()) {
            return;
        }
        if (node.isArray()) {
            for (JsonNode child : node) {
                collectExtractedItems(child, inheritedType, items);
            }
            return;
        }
        if (node.isTextual()) {
            addExtractedItem(items, inheritedType, node.asText(), node.asText(), "warning");
            return;
        }
        String currentType = normalizeItemType(firstText(node, "groupCode", "categoryCode", "code", "groupName", "categoryName", "name"), inheritedType);
        if (StringUtils.isBlank(currentType)) {
            currentType = inheritedType;
        }
        JsonNode itemArray = firstArray(node, "items", "checkItems", "children", "v");
        if (itemArray != null && itemArray.isArray()) {
            for (JsonNode child : itemArray) {
                if (child.isTextual()) {
                    addExtractedItem(items, currentType, child.asText(), child.asText(), "warning");
                } else if (looksLikeItemNode(child)) {
                    String itemName = firstText(child, "itemName", "question", "name", "n", "title", "content");
                    String rule = firstText(child, "checkRule", "rule", "requirement", "sourceQuote", "text");
                    String severity = normalizeSeverity(firstText(child, "severity", "riskLevel", "risk"));
                    addExtractedItem(items, currentType, itemName, StringUtils.defaultIfBlank(rule, itemName), severity);
                } else {
                    collectExtractedItems(child, currentType, items);
                }
            }
        }
        JsonNode groups = firstArray(node, "groups", "categories", "subTypes");
        if (groups != null) {
            collectExtractedItems(groups, currentType, items);
        }
    }

    private JsonNode firstArray(JsonNode node, String... fields)
    {
        for (String field : fields) {
            JsonNode value = node.get(field);
            if (value != null && value.isArray()) {
                return value;
            }
        }
        return null;
    }

    private boolean looksLikeItemNode(JsonNode node)
    {
        return node.has("itemName") || node.has("question") || node.has("checkRule")
                || node.has("requirement") || node.has("sourceQuote");
    }

    private void addExtractedItem(List<TdwQualityItem> items, String itemType, String itemName, String checkRule, String severity)
    {
        if (StringUtils.isBlank(itemName)) {
            return;
        }
        TdwQualityItem item = new TdwQualityItem();
        item.setItemName(toQuestion(itemName));
        item.setCheckRule(StringUtils.defaultIfBlank(checkRule, itemName));
        item.setItemType(normalizeItemType(itemType, itemName));
        item.setSeverity(normalizeSeverity(severity));
        item.setEnabled("1");
        items.add(item);
    }

    private List<TdwQualityItem> fallbackExtractItems(Long frameworkId, String tenderText)
    {
        List<TdwQualityItem> items = new ArrayList<TdwQualityItem>();
        addExtractedItem(items, "form_review", "投标文件中是否包含目录？", "检查投标文件目录及页码是否完整。", "warning");
        addExtractedItem(items, "form_review", "投标文件中是否包含开标一览表？", "检查投标文件组成是否包含开标一览表或报价总表。", "warning");
        addExtractedItem(items, "form_review", "投标文件签字盖章是否符合招标文件格式要求？", "检查签署、盖章、日期、授权等形式要求。", "error");
        addExtractedItem(items, "qualification_review", "投标人是否提供有效营业执照或主体资格证明？", "检查营业执照、法人资格或其他组织证明。", "error");
        addExtractedItem(items, "qualification_review", "投标人是否提供招标文件要求的资质、认证或人员证明材料？", "检查资质证书、体系认证、人员证书及有效期。", "error");
        addExtractedItem(items, "qualification_review", "投标人是否提供招标文件要求的业绩证明材料？", "检查近年类似项目合同、验收或用户证明。", "warning");
        addExtractedItem(items, "reject_review", "投标文件是否不存在招标文件列明的无效投标或废标情形？", "检查保证金、报价上限、重大偏离、签章缺失等否决条件。", "error");
        for (String sentence : candidateSentences(tenderText)) {
            String type = sentence.matches(".*(不得|禁止|无效|废标|否决|不予受理).*") ? "reject_review"
                    : sentence.matches(".*(资质|资格|业绩|财务|营业执照|证书|证明).*") ? "qualification_review"
                    : "response_review";
            addExtractedItem(items, type, sentence, sentence, "warning");
            if (items.size() >= 18) {
                break;
            }
        }
        for (TdwQualityItem item : items) {
            item.setFrameworkId(frameworkId);
        }
        return items;
    }

    private List<String> candidateSentences(String text)
    {
        List<String> result = new ArrayList<String>();
        String compact = StringUtils.defaultString(text).replace("\r", "\n");
        String[] lines = compact.split("[\\n。；;]");
        for (String line : lines) {
            String value = line.replaceAll("\\s+", " ").trim();
            if (value.length() < 12 || value.length() > 160) {
                continue;
            }
            if (value.matches(".*(须|需|应|必须|不得|提供|满足|包含|不低于|不少于|有效期|保证金|投标文件).*")) {
                result.add(toQuestion(value));
            }
        }
        return result;
    }

    private Map<String, String> parseInspectionAnswer(String answer)
    {
        String json = extractJson(answer);
        if (StringUtils.isBlank(json)) {
            throw new IllegalStateException("大模型质检结果为空或不包含 JSON");
        }
        try {
            JsonNode root = objectMapper.readTree(json);
            if (root.has("results") && root.get("results").isArray() && root.get("results").size() > 0) {
                root = root.get("results").get(0);
            }
            Map<String, String> result = new HashMap<String, String>();
            result.put("resultStatus", firstText(root, "resultStatus", "status", "result"));
            result.put("resultLabel", firstText(root, "resultLabel", "label", "检查结果"));
            result.put("analysis", firstText(root, "analysis", "resultAnalysis", "结果分析", "reason"));
            result.put("evidence", firstText(root, "evidence", "sourceEvidence", "依据内容", "quote"));
            result.put("suggestion", firstText(root, "suggestion", "fixSuggestion", "整改建议", "建议"));
            return result;
        } catch (Exception e) {
            throw new IllegalStateException("大模型质检结果 JSON 解析失败：" + e.getMessage(), e);
        }
    }

    private Map<String, String> fallbackInspect(TdwQualityItem item, String bidText)
    {
        String keyword = significantKeyword(item.getItemName());
        boolean hit = StringUtils.isNotBlank(keyword) && StringUtils.defaultString(bidText).contains(keyword);
        Map<String, String> result = new HashMap<String, String>();
        result.put("resultStatus", hit ? "pass" : "fail");
        result.put("resultLabel", hit ? "包含" : "不包含");
        result.put("analysis", hit
                ? "投标文件中检索到与检查项相关的关键词“" + keyword + "”，系统初步判断已响应。"
                : "投标文件中未检索到与检查项直接相关的关键词“" + keyword + "”，无法确认已满足该要求。");
        result.put("evidence", hit ? "关键词命中：" + keyword : "未检索到明确依据");
        result.put("suggestion", hit ? "建议人工复核相关章节的完整性和证明材料。" : "建议补充对应章节、承诺或证明材料。");
        return result;
    }

    private String significantKeyword(String value)
    {
        String text = StringUtils.defaultString(value);
        Matcher matcher = QUOTED_TEXT_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        text = text.replaceAll("投标文件中是否|是否|包含|提供|满足|符合|招标文件要求|[？?。；;，,、\\s]", "");
        return text.length() > 18 ? text.substring(0, 18) : text;
    }

    private String firstText(JsonNode node, String... fields)
    {
        if (node == null) {
            return "";
        }
        for (String field : fields) {
            JsonNode value = node.get(field);
            if (value != null && !value.isNull()) {
                if (value.isTextual() || value.isNumber() || value.isBoolean()) {
                    return value.asText();
                }
                return value.toString();
            }
        }
        return "";
    }

    private Map<String, Object> itemToPromptMap(TdwQualityItem item)
    {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("id", item.getId());
        map.put("category", itemTypeText(item.getItemType()));
        map.put("itemName", item.getItemName());
        map.put("checkRule", item.getCheckRule());
        map.put("severity", item.getSeverity());
        return map;
    }

    private JsonNode loadPromptConfig()
    {
        try {
            ClassPathResource resource = new ClassPathResource(PROMPT_RESOURCE);
            String text = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            return objectMapper.readTree(text);
        } catch (IOException e) {
            throw new IllegalStateException("AI质检提示词配置不存在：" + PROMPT_RESOURCE, e);
        }
    }

    private String renderStagePrompt(JsonNode config, String stageId, Map<String, String> variables)
    {
        JsonNode stage = config.path("stages").path(stageId);
        String prompt = stage.path("prompt").asText("");
        if (StringUtils.isBlank(prompt)) {
            throw new IllegalStateException("AI质检提示词阶段不存在：" + stageId);
        }
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            prompt = prompt.replace("{{" + entry.getKey() + "}}", StringUtils.defaultString(entry.getValue()));
        }
        return prompt;
    }

    private String reviewFrameworkKnowledge(JsonNode config)
    {
        StringBuilder builder = new StringBuilder();
        builder.append(config.path("globalSystemPrompt").asText(""));
        builder.append("\n\n【审查框架知识库】\n").append(config.path("reviewFramework").toString());
        builder.append("\n\n【分类与提取规则】\n").append(config.path("categories").toString());
        return builder.toString();
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
        int arrayStart = value.indexOf('[');
        int arrayEnd = value.lastIndexOf(']');
        if (arrayStart >= 0 && arrayEnd > arrayStart) {
            return value.substring(arrayStart, arrayEnd + 1);
        }
        return value;
    }

    private String parseStoredText(TdwTenderFile file, int maxChars)
    {
        try {
            File local = resolveUploadedFile(file.getFileUrl());
            return FileParser.parseFileText(local, file.getOriginalName(), maxChars);
        } catch (Exception e) {
            return "";
        }
    }

    private File resolveUploadedFile(String fileUrl)
    {
        if (StringUtils.isBlank(fileUrl)) {
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

    private TdwBids ensureQualityProject(Long bidId)
    {
        if (bidId == null) {
            throw new IllegalArgumentException("项目ID不能为空");
        }
        TdwBids bid = tdwBidsMapper.selectTdwBidsById(bidId);
        if (bid == null) {
            throw new IllegalArgumentException("质检项目不存在");
        }
        return bid;
    }

    private void validateUploadFile(MultipartFile file, String[] allowedExtensions, long maxBytes, String label)
    {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(label + "不能为空");
        }
        if (file.getSize() > maxBytes) {
            throw new IllegalArgumentException(label + "大小超出限制");
        }
        String ext = FilenameUtils.getExtension(file.getOriginalFilename());
        for (String allowed : allowedExtensions) {
            if (allowed.equalsIgnoreCase(ext)) {
                return;
            }
        }
        throw new IllegalArgumentException(label + "格式不支持");
    }

    private String normalizeItemType(String type, String fallbackText)
    {
        String text = StringUtils.defaultString(type) + " " + StringUtils.defaultString(fallbackText);
        if (text.contains("form_review") || text.contains("composition") || text.contains("format")
                || text.contains("形式") || text.contains("组成") || text.contains("格式")) {
            return "form_review";
        }
        if (text.contains("qualification_review") || text.contains("financial") || text.contains("performance")
                || text.contains("qualification") || text.contains("certificate") || text.contains("资格")
                || text.contains("资质") || text.contains("财务") || text.contains("业绩")) {
            return "qualification_review";
        }
        if (text.contains("reject_review") || text.contains("reject") || text.contains("invalid")
                || text.contains("废标") || text.contains("否决") || text.contains("无效") || text.contains("不予受理")) {
            return "reject_review";
        }
        return "response_review";
    }

    private String itemTypeText(String itemType)
    {
        if ("form_review".equals(itemType)) return "形式审查";
        if ("qualification_review".equals(itemType)) return "资格审查";
        if ("reject_review".equals(itemType)) return "废标项审查";
        return "响应审查";
    }

    private String normalizeSeverity(String severity)
    {
        String value = StringUtils.defaultString(severity);
        if (value.contains("error") || value.contains("high") || value.contains("严重") || value.contains("高") || value.contains("必须")) {
            return "error";
        }
        if (value.contains("info") || value.contains("低")) {
            return "info";
        }
        return "warning";
    }

    private String normalizeResultStatus(String status)
    {
        String value = StringUtils.defaultString(status).toLowerCase();
        if (value.contains("pass") || value.contains("符合") || value.contains("包含") || value.contains("通过")) {
            if (!value.contains("不符合") && !value.contains("不包含")) {
                return "pass";
            }
        }
        if (value.contains("fail") || value.contains("不符合") || value.contains("不包含") || value.contains("缺失") || value.contains("未")) {
            return "fail";
        }
        return "uncertain";
    }

    private String resultLabel(String status)
    {
        if ("pass".equals(status)) return "包含";
        if ("fail".equals(status)) return "不包含";
        return "无法判断";
    }

    private String toQuestion(String text)
    {
        String value = StringUtils.defaultString(text).trim();
        if (value.endsWith("?") || value.endsWith("？")) {
            return value;
        }
        if (value.startsWith("投标文件") || value.startsWith("投标人")) {
            return value + "？";
        }
        return "投标文件中是否包含“" + value.replaceAll("[。；;]$", "") + "”？";
    }

    private String truncate(String text, int max)
    {
        String value = StringUtils.defaultString(text);
        return value.length() > max ? value.substring(0, max) + "\n\n【内容过长已截断】" : value;
    }
}
