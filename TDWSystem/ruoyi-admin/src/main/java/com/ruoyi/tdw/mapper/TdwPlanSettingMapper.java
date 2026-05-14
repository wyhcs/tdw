package com.ruoyi.tdw.mapper;

import com.ruoyi.tdw.domain.TdwPlanSetting;
import org.apache.ibatis.annotations.Param;

/**
 * AI方案设置Mapper接口。
 */
public interface TdwPlanSettingMapper
{
    TdwPlanSetting selectByBidId(@Param("bidId") Long bidId);

    int insertTdwPlanSetting(TdwPlanSetting setting);

    int updateTdwPlanSetting(TdwPlanSetting setting);

    int upsertTdwPlanSetting(TdwPlanSetting setting);
}
