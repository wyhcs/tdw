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
    private String status;
    private Integer totalCount;
    private Integer issueCount;
    private Integer totalTextLength;
    private BigDecimal overallSimilarity;
    private Date startTime;
    private Date finishTime;
    private String delFlag;

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
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }
    public Integer getIssueCount() { return issueCount; }
    public void setIssueCount(Integer issueCount) { this.issueCount = issueCount; }
    public Integer getTotalTextLength() { return totalTextLength; }
    public void setTotalTextLength(Integer totalTextLength) { this.totalTextLength = totalTextLength; }
    public BigDecimal getOverallSimilarity() { return overallSimilarity; }
    public void setOverallSimilarity(BigDecimal overallSimilarity) { this.overallSimilarity = overallSimilarity; }
    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }
    public Date getFinishTime() { return finishTime; }
    public void setFinishTime(Date finishTime) { this.finishTime = finishTime; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
