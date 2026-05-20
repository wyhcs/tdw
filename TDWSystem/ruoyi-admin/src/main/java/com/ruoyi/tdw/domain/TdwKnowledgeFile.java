package com.ruoyi.tdw.domain;

import com.ruoyi.common.core.domain.BaseEntity;

public class TdwKnowledgeFile extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long knowledgeFileId;
    private Long knowledgeId;
    private String fileName;
    private String originalName;
    private String fileUrl;
    private String fileType;
    private Long fileSize;
    private String fileUsage;
    private String isTemplate;
    private String parseStatus;
    private String imageStatus;
    private Integer chunkCount;
    private String delFlag;

    public Long getKnowledgeFileId() { return knowledgeFileId; }
    public void setKnowledgeFileId(Long knowledgeFileId) { this.knowledgeFileId = knowledgeFileId; }
    public Long getKnowledgeId() { return knowledgeId; }
    public void setKnowledgeId(Long knowledgeId) { this.knowledgeId = knowledgeId; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public String getFileUsage() { return fileUsage; }
    public void setFileUsage(String fileUsage) { this.fileUsage = fileUsage; }
    public String getIsTemplate() { return isTemplate; }
    public void setIsTemplate(String isTemplate) { this.isTemplate = isTemplate; }
    public String getParseStatus() { return parseStatus; }
    public void setParseStatus(String parseStatus) { this.parseStatus = parseStatus; }
    public String getImageStatus() { return imageStatus; }
    public void setImageStatus(String imageStatus) { this.imageStatus = imageStatus; }
    public Integer getChunkCount() { return chunkCount; }
    public void setChunkCount(Integer chunkCount) { this.chunkCount = chunkCount; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }

    @Override
    public String toString() {
        return "TdwKnowledgeFile{" +
                "knowledgeFileId=" + knowledgeFileId +
                ", knowledgeId=" + knowledgeId +
                ", fileName='" + fileName + '\'' +
                ", originalName='" + originalName + '\'' +
                ", fileUrl='" + fileUrl + '\'' +
                ", fileType='" + fileType + '\'' +
                ", fileSize=" + fileSize +
                ", fileUsage='" + fileUsage + '\'' +
                ", isTemplate='" + isTemplate + '\'' +
                ", parseStatus='" + parseStatus + '\'' +
                ", imageStatus='" + imageStatus + '\'' +
                ", chunkCount=" + chunkCount +
                ", delFlag='" + delFlag + '\'' +
                '}';
    }
}
