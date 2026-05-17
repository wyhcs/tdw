package com.ruoyi.tdw.domain;

import java.util.Date;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * Download center record.
 */
public class TdwDownloadRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long downloadId;
    private String moduleType;
    private Long bizId;
    private Long fileId;
    private String downloadName;
    private String downloadType;
    private String fileFormat;
    private String generateStatus;
    private Long fileSize;
    private String fileUrl;
    private Integer downloadCount;
    private Date lastDownloadTime;
    private Date expireTime;
    private Long ownerId;
    private String delFlag;
    private String beginTime;
    private String endTime;

    public Long getDownloadId() { return downloadId; }
    public void setDownloadId(Long downloadId) { this.downloadId = downloadId; }
    public String getModuleType() { return moduleType; }
    public void setModuleType(String moduleType) { this.moduleType = moduleType; }
    public Long getBizId() { return bizId; }
    public void setBizId(Long bizId) { this.bizId = bizId; }
    public Long getFileId() { return fileId; }
    public void setFileId(Long fileId) { this.fileId = fileId; }
    public String getDownloadName() { return downloadName; }
    public void setDownloadName(String downloadName) { this.downloadName = downloadName; }
    public String getDownloadType() { return downloadType; }
    public void setDownloadType(String downloadType) { this.downloadType = downloadType; }
    public String getFileFormat() { return fileFormat; }
    public void setFileFormat(String fileFormat) { this.fileFormat = fileFormat; }
    public String getGenerateStatus() { return generateStatus; }
    public void setGenerateStatus(String generateStatus) { this.generateStatus = generateStatus; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public Integer getDownloadCount() { return downloadCount; }
    public void setDownloadCount(Integer downloadCount) { this.downloadCount = downloadCount; }
    public Date getLastDownloadTime() { return lastDownloadTime; }
    public void setLastDownloadTime(Date lastDownloadTime) { this.lastDownloadTime = lastDownloadTime; }
    public Date getExpireTime() { return expireTime; }
    public void setExpireTime(Date expireTime) { this.expireTime = expireTime; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getBeginTime() { return beginTime; }
    public void setBeginTime(String beginTime) { this.beginTime = beginTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public Long getId() { return downloadId; }
    public void setId(Long id) { this.downloadId = id; }
    public String getSourceModule() { return moduleType; }
    public void setSourceModule(String sourceModule) { this.moduleType = sourceModule; }
    public Long getSourceId() { return bizId; }
    public void setSourceId(Long sourceId) { this.bizId = sourceId; }
    public String getFileName() { return downloadName; }
    public void setFileName(String fileName) { this.downloadName = fileName; }
    public String getFileType() { return fileFormat; }
    public void setFileType(String fileType) { this.fileFormat = fileType; }
}
