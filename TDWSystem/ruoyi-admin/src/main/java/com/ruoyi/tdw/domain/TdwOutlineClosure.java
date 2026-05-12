package com.ruoyi.tdw.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 大纲树形闭包关系，存储所有祖先后代关系对象 tdw_outline_closure
 * 
 * @author ruoyi
 * @date 2026-03-28
 */
public class TdwOutlineClosure extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 祖先节点ID */
    @Excel(name = "祖先节点ID")
    private Long ancestor;

    /** 后代节点ID */
    @Excel(name = "后代节点ID")
    private Long descendant;

    /** 祖先到后代的深度，0代表节点自己 */
    @Excel(name = "祖先到后代的深度，0代表节点自己")
    private Long depth;

    public TdwOutlineClosure() {
    }

    public TdwOutlineClosure(Long ancestor, Long descendant, Long depth) {
        this.ancestor = ancestor;
        this.descendant = descendant;
        this.depth = depth;
    }

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setAncestor(Long ancestor) 
    {
        this.ancestor = ancestor;
    }

    public Long getAncestor() 
    {
        return ancestor;
    }

    public void setDescendant(Long descendant) 
    {
        this.descendant = descendant;
    }

    public Long getDescendant() 
    {
        return descendant;
    }

    public void setDepth(Long depth) 
    {
        this.depth = depth;
    }

    public Long getDepth() 
    {
        return depth;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("ancestor", getAncestor())
            .append("descendant", getDescendant())
            .append("depth", getDepth())
            .toString();
    }
}
