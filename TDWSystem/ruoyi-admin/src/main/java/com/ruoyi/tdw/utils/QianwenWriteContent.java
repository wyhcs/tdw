package com.ruoyi.tdw.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class QianwenWriteContent {

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

    public static String getQianwenContent (String projectName, String nodeTitle, int wordLimit){

        String apiKey = getApiKey();
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("apiKey not set");
            return "";
        }

//        String prompt = contentPromptTemplate
//                .replace("{projectName}", projectName)
//                .replace("{nodeTitle}", nodeTitle)
//                .replace("{wordLimit}", String.valueOf(wordLimit));
        String prompt = buildPrompt(projectName, nodeTitle, wordLimit);

        System.out.println(prompt);
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("user", prompt));

        CompletionRequest request = new CompletionRequest("Qwen3-32B", messages, false);

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
    /**
     * 构建千问大模型的提示词，要求大模型严格返回结构化的三级大纲
     */
    private static  String buildPrompt(String projectName, String nodeTitle, int wordLimit) {
        return "你是一个专业的标书内容生成专家，现在要为标书项目「" + projectName + "」的大纲节点「" + nodeTitle + "」生成对应的专业内容。\n" +
                "要求如下：\n" +
                "1. 文本内容的字数控制在 " + wordLimit + " 字左右，专业、正式，符合标书的写作规范，不能有口语化内容。\n" +
                "2. 【核心要求】优先使用文本块！表格和图片不是必须的，**绝对不要为了凑格式强行添加表格或图片！**\n" +
                " - 只有当内容天然适合用表格展示的时候（比如清单、参数对比、进度计划、人员配置表等），才使用表格块。\n" +
                " - 只有当内容确实需要配图的时候（比如系统架构图、业务流程图等），才使用图片块。\n" +
                " - 大部分普通的描述性内容（比如背景介绍、政策说明、需求分析、意义阐述等），只需要输出纯文本就足够了，不要硬加多余的表格！\n" +
                "3. 你可以根据内容的实际需要，将内容拆分为多个内容块，支持三种类型，严格按照要求输出：\n" +
                " - 文本块：content_type=1，content是文本的JSON，包含text字段，存储段落文本。\n" +
                " - 表格块：content_type=2，content是表格的JSON，包含name(表格名称)、headers(表头数组)、rows(行数据数组)。\n" +
                " - 图片块：content_type=3，content是图片的JSON，包含name(图片名称)、url(图片访问地址)、width(宽度)、height(高度)。\n" +
                "4. 每个块必须给出 sort_order，就是这个块在当前节点下的显示顺序，从1开始递增，不能重复。\n" +
                "5. 输出必须是严格的JSON格式，不能有任何额外的解释、markdown标记、注释，只能输出纯JSON。\n" +
                "6. 注意：blocks数组的长度没有限制，可以只有1个元素（大部分情况都是这样），也可以有多个，完全根据内容的实际需要来，不要强行凑数量！\n" +
                "\n" +
                "示例1：普通描述性内容（只需要文本）的输出格式：\n" +
                "{\n" +
                "\"blocks\": [\n" +
                "  {\n" +
                "    \"sort_order\": 1,\n" +
                "    \"content_type\": 1,\n" +
                "    \"content\": {\"text\": \"这里是完整的段落文本，不需要加任何表格\"}\n" +
                "  }\n" +
                "]\n" +
                "}\n" +
                "示例2：需要表格的内容的输出格式：\n" +
                "{\n" +
                "\"blocks\": [\n" +
                "  {\n" +
                "    \"sort_order\": 1,\n" +
                "    \"content_type\": 1,\n" +
                "    \"content\": {\"text\": \"本项目的硬件配置清单如下：\"}\n" +
                "  },\n" +
                "  {\n" +
                "    \"sort_order\": 2,\n" +
                "    \"content_type\": 2,\n" +
                "    \"content\": {\"name\": \"硬件配置清单\", \"headers\": [\"列1\", \"列2\"], \"rows\": [[\"行1列1\", \"行1列2\"]]}\n" +
                "  }\n" +
                "]\n" +
                "}\n" +
                "严格按照这个格式输出，不要加任何其他内容！";
    }

    private static String getApiKey() {
        String apiKey = System.getProperty("tdw.ai.glm.api-key");
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = System.getenv("TDW_GLM_API_KEY");
        }
        return apiKey;
    }
}
