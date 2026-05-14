package com.ruoyi.tdw.domain.dto;

import java.util.List;

/**
 * AI方案目录删除节点请求。
 */
public class TdwPlanDeleteNodeRequest
{
    private List<Long> ids;

    public List<Long> getIds() { return ids; }
    public void setIds(List<Long> ids) { this.ids = ids; }
}
