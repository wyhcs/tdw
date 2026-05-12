package com.ruoyi.tdw.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.ruoyi.tdw.domain.TdwBids;
import com.ruoyi.tdw.domain.TdwTenderFile;
import com.ruoyi.tdw.domain.TdwTenderParseReport;
import org.springframework.web.multipart.MultipartFile;

public interface ITdwTenderService
{
    List<TdwBids> selectTenderBidList(TdwBids query);

    Map<String, Object> createTenderProject(String title, String category, String note, MultipartFile file) throws IOException;

    TdwTenderFile uploadTenderFile(Long bidId, MultipartFile file) throws IOException;

    TdwTenderParseReport mockParse(Long tenderFileId);

    List<TdwTenderFile> selectFilesByBidId(Long bidId);

    TdwTenderParseReport selectReportById(Long id);

    TdwTenderParseReport selectReportByFileId(Long tenderFileId);

    TdwTenderParseReport selectLatestReportByBidId(Long bidId);
}
