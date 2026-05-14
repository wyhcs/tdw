package com.ruoyi.tdw.domain.dto;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AI 方案招标文件解析结果。
 */
public class TdwServicePlanParseResult
{
    private Long reportId;
    private Long bidId;
    private Long tenderFileId;
    private String projectName;
    private String purchaseRequirement;
    private String otherAttachment;
    private String fullScoreItems;
    private String technicalScoreItems;
    private Map<String, String> extractedFields = new LinkedHashMap<String, String>();

    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }
    public Long getBidId() { return bidId; }
    public void setBidId(Long bidId) { this.bidId = bidId; }
    public Long getTenderFileId() { return tenderFileId; }
    public void setTenderFileId(Long tenderFileId) { this.tenderFileId = tenderFileId; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getPurchaseRequirement() { return purchaseRequirement; }
    public void setPurchaseRequirement(String purchaseRequirement) { this.purchaseRequirement = purchaseRequirement; }
    public String getOtherAttachment() { return otherAttachment; }
    public void setOtherAttachment(String otherAttachment) { this.otherAttachment = otherAttachment; }
    public String getFullScoreItems() { return fullScoreItems; }
    public void setFullScoreItems(String fullScoreItems) { this.fullScoreItems = fullScoreItems; }
    public String getTechnicalScoreItems() { return technicalScoreItems; }
    public void setTechnicalScoreItems(String technicalScoreItems) { this.technicalScoreItems = technicalScoreItems; }
    public Map<String, String> getExtractedFields() { return extractedFields; }
    public void setExtractedFields(Map<String, String> extractedFields) { this.extractedFields = extractedFields; }
}
