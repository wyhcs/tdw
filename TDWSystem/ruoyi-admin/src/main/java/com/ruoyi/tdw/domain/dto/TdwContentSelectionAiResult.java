package com.ruoyi.tdw.domain.dto;

import java.util.List;

/**
 * AI operation result returned to the rich editor.
 */
public class TdwContentSelectionAiResult
{
    private String action;

    private String text;

    private String html;

    private String title;

    private String description;

    private List<String> headers;

    private List<List<String>> rows;

    private List<String> keywords;

    public String getAction()
    {
        return action;
    }

    public void setAction(String action)
    {
        this.action = action;
    }

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    public String getHtml()
    {
        return html;
    }

    public void setHtml(String html)
    {
        this.html = html;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public List<String> getHeaders()
    {
        return headers;
    }

    public void setHeaders(List<String> headers)
    {
        this.headers = headers;
    }

    public List<List<String>> getRows()
    {
        return rows;
    }

    public void setRows(List<List<String>> rows)
    {
        this.rows = rows;
    }

    public List<String> getKeywords()
    {
        return keywords;
    }

    public void setKeywords(List<String> keywords)
    {
        this.keywords = keywords;
    }
}
