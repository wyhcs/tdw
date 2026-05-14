package com.ruoyi.tdw.controller;

import java.util.List;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.tdw.domain.TdwKnowledgeBase;
import com.ruoyi.tdw.domain.TdwKnowledgeChunk;
import com.ruoyi.tdw.domain.TdwKnowledgeFile;
import com.ruoyi.tdw.service.ITdwKnowledgeService;
import com.ruoyi.tdw.service.ITdwToolService;
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
@RequestMapping("/tdw/knowledge")
public class TdwKnowledgeController extends BaseController
{
    @Autowired
    private ITdwKnowledgeService knowledgeService;
    @Autowired
    private ITdwToolService toolService;

    @GetMapping("/list")
    public TableDataInfo list(TdwKnowledgeBase query)
    {
        startPage();
        List<TdwKnowledgeBase> list = knowledgeService.selectKnowledgeBaseList(query);
        return getDataTable(list);
    }

    @GetMapping("/{knowledgeId}")
    public AjaxResult getInfo(@PathVariable Long knowledgeId)
    {
        return success(knowledgeService.selectKnowledgeBaseById(knowledgeId));
    }

    @PostMapping
    public AjaxResult add(@RequestBody TdwKnowledgeBase knowledgeBase)
    {
        knowledgeBase.setStatus("0");
        return toAjax(knowledgeService.insertKnowledgeBase(knowledgeBase));
    }

    @PutMapping("/rename")
    public AjaxResult rename(@RequestBody TdwKnowledgeBase knowledgeBase)
    {
        return toAjax(knowledgeService.renameKnowledgeBase(knowledgeBase));
    }

    @DeleteMapping("/{knowledgeId}")
    public AjaxResult remove(@PathVariable Long knowledgeId)
    {
        return toAjax(knowledgeService.deleteKnowledgeBase(knowledgeId));
    }

    @GetMapping("/file/list")
    public AjaxResult fileList(TdwKnowledgeFile query)
    {
        System.out.println("######### list list");
        System.out.println(query);
        return success(knowledgeService.selectKnowledgeFileList(query));
    }

    @PostMapping("/file/upload")
    public AjaxResult upload(@RequestParam(value = "file", required = false) MultipartFile file,
                             @RequestParam("knowledgeId") Long knowledgeId,
                             @RequestParam(value = "fileUsage", required = false) String fileUsage,
                             @RequestParam(value = "isTemplate", required = false) String isTemplate)
    {
        System.out.println("##################");
        System.out.println(knowledgeId);
        System.out.println(file);
        return success(knowledgeService.uploadKnowledgeFile(file, knowledgeId, fileUsage, isTemplate));
    }

    @PostMapping("/file/extractUpload")
    public AjaxResult extractUpload(@RequestParam("file") MultipartFile file,
                                    @RequestParam("knowledgeId") Long knowledgeId,
                                    @RequestParam(value = "galleryId", required = false) Long galleryId) throws Exception
    {
        TdwKnowledgeFile knowledgeFile = knowledgeService.uploadKnowledgeFile(file, knowledgeId, "material", "0");
        knowledgeService.extractImagesMock(knowledgeFile.getKnowledgeFileId());
        if (galleryId != null) {
            toolService.extractDocImage(galleryId, file);
        }
        return success(knowledgeFile);
    }

    @PostMapping("/file/{knowledgeFileId}/parse")
    public AjaxResult parse(@PathVariable Long knowledgeFileId)
    {
        return toAjax(knowledgeService.parseFileMock(knowledgeFileId));
    }

    @PostMapping("/file/{knowledgeFileId}/extractImages")
    public AjaxResult extractImages(@PathVariable Long knowledgeFileId)
    {
        return toAjax(knowledgeService.extractImagesMock(knowledgeFileId));
    }

    @GetMapping("/templateFiles")
    public AjaxResult templateFiles(Long knowledgeId)
    {
        return success(knowledgeService.selectTemplateFiles(knowledgeId));
    }

    @GetMapping("/chunks")
    public AjaxResult chunks(TdwKnowledgeChunk query)
    {
        return success(knowledgeService.selectKnowledgeChunks(query));
    }
}
