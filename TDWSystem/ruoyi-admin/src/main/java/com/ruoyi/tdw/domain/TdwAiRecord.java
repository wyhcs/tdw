package com.ruoyi.tdw.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * AI调用记录对象 tdw_ai_record。
 */
public class TdwAiRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long taskId;
    private String moduleType;
    private Long bizId;
    private String targetType;
    private Long targetId;
    private String taskType;
    private String promptKey;
    private String promptVersion;
    private String systemPrompt;
    private String userPrompt;
    private String finalPrompt;
    private String inputText;
    private String outputText;
    private String outputJson;
    private String requestJson;
    private String responseJson;
    private String providerName;
    private String modelName;
    private Integer tokensInput;
    private Integer tokensOutput;
    private Long elapsedMs;
    private String status;
    private String errorMessage;
    private String delFlag;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
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
    public String getPromptKey() { return promptKey; }
    public void setPromptKey(String promptKey) { this.promptKey = promptKey; }
    public String getPromptVersion() { return promptVersion; }
    public void setPromptVersion(String promptVersion) { this.promptVersion = promptVersion; }
    public String getSystemPrompt() { return systemPrompt; }
    public void setSystemPrompt(String systemPrompt) { this.systemPrompt = systemPrompt; }
    public String getUserPrompt() { return userPrompt; }
    public void setUserPrompt(String userPrompt) { this.userPrompt = userPrompt; }
    public String getFinalPrompt() { return finalPrompt; }
    public void setFinalPrompt(String finalPrompt) { this.finalPrompt = finalPrompt; }
    public String getInputText() { return inputText; }
    public void setInputText(String inputText) { this.inputText = inputText; }
    public String getOutputText() { return outputText; }
    public void setOutputText(String outputText) { this.outputText = outputText; }
    public String getOutputJson() { return outputJson; }
    public void setOutputJson(String outputJson) { this.outputJson = outputJson; }
    public String getRequestJson() { return requestJson; }
    public void setRequestJson(String requestJson) { this.requestJson = requestJson; }
    public String getResponseJson() { return responseJson; }
    public void setResponseJson(String responseJson) { this.responseJson = responseJson; }
    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public Integer getTokensInput() { return tokensInput; }
    public void setTokensInput(Integer tokensInput) { this.tokensInput = tokensInput; }
    public Integer getTokensOutput() { return tokensOutput; }
    public void setTokensOutput(Integer tokensOutput) { this.tokensOutput = tokensOutput; }
    public Long getElapsedMs() { return elapsedMs; }
    public void setElapsedMs(Long elapsedMs) { this.elapsedMs = elapsedMs; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
