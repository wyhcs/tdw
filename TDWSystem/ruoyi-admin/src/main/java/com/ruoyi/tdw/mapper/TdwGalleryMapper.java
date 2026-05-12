package com.ruoyi.tdw.mapper;

import java.util.List;
import com.ruoyi.tdw.domain.TdwGallery;

public interface TdwGalleryMapper
{
    TdwGallery selectGalleryById(Long id);

    List<TdwGallery> selectGalleryList(TdwGallery query);

    int insertGallery(TdwGallery gallery);

    int updateGallery(TdwGallery gallery);

    int updateImageStats(Long id);

    int deleteGalleryByIds(Long[] ids);
}
