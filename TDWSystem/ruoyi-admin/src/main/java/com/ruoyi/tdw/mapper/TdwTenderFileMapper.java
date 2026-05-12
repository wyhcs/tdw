package com.ruoyi.tdw.mapper;

import java.util.List;

import com.ruoyi.tdw.domain.TdwBids;
import com.ruoyi.tdw.domain.TdwTenderFile;
import org.apache.ibatis.annotations.Param;

public interface TdwTenderFileMapper
{
    TdwTenderFile selectById(Long id);

    List<TdwTenderFile> selectByBidId(Long bidId);

    List<TdwBids> selectTenderBidList(TdwBids tdwBids);

    int insertTenderFile(TdwTenderFile tenderFile);

    int updateParseStatus(@Param("id") Long id, @Param("parseStatus") String parseStatus, @Param("parseReportId") Long parseReportId);

    int deleteByBidId(Long bidId);
}
