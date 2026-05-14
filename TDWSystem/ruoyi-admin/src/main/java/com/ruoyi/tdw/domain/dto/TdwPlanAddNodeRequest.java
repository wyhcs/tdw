package com.ruoyi.tdw.domain.dto;

/**
 * AI方案目录新增节点请求。
 */
public class TdwPlanAddNodeRequest
{
    private String title;
    private Integer sectionCount;
    private Integer paragraphCount;
    private Integer wordLimit;
    private String requirementDesc;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Integer getSectionCount() { return sectionCount; }
    public void setSectionCount(Integer sectionCount) { this.sectionCount = sectionCount; }
    public Integer getParagraphCount() { return paragraphCount; }
    public void setParagraphCount(Integer paragraphCount) { this.paragraphCount = paragraphCount; }
    public Integer getWordLimit() { return wordLimit; }
    public void setWordLimit(Integer wordLimit) { this.wordLimit = wordLimit; }
    public String getRequirementDesc() { return requirementDesc; }
    public void setRequirementDesc(String requirementDesc) { this.requirementDesc = requirementDesc; }
}
