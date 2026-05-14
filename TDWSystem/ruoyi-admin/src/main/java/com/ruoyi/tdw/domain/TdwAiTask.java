package com.ruoyi.tdw.domain;

import com.ruoyi.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * AI任务对象 tdw_ai_task。
 */
public class TdwAiTask extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private String moduleType;
    private Long bizId;
    private String targetType;
    private Long targetId;
    private String taskType;
    private String taskName;
    private String promptKey;
    private String providerName;
    private String modelName;
    private String status;
    private String streamStatus;
    private Integer progress;
    private String currentOutput;
    private String resultType;
    private Long resultId;
    private Integer inputChars;
    private Integer outputChars;
    private Integer tokensInput;
    private Integer tokensOutput;
    private Integer retryCount;
    private String errorMessage;
    private Date startedTime;
    private Date finishedTime;
    private Long elapsedMs;
    private String delFlag;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getModuleType() { return moduleType; }
    public void setModuleType(String moduleType) { this.moduleType = moduleType; }
    public Long getBizId() { return bizId; }
    public void setBizId(Long bizId) { this.bizId = bizId; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }
    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
    public String getPromptKey() { return promptKey; }
    public void setPromptKey(String promptKey) { this.promptKey = promptKey; }
    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getStreamStatus() { return streamStatus; }
    public void setStreamStatus(String streamStatus) { this.streamStatus = streamStatus; }
    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }
    public String getCurrentOutput() { return currentOutput; }
    public void setCurrentOutput(String currentOutput) { this.currentOutput = currentOutput; }
    public String getResultType() { return resultType; }
    public void setResultType(String resultType) { this.resultType = resultType; }
    public Long getResultId() { return resultId; }
    public void setResultId(Long resultId) { this.resultId = resultId; }
    public Integer getInputChars() { return inputChars; }
    public void setInputChars(Integer inputChars) { this.inputChars = inputChars; }
    public Integer getOutputChars() { return outputChars; }
    public void setOutputChars(Integer outputChars) { this.outputChars = outputChars; }
    public Integer getTokensInput() { return tokensInput; }
    public void setTokensInput(Integer tokensInput) { this.tokensInput = tokensInput; }
    public Integer getTokensOutput() { return tokensOutput; }
    public void setTokensOutput(Integer tokensOutput) { this.tokensOutput = tokensOutput; }
    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Date getStartedTime() { return startedTime; }
    public void setStartedTime(Date startedTime) { this.startedTime = startedTime; }
    public Date getFinishedTime() { return finishedTime; }
    public void setFinishedTime(Date finishedTime) { this.finishedTime = finishedTime; }
    public Long getElapsedMs() { return elapsedMs; }
    public void setElapsedMs(Long elapsedMs) { this.elapsedMs = elapsedMs; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
