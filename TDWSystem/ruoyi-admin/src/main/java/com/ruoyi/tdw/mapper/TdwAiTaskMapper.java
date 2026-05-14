package com.ruoyi.tdw.mapper;

import com.ruoyi.tdw.domain.TdwAiTask;
import org.apache.ibatis.annotations.Param;

/**
 * AI任务Mapper接口。
 */
public interface TdwAiTaskMapper
{
    TdwAiTask selectTdwAiTaskById(@Param("id") Long id);

    int insertTdwAiTask(TdwAiTask task);

    int updateTdwAiTask(TdwAiTask task);
}
