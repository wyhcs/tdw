package com.ruoyi.tdw.mapper;

import java.util.List;
import com.ruoyi.tdw.domain.TdwQualityFramework;

public interface TdwQualityFrameworkMapper
{
    TdwQualityFramework selectQualityFrameworkById(Long id);

    List<TdwQualityFramework> selectQualityFrameworkList(TdwQualityFramework query);

    TdwQualityFramework selectLatestByBidId(Long bidId);

    int insertQualityFramework(TdwQualityFramework framework);

    int updateQualityFramework(TdwQualityFramework framework);

    int updateItemCount(Long id);

    int deleteQualityFrameworkByIds(Long[] ids);
}
