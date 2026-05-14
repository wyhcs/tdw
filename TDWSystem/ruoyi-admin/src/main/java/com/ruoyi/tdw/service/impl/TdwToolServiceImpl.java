package com.ruoyi.tdw.service.impl;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.tdw.domain.TdwGallery;
import com.ruoyi.tdw.domain.TdwGalleryImage;
import com.ruoyi.tdw.mapper.TdwGalleryImageMapper;
import com.ruoyi.tdw.mapper.TdwGalleryMapper;
import com.ruoyi.tdw.service.ITdwToolService;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TdwToolServiceImpl implements ITdwToolService
{
    @Autowired
    private TdwGalleryMapper galleryMapper;

    @Autowired
    private TdwGalleryImageMapper galleryImageMapper;

    @Override
    public List<TdwGallery> selectGalleryList(TdwGallery query)
    {
        return galleryMapper.selectGalleryList(query);
    }

    @Override
    public TdwGallery selectGalleryById(Long id)
    {
        return galleryMapper.selectGalleryById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertGallery(TdwGallery gallery)
    {
        if (gallery == null || StringUtils.isBlank(gallery.getGalleryName())) {
            throw new IllegalArgumentException("图库名称不能为空");
        }
        gallery.setImageCount(0);
        gallery.setCoverUrl("");
        gallery.setCreateTime(DateUtils.getNowDate());
        gallery.setUpdateTime(DateUtils.getNowDate());
        gallery.setDelFlag("0");
        return galleryMapper.insertGallery(gallery);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateGallery(TdwGallery gallery)
    {
        if (gallery == null || gallery.getId() == null) {
            throw new IllegalArgumentException("图库ID不能为空");
        }
        gallery.setUpdateTime(DateUtils.getNowDate());
        return galleryMapper.updateGallery(gallery);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteGalleryByIds(Long[] ids)
    {
        galleryImageMapper.deleteByGalleryIds(ids);
        return galleryMapper.deleteGalleryByIds(ids);
    }

    @Override
    public List<TdwGalleryImage> selectGalleryImageList(TdwGalleryImage query)
    {
        return galleryImageMapper.selectGalleryImageList(query);
    }

    @Override
    public TdwGalleryImage selectGalleryImageById(Long id)
    {
        return galleryImageMapper.selectGalleryImageById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TdwGalleryImage uploadImage(Long galleryId, String imageName, String description, MultipartFile file) throws IOException
    {
        if (galleryId == null) {
            throw new IllegalArgumentException("galleryId不能为空");
        }
        if (galleryMapper.selectGalleryById(galleryId) == null) {
            throw new IllegalArgumentException("图库不存在");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("图片不能为空");
        }
        String fileUrl = FileUploadUtils.upload(RuoYiConfig.getUploadPath(), file);
        TdwGalleryImage image = new TdwGalleryImage();
        image.setGalleryId(galleryId);
        image.setOriginalName(file.getOriginalFilename());
        image.setImageName(StringUtils.isBlank(imageName) ? FilenameUtils.getBaseName(file.getOriginalFilename()) : imageName);
        image.setImageUrl(fileUrl);
        image.setImageSize(file.getSize());
        image.setImageType(FilenameUtils.getExtension(file.getOriginalFilename()));
        image.setDescription(StringUtils.defaultIfBlank(description, "AI总结：图片已上传至私人图库，可用于方案和标书正文配图。"));
        image.setImageTags("上传图片,私人图库");
        fillImageSize(image, file);
        image.setCreateTime(DateUtils.getNowDate());
        image.setUpdateTime(DateUtils.getNowDate());
        image.setDelFlag("0");
        galleryImageMapper.insertGalleryImage(image);
        galleryMapper.updateImageStats(galleryId);
        return image;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TdwGalleryImage extractDocImage(Long galleryId, MultipartFile file) throws IOException
    {
        if (galleryId == null) {
            throw new IllegalArgumentException("galleryId不能为空");
        }
        if (galleryMapper.selectGalleryById(galleryId) == null) {
            throw new IllegalArgumentException("图库不存在");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文档不能为空");
        }
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (!"doc".equalsIgnoreCase(extension) && !"docx".equalsIgnoreCase(extension)) {
            throw new IllegalArgumentException("仅支持doc/docx格式文档抽图");
        }
        String baseName = FilenameUtils.getBaseName(file.getOriginalFilename());
        TdwGalleryImage image = new TdwGalleryImage();
        image.setGalleryId(galleryId);
        image.setOriginalName(file.getOriginalFilename());
        image.setImageName(StringUtils.defaultIfBlank(baseName, "文档抽取图片"));
        image.setImageUrl(buildMockExtractImageUrl(baseName));
        image.setImageSize(file.getSize());
        image.setImageType("svg");
        image.setWidth(640);
        image.setHeight(360);
        image.setDescription("AI总结：从文档中抽取的会议、项目或资料配图，适合用于标书章节图文说明。");
        image.setImageTags("文档抽图,AI识别,标书素材");
        image.setCreateTime(DateUtils.getNowDate());
        image.setUpdateTime(DateUtils.getNowDate());
        image.setDelFlag("0");
        galleryImageMapper.insertGalleryImage(image);
        galleryMapper.updateImageStats(galleryId);
        return image;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateGalleryImage(TdwGalleryImage image)
    {
        if (image == null || image.getId() == null) {
            throw new IllegalArgumentException("图片ID不能为空");
        }
        TdwGalleryImage old = galleryImageMapper.selectGalleryImageById(image.getId());
        image.setUpdateTime(DateUtils.getNowDate());
        int rows = galleryImageMapper.updateGalleryImage(image);
        if (old != null) {
            galleryMapper.updateImageStats(old.getGalleryId());
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteGalleryImageByIds(Long[] ids)
    {
        Set<Long> galleryIds = new LinkedHashSet<Long>();
        if (ids != null) {
            for (Long id : ids) {
                TdwGalleryImage image = galleryImageMapper.selectGalleryImageById(id);
                if (image != null) {
                    galleryIds.add(image.getGalleryId());
                }
            }
        }
        int rows = galleryImageMapper.deleteGalleryImageByIds(ids);
        for (Long galleryId : galleryIds) {
            galleryMapper.updateImageStats(galleryId);
        }
        return rows;
    }

    private void fillImageSize(TdwGalleryImage image, MultipartFile file)
    {
        try {
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            if (bufferedImage != null) {
                image.setWidth(bufferedImage.getWidth());
                image.setHeight(bufferedImage.getHeight());
            }
        } catch (Exception e) {
            image.setWidth(null);
            image.setHeight(null);
        }
    }

    private String buildMockExtractImageUrl(String title)
    {
        String safeTitle = StringUtils.defaultIfBlank(title, "DOC");
        if (safeTitle.length() > 10) {
            safeTitle = safeTitle.substring(0, 10);
        }
        return "data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='640' height='360'><rect width='640' height='360' fill='%23e8eefc'/><rect x='70' y='70' width='500' height='220' rx='12' fill='%23ffffff'/><text x='320' y='165' text-anchor='middle' font-size='34' fill='%232f6bff'>文档抽图</text><text x='320' y='215' text-anchor='middle' font-size='22' fill='%236b7280'>" + safeTitle + "</text></svg>";
    }
}
