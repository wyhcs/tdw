package com.ruoyi.tdw.controller;

import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.tdw.domain.TdwDownloadRecord;
import com.ruoyi.tdw.service.ITdwDownloadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Anonymous
@RestController
@RequestMapping("/tdw/download")
public class TdwDownloadController extends BaseController
{
    @Autowired
    private ITdwDownloadService downloadService;

    @GetMapping("/list")
    public TableDataInfo list(TdwDownloadRecord query)
    {
        startPage();
        List<TdwDownloadRecord> list = downloadService.selectDownloadRecordList(query);
        return getDataTable(list);
    }

    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(downloadService.selectDownloadRecordById(id));
    }

    @GetMapping("/file/{id}")
    public void file(@PathVariable Long id, HttpServletResponse response) throws IOException
    {
        downloadService.downloadFile(id, response);
    }

    @PostMapping("/report/{sourceModule}/{sourceId}")
    public AjaxResult exportReport(@PathVariable String sourceModule, @PathVariable Long sourceId) throws IOException
    {
        return success(downloadService.exportReport(sourceModule, sourceId));
    }

    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(downloadService.deleteDownloadRecordByIds(ids));
    }
}
