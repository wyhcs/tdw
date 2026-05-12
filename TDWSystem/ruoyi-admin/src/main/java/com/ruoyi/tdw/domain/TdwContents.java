package com.ruoyi.tdw.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 内容块，支持文本/格/图片混排，随意增删改对象 tdw_contents
 * 
 * @author ruoyi
 * @date 2026-03-28
 */
public class TdwContents extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 内容块ID */
    private Long id;

    /** 关联的大纲节点ID（永远不变，大纲移动也不用改） */
    @Excel(name = "关联的大纲节点ID", readConverterExp = "永=远不变，大纲移动也不用改")
    private Long outlineId;

    /** 内容类型：1=文本，2=表格，3=图片 */
    @Excel(name = "内容类型：1=文本，2=表格，3=图片")
    private Integer contentType;

    /** 具体内容，用MySQL原生JSON类型，比text性能高、支持校验 */
    @Excel(name = "具体内容，用MySQL原生JSON类型，比text性能高、支持校验")
    private String content;

    /** 节点下内容的显示顺序，用于混排排序 */
    @Excel(name = "节点下内容的显示顺序，用于混排排序")
    private Integer sortOrder;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setOutlineId(Long outlineId) 
    {
        this.outlineId = outlineId;
    }

    public Long getOutlineId() 
    {
        return outlineId;
    }

    public void setContentType(Integer contentType)
    {
        this.contentType = contentType;
    }

    public Integer getContentType()
    {
        return contentType;
    }

    public void setContent(String content) 
    {
        this.content = content;
    }

    public String getContent() 
    {
        return content;
    }

    public void setSortOrder(Integer sortOrder)
    {
        this.sortOrder = sortOrder;
    }

    public Integer getSortOrder()
    {
        return sortOrder;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("outlineId", getOutlineId())
            .append("contentType", getContentType())
            .append("content", getContent())
            .append("sortOrder", getSortOrder())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
