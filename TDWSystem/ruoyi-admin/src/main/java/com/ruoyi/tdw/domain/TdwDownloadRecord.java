package com.ruoyi.tdw.domain;

import java.util.Date;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * Download center record.
 */
public class TdwDownloadRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private String sourceModule;
    private Long sourceId;
    private String fileName;
    private String fileType;
    private String fileUrl;
    private Long fileSize;
    private Integer downloadCount;
    private Date lastDownloadTime;
    private String delFlag;
    private String beginTime;
    private String endTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSourceModule() { return sourceModule; }
    public void setSourceModule(String sourceModule) { this.sourceModule = sourceModule; }
    public Long getSourceId() { return sourceId; }
    public void setSourceId(Long sourceId) { this.sourceId = sourceId; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public Integer getDownloadCount() { return downloadCount; }
    public void setDownloadCount(Integer downloadCount) { this.downloadCount = downloadCount; }
    public Date getLastDownloadTime() { return lastDownloadTime; }
    public void setLastDownloadTime(Date lastDownloadTime) { this.lastDownloadTime = lastDownloadTime; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getBeginTime() { return beginTime; }
    public void setBeginTime(String beginTime) { this.beginTime = beginTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
}
