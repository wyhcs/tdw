package com.ruoyi.tdw.domain;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 大纲节点对象 tdw_outlines
 * 
 * @author ruoyi
 * @date 2026-03-28
 */
public class TdwOutlines extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 所属标书ID */
    @Excel(name = "所属标书ID")
    private Long bidId;

    /** 父节点ID */
    @Excel(name = "父节点ID")
    private Long parentId;

    /** 层级（1:章, 2:节, 3:内容标题） */
    @Excel(name = "层级", readConverterExp = "1=:章,,2=:节,,3=:内容标题")
    private int level;

    /** 大纲标题 */
    @Excel(name = "大纲标题")
    private String title;

    /** 同级排序号 */
    @Excel(name = "同级排序号")
    private int sortNum;

    @Excel(name = "内容字数限制")
    private int wordLimit;

    /** 子节点，用于返回三级大纲树 */
    private List<TdwOutlines> children = new ArrayList<TdwOutlines>();

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setBidId(Long bidId) 
    {
        this.bidId = bidId;
    }

    public Long getBidId() 
    {
        return bidId;
    }

    public void setParentId(Long parentId) 
    {
        this.parentId = parentId;
    }

    public Long getParentId() 
    {
        return parentId;
    }

    public void setLevel(int level)
    {
        this.level = level;
    }

    public int getLevel()
    {
        return level;
    }

    public void setTitle(String title) 
    {
        this.title = title;
    }

    public String getTitle() 
    {
        return title;
    }

    public void setSortNum(int sortNum)
    {
        this.sortNum = sortNum;
    }

    public int getSortNum()
    {
        return sortNum;
    }

    public void setWordLimit(int wordLimit)
    {
        this.wordLimit = wordLimit;
    }

    public int getWordLimit()
    {
        return wordLimit;
    }

    public void setChildren(List<TdwOutlines> children)
    {
        this.children = children;
    }

    public List<TdwOutlines> getChildren()
    {
        return children;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("bidId", getBidId())
            .append("parentId", getParentId())
            .append("level", getLevel())
            .append("title", getTitle())
            .append("sortNum", getSortNum())
            .append("wordLimit", getWordLimit())
            .append("children", getChildren())
            .toString();
    }
}
