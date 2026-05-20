package com.ruoyi.tdw.controller;

import java.io.IOException;
import java.util.List;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.tdw.domain.TdwDuplicateFile;
import com.ruoyi.tdw.domain.TdwDuplicateResult;
import com.ruoyi.tdw.domain.TdwDuplicateTask;
import com.ruoyi.tdw.domain.dto.TdwDuplicateRunRequest;
import com.ruoyi.tdw.service.ITdwDuplicateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Anonymous
@RestController
@RequestMapping("/tdw/duplicate")
public class TdwDuplicateController extends BaseController
{
    @Autowired
    private ITdwDuplicateService duplicateService;

    @GetMapping("/task/list")
    public TableDataInfo taskList(TdwDuplicateTask query)
    {
        startPage();
        List<TdwDuplicateTask> list = duplicateService.selectDuplicateTaskList(query);
        return getDataTable(list);
    }

    @GetMapping("/task/{id}")
    public AjaxResult getTask(@PathVariable Long id)
    {
        return success(duplicateService.selectDuplicateTaskById(id));
    }

    @PostMapping("/task")
    public AjaxResult addTask(@RequestBody TdwDuplicateTask task) throws IOException
    {
        return success(duplicateService.createDuplicateTask(task, null));
    }

    @PostMapping("/task/create")
    public AjaxResult createTask(@RequestParam(value = "taskName", required = false) String taskName,
                                 @RequestParam(value = "bidId", required = false) Long bidId,
                                 @RequestParam(value = "compareBidId", required = false) Long compareBidId,
                                 @RequestParam(value = "file", required = false) MultipartFile file) throws IOException
    {
        TdwDuplicateTask task = new TdwDuplicateTask();
        task.setTaskName(taskName);
        task.setBidId(bidId);
        task.setCompareBidId(compareBidId);
        return success(duplicateService.createDuplicateTask(task, file));
    }

    @PostMapping("/task/{taskId}/upload")
    public AjaxResult uploadFile(@PathVariable Long taskId, @RequestParam("file") MultipartFile file) throws IOException
    {
        return success(duplicateService.uploadCompareFile(taskId, file));
    }

    @PostMapping("/task/{taskId}/uploadBatch")
    public AjaxResult uploadFiles(@PathVariable Long taskId, @RequestParam("files") MultipartFile[] files) throws IOException
    {
        return success(duplicateService.uploadCompareFiles(taskId, files));
    }

    @PostMapping("/task/{taskId}/libraries")
    public AjaxResult updateLibraries(@PathVariable Long taskId, @RequestBody TdwDuplicateRunRequest request)
    {
        List<Long> libraryIds = request == null ? null : request.getCompareLibraryIds();
        return toAjax(duplicateService.updateCompareLibraries(taskId, libraryIds));
    }

    @PostMapping("/task/{taskId}/run")
    public AjaxResult runTask(@PathVariable Long taskId, @RequestBody(required = false) TdwDuplicateRunRequest request)
    {
        return success(duplicateService.runDuplicateTask(taskId, request));
    }

    @DeleteMapping("/task/{ids}")
    public AjaxResult removeTask(@PathVariable Long[] ids)
    {
        return toAjax(duplicateService.deleteDuplicateTaskByIds(ids));
    }

    @DeleteMapping("/file/{ids}")
    public AjaxResult removeFile(@PathVariable Long[] ids)
    {
        return toAjax(duplicateService.deleteDuplicateFileByIds(ids));
    }

    @GetMapping("/file/list")
    public AjaxResult fileList(TdwDuplicateFile query)
    {
        return success(duplicateService.selectDuplicateFileList(query));
    }

    @GetMapping("/result/list")
    public TableDataInfo resultList(TdwDuplicateResult query)
    {
        startPage();
        List<TdwDuplicateResult> list = duplicateService.selectDuplicateResultList(query);
        return getDataTable(list);
    }

    @GetMapping("/task/{taskId}/report")
    public AjaxResult report(@PathVariable Long taskId) throws IOException
    {
        return success(duplicateService.selectDuplicateReport(taskId));
    }

    @PostMapping("/task/{taskId}/exportReport")
    public AjaxResult exportReport(@PathVariable Long taskId) throws IOException
    {
        return success(duplicateService.exportDuplicateReport(taskId));
    }
}
