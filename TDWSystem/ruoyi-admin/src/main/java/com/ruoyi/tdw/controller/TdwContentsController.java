package com.ruoyi.tdw.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.common.annotation.Anonymous;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.tdw.domain.TdwContents;
import com.ruoyi.tdw.domain.dto.TdwContentGenerateRequest;
import com.ruoyi.tdw.domain.dto.TdwContentSelectionAiRequest;
import com.ruoyi.tdw.domain.dto.TdwContentSortRequest;
import com.ruoyi.tdw.domain.dto.TdwRichContentSaveRequest;
import com.ruoyi.tdw.service.ITdwContentsService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 内容块，支持文本/格/图片混排，随意增删改Controller
 * 
 * @author ruoyi
 * @date 2026-03-28
 */
@Anonymous
@RestController
@RequestMapping("/tdw/contents")
public class TdwContentsController extends BaseController
{
    private final ExecutorService contentGenerateExecutor = Executors.newCachedThreadPool();

    private final ScheduledExecutorService contentHeartbeatExecutor = Executors.newScheduledThreadPool(1);

    @Value("${llm.prompt.content-template}")
    private String contentPromptTemplate;

    @Autowired
    private ITdwContentsService tdwContentsService;

    @ApiOperation("生成内容块")
    @PostMapping(value ="/generate")
    public AjaxResult generate(@RequestBody TdwContentGenerateRequest request) throws IOException {
        return success(tdwContentsService.generateContentBlocks(request));
    }

    @ApiOperation("流式生成内容块")
    @PostMapping(value ="/generate/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generateStream(@RequestBody TdwContentGenerateRequest request) {
        SseEmitter emitter = new SseEmitter(0L);
        java.util.concurrent.atomic.AtomicLong lastEventAt = new java.util.concurrent.atomic.AtomicLong(System.currentTimeMillis());
        contentGenerateExecutor.execute(() -> {
            ScheduledFuture<?> heartbeat = contentHeartbeatExecutor.scheduleAtFixedRate(() -> {
                try {
                    long now = System.currentTimeMillis();
                    if (now - lastEventAt.get() >= 30000L) {
                        sendEvent(emitter, "heartbeat", "generating");
                        lastEventAt.set(now);
                    }
                } catch (IOException ignored) {
                }
            }, 30L, 30L, TimeUnit.SECONDS);
            try {
                sendEvent(emitter, "started", "正文生成中");
                lastEventAt.set(System.currentTimeMillis());
                List<TdwContents> contents = tdwContentsService.generateContentBlocks(request, generated -> {
                    try {
                        sendEvent(emitter, "generated", generated);
                        lastEventAt.set(System.currentTimeMillis());
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                }, (outlineId, content) -> {
                    try {
                        java.util.Map<String, Object> payload = new java.util.LinkedHashMap<>();
                        payload.put("outlineId", outlineId);
                        payload.put("content", content);
                        sendEvent(emitter, "content", payload);
                        lastEventAt.set(System.currentTimeMillis());
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                }, status -> {
                    try {
                        sendEvent(emitter, "ai", status);
                        lastEventAt.set(System.currentTimeMillis());
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                });
                sendEvent(emitter, "done", contents);
                lastEventAt.set(System.currentTimeMillis());
                emitter.complete();
            } catch (Exception e) {
                completeWithError(emitter, e);
            } finally {
                heartbeat.cancel(true);
            }
        });
        return emitter;
    }

    @ApiOperation("保存富文本内容")
    @PostMapping(value ="/rich")
    public AjaxResult saveRich(@RequestBody TdwRichContentSaveRequest request) throws IOException {
        return success(tdwContentsService.saveRichContent(request));
    }

    @ApiOperation("选中文本AI处理")
    @PostMapping(value ="/selection/ai")
    public AjaxResult selectionAi(@RequestBody TdwContentSelectionAiRequest request) throws IOException {
        return success(tdwContentsService.handleSelectionAi(request));
    }

    @ApiOperation("根据大纲ID查询内容块列表")
    @GetMapping(value ="/byOutline/{outlineId}")
    public AjaxResult byOutline(@PathVariable Long outlineId) {
        return success(tdwContentsService.selectTdwContentsByOutlineId(outlineId));
    }

    @ApiOperation("根据多个大纲ID批量查询内容块列表")
    @PostMapping(value ="/byOutlines")
    public AjaxResult byOutlines(@RequestBody List<Long> outlineIds) {
        return success(tdwContentsService.selectTdwContentsByOutlineIds(outlineIds));
    }

    @ApiOperation("调整内容块排序")
    @PutMapping(value ="/sort")
    public AjaxResult sort(@RequestBody TdwContentSortRequest request) {
        return toAjax(tdwContentsService.sortContents(request));
    }


    @ApiOperation("生成标书内容")
    @PostMapping(value ="/generateContent")
    public AjaxResult generateContent(Long id, Long[] knowledgeFileIds, Long[] knowledgeChunkIds) throws IOException {
        System.out.println(contentPromptTemplate);
        String bidId = tdwContentsService.generateContent(id,
                knowledgeFileIds == null ? null : Arrays.asList(knowledgeFileIds),
                knowledgeChunkIds == null ? null : Arrays.asList(knowledgeChunkIds));
        System.out.println("内容生成完成");
        return success(contentPromptTemplate);
    }

    @ApiOperation("根据tdw_contents表的id查询对应内容")
    @GetMapping(value ="/getContent")
    public AjaxResult getContent(Long id) throws IOException {
        List<TdwContents> content = tdwContentsService.getContent(id);
        System.out.println(content);
        System.out.println("内容生成完成");
        return success(content);
    }

    /**
     * 查询内容块，支持文本/格/图片混排，随意增删改列表
     */
    @PreAuthorize("@ss.hasPermi('bid:content:list')")
    @GetMapping("/list")
    public TableDataInfo list(TdwContents tdwContents)
    {
        startPage();
        List<TdwContents> list = tdwContentsService.selectTdwContentsList(tdwContents);
        return getDataTable(list);
    }

    /**
     * 导出内容块，支持文本/格/图片混排，随意增删改列表
     */
    @PreAuthorize("@ss.hasPermi('bid:content:export')")
    @Log(title = "内容块，支持文本/格/图片混排，随意增删改", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TdwContents tdwContents)
    {
        List<TdwContents> list = tdwContentsService.selectTdwContentsList(tdwContents);
        ExcelUtil<TdwContents> util = new ExcelUtil<TdwContents>(TdwContents.class);
        util.exportExcel(response, list, "内容块，支持文本/格/图片混排，随意增删改数据");
    }

    /**
     * 获取内容块，支持文本/格/图片混排，随意增删改详细信息
     */
    @PreAuthorize("@ss.hasPermi('bid:content:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tdwContentsService.selectTdwContentsById(id));
    }

    /**
     * 新增内容块，支持文本/格/图片混排，随意增删改
     */
    @PreAuthorize("@ss.hasPermi('bid:content:add')")
    @Log(title = "内容块，支持文本/格/图片混排，随意增删改", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TdwContents tdwContents)
    {
        return toAjax(tdwContentsService.insertTdwContents(tdwContents));
    }

    /**
     * 修改内容块，支持文本/格/图片混排，随意增删改
     */
    @PreAuthorize("@ss.hasPermi('bid:content:edit')")
    @Log(title = "内容块，支持文本/格/图片混排，随意增删改", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TdwContents tdwContents)
    {
        return toAjax(tdwContentsService.updateTdwContents(tdwContents));
    }

    /**
     * 删除内容块，支持文本/格/图片混排，随意增删改
     */
    @PreAuthorize("@ss.hasPermi('bid:content:remove')")
    @Log(title = "内容块，支持文本/格/图片混排，随意增删改", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tdwContentsService.deleteTdwContentsByIds(ids));
    }

    private void sendEvent(SseEmitter emitter, String name, Object data) throws IOException
    {
        synchronized (emitter) {
            emitter.send(SseEmitter.event().name(name).data(data));
        }
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
