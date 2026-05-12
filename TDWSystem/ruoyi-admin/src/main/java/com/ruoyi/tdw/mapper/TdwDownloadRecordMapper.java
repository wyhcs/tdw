package com.ruoyi.tdw.mapper;

import java.util.List;
import com.ruoyi.tdw.domain.TdwDownloadRecord;

public interface TdwDownloadRecordMapper
{
    TdwDownloadRecord selectDownloadRecordById(Long id);

    List<TdwDownloadRecord> selectDownloadRecordList(TdwDownloadRecord query);

    int insertDownloadRecord(TdwDownloadRecord record);

    int updateDownloadRecord(TdwDownloadRecord record);

    int increaseDownloadCount(Long id);

    int deleteDownloadRecordByIds(Long[] ids);
}
