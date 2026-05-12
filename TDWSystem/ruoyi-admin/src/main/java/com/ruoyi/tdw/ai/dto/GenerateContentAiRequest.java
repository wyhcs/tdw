package com.ruoyi.tdw.ai.dto;

import java.util.List;

/**
 * Request for content block generation and content operations.
 */
public class GenerateContentAiRequest
{
    private Long bidId;

    private Long outlineId;

    private String outlineTitle;

    private Integer outlineLevel;

    private Integer wordLimit;

    private String requirement;

    private Boolean includeTable;

    private Boolean includeDiagram;

    private String existingContent;

    private String tenderParseResult;

    private String qualityItem;

    private List<Long> knowledgeFileIds;

    private List<Long> knowledgeChunkIds;

    private String knowledgeContext;

    private List<AiMessage> messages;

    public Long getBidId()
    {
        return bidId;
    }

    public void setBidId(Long bidId)
    {
        this.bidId = bidId;
    }

    public Long getOutlineId()
    {
        return outlineId;
    }

    public void setOutlineId(Long outlineId)
    {
        this.outlineId = outlineId;
    }

    public String getOutlineTitle()
    {
        return outlineTitle;
    }

    public void setOutlineTitle(String outlineTitle)
    {
        this.outlineTitle = outlineTitle;
    }

    public Integer getOutlineLevel()
    {
        return outlineLevel;
    }

    public void setOutlineLevel(Integer outlineLevel)
    {
        this.outlineLevel = outlineLevel;
    }

    public Integer getWordLimit()
    {
        return wordLimit;
    }

    public void setWordLimit(Integer wordLimit)
    {
        this.wordLimit = wordLimit;
    }

    public String getRequirement()
    {
        return requirement;
    }

    public void setRequirement(String requirement)
    {
        this.requirement = requirement;
    }

    public Boolean getIncludeTable()
    {
        return includeTable;
    }

    public void setIncludeTable(Boolean includeTable)
    {
        this.includeTable = includeTable;
    }

    public Boolean getIncludeDiagram()
    {
        return includeDiagram;
    }

    public void setIncludeDiagram(Boolean includeDiagram)
    {
        this.includeDiagram = includeDiagram;
    }

    public String getExistingContent()
    {
        return existingContent;
    }

    public void setExistingContent(String existingContent)
    {
        this.existingContent = existingContent;
    }

    public String getTenderParseResult()
    {
        return tenderParseResult;
    }

    public void setTenderParseResult(String tenderParseResult)
    {
        this.tenderParseResult = tenderParseResult;
    }

    public String getQualityItem()
    {
        return qualityItem;
    }

    public void setQualityItem(String qualityItem)
    {
        this.qualityItem = qualityItem;
    }

    public List<AiMessage> getMessages()
    {
        return messages;
    }

    public void setMessages(List<AiMessage> messages)
    {
        this.messages = messages;
    }

    public List<Long> getKnowledgeFileIds() { return knowledgeFileIds; }
    public void setKnowledgeFileIds(List<Long> knowledgeFileIds) { this.knowledgeFileIds = knowledgeFileIds; }
    public List<Long> getKnowledgeChunkIds() { return knowledgeChunkIds; }
    public void setKnowledgeChunkIds(List<Long> knowledgeChunkIds) { this.knowledgeChunkIds = knowledgeChunkIds; }
    public String getKnowledgeContext() { return knowledgeContext; }
    public void setKnowledgeContext(String knowledgeContext) { this.knowledgeContext = knowledgeContext; }
}
