package com.ruoyi.tdw.mapper;

import com.ruoyi.tdw.domain.TdwTenderParseReport;

public interface TdwTenderParseReportMapper
{
    TdwTenderParseReport selectById(Long id);

    TdwTenderParseReport selectByTenderFileId(Long tenderFileId);

    TdwTenderParseReport selectLatestByBidId(Long bidId);

    int insertParseReport(TdwTenderParseReport parseReport);

    int deleteByBidId(Long bidId);
}
