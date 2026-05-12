package com.ruoyi.tdw.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.common.annotation.Anonymous;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.FilenameUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.tdw.domain.TdwOutlines;
import com.ruoyi.tdw.domain.dto.TdwOutlineGenerateRequest;
import com.ruoyi.tdw.domain.dto.TdwOutlineInsertRequest;
import com.ruoyi.tdw.domain.dto.TdwOutlineMoveRequest;
import com.ruoyi.tdw.domain.dto.TdwOutlineSortRequest;
import com.ruoyi.tdw.service.ITdwOutlinesService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 大纲节点Controller
 * 
 * @author ruoyi
 * @date 2026-03-28
 */
@Anonymous
@RestController
@RequestMapping("/tdw/outlines")
public class TdwOutlinesController extends BaseController
{
    @Autowired
    private ITdwOutlinesService tdwOutlinesService;

    @ApiOperation("查询完整三级大纲树")
    @GetMapping("/tree/{bidId}")
    public AjaxResult tree(@PathVariable Long bidId)
    {
        return success(tdwOutlinesService.selectOutlineTree(bidId));
    }

    @ApiOperation("修改大纲标题")
    @PutMapping("/title")
    public AjaxResult updateTitle(@RequestBody TdwOutlines tdwOutlines)
    {
        return toAjax(tdwOutlinesService.updateOutlineTitle(tdwOutlines));
    }

    @ApiOperation("插入大纲节点")
    @PostMapping("/insert")
    public AjaxResult insert(@RequestBody TdwOutlineInsertRequest request)
    {
        return success(tdwOutlinesService.insertOutlineNode(request));
    }

    @ApiOperation("删除大纲节点")
    @DeleteMapping("/{outlineId}")
    public AjaxResult remove(@PathVariable Long outlineId)
    {
        return toAjax(tdwOutlinesService.deleteOutlineById(outlineId));
    }

    @ApiOperation("移动大纲节点")
    @PutMapping("/move")
    public AjaxResult move(@RequestBody TdwOutlineMoveRequest request)
    {
        return toAjax(tdwOutlinesService.moveOutline(request));
    }

    @ApiOperation("调整同级大纲排序")
    @PutMapping("/sort")
    public AjaxResult sort(@RequestBody TdwOutlineSortRequest request)
    {
        return toAjax(tdwOutlinesService.sortOutlines(request));
    }

    @ApiOperation("生成表书大纲")
    @PostMapping(value ="/generateOutline")
    public AjaxResult generateOutline(@RequestBody(required = false) TdwOutlineGenerateRequest request, Long id, Long[] knowledgeFileIds) throws IOException {
        Long bidId;
        if (request != null && request.getBidId() != null) {
            bidId = tdwOutlinesService.generateOutline(request);
        } else {
            bidId = tdwOutlinesService.generateOutline(id, knowledgeFileIds == null ? null : Arrays.asList(knowledgeFileIds));
        }
        System.out.println("大纲生成完成，标书ID：" + bidId);
        return success("");
    }


    @ApiOperation("任意位置插入大纲")
    @PostMapping(value ="/insertOutline")
    public AjaxResult insertOutline(TdwOutlines tdwOutlines) throws IOException {
        TdwOutlines tdwOutlineSelect = tdwOutlinesService.selectLevelSortNumById(tdwOutlines.getId());

        tdwOutlineSelect.setTitle(tdwOutlines.getTitle());
        tdwOutlineSelect.setWordLimit(tdwOutlines.getWordLimit());

        if (tdwOutlineSelect.getLevel() == 1){
            int level1 = tdwOutlinesService.insertOutlineLeval1(tdwOutlineSelect);
            System.out.println("章节插入成功");
        } else if (tdwOutlineSelect.getLevel() == 2) {
            int level2 = tdwOutlinesService.insertOutlineLeval2(tdwOutlineSelect);
            System.out.println("节级插入成功");
        } else if (tdwOutlineSelect.getLevel() == 3) {

            int level3 = tdwOutlinesService.insertOutlineLeval3(tdwOutlineSelect);
            System.out.println("内容级插入成功");
        }
        return success("");
    }


    @ApiOperation("根据id删除大纲")
    @DeleteMapping("/deleteOutline")
    public AjaxResult deleteOutline(Long id) throws IOException {
        TdwOutlines tdwOutlines = tdwOutlinesService.selectLevelSortNumById(id);
        int level = tdwOutlinesService.deleteOutlineLeval(tdwOutlines);
        return success("success");
    }

}
