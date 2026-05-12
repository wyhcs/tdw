package com.ruoyi.tdw.controller;

import java.io.IOException;
import java.util.List;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.tdw.domain.TdwQualityResult;
import com.ruoyi.tdw.domain.TdwQualityTask;
import com.ruoyi.tdw.service.ITdwDownloadService;
import com.ruoyi.tdw.service.ITdwQualityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Anonymous
@RestController
@RequestMapping("/tdw/quality")
public class TdwQualityController extends BaseController
{
    @Autowired
    private ITdwQualityService qualityService;

    @Autowired
    private ITdwDownloadService downloadService;

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
