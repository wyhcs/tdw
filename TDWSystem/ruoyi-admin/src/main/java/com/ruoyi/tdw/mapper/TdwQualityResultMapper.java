package com.ruoyi.tdw.mapper;

import java.util.List;
import com.ruoyi.tdw.domain.TdwQualityResult;

public interface TdwQualityResultMapper
{
    TdwQualityResult selectQualityResultById(Long id);

    List<TdwQualityResult> selectQualityResultList(TdwQualityResult query);

    int insertQualityResult(TdwQualityResult result);

    int batchInsertQualityResult(List<TdwQualityResult> results);

    int deleteByTaskId(Long taskId);

    int deleteByTaskIds(Long[] taskIds);
}
