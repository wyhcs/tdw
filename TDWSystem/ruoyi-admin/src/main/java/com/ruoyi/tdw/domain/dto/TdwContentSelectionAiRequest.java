package com.ruoyi.tdw.domain.dto;

/**
 * AI operation request for selected text in the rich editor.
 */
public class TdwContentSelectionAiRequest
{
    private Long bidId;

    private Long outlineId;

    private String action;

    private String selectedText;

    private String documentText;

    private String requirement;

    public Long getBidId()
    {
        return bidId;
    }

    public void setBidId(Long bidId)
    {
        this.bidId = bidId;
    }

    public Long getOutlineId()
    {
        return outlineId;
    }

    public void setOutlineId(Long outlineId)
    {
        this.outlineId = outlineId;
    }

    public String getAction()
    {
        return action;
    }

    public void setAction(String action)
    {
        this.action = action;
    }

    public String getSelectedText()
    {
        return selectedText;
    }

    public void setSelectedText(String selectedText)
    {
        this.selectedText = selectedText;
    }

    public String getDocumentText()
    {
        return documentText;
    }

    public void setDocumentText(String documentText)
    {
        this.documentText = documentText;
    }

    public String getRequirement()
    {
        return requirement;
    }

    public void setRequirement(String requirement)
    {
        this.requirement = requirement;
    }
}
