package com.ruoyi.tdw.controller;

import java.io.IOException;
import java.util.List;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.tdw.domain.TdwGallery;
import com.ruoyi.tdw.domain.TdwGalleryImage;
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
@RequestMapping("/tdw/tool")
public class TdwToolController extends BaseController
{
    @Autowired
    private ITdwToolService toolService;

    @GetMapping("/gallery/list")
    public TableDataInfo galleryList(TdwGallery query)
    {
        startPage();
        List<TdwGallery> list = toolService.selectGalleryList(query);
        return getDataTable(list);
    }

    @GetMapping("/gallery/{id}")
    public AjaxResult getGallery(@PathVariable Long id)
    {
        return success(toolService.selectGalleryById(id));
    }

    @PostMapping("/gallery")
    public AjaxResult addGallery(@RequestBody TdwGallery gallery)
    {
        return toAjax(toolService.insertGallery(gallery));
    }

    @PutMapping("/gallery")
    public AjaxResult editGallery(@RequestBody TdwGallery gallery)
    {
        return toAjax(toolService.updateGallery(gallery));
    }

    @DeleteMapping("/gallery/{ids}")
    public AjaxResult removeGallery(@PathVariable Long[] ids)
    {
        return toAjax(toolService.deleteGalleryByIds(ids));
    }

    @GetMapping("/image/list")
    public TableDataInfo imageList(TdwGalleryImage query)
    {
        startPage();
        List<TdwGalleryImage> list = toolService.selectGalleryImageList(query);
        return getDataTable(list);
    }

    @GetMapping("/image/{id}")
    public AjaxResult getImage(@PathVariable Long id)
    {
        return success(toolService.selectGalleryImageById(id));
    }

    @PostMapping("/image/upload")
    public AjaxResult uploadImage(@RequestParam("galleryId") Long galleryId,
                                  @RequestParam(value = "imageName", required = false) String imageName,
                                  @RequestParam(value = "description", required = false) String description,
                                  @RequestParam("file") MultipartFile file) throws IOException
    {
        return success(toolService.uploadImage(galleryId, imageName, description, file));
    }

    @PostMapping("/image/extractDoc")
    public AjaxResult extractDocImage(@RequestParam("galleryId") Long galleryId,
                                      @RequestParam("file") MultipartFile file) throws IOException
    {
        return success(toolService.extractDocImage(galleryId, file));
    }

    @PutMapping("/image")
    public AjaxResult editImage(@RequestBody TdwGalleryImage image)
    {
        return toAjax(toolService.updateGalleryImage(image));
    }

    @DeleteMapping("/image/{ids}")
    public AjaxResult removeImage(@PathVariable Long[] ids)
    {
        return toAjax(toolService.deleteGalleryImageByIds(ids));
    }
}
