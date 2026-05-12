package com.ruoyi.tdw.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 【请填写功能名称】对象 tdw_bids
 * 
 * @author ruoyi
 * @date 2026-03-28
 */
public class TdwBids extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
    private Long id;

    /** 表示名称 */
    @Excel(name = "表示名称")
    private String title;

    /** 关联的模板ID */
    @Excel(name = "关联的模板ID")
    private Long templateId;

    /** 状态：1-草稿，2-已完成 */
    @Excel(name = "状态：1-草稿，2-已完成")
    private Long status;

    private String category;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date createdTime;

    /** 用户id */
    @Excel(name = "用户id")
    private Long userid;

    /** 部门id */
    @Excel(name = "部门id")
    private Long deptid;

    /** 单位id */
    @Excel(name = "单位id")
    private Long unitid;

    /** 备注 */
    @Excel(name = "备注")
    private String note;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setTitle(String title) 
    {
        this.title = title;
    }

    public String getTitle() 
    {
        return title;
    }

    public void setTemplateId(Long templateId) 
    {
        this.templateId = templateId;
    }

    public Long getTemplateId() 
    {
        return templateId;
    }

    public void setStatus(Long status) 
    {
        this.status = status;
    }

    public Long getStatus() 
    {
        return status;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setCreatedTime(Date createdTime)
    {
        this.createdTime = createdTime;
    }

    public Date getCreatedTime() 
    {
        return createdTime;
    }

    public void setUserid(Long userid) 
    {
        this.userid = userid;
    }

    public Long getUserid() 
    {
        return userid;
    }

    public void setDeptid(Long deptid) 
    {
        this.deptid = deptid;
    }

    public Long getDeptid() 
    {
        return deptid;
    }

    public void setUnitid(Long unitid) 
    {
        this.unitid = unitid;
    }

    public Long getUnitid() 
    {
        return unitid;
    }

    public void setNote(String note) 
    {
        this.note = note;
    }

    public String getNote() 
    {
        return note;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("title", getTitle())
            .append("templateId", getTemplateId())
            .append("status", getStatus())
            .append("category", getCategory())
            .append("createdTime", getCreatedTime())
            .append("userid", getUserid())
            .append("deptid", getDeptid())
            .append("unitid", getUnitid())
            .append("note", getNote())
            .toString();
    }
}
