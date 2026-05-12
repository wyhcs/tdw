package com.ruoyi.tdw.domain.dto;

import java.util.List;

/**
 * 同级大纲排序请求。
 */
public class TdwOutlineSortRequest
{
    /** 项目ID。根节点排序时必填；子节点排序时可由父节点推导。 */
    private Long bidId;

    /** 父节点ID。为空表示章级根节点排序。 */
    private Long parentId;

    /** 新的同级节点ID顺序。 */
    private List<Long> outlineIds;

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

    public List<Long> getOutlineIds()
    {
        return outlineIds;
    }

    public void setOutlineIds(List<Long> outlineIds)
    {
        this.outlineIds = outlineIds;
    }
}
