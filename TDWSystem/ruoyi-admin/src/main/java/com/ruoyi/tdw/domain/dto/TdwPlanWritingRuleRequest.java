package com.ruoyi.tdw.domain.dto;

/**
 * AI方案编写方向/编写要求保存请求。
 */
public class TdwPlanWritingRuleRequest
{
    private String content;
    private String mode;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
}
