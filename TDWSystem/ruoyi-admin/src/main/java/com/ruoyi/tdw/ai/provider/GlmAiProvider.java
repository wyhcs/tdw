package com.ruoyi.tdw.ai.provider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.tdw.ai.dto.GenerateContentAiRequest;
import com.ruoyi.tdw.ai.dto.GenerateContentAiResponse;
import com.ruoyi.tdw.ai.dto.GenerateOutlineAiRequest;
import com.ruoyi.tdw.ai.dto.GenerateOutlineAiResponse;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * GLM compatible chat-completions provider for tender parsing.
 */
@Component
public class GlmAiProvider implements AiProvider
{
    private static final Logger log = LoggerFactory.getLogger(GlmAiProvider.class);

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockAiProvider mockAiProvider;

    @Value("${tdw.ai.glm.api-url:https://llmapi.paratera.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${tdw.ai.glm.api-key:${TDW_GLM_API_KEY:}}")
    private String apiKey;

    @Value("${tdw.ai.glm.model:GLM-4-Flash}")
    private String model;

    @Value("${tdw.ai.glm.stream:true}")
    private boolean stream;

    @Value("${tdw.ai.glm.enable-thinking:false}")
    private boolean enableThinking;

    @Value("${tdw.ai.glm.connect-timeout-seconds:20}")
    private long connectTimeoutSeconds;

    @Value("${tdw.ai.glm.read-timeout-seconds:180}")
    private long readTimeoutSeconds;

    @Value("${tdw.ai.glm.max-prompt-chars:32000}")
    private int maxPromptChars;

    @Override
    public String getName()
    {
        return "glm";
    }

    @Override
    public GenerateOutlineAiResponse generateOutline(GenerateOutlineAiRequest request)
    {
        return mockAiProvider.generateOutline(request);
    }

    @Override
    public GenerateContentAiResponse generateContentBlocks(GenerateContentAiRequest request)
    {
        return mockAiProvider.generateContentBlocks(request);
    }

    @Override
    public String optimizeContent(GenerateContentAiRequest request)
    {
        return mockAiProvider.optimizeContent(request);
    }

    @Override
    public String expandContent(GenerateContentAiRequest request)
    {
        return mockAiProvider.expandContent(request);
    }

    @Override
    public String shortenContent(GenerateContentAiRequest request)
    {
        return mockAiProvider.shortenContent(request);
    }

    @Override
    public String generateTenderResponse(GenerateContentAiRequest request)
    {
        return mockAiProvider.generateTenderResponse(request);
    }

    @Override
    public String checkQuality(GenerateContentAiRequest request)
    {
        return mockAiProvider.checkQuality(request);
    }

    @Override
    public String buildDuplicateText(GenerateContentAiRequest request) throws JsonProcessingException
    {
        return mockAiProvider.buildDuplicateText(request);
    }

    @Override
    public String extractText(String prompt, String inputText, String taskType)
    {
        return callChatCompletion(normalizePrompt(prompt, inputText));
    }

    private String callChatCompletion(String prompt)
    {
        if (StringUtils.isBlank(apiKey)) {
            throw new IllegalStateException("大模型API Key未配置，请设置 TDW_GLM_API_KEY 或 tdw.ai.glm.api-key");
        }
        if (StringUtils.isBlank(prompt)) {
            return "";
        }

        List<Message> messages = new ArrayList<Message>();
        messages.add(new Message("user", prompt));
        CompletionRequest completionRequest = new CompletionRequest(model, messages, stream, enableThinking);

        try {
            String requestBody = objectMapper.writeValueAsString(completionRequest);
            Request httpRequest = new Request.Builder()
                    .url(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", stream ? "text/event-stream" : "application/json")
                    .post(RequestBody.create(requestBody, JSON))
                    .build();

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(connectTimeoutSeconds, TimeUnit.SECONDS)
                    .readTimeout(readTimeoutSeconds, TimeUnit.SECONDS)
                    .writeTimeout(readTimeoutSeconds, TimeUnit.SECONDS)
                    .build();

            try (Response response = client.newCall(httpRequest).execute()) {
                String responseBody = response.body() == null ? "" : response.body().string();
                if (!response.isSuccessful()) {
                    throw new IllegalStateException("大模型请求失败：" + response.code() + "，" + truncate(responseBody, 500));
                }
                return stream ? parseStreamResponse(responseBody) : parseJsonResponse(responseBody);
            }
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("构造大模型请求失败：" + e.getMessage(), e);
        } catch (IOException e) {
            throw new IllegalStateException("大模型请求异常：" + e.getMessage(), e);
        }
    }

    private String parseStreamResponse(String responseBody)
    {
        StringBuilder answerContent = new StringBuilder();
        String[] lines = responseBody.split("\\r?\\n");
        for (String line : lines) {
            String value = line == null ? "" : line.trim();
            if (value.length() == 0 || value.startsWith(":")) {
                continue;
            }
            String json = value.startsWith("data:") ? value.substring(5).trim() : value;
            if ("[DONE]".equals(json)) {
                continue;
            }
            appendChunkContent(answerContent, json);
        }
        return answerContent.toString();
    }

    private String parseJsonResponse(String responseBody)
    {
        StringBuilder answerContent = new StringBuilder();
        appendChunkContent(answerContent, responseBody);
        return answerContent.toString();
    }

    private void appendChunkContent(StringBuilder answerContent, String json)
    {
        try {
            CompletionChunk chunk = objectMapper.readValue(json, CompletionChunk.class);
            if (chunk.getChoices() == null || chunk.getChoices().isEmpty()) {
                return;
            }
            for (Choice choice : chunk.getChoices()) {
                if (choice == null) {
                    continue;
                }
                Delta delta = choice.getDelta();
                if (delta != null && delta.getContent() != null) {
                    answerContent.append(delta.getContent());
                    continue;
                }
                Message message = choice.getMessage();
                if (message != null && message.getContent() != null) {
                    answerContent.append(message.getContent());
                }
            }
        } catch (JsonProcessingException e) {
            log.debug("忽略无法解析的大模型流式片段：{}", truncate(json, 200));
        }
    }

    private String normalizePrompt(String prompt, String inputText)
    {
        String value = StringUtils.defaultString(prompt);
        if (StringUtils.isBlank(value)) {
            value = StringUtils.defaultString(inputText);
        }
        if (maxPromptChars > 0 && value.length() > maxPromptChars) {
            return value.substring(0, maxPromptChars) + "\n\n【以上内容因长度限制已截断】";
        }
        return value;
    }

    private String truncate(String text, int max)
    {
        String value = StringUtils.defaultString(text);
        return value.length() > max ? value.substring(0, max) : value;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message
    {
        private String role;
        private String content;

        public Message()
        {
        }

        public Message(String role, String content)
        {
            this.role = role;
            this.content = content;
        }

        public String getRole()
        {
            return role;
        }

        public void setRole(String role)
        {
            this.role = role;
        }

        public String getContent()
        {
            return content;
        }

        public void setContent(String content)
        {
            this.content = content;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CompletionRequest
    {
        private String model;
        private List<Message> messages;
        private boolean stream;

        @JsonProperty("extra_body")
        private ExtraBody extraBody;

        public CompletionRequest(String model, List<Message> messages, boolean stream, boolean enableThinking)
        {
            this.model = model;
            this.messages = messages;
            this.stream = stream;
            this.extraBody = new ExtraBody(enableThinking);
        }

        public String getModel()
        {
            return model;
        }

        public List<Message> getMessages()
        {
            return messages;
        }

        public boolean isStream()
        {
            return stream;
        }

        public ExtraBody getExtraBody()
        {
            return extraBody;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ExtraBody
    {
        @JsonProperty("enable_thinking")
        private boolean enableThinking;

        public ExtraBody(boolean enableThinking)
        {
            this.enableThinking = enableThinking;
        }

        public boolean isEnableThinking()
        {
            return enableThinking;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CompletionChunk
    {
        private List<Choice> choices;

        public List<Choice> getChoices()
        {
            return choices;
        }

        public void setChoices(List<Choice> choices)
        {
            this.choices = choices;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice
    {
        private Delta delta;
        private Message message;

        public Delta getDelta()
        {
            return delta;
        }

        public void setDelta(Delta delta)
        {
            this.delta = delta;
        }

        public Message getMessage()
        {
            return message;
        }

        public void setMessage(Message message)
        {
            this.message = message;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Delta
    {
        @JsonProperty("reasoning_content")
        private String reasoningContent;
        private String content;

        public String getReasoningContent()
        {
            return reasoningContent;
        }

        public void setReasoningContent(String reasoningContent)
        {
            this.reasoningContent = reasoningContent;
        }

        public String getContent()
        {
            return content;
        }

        public void setContent(String content)
        {
            this.content = content;
        }
    }
}
