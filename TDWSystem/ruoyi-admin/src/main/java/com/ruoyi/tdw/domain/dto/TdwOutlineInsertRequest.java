package com.ruoyi.tdw.domain.dto;

/**
 * 大纲节点插入请求。
 */
public class TdwOutlineInsertRequest
{
    /** 项目ID。插入章级节点时必填；按父节点或参考节点插入时可由服务端推导。 */
    private Long bidId;

    /** 父节点ID。传入后表示追加或插入到该父节点下。 */
    private Long parentId;

    /** 参考节点ID。传入后表示插入到该节点后面，并继承其父级和层级。 */
    private Long afterId;

    /** 节点层级：1章，2节，3内容标题。可选，服务端会校验。 */
    private Integer level;

    /** 标题。 */
    private String title;

    /** 字数限制。 */
    private Integer wordLimit;

    /** 目标排序号，从1开始；未传时追加到末尾。 */
    private Integer sortNum;

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

    public Integer getLevel()
    {
        return level;
    }

    public void setLevel(Integer level)
    {
        this.level = level;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public Integer getWordLimit()
    {
        return wordLimit;
    }

    public void setWordLimit(Integer wordLimit)
    {
        this.wordLimit = wordLimit;
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
