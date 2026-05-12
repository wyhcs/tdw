package com.ruoyi.tdw.mapper;

import java.util.List;
import com.ruoyi.tdw.domain.TdwQualityTask;

public interface TdwQualityTaskMapper
{
    TdwQualityTask selectQualityTaskById(Long id);

    List<TdwQualityTask> selectQualityTaskList(TdwQualityTask query);

    int insertQualityTask(TdwQualityTask task);

    int updateQualityTask(TdwQualityTask task);

    int deleteQualityTaskByIds(Long[] ids);
}
