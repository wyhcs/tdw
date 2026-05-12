package com.ruoyi.tdw.domain.dto;

import java.util.List;

/**
 * 内容块生成请求。
 */
public class TdwContentGenerateRequest
{
    /** 项目ID，全文生成时必填。 */
    private Long bidId;

    /** 大纲节点ID，选中节点生成时必填。 */
    private Long outlineId;

    /** 生成范围：full全文，selected选中节点。 */
    private String scope;

    /** 写入模式：overwrite覆盖，append追加，keep只返回建议。 */
    private String mode;

    /** 用户补充要求。 */
    private String requirement;

    /** 招标文件解析报告ID，用于AI标书生成。 */
    private Long tenderParseReportId;

    /** 是否生成表格块。 */
    private Boolean includeTable;

    /** 是否生成图片/结构图块。 */
    private Boolean includeDiagram;

    /** 引用的知识库文件ID集合。 */
    private List<Long> knowledgeFileIds;

    /** 引用的知识片段ID集合。 */
    private List<Long> knowledgeChunkIds;

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

    public String getScope()
    {
        return scope;
    }

    public void setScope(String scope)
    {
        this.scope = scope;
    }

    public String getMode()
    {
        return mode;
    }

    public void setMode(String mode)
    {
        this.mode = mode;
    }

    public String getRequirement()
    {
        return requirement;
    }

    public void setRequirement(String requirement)
    {
        this.requirement = requirement;
    }

    public Long getTenderParseReportId()
    {
        return tenderParseReportId;
    }

    public void setTenderParseReportId(Long tenderParseReportId)
    {
        this.tenderParseReportId = tenderParseReportId;
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

    public List<Long> getKnowledgeFileIds() { return knowledgeFileIds; }
    public void setKnowledgeFileIds(List<Long> knowledgeFileIds) { this.knowledgeFileIds = knowledgeFileIds; }
    public List<Long> getKnowledgeChunkIds() { return knowledgeChunkIds; }
    public void setKnowledgeChunkIds(List<Long> knowledgeChunkIds) { this.knowledgeChunkIds = knowledgeChunkIds; }
}
