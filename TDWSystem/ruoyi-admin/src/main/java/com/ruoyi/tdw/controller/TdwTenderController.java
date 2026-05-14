package com.ruoyi.tdw.controller;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.tdw.domain.TdwBids;
import com.ruoyi.tdw.domain.dto.TdwServiceFieldParseRequest;
import com.ruoyi.tdw.domain.dto.TdwServicePlanParseRequest;
import com.ruoyi.tdw.domain.dto.TdwServicePlanParseResult;
import com.ruoyi.tdw.domain.dto.TdwTechnicalScoreExtractRequest;
import com.ruoyi.tdw.service.ITdwDownloadService;
import com.ruoyi.tdw.service.ITdwTenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Anonymous
@RestController
@RequestMapping("/tdw/tender")
public class TdwTenderController extends BaseController
{
    private final ExecutorService streamExecutor = Executors.newCachedThreadPool();

    @Autowired
    private ITdwTenderService tdwTenderService;

    @Autowired
    private ITdwDownloadService downloadService;

    @GetMapping("/list")
    public TableDataInfo list(TdwBids query)
    {
        startPage();
        List<TdwBids> list = tdwTenderService.selectTenderBidList(query);
        return getDataTable(list);
    }

    @PostMapping("/create")
    public AjaxResult create(@RequestParam("title") String title,
                             @RequestParam(value = "category", required = false) String category,
                             @RequestParam(value = "note", required = false) String note,
                             @RequestParam(value = "file", required = false) MultipartFile file) throws IOException
    {
        return success(tdwTenderService.createTenderProject(title, category, note, file));
    }

    @PostMapping("/upload")
    public AjaxResult upload(@RequestParam("bidId") Long bidId,
                             @RequestParam("file") MultipartFile file,
                             @RequestParam(value = "stage", required = false) String stage) throws IOException
    {
        return success(tdwTenderService.uploadTenderFile(bidId, file, stage));
    }

    @PostMapping("/parse/{tenderFileId}")
    public AjaxResult parse(@PathVariable Long tenderFileId)
    {
        return success(tdwTenderService.mockParse(tenderFileId));
    }

    @PostMapping(value = "/parse/service/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter parseServicePlanStream(@RequestBody TdwServicePlanParseRequest request)
    {
        SseEmitter emitter = new SseEmitter(0L);
        streamExecutor.execute(() -> {
            try {
                TdwServicePlanParseResult result = tdwTenderService.parseServicePlan(request);
                sendChunks(emitter, "projectName", result.getProjectName());
                sendFieldChunks(emitter, result.getExtractedFields());
                sendChunks(emitter, "scoreItems", result.getFullScoreItems());
                sendChunks(emitter, "technicalScoreItems", result.getTechnicalScoreItems());
                sendEvent(emitter, "done", result);
                emitter.complete();
            } catch (Exception e) {
                completeWithError(emitter, e);
            }
        });
        return emitter;
    }

    @PostMapping(value = "/parse/service/field/tender/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter extractPlanFieldTenderStream(@RequestBody TdwServiceFieldParseRequest request)
    {
        SseEmitter emitter = new SseEmitter(0L);
        streamExecutor.execute(() -> {
            try {
                String fieldKey = request == null ? null : request.getFieldKey();
                String content = tdwTenderService.extractPlanFieldFromTenderFile(request == null ? null : request.getFileId(),
                        request == null ? null : request.getCategory(), fieldKey);
                sendChunks(emitter, normalizeFieldEventName(fieldKey), content);
                sendEvent(emitter, "done", content);
                emitter.complete();
            } catch (Exception e) {
                completeWithError(emitter, e);
            }
        });
        return emitter;
    }

    @PostMapping(value = "/parse/service/field/upload/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter extractPlanFieldUploadStream(@RequestBody TdwServiceFieldParseRequest request)
    {
        SseEmitter emitter = new SseEmitter(0L);
        streamExecutor.execute(() -> {
            try {
                String fieldKey = request == null ? null : request.getFieldKey();
                String content = tdwTenderService.extractPlanFieldFromFile(request == null ? null : request.getFileId(),
                        request == null ? null : request.getCategory(), fieldKey);
                sendChunks(emitter, normalizeFieldEventName(fieldKey), content);
                sendEvent(emitter, "done", content);
                emitter.complete();
            } catch (Exception e) {
                completeWithError(emitter, e);
            }
        });
        return emitter;
    }

    @PostMapping(value = "/parse/service/purchase-requirement/tender/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter extractPurchaseRequirementTenderStream(@RequestBody TdwServiceFieldParseRequest request)
    {
        SseEmitter emitter = new SseEmitter(0L);
        streamExecutor.execute(() -> {
            try {
                String content = tdwTenderService.extractServicePurchaseRequirementFromTenderFile(request == null ? null : request.getFileId(),
                        request == null ? null : request.getCategory());
                sendChunks(emitter, "purchaseRequirement", content);
                sendEvent(emitter, "done", content);
                emitter.complete();
            } catch (Exception e) {
                completeWithError(emitter, e);
            }
        });
        return emitter;
    }

    @PostMapping(value = "/parse/service/purchase-requirement/upload/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter extractPurchaseRequirementUploadStream(@RequestBody TdwServiceFieldParseRequest request)
    {
        SseEmitter emitter = new SseEmitter(0L);
        streamExecutor.execute(() -> {
            try {
                String content = tdwTenderService.extractServicePurchaseRequirementFromFile(request == null ? null : request.getFileId(),
                        request == null ? null : request.getCategory());
                sendChunks(emitter, "purchaseRequirement", content);
                sendEvent(emitter, "done", content);
                emitter.complete();
            } catch (Exception e) {
                completeWithError(emitter, e);
            }
        });
        return emitter;
    }

    @PostMapping(value = "/parse/service/other-attachment/upload/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter extractOtherAttachmentUploadStream(@RequestBody TdwServiceFieldParseRequest request)
    {
        SseEmitter emitter = new SseEmitter(0L);
        streamExecutor.execute(() -> {
            try {
                String content = tdwTenderService.extractServiceOtherAttachmentFromFile(request == null ? null : request.getFileId(),
                        request == null ? null : request.getCategory());
                sendChunks(emitter, "otherAttachment", content);
                sendEvent(emitter, "done", content);
                emitter.complete();
            } catch (Exception e) {
                completeWithError(emitter, e);
            }
        });
        return emitter;
    }

    @PostMapping(value = "/parse/service/score-items/tender/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter extractScoreItemsFromTenderStream(@RequestBody TdwServiceFieldParseRequest request)
    {
        SseEmitter emitter = new SseEmitter(0L);
        streamExecutor.execute(() -> {
            try {
                String content = tdwTenderService.extractServiceScoreItemsFromTenderFile(request == null ? null : request.getFileId(),
                        request == null ? null : request.getCategory());
                sendChunks(emitter, "scoreItems", content);
                sendEvent(emitter, "done", content);
                emitter.complete();
            } catch (Exception e) {
                completeWithError(emitter, e);
            }
        });
        return emitter;
    }

    @PostMapping(value = "/parse/service/score-items/requirement/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter extractScoreItemsFromRequirementStream(@RequestBody TdwServiceFieldParseRequest request)
    {
        SseEmitter emitter = new SseEmitter(0L);
        streamExecutor.execute(() -> {
            try {
                String content = tdwTenderService.extractServiceScoreItemsFromPurchaseRequirement(request == null ? null : request.getContent(),
                        request == null ? null : request.getCategory());
                sendChunks(emitter, "scoreItems", content);
                sendEvent(emitter, "done", content);
                emitter.complete();
            } catch (Exception e) {
                completeWithError(emitter, e);
            }
        });
        return emitter;
    }

    @PostMapping(value = "/parse/service/technical-score/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter extractTechnicalScoreStream(@RequestBody TdwTechnicalScoreExtractRequest request)
    {
        SseEmitter emitter = new SseEmitter(0L);
        streamExecutor.execute(() -> {
            try {
                String technicalScoreItems = tdwTenderService.extractTechnicalScoreItems(request == null ? "" : request.getFullScoreItems(),
                        request == null ? null : request.getCategory());
                sendChunks(emitter, "technicalScoreItems", technicalScoreItems);
                sendEvent(emitter, "done", technicalScoreItems);
                emitter.complete();
            } catch (Exception e) {
                completeWithError(emitter, e);
            }
        });
        return emitter;
    }

    @GetMapping("/files/{bidId}")
    public AjaxResult files(@PathVariable Long bidId)
    {
        return success(tdwTenderService.selectFilesByBidId(bidId));
    }

    @GetMapping("/report/{id}")
    public AjaxResult report(@PathVariable Long id)
    {
        return success(tdwTenderService.selectReportById(id));
    }

    @GetMapping("/report/byFile/{tenderFileId}")
    public AjaxResult reportByFile(@PathVariable Long tenderFileId)
    {
        return success(tdwTenderService.selectReportByFileId(tenderFileId));
    }

    @GetMapping("/report/latest/{bidId}")
    public AjaxResult latestReport(@PathVariable Long bidId)
    {
        return success(tdwTenderService.selectLatestReportByBidId(bidId));
    }

    @PostMapping("/report/{id}/export")
    public AjaxResult exportReport(@PathVariable Long id) throws IOException
    {
        return success(downloadService.exportReport("tender", id));
    }

    private void sendChunks(SseEmitter emitter, String eventName, String text) throws IOException, InterruptedException
    {
        for (String chunk : splitForStream(text == null ? "" : text)) {
            sendEvent(emitter, eventName, chunk);
            Thread.sleep(25L);
        }
    }

    private void sendFieldChunks(SseEmitter emitter, java.util.Map<String, String> fields) throws IOException, InterruptedException
    {
        if (fields == null) {
            return;
        }
        for (java.util.Map.Entry<String, String> entry : fields.entrySet()) {
            sendChunks(emitter, normalizeFieldEventName(entry.getKey()), entry.getValue());
        }
    }

    private String normalizeFieldEventName(String fieldKey)
    {
        return fieldKey == null || fieldKey.trim().length() == 0 ? "procurementNeed" : fieldKey.trim();
    }

    private void sendEvent(SseEmitter emitter, String name, Object data) throws IOException
    {
        emitter.send(SseEmitter.event().name(name).data(data));
    }

    private String[] splitForStream(String text)
    {
        if (text.length() <= 36) {
            return new String[] { text };
        }
        int size = 36;
        int count = (text.length() + size - 1) / size;
        String[] chunks = new String[count];
        for (int i = 0; i < count; i++) {
            int start = i * size;
            chunks[i] = text.substring(start, Math.min(text.length(), start + size));
        }
        return chunks;
    }

    private void completeWithError(SseEmitter emitter, Exception e)
    {
        try {
            sendEvent(emitter, "error", e.getMessage());
        } catch (IOException ignored) {
        }
        emitter.completeWithError(e);
    }
}
