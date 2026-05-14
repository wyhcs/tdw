package com.ruoyi.tdw.domain.dto;

import java.util.List;

/**
 * AI方案目录同级排序请求。
 */
public class TdwPlanSortRequest
{
    private Long parentId;
    private List<Long> orderedIds;

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public List<Long> getOrderedIds() { return orderedIds; }
    public void setOrderedIds(List<Long> orderedIds) { this.orderedIds = orderedIds; }
}
