package com.ruoyi.tdw.domain.dto;

/**
 * Rich text content saved from the bid editor.
 */
public class TdwRichContentSaveRequest
{
    private Long outlineId;

    private String html;

    private String text;

    public Long getOutlineId()
    {
        return outlineId;
    }

    public void setOutlineId(Long outlineId)
    {
        this.outlineId = outlineId;
    }

    public String getHtml()
    {
        return html;
    }

    public void setHtml(String html)
    {
        this.html = html;
    }

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }
}
