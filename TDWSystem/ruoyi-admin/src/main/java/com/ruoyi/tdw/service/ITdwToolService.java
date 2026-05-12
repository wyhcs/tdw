package com.ruoyi.tdw.service;

import java.io.IOException;
import java.util.List;
import com.ruoyi.tdw.domain.TdwGallery;
import com.ruoyi.tdw.domain.TdwGalleryImage;
import org.springframework.web.multipart.MultipartFile;

public interface ITdwToolService
{
    List<TdwGallery> selectGalleryList(TdwGallery query);

    TdwGallery selectGalleryById(Long id);

    int insertGallery(TdwGallery gallery);

    int updateGallery(TdwGallery gallery);

    int deleteGalleryByIds(Long[] ids);

    List<TdwGalleryImage> selectGalleryImageList(TdwGalleryImage query);

    TdwGalleryImage selectGalleryImageById(Long id);

    TdwGalleryImage uploadImage(Long galleryId, String imageName, String description, MultipartFile file) throws IOException;

    int updateGalleryImage(TdwGalleryImage image);

    int deleteGalleryImageByIds(Long[] ids);
}
