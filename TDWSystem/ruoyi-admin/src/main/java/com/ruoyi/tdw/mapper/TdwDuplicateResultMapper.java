package com.ruoyi.tdw.mapper;

import java.util.List;
import com.ruoyi.tdw.domain.TdwDuplicateResult;

public interface TdwDuplicateResultMapper
{
    TdwDuplicateResult selectDuplicateResultById(Long id);

    List<TdwDuplicateResult> selectDuplicateResultList(TdwDuplicateResult query);

    int batchInsertDuplicateResult(List<TdwDuplicateResult> results);

    int deleteByTaskId(Long taskId);

    int deleteByTaskIds(Long[] taskIds);
}
