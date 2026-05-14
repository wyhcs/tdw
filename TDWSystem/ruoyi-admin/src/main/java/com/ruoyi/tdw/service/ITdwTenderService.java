package com.ruoyi.tdw.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.ruoyi.tdw.domain.TdwBids;
import com.ruoyi.tdw.domain.TdwTenderFile;
import com.ruoyi.tdw.domain.TdwTenderParseReport;
import com.ruoyi.tdw.domain.dto.TdwServicePlanParseRequest;
import com.ruoyi.tdw.domain.dto.TdwServicePlanParseResult;
import org.springframework.web.multipart.MultipartFile;

public interface ITdwTenderService
{
    List<TdwBids> selectTenderBidList(TdwBids query);

    Map<String, Object> createTenderProject(String title, String category, String note, MultipartFile file) throws IOException;

    TdwTenderFile uploadTenderFile(Long bidId, MultipartFile file) throws IOException;

    TdwTenderFile uploadTenderFile(Long bidId, MultipartFile file, String fileStage) throws IOException;

    TdwTenderParseReport mockParse(Long tenderFileId);

    TdwServicePlanParseResult parseServicePlan(TdwServicePlanParseRequest request);

    String extractServicePurchaseRequirementFromTenderFile(Long fileId, String category);

    String extractServicePurchaseRequirementFromFile(Long fileId, String category);

    String extractPlanFieldFromTenderFile(Long fileId, String category, String fieldKey);

    String extractPlanFieldFromFile(Long fileId, String category, String fieldKey);

    String extractServiceOtherAttachmentFromFile(Long fileId, String category);

    String extractServiceScoreItemsFromTenderFile(Long fileId, String category);

    String extractServiceScoreItemsFromPurchaseRequirement(String purchaseRequirement, String category);

    String extractTechnicalScoreItems(String fullScoreItems, String category);

    List<TdwTenderFile> selectFilesByBidId(Long bidId);

    TdwTenderParseReport selectReportById(Long id);

    TdwTenderParseReport selectReportByFileId(Long tenderFileId);

    TdwTenderParseReport selectLatestReportByBidId(Long bidId);
}
