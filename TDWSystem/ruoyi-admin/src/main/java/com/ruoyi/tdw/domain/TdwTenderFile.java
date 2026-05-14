package com.ruoyi.tdw.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 招标文件上传记录 tdw_tender_file。
 */
public class TdwTenderFile extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long bidId;
    private String fileName;
    private String originalName;
    private String fileUrl;
    private Long fileSize;
    private String fileType;
    private String fileStage;
    private String parseStatus;
    private Long parseReportId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBidId() { return bidId; }
    public void setBidId(Long bidId) { this.bidId = bidId; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    public String getFileStage() { return fileStage; }
    public void setFileStage(String fileStage) { this.fileStage = fileStage; }
    public String getParseStatus() { return parseStatus; }
    public void setParseStatus(String parseStatus) { this.parseStatus = parseStatus; }
    public Long getParseReportId() { return parseReportId; }
    public void setParseReportId(Long parseReportId) { this.parseReportId = parseReportId; }
}
