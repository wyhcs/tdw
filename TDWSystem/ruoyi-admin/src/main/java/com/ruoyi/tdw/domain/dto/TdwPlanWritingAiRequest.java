package com.ruoyi.tdw.domain.dto;

/**
 * AI方案编写规则AI生成请求。
 */
public class TdwPlanWritingAiRequest
{
    private String mode;
    private String manualInstruction;
    private String outlineText;

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getManualInstruction() { return manualInstruction; }
    public void setManualInstruction(String manualInstruction) { this.manualInstruction = manualInstruction; }
    public String getOutlineText() { return outlineText; }
    public void setOutlineText(String outlineText) { this.outlineText = outlineText; }
}
