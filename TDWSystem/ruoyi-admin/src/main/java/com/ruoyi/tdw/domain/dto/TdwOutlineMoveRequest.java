package com.ruoyi.tdw.domain.dto;

/**
 * 大纲节点移动请求。
 */
public class TdwOutlineMoveRequest
{
    /** 被移动的大纲节点ID。 */
    private Long id;

    /** 目标项目ID。移动章级节点时可用于定位根节点列表。 */
    private Long bidId;

    /** 目标父节点ID。为空表示移动到章级根节点列表。 */
    private Long parentId;

    /** 参考节点ID。传入后移动到该节点后面。 */
    private Long afterId;

    /** 目标排序号，从1开始；未传时追加到末尾。 */
    private Integer sortNum;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getBidId()
    {
        return bidId;
    }

    public void setBidId(Long bidId)
    {
        this.bidId = bidId;
    }

    public Long getParentId()
    {
        return parentId;
    }

    public void setParentId(Long parentId)
    {
        this.parentId = parentId;
    }

    public Long getAfterId()
    {
        return afterId;
    }

    public void setAfterId(Long afterId)
    {
        this.afterId = afterId;
    }

    public Integer getSortNum()
    {
        return sortNum;
    }

    public void setSortNum(Integer sortNum)
    {
        this.sortNum = sortNum;
    }
}
