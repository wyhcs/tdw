package com.ruoyi.tdw.domain;

import com.ruoyi.common.core.domain.BaseEntity;

public class TdwKnowledgeChunk extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long chunkId;
    private Long knowledgeId;
    private Long knowledgeFileId;
    private String chunkTitle;
    private String chunkContent;
    private String chunkType;
    private Integer chunkIndex;
    private String delFlag;

    public Long getChunkId() { return chunkId; }
    public void setChunkId(Long chunkId) { this.chunkId = chunkId; }
    public Long getKnowledgeId() { return knowledgeId; }
    public void setKnowledgeId(Long knowledgeId) { this.knowledgeId = knowledgeId; }
    public Long getKnowledgeFileId() { return knowledgeFileId; }
    public void setKnowledgeFileId(Long knowledgeFileId) { this.knowledgeFileId = knowledgeFileId; }
    public String getChunkTitle() { return chunkTitle; }
    public void setChunkTitle(String chunkTitle) { this.chunkTitle = chunkTitle; }
    public String getChunkContent() { return chunkContent; }
    public void setChunkContent(String chunkContent) { this.chunkContent = chunkContent; }
    public String getChunkType() { return chunkType; }
    public void setChunkType(String chunkType) { this.chunkType = chunkType; }
    public Integer getChunkIndex() { return chunkIndex; }
    public void setChunkIndex(Integer chunkIndex) { this.chunkIndex = chunkIndex; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
