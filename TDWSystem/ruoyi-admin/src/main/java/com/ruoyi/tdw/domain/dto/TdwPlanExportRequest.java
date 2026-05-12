package com.ruoyi.tdw.domain.dto;

/**
 * AI方案导出请求。
 */
public class TdwPlanExportRequest
{
    private Long bidId;

    /** html为第一阶段默认格式。 */
    private String fileFormat;

    private Boolean includeEmptyOutline;

    public Long getBidId()
    {
        return bidId;
    }

    public void setBidId(Long bidId)
    {
        this.bidId = bidId;
    }

    public String getFileFormat()
    {
        return fileFormat;
    }

    public void setFileFormat(String fileFormat)
    {
        this.fileFormat = fileFormat;
    }

    public Boolean getIncludeEmptyOutline()
    {
        return includeEmptyOutline;
    }

    public void setIncludeEmptyOutline(Boolean includeEmptyOutline)
    {
        this.includeEmptyOutline = includeEmptyOutline;
    }
}
