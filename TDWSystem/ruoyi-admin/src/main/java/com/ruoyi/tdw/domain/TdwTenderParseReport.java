package com.ruoyi.tdw.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 招标文件解析报告 tdw_tender_parse_report。
 */
public class TdwTenderParseReport extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long bidId;
    private Long tenderFileId;
    private String projectName;
    private String requirementSummary;
    private String scoreItems;
    private String reportContent;
    private String parseStatus;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBidId() { return bidId; }
    public void setBidId(Long bidId) { this.bidId = bidId; }
    public Long getTenderFileId() { return tenderFileId; }
    public void setTenderFileId(Long tenderFileId) { this.tenderFileId = tenderFileId; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getRequirementSummary() { return requirementSummary; }
    public void setRequirementSummary(String requirementSummary) { this.requirementSummary = requirementSummary; }
    public String getScoreItems() { return scoreItems; }
    public void setScoreItems(String scoreItems) { this.scoreItems = scoreItems; }
    public String getReportContent() { return reportContent; }
    public void setReportContent(String reportContent) { this.reportContent = reportContent; }
    public String getParseStatus() { return parseStatus; }
    public void setParseStatus(String parseStatus) { this.parseStatus = parseStatus; }
}
