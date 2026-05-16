package com.ruoyi.tdw.controller;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.tdw.domain.dto.TdwPlanAddNodeRequest;
import com.ruoyi.tdw.domain.dto.TdwPlanAiTextResult;
import com.ruoyi.tdw.domain.dto.TdwPlanDeleteNodeRequest;
import com.ruoyi.tdw.domain.dto.TdwPlanSortRequest;
import com.ruoyi.tdw.domain.dto.TdwPlanTitleRequest;
import com.ruoyi.tdw.domain.dto.TdwPlanWordLimitRequest;
import com.ruoyi.tdw.domain.dto.TdwPlanWordPresetRequest;
import com.ruoyi.tdw.domain.dto.TdwPlanWritingAiRequest;
import com.ruoyi.tdw.domain.dto.TdwPlanWritingRuleRequest;
import com.ruoyi.tdw.service.ITdwPlanOutlineService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI方案目录生成后编辑Controller。
 */
@Anonymous
@RestController
@RequestMapping("/tdw/plan/{bidId}/outline")
public class TdwPlanOutlineController extends BaseController
{
    private final ExecutorService streamExecutor = Executors.newCachedThreadPool();

    @Autowired
    private ITdwPlanOutlineService planOutlineService;

    @ApiOperation("AI方案目录编辑总览")
    @GetMapping("/overview")
    public AjaxResult overview(@PathVariable Long bidId)
    {
        return success(planOutlineService.getOverview(bidId));
    }

    @ApiOperation("AI方案进入编辑")
    @GetMapping("/edit")
    public AjaxResult edit(@PathVariable Long bidId)
    {
        return success(planOutlineService.getOverview(bidId));
    }

    @ApiOperation("AI方案设置篇幅")
    @PostMapping("/word-preset")
    public AjaxResult wordPreset(@PathVariable Long bidId, @RequestBody TdwPlanWordPresetRequest request)
    {
        return success(planOutlineService.applyWordPreset(bidId, request));
    }

    @ApiOperation("AI方案修改单节点字数")
    @PutMapping("/nodes/{outlineId}/word-limit")
    public AjaxResult updateWordLimit(@PathVariable Long bidId,
                                      @PathVariable Long outlineId,
                                      @RequestBody TdwPlanWordLimitRequest request)
    {
        return success(planOutlineService.updateNodeWordLimit(bidId, outlineId, request));
    }

    @ApiOperation("AI方案批量修改字数")
    @PutMapping("/nodes/{outlineId}/word-limit/batch")
    public AjaxResult batchWordLimit(@PathVariable Long bidId,
                                     @PathVariable Long outlineId,
                                     @RequestBody TdwPlanWordLimitRequest request)
    {
        return success(planOutlineService.batchUpdateWordLimit(bidId, outlineId, request));
    }

    @ApiOperation("AI方案修改目录标题")
    @PutMapping("/nodes/{outlineId}/title")
    public AjaxResult updateTitle(@PathVariable Long bidId,
                                  @PathVariable Long outlineId,
                                  @RequestBody TdwPlanTitleRequest request)
    {
        return success(planOutlineService.updateTitle(bidId, outlineId, request));
    }

    @ApiOperation("保存编写方向")
    @PutMapping("/nodes/{outlineId}/writing-direction")
    public AjaxResult saveWritingDirection(@PathVariable Long bidId,
                                           @PathVariable Long outlineId,
                                           @RequestBody TdwPlanWritingRuleRequest request)
    {
        return success(planOutlineService.updateWritingDirection(bidId, outlineId, request));
    }

    @ApiOperation("保存编写要求")
    @PutMapping("/nodes/{outlineId}/writing-requirement")
    public AjaxResult saveWritingRequirement(@PathVariable Long bidId,
                                             @PathVariable Long outlineId,
                                             @RequestBody TdwPlanWritingRuleRequest request)
    {
        return success(planOutlineService.updateWritingRequirement(bidId, outlineId, request));
    }

    @ApiOperation("保存方案整体编写要求")
    @PutMapping("/writing-requirement")
    public AjaxResult saveGlobalWritingRequirement(@PathVariable Long bidId,
                                                   @RequestBody TdwPlanWritingRuleRequest request)
    {
        return success(planOutlineService.updateGlobalWritingRequirement(bidId, request));
    }

    @ApiOperation("AI生成编写方向")
    @PostMapping(value = "/nodes/{outlineId}/writing-direction/ai", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generateWritingDirection(@PathVariable Long bidId,
                                               @PathVariable Long outlineId,
                                               @RequestBody(required = false) TdwPlanWritingAiRequest request)
    {
        return streamText(() -> planOutlineService.generateWritingDirection(bidId, outlineId, request));
    }

    @ApiOperation("AI生成编写要求")
    @PostMapping(value = "/nodes/{outlineId}/writing-requirement/ai", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generateWritingRequirement(@PathVariable Long bidId,
                                                 @PathVariable Long outlineId,
                                                 @RequestBody(required = false) TdwPlanWritingAiRequest request)
    {
        return streamText(() -> planOutlineService.generateWritingRequirement(bidId, outlineId, request));
    }

    @ApiOperation("AI方案新增同级节点")
    @PostMapping("/nodes/{outlineId}/sibling")
    public AjaxResult addSibling(@PathVariable Long bidId,
                                 @PathVariable Long outlineId,
                                 @RequestBody TdwPlanAddNodeRequest request)
    {
        return success(planOutlineService.addSibling(bidId, outlineId, request));
    }

    @ApiOperation("AI方案新增子级节点")
    @PostMapping("/nodes/{outlineId}/child")
    public AjaxResult addChild(@PathVariable Long bidId,
                               @PathVariable Long outlineId,
                               @RequestBody TdwPlanAddNodeRequest request)
    {
        return success(planOutlineService.addChild(bidId, outlineId, request));
    }

    @ApiOperation("AI方案新增单段节点")
    @PostMapping("/nodes/{outlineId}/paragraph")
    public AjaxResult addParagraph(@PathVariable Long bidId,
                                   @PathVariable Long outlineId,
                                   @RequestBody TdwPlanAddNodeRequest request)
    {
        return success(planOutlineService.addParagraph(bidId, outlineId, request));
    }

    @ApiOperation("AI方案批量删除目录节点")
    @PostMapping("/nodes/delete")
    public AjaxResult deleteNodes(@PathVariable Long bidId, @RequestBody TdwPlanDeleteNodeRequest request)
    {
        return success(planOutlineService.deleteNodes(bidId, request));
    }

    @ApiOperation("AI方案目录节点排序")
    @PutMapping("/nodes/sort")
    public AjaxResult sortNodes(@PathVariable Long bidId, @RequestBody TdwPlanSortRequest request)
    {
        return success(planOutlineService.sortNodes(bidId, request));
    }

    @ApiOperation("AI方案完成目录编辑")
    @PostMapping("/finalize")
    public AjaxResult finalizeOutline(@PathVariable Long bidId)
    {
        return success(planOutlineService.finalizeOutline(bidId));
    }

    private SseEmitter streamText(StreamSupplier supplier)
    {
        SseEmitter emitter = new SseEmitter(0L);
        streamExecutor.execute(() -> {
            try {
                TdwPlanAiTextResult result = supplier.get();
                sendEvent(emitter, "meta", meta(result));
                String content = result.getContent() == null ? "" : result.getContent();
                for (String chunk : splitForStream(content)) {
                    sendEvent(emitter, "message", chunk);
                    Thread.sleep(30L);
                }
                sendEvent(emitter, "done", meta(result));
                emitter.complete();
            } catch (Exception e) {
                try {
                    sendEvent(emitter, "error", e.getMessage());
                } catch (IOException ignored) {
                }
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    private Map<String, Object> meta(TdwPlanAiTextResult result)
    {
        Map<String, Object> meta = new LinkedHashMap<String, Object>();
        meta.put("taskId", result.getTaskId());
        meta.put("recordId", result.getRecordId());
        return meta;
    }

    private void sendEvent(SseEmitter emitter, String name, Object data) throws IOException
    {
        emitter.send(SseEmitter.event().name(name).data(data));
    }

    private String[] splitForStream(String text)
    {
        if (text.length() <= 24) {
            return new String[] { text };
        }
        int size = 24;
        int count = (text.length() + size - 1) / size;
        String[] chunks = new String[count];
        for (int i = 0; i < count; i++) {
            int start = i * size;
            chunks[i] = text.substring(start, Math.min(text.length(), start + size));
        }
        return chunks;
    }

    private interface StreamSupplier
    {
        TdwPlanAiTextResult get();
    }
}
