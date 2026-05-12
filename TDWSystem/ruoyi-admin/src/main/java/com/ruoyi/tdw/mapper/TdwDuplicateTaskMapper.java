package com.ruoyi.tdw.mapper;

import java.util.List;
import com.ruoyi.tdw.domain.TdwDuplicateTask;

public interface TdwDuplicateTaskMapper
{
    TdwDuplicateTask selectDuplicateTaskById(Long id);

    List<TdwDuplicateTask> selectDuplicateTaskList(TdwDuplicateTask query);

    int insertDuplicateTask(TdwDuplicateTask task);

    int updateDuplicateTask(TdwDuplicateTask task);

    int deleteDuplicateTaskByIds(Long[] ids);
}
