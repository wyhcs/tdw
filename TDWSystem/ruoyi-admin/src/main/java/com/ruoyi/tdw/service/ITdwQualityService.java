package com.ruoyi.tdw.service;

import java.util.List;
import com.ruoyi.tdw.domain.TdwQualityResult;
import com.ruoyi.tdw.domain.TdwQualityTask;

public interface ITdwQualityService
{
    List<TdwQualityTask> selectQualityTaskList(TdwQualityTask query);

    TdwQualityTask selectQualityTaskById(Long id);

    TdwQualityTask createQualityTask(TdwQualityTask task);

    List<TdwQualityResult> runQualityTask(Long taskId);

    int deleteQualityTaskByIds(Long[] ids);

    List<TdwQualityResult> selectQualityResultList(TdwQualityResult query);
}
