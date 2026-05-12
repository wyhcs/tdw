package com.ruoyi.tdw.mapper;

import java.util.List;
import com.ruoyi.tdw.domain.TdwGalleryImage;

public interface TdwGalleryImageMapper
{
    TdwGalleryImage selectGalleryImageById(Long id);

    List<TdwGalleryImage> selectGalleryImageList(TdwGalleryImage query);

    int insertGalleryImage(TdwGalleryImage image);

    int updateGalleryImage(TdwGalleryImage image);

    int deleteGalleryImageByIds(Long[] ids);

    int deleteByGalleryIds(Long[] galleryIds);
}
