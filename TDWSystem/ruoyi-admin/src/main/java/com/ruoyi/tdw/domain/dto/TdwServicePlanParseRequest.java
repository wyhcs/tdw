package com.ruoyi.tdw.domain.dto;

/**
 * 服务类 AI 方案招标文件解析请求。
 */
public class TdwServicePlanParseRequest
{
    private Long tenderFileId;
    private Long purchaseRequirementFileId;
    private Long otherAttachmentFileId;
    private String category;

    public Long getTenderFileId() { return tenderFileId; }
    public void setTenderFileId(Long tenderFileId) { this.tenderFileId = tenderFileId; }
    public Long getPurchaseRequirementFileId() { return purchaseRequirementFileId; }
    public void setPurchaseRequirementFileId(Long purchaseRequirementFileId) { this.purchaseRequirementFileId = purchaseRequirementFileId; }
    public Long getOtherAttachmentFileId() { return otherAttachmentFileId; }
    public void setOtherAttachmentFileId(Long otherAttachmentFileId) { this.otherAttachmentFileId = otherAttachmentFileId; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
