package com.ruoyi.tdw.domain.dto;

/**
 * AI 方案单字段抽取请求。
 */
public class TdwServiceFieldParseRequest
{
    private Long fileId;
    private String content;
    private String category;
    private String fieldKey;

    public Long getFileId() { return fileId; }
    public void setFileId(Long fileId) { this.fileId = fileId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getFieldKey() { return fieldKey; }
    public void setFieldKey(String fieldKey) { this.fieldKey = fieldKey; }
}
