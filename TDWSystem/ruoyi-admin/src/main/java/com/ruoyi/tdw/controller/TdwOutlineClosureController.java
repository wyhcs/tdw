package com.ruoyi.tdw.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
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
import com.ruoyi.tdw.domain.TdwOutlineClosure;
import com.ruoyi.tdw.service.ITdwOutlineClosureService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 大纲树形闭包关系，存储所有祖先后代关系Controller
 * 
 * @author ruoyi
 * @date 2026-03-28
 */
@RestController
@RequestMapping("/tdw/closure")
public class TdwOutlineClosureController extends BaseController
{
    @Autowired
    private ITdwOutlineClosureService tdwOutlineClosureService;

    /**
     * 查询大纲树形闭包关系，存储所有祖先后代关系列表
     */
    @PreAuthorize("@ss.hasPermi('system:closure:list')")
    @GetMapping("/list")
    public TableDataInfo list(TdwOutlineClosure tdwOutlineClosure)
    {
        startPage();
        List<TdwOutlineClosure> list = tdwOutlineClosureService.selectTdwOutlineClosureList(tdwOutlineClosure);
        return getDataTable(list);
    }

    /**
     * 导出大纲树形闭包关系，存储所有祖先后代关系列表
     */
    @PreAuthorize("@ss.hasPermi('system:closure:export')")
    @Log(title = "大纲树形闭包关系，存储所有祖先后代关系", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TdwOutlineClosure tdwOutlineClosure)
    {
        List<TdwOutlineClosure> list = tdwOutlineClosureService.selectTdwOutlineClosureList(tdwOutlineClosure);
        ExcelUtil<TdwOutlineClosure> util = new ExcelUtil<TdwOutlineClosure>(TdwOutlineClosure.class);
        util.exportExcel(response, list, "大纲树形闭包关系，存储所有祖先后代关系数据");
    }

    /**
     * 获取大纲树形闭包关系，存储所有祖先后代关系详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:closure:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tdwOutlineClosureService.selectTdwOutlineClosureById(id));
    }

    /**
     * 新增大纲树形闭包关系，存储所有祖先后代关系
     */
    @PreAuthorize("@ss.hasPermi('system:closure:add')")
    @Log(title = "大纲树形闭包关系，存储所有祖先后代关系", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TdwOutlineClosure tdwOutlineClosure)
    {
        return toAjax(tdwOutlineClosureService.insertTdwOutlineClosure(tdwOutlineClosure));
    }

    /**
     * 修改大纲树形闭包关系，存储所有祖先后代关系
     */
    @PreAuthorize("@ss.hasPermi('system:closure:edit')")
    @Log(title = "大纲树形闭包关系，存储所有祖先后代关系", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TdwOutlineClosure tdwOutlineClosure)
    {
        return toAjax(tdwOutlineClosureService.updateTdwOutlineClosure(tdwOutlineClosure));
    }

    /**
     * 删除大纲树形闭包关系，存储所有祖先后代关系
     */
    @PreAuthorize("@ss.hasPermi('system:closure:remove')")
    @Log(title = "大纲树形闭包关系，存储所有祖先后代关系", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tdwOutlineClosureService.deleteTdwOutlineClosureByIds(ids));
    }
}
