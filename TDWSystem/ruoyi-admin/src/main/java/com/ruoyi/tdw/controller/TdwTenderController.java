package com.ruoyi.tdw.controller;

import java.io.IOException;
import java.util.List;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.tdw.domain.TdwBids;
import com.ruoyi.tdw.service.ITdwDownloadService;
import com.ruoyi.tdw.service.ITdwTenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Anonymous
@RestController
@RequestMapping("/tdw/tender")
public class TdwTenderController extends BaseController
{
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
    public AjaxResult upload(@RequestParam("bidId") Long bidId, @RequestParam("file") MultipartFile file) throws IOException
    {
        return success(tdwTenderService.uploadTenderFile(bidId, file));
    }

    @PostMapping("/parse/{tenderFileId}")
    public AjaxResult parse(@PathVariable Long tenderFileId)
    {
        return success(tdwTenderService.mockParse(tenderFileId));
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
}
