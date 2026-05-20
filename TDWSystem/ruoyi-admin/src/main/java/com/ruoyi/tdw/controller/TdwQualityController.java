package com.ruoyi.tdw.controller;

import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.tdw.domain.TdwBids;
import com.ruoyi.tdw.domain.TdwQualityFramework;
import com.ruoyi.tdw.domain.TdwQualityItem;
import com.ruoyi.tdw.domain.TdwQualityResult;
import com.ruoyi.tdw.domain.TdwQualityTask;
import com.ruoyi.tdw.service.ITdwDownloadService;
import com.ruoyi.tdw.service.ITdwQualityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Anonymous
@RestController
@RequestMapping("/tdw/quality")
public class TdwQualityController extends BaseController
{
    @Autowired
    private ITdwQualityService qualityService;

    @Autowired
    private ITdwDownloadService downloadService;

    @GetMapping("/project/list")
    public TableDataInfo projectList(TdwBids query)
    {
        startPage();
        List<TdwBids> list = qualityService.selectQualityProjectList(query);
        return getDataTable(list);
    }

    @PostMapping("/project")
    public AjaxResult createProject(@RequestParam("file") MultipartFile file) throws IOException
    {
        return success(qualityService.createQualityProject(file));
    }

    @GetMapping("/project/{bidId}")
    public AjaxResult projectDetail(@PathVariable Long bidId)
    {
        return success(qualityService.selectQualityProjectDetail(bidId));
    }

    @GetMapping("/framework/list")
    public TableDataInfo frameworkList(TdwQualityFramework query)
    {
        startPage();
        List<TdwQualityFramework> list = qualityService.selectQualityFrameworkList(query);
        return getDataTable(list);
    }

    @PostMapping("/framework/upload")
    public AjaxResult uploadFramework(@RequestParam("bidId") Long bidId,
                                      @RequestParam("file") MultipartFile file) throws IOException
    {
        return success(qualityService.uploadQualityFramework(bidId, file));
    }

    @PostMapping("/framework/extract/{bidId}")
    public AjaxResult extractFramework(@PathVariable Long bidId)
    {
        return success(qualityService.extractQualityFramework(bidId));
    }

    @GetMapping("/framework/{frameworkId}/export")
    public void exportFramework(@PathVariable Long frameworkId, HttpServletResponse response) throws IOException
    {
        qualityService.exportQualityFramework(frameworkId, response);
    }

    @GetMapping("/item/list")
    public TableDataInfo itemList(TdwQualityItem query)
    {
        startPage();
        List<TdwQualityItem> list = qualityService.selectQualityItemList(query);
        return getDataTable(list);
    }

    @PostMapping("/item")
    public AjaxResult addItem(@RequestBody TdwQualityItem item)
    {
        return success(qualityService.createQualityItem(item));
    }

    @PutMapping("/item")
    public AjaxResult updateItem(@RequestBody TdwQualityItem item)
    {
        return toAjax(qualityService.updateQualityItem(item));
    }

    @DeleteMapping("/item/{ids}")
    public AjaxResult removeItem(@PathVariable Long[] ids)
    {
        return toAjax(qualityService.deleteQualityItemByIds(ids));
    }

    @PostMapping("/version")
    public AjaxResult createVersion(@RequestParam("bidId") Long bidId,
                                    @RequestParam(value = "frameworkId", required = false) Long frameworkId,
                                    @RequestParam("file") MultipartFile file) throws IOException
    {
        return success(qualityService.createQualityVersion(bidId, frameworkId, file));
    }

    @GetMapping("/task/list")
    public TableDataInfo taskList(TdwQualityTask query)
    {
        startPage();
        List<TdwQualityTask> list = qualityService.selectQualityTaskList(query);
        return getDataTable(list);
    }

    @GetMapping("/task/{id}")
    public AjaxResult getTask(@PathVariable Long id)
    {
        return success(qualityService.selectQualityTaskById(id));
    }

    @PostMapping("/task")
    public AjaxResult addTask(@RequestBody TdwQualityTask task)
    {
        return success(qualityService.createQualityTask(task));
    }

    @PostMapping("/task/{taskId}/run")
    public AjaxResult runTask(@PathVariable Long taskId)
    {
        return success(qualityService.runQualityTask(taskId));
    }

    @PostMapping("/task/{taskId}/exportReport")
    public AjaxResult exportReport(@PathVariable Long taskId) throws IOException
    {
        return success(downloadService.exportReport("quality", taskId));
    }

    @DeleteMapping("/task/{ids}")
    public AjaxResult removeTask(@PathVariable Long[] ids)
    {
        return toAjax(qualityService.deleteQualityTaskByIds(ids));
    }

    @GetMapping("/result/list")
    public TableDataInfo resultList(TdwQualityResult query)
    {
        startPage();
        List<TdwQualityResult> list = qualityService.selectQualityResultList(query);
        return getDataTable(list);
    }
}
