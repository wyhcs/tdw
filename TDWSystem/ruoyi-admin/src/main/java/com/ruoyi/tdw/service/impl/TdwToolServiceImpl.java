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
        image.setDescription(description);
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
}
