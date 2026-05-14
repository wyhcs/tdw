package com.ruoyi.tdw.mapper;

import java.util.List;

import com.ruoyi.tdw.domain.TdwAiRecord;
import org.apache.ibatis.annotations.Param;

/**
 * AI调用记录Mapper接口。
 */
public interface TdwAiRecordMapper
{
    TdwAiRecord selectTdwAiRecordById(@Param("id") Long id);

    List<TdwAiRecord> selectByTaskId(@Param("taskId") Long taskId);

    int insertTdwAiRecord(TdwAiRecord record);
}
