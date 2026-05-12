package com.ruoyi.tdw.domain.dto;

import java.util.List;

/**
 * 内容块排序请求。
 */
public class TdwContentSortRequest
{
    /** 大纲节点ID。 */
    private Long outlineId;

    /** 新的内容块ID顺序。 */
    private List<Long> contentIds;

    public Long getOutlineId()
    {
        return outlineId;
    }

    public void setOutlineId(Long outlineId)
    {
        this.outlineId = outlineId;
    }

    public List<Long> getContentIds()
    {
        return contentIds;
    }

    public void setContentIds(List<Long> contentIds)
    {
        this.contentIds = contentIds;
    }
}
