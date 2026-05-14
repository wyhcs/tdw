package com.ruoyi.tdw.domain.dto;

/**
 * 从完整评分标准中提取技术评分项。
 */
public class TdwTechnicalScoreExtractRequest
{
    private String fullScoreItems;
    private String category;

    public String getFullScoreItems() { return fullScoreItems; }
    public void setFullScoreItems(String fullScoreItems) { this.fullScoreItems = fullScoreItems; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
