package com.ruoyi.tdw.ai.dto;

import java.util.List;

/**
 * Request for outline generation.
 */
public class GenerateOutlineAiRequest
{
    private Long bidId;

    private String projectName;

    private String projectType;

    private String requirement;

    private List<Long> knowledgeFileIds;

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

    public String getProjectName()
    {
        return projectName;
    }

    public void setProjectName(String projectName)
    {
        this.projectName = projectName;
    }

    public String getProjectType()
    {
        return projectType;
    }

    public void setProjectType(String projectType)
    {
        this.projectType = projectType;
    }

    public String getRequirement()
    {
        return requirement;
    }

    public void setRequirement(String requirement)
    {
        this.requirement = requirement;
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
    public String getKnowledgeContext() { return knowledgeContext; }
    public void setKnowledgeContext(String knowledgeContext) { this.knowledgeContext = knowledgeContext; }
}
