package com.ruoyi.tdw.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * Duplicate check task.
 */
public class TdwDuplicateTask extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private String taskName;
    private Long bidId;
    private Long compareBidId;
    private String sourceType;
    private String checkScope;
    private String compareLibraryIds;
    private String status;
    private Integer totalCount;
    private Integer issueCount;
    private Integer riskCount;
    private Integer totalTextLength;
    private BigDecimal overallSimilarity;
    private BigDecimal textSimilarity;
    private BigDecimal imageSimilarity;
    private String riskLevel;
    private String reportJson;
    private String errorMessage;
    private Date startTime;
    private Date finishTime;
    private String delFlag;

    /** Transient source file fields for project list/detail pages. */
    private String sourceFileName;
    private String sourceFileUrl;
    private Long sourceFileSize;
    private String sourceFileType;
    private Integer sourceTextLength;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
    public Long getBidId() { return bidId; }
    public void setBidId(Long bidId) { this.bidId = bidId; }
    public Long getCompareBidId() { return compareBidId; }
    public void setCompareBidId(Long compareBidId) { this.compareBidId = compareBidId; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getCheckScope() { return checkScope; }
    public void setCheckScope(String checkScope) { this.checkScope = checkScope; }
    public String getCompareLibraryIds() { return compareLibraryIds; }
    public void setCompareLibraryIds(String compareLibraryIds) { this.compareLibraryIds = compareLibraryIds; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }
    public Integer getIssueCount() { return issueCount; }
    public void setIssueCount(Integer issueCount) { this.issueCount = issueCount; }
    public Integer getRiskCount() { return riskCount; }
    public void setRiskCount(Integer riskCount) { this.riskCount = riskCount; }
    public Integer getTotalTextLength() { return totalTextLength; }
    public void setTotalTextLength(Integer totalTextLength) { this.totalTextLength = totalTextLength; }
    public BigDecimal getOverallSimilarity() { return overallSimilarity; }
    public void setOverallSimilarity(BigDecimal overallSimilarity) { this.overallSimilarity = overallSimilarity; }
    public BigDecimal getTextSimilarity() { return textSimilarity; }
    public void setTextSimilarity(BigDecimal textSimilarity) { this.textSimilarity = textSimilarity; }
    public BigDecimal getImageSimilarity() { return imageSimilarity; }
    public void setImageSimilarity(BigDecimal imageSimilarity) { this.imageSimilarity = imageSimilarity; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getReportJson() { return reportJson; }
    public void setReportJson(String reportJson) { this.reportJson = reportJson; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }
    public Date getFinishTime() { return finishTime; }
    public void setFinishTime(Date finishTime) { this.finishTime = finishTime; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getSourceFileName() { return sourceFileName; }
    public void setSourceFileName(String sourceFileName) { this.sourceFileName = sourceFileName; }
    public String getSourceFileUrl() { return sourceFileUrl; }
    public void setSourceFileUrl(String sourceFileUrl) { this.sourceFileUrl = sourceFileUrl; }
    public Long getSourceFileSize() { return sourceFileSize; }
    public void setSourceFileSize(Long sourceFileSize) { this.sourceFileSize = sourceFileSize; }
    public String getSourceFileType() { return sourceFileType; }
    public void setSourceFileType(String sourceFileType) { this.sourceFileType = sourceFileType; }
    public Integer getSourceTextLength() { return sourceTextLength; }
    public void setSourceTextLength(Integer sourceTextLength) { this.sourceTextLength = sourceTextLength; }
}
