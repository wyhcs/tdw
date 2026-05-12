package com.ruoyi.tdw.domain.dto;

/**
 * AI方案导出结果。
 */
public class TdwPlanExportResult
{
    private String fileName;

    private String fileUrl;

    private String downloadName;

    private String fileFormat;

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public String getFileUrl()
    {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl)
    {
        this.fileUrl = fileUrl;
    }

    public String getDownloadName()
    {
        return downloadName;
    }

    public void setDownloadName(String downloadName)
    {
        this.downloadName = downloadName;
    }

    public String getFileFormat()
    {
        return fileFormat;
    }

    public void setFileFormat(String fileFormat)
    {
        this.fileFormat = fileFormat;
    }
}
