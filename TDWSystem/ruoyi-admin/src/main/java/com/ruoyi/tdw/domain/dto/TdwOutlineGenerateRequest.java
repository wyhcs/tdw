package com.ruoyi.tdw.domain.dto;

import java.util.List;

/**
 * AI方案大纲生成请求。
 */
public class TdwOutlineGenerateRequest
{
    private Long bidId;

    /** overwrite覆盖已有大纲，append追加到现有大纲后。 */
    private String mode;

    private String requirement;

    private String writingStyle;

    private Long tenderParseReportId;

    private List<Long> templateFileIds;

    private List<Long> knowledgeFileIds;

    public Long getBidId()
    {
        return bidId;
    }

    public void setBidId(Long bidId)
    {
        this.bidId = bidId;
    }

    public String getMode()
    {
        return mode;
    }

    public void setMode(String mode)
    {
        this.mode = mode;
    }

    public String getRequirement()
    {
        return requirement;
    }

    public void setRequirement(String requirement)
    {
        this.requirement = requirement;
    }

    public String getWritingStyle()
    {
        return writingStyle;
    }

    public void setWritingStyle(String writingStyle)
    {
        this.writingStyle = writingStyle;
    }

    public Long getTenderParseReportId()
    {
        return tenderParseReportId;
    }

    public void setTenderParseReportId(Long tenderParseReportId)
    {
        this.tenderParseReportId = tenderParseReportId;
    }

    public List<Long> getTemplateFileIds()
    {
        return templateFileIds;
    }

    public void setTemplateFileIds(List<Long> templateFileIds)
    {
        this.templateFileIds = templateFileIds;
    }

    public List<Long> getKnowledgeFileIds()
    {
        return knowledgeFileIds;
    }

    public void setKnowledgeFileIds(List<Long> knowledgeFileIds)
    {
        this.knowledgeFileIds = knowledgeFileIds;
    }
}
