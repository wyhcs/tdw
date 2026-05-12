package com.ruoyi.tdw.mapper;

import java.util.List;
import com.ruoyi.tdw.domain.TdwQualityItem;
import org.apache.ibatis.annotations.Param;

public interface TdwQualityItemMapper
{
    TdwQualityItem selectQualityItemById(Long id);

    List<TdwQualityItem> selectQualityItemList(TdwQualityItem query);

    List<TdwQualityItem> selectEnabledItemsByFrameworkId(@Param("frameworkId") Long frameworkId);

    Integer selectMaxSortNum(@Param("frameworkId") Long frameworkId);

    int insertQualityItem(TdwQualityItem item);

    int updateQualityItem(TdwQualityItem item);

    int deleteQualityItemByIds(Long[] ids);

    int deleteByFrameworkIds(Long[] frameworkIds);
}
