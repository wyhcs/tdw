package com.ruoyi.tdw.domain;

import com.ruoyi.common.core.domain.BaseEntity;

public class TdwKnowledgeBase extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long knowledgeId;
    private String knowledgeName;
    private String description;
    private String status;
    private Integer fileCount;
    private String delFlag;

    public Long getKnowledgeId() { return knowledgeId; }
    public void setKnowledgeId(Long knowledgeId) { this.knowledgeId = knowledgeId; }
    public String getKnowledgeName() { return knowledgeName; }
    public void setKnowledgeName(String knowledgeName) { this.knowledgeName = knowledgeName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getFileCount() { return fileCount; }
    public void setFileCount(Integer fileCount) { this.fileCount = fileCount; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }

    @Override
    public String toString() {
        return "TdwKnowledgeBase{" +
                "knowledgeId=" + knowledgeId +
                ", knowledgeName='" + knowledgeName + '\'' +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                ", fileCount=" + fileCount +
                ", delFlag='" + delFlag + '\'' +
                '}';
    }
}
