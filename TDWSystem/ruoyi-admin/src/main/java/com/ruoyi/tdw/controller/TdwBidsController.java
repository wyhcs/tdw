package com.ruoyi.tdw.controller;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.tdw.domain.dto.TdwPlanExportRequest;
import com.ruoyi.tdw.domain.TdwOutlines;
import io.swagger.annotations.ApiOperation;
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
import com.ruoyi.tdw.domain.TdwBids;
import com.ruoyi.tdw.service.ITdwBidsService;
import com.ruoyi.tdw.service.ITdwPlanExportService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 【请填写功能名称】Controller
 * 
 * @author ruoyi
 * @date 2026-03-28
 */
@Anonymous
@RestController
@RequestMapping("/tdw/bids")
public class TdwBidsController extends BaseController
{
    @Autowired
    private ITdwBidsService tdwBidsService;

    @Autowired
    private ITdwPlanExportService tdwPlanExportService;

    /**
     * 新增【请填写功能名称】
     */
    @ApiOperation("标书标题信息添加")
    @PostMapping("/add")
    public AjaxResult add(@RequestBody TdwBids tdwBids) {
        if (tdwBids.getStatus() == null) {
            tdwBids.setStatus(1L);
        }
        if (tdwBids.getCreatedTime() == null) {
            tdwBids.setCreatedTime(new Date());
        }
        int rows = tdwBidsService.insertTdwBids(tdwBids);
        return rows > 0 ? success(tdwBids) : error();
    }


    @ApiOperation("标书删除")
    @DeleteMapping("/deleteBid")
    public AjaxResult deleteBid(Long id) throws IOException {
        int level = tdwBidsService.deleteBids(id);
        return success("success");
    }

    @ApiOperation("AI方案HTML导出")
    @PostMapping("/exportHtml")
    public AjaxResult exportHtml(@RequestBody TdwPlanExportRequest request) throws IOException {
        return success(tdwPlanExportService.exportHtml(request));
    }

    /**
     * 查询【请填写功能名称】列表
     */
    @PreAuthorize("@ss.hasPermi('bid:plan:list')")
    @GetMapping("/list")
    public TableDataInfo list(TdwBids tdwBids)
    {
        startPage();
        List<TdwBids> list = tdwBidsService.selectTdwBidsList(tdwBids);
        return getDataTable(list);
    }

    /**
     * 导出【请填写功能名称】列表
     */
    @PreAuthorize("@ss.hasPermi('bid:plan:export')")
    @Log(title = "【请填写功能名称】", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TdwBids tdwBids)
    {
        List<TdwBids> list = tdwBidsService.selectTdwBidsList(tdwBids);
        ExcelUtil<TdwBids> util = new ExcelUtil<TdwBids>(TdwBids.class);
        util.exportExcel(response, list, "【请填写功能名称】数据");
    }

    /**
     * 获取【请填写功能名称】详细信息
     */
    @PreAuthorize("@ss.hasPermi('bid:plan:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tdwBidsService.selectTdwBidsById(id));
    }


    /**
     * 修改【请填写功能名称】
     */
    @PreAuthorize("@ss.hasPermi('bid:plan:edit')")
    @Log(title = "【请填写功能名称】", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TdwBids tdwBids)
    {
        return toAjax(tdwBidsService.updateTdwBids(tdwBids));
    }

    /**
     * 删除【请填写功能名称】
     */
    @PreAuthorize("@ss.hasPermi('bid:plan:remove')")
    @Log(title = "【请填写功能名称】", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tdwBidsService.deleteTdwBidsByIds(ids));
    }
}
