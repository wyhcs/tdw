package com.ruoyi.tdw.domain.dto;

/**
 * AI方案篇幅预设请求。
 */
public class TdwPlanWordPresetRequest
{
    private String preset;
    private Integer wordLimit;
    private Integer pageWords;

    public String getPreset() { return preset; }
    public void setPreset(String preset) { this.preset = preset; }
    public Integer getWordLimit() { return wordLimit; }
    public void setWordLimit(Integer wordLimit) { this.wordLimit = wordLimit; }
    public Integer getPageWords() { return pageWords; }
    public void setPageWords(Integer pageWords) { this.pageWords = pageWords; }
}
