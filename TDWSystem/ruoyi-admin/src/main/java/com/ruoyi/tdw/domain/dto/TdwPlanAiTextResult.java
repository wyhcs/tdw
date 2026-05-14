package com.ruoyi.tdw.domain.dto;

/**
 * AI方案文本生成结果。
 */
public class TdwPlanAiTextResult
{
    private Long taskId;
    private Long recordId;
    private String content;

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
