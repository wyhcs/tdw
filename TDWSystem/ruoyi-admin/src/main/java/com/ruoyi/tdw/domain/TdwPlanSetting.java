package com.ruoyi.tdw.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * AI方案设置 tdw_plan_setting。
 */
public class TdwPlanSetting extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long bidId;
    private String wordPreset;
    private Integer pageWords;
    private String globalWritingDirection;
    private String globalWritingRequirement;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBidId() { return bidId; }
    public void setBidId(Long bidId) { this.bidId = bidId; }
    public String getWordPreset() { return wordPreset; }
    public void setWordPreset(String wordPreset) { this.wordPreset = wordPreset; }
    public Integer getPageWords() { return pageWords; }
    public void setPageWords(Integer pageWords) { this.pageWords = pageWords; }
    public String getGlobalWritingDirection() { return globalWritingDirection; }
    public void setGlobalWritingDirection(String globalWritingDirection) { this.globalWritingDirection = globalWritingDirection; }
    public String getGlobalWritingRequirement() { return globalWritingRequirement; }
    public void setGlobalWritingRequirement(String globalWritingRequirement) { this.globalWritingRequirement = globalWritingRequirement; }
}
