package com.ruoyi.tdw.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.ruoyi.tdw.domain.LlmPromptProperties;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;


public class QianwenWriteOutline {

    private static final String API_URL = "https://llmapi.paratera.com/v1/chat/completions";

    private static final ObjectMapper objectMapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    public static class Message {
        private String role;
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() { return role; }
        public String getContent() { return content; }
        public void setRole(String role) { this.role = role; }
        public void setContent(String content) { this.content = content; }
    }

    public static class CompletionRequest {
        private String model;
        private List<Message> messages;
        private boolean stream = true;

        @JsonProperty("extra_body")
        private ExtraBody extraBody;

        public CompletionRequest(String model, List<Message> messages, boolean enableThinking) {
            this.model = model;
            this.messages = messages;
            this.extraBody = new ExtraBody(enableThinking);
        }

        public String getModel() { return model; }
        public List<Message> getMessages() { return messages; }
        public boolean isStream() { return stream; }
        public ExtraBody getExtraBody() { return extraBody; }
    }

    public static class ExtraBody {
        @JsonProperty("enable_thinking")
        private boolean enableThinking;

        public ExtraBody(boolean enableThinking) {
            this.enableThinking = enableThinking;
        }

        public boolean isEnableThinking() { return enableThinking; }
    }

    public static class CompletionChunk {
        private List<Choice> choices;
        private Usage usage;

        public List<Choice> getChoices() { return choices; }
        public Usage getUsage() { return usage; }
    }

    public static class Choice {
        private Delta delta;
        public Delta getDelta() { return delta; }
    }

    public static class Delta {
        @JsonProperty("reasoning_content")
        private String reasoningContent;
        private String content;

        public String getReasoningContent() { return reasoningContent; }
        public String getContent() { return content; }
    }

    public static class Usage {
        @JsonProperty("prompt_tokens")
        private int promptTokens;
        @JsonProperty("completion_tokens")
        private int completionTokens;
        @JsonProperty("total_tokens")
        private int totalTokens;

        public int getPromptTokens() { return promptTokens; }
        public int getCompletionTokens() { return completionTokens; }
        public int getTotalTokens() { return totalTokens; }
    }

    public static String getQianwenOutline(String prompt){
        String apiKey = getApiKey();
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("apiKey not set");
            return "";
        }

//        String prompt = buildPrompt(title);
//        String prompt = buildOutlinePrompt(projectType, projectName, "");

        System.out.println(prompt);

        List<Message> messages = new ArrayList<>();
        messages.add(new Message("user", prompt));

        CompletionRequest request = new CompletionRequest("GLM-4-Flash", messages, false);

        try {
            String requestBody = objectMapper.writeValueAsString(request);

            OkHttpClient client = new OkHttpClient();

            Request httpRequest = new Request.Builder()
                    .url(API_URL)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "text/event-stream")
                    .post(RequestBody.create(requestBody, MediaType.parse("application/json; charset=utf-8")))
                    .build();

            Response response = client.newCall(httpRequest).execute();
            if (!response.isSuccessful()) {
                System.err.println("请求失败：" + response.code());
                System.err.println(response.body().string());
                return "";
            }

            String responseBody = response.body().string();
            String[] lines = responseBody.split("\n");

            AtomicBoolean isAnswering = new AtomicBoolean(false);
            StringBuilder reasoningContent = new StringBuilder();
            StringBuilder answerContent = new StringBuilder();

            for (String line : lines) {
                if (line.trim().isEmpty()) continue;

                try {
                    String jsonStr = line.startsWith("data: ") ? line.substring(6) : line;
                    if (jsonStr.equals("[DONE]")) continue;

                    CompletionChunk chunk = objectMapper.readValue(jsonStr, CompletionChunk.class);

                    if (chunk.getChoices() == null || chunk.getChoices().isEmpty()) {
                        continue;
                    }

                    Delta delta = chunk.getChoices().get(0).getDelta();

                    if (delta.getReasoningContent() != null) {
                        if (!isAnswering.get()) {
                            System.out.print(delta.getReasoningContent());
                        }
                        reasoningContent.append(delta.getReasoningContent());
                    }

                    if (delta.getContent() != null) {
                        if (!isAnswering.get()) {
                            System.out.println("\n" + "==============" + "完整回复" + "================" + "\n");
                            isAnswering.set(true);
                        }
                        System.out.print(delta.getContent());
                        answerContent.append(delta.getContent());
                    }

                } catch (JsonProcessingException e) {
                    // 忽略解析异常
                }
            }
            return answerContent.toString();
        } catch (JsonProcessingException e) {
            System.err.println("构造请求失败：" + e.getMessage());
            return "";
        } catch (IOException e) {
            System.err.println("请求异常：" + e.getMessage());
            return "";
        }

    }

    private static String getApiKey() {
        String apiKey = System.getProperty("tdw.ai.glm.api-key");
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = System.getenv("TDW_GLM_API_KEY");
        }
        return apiKey;
    }


}
