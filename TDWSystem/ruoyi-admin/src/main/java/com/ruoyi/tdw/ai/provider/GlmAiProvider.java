package com.ruoyi.tdw.ai.provider;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static final Pattern JSON_TITLE_PATTERN = Pattern.compile("\"level\"\\s*:\\s*(\\d+)\\s*,\\s*\"title\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");

    private static final Pattern MARKDOWN_HEADING_PATTERN = Pattern.compile("^(#{1,6})\\s+(.+)$");

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockAiProvider mockAiProvider;

    @Value("${tdw.ai.glm.api-url:https://llmapi.paratera.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${tdw.ai.glm.api-key}")
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
        return generateOutline(request, null);
    }

    @Override
    public GenerateOutlineAiResponse generateOutline(GenerateOutlineAiRequest request, Consumer<String> outlineMarkdownConsumer)
    {
        StringBuilder prompt = new StringBuilder();
        prompt.append(StringUtils.defaultString(request == null ? "" : request.getRequirement()));
        if (request != null && StringUtils.isNotBlank(request.getKnowledgeContext())) {
            prompt.append("\n\n【知识库参考资料】\n").append(request.getKnowledgeContext());
        }
        OutlineStreamMarkdownAdapter adapter = new OutlineStreamMarkdownAdapter(outlineMarkdownConsumer);
        String answer = callChatCompletion(prompt.toString(), adapter::accept);
        adapter.flush();
        return parseOutlineResponse(answer);
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
        return callChatCompletion(prompt, null);
    }

    private String callChatCompletion(String prompt, Consumer<String> onContent)
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
                if (response.body() == null) {
                    return "";
                }
                if (!response.isSuccessful()) {
                    String responseBody = response.body().string();
                    throw new IllegalStateException("大模型请求失败：" + response.code() + "，" + truncate(responseBody, 500));
                }
                if (stream) {
                    return readStreamResponse(response, onContent);
                }
                String responseBody = response.body().string();
                return parseJsonResponse(responseBody, onContent);
            }
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("构造大模型请求失败：" + e.getMessage(), e);
        } catch (IOException e) {
            throw new IllegalStateException("大模型请求异常：" + e.getMessage(), e);
        }
    }

    private String readStreamResponse(Response response, Consumer<String> onContent) throws IOException
    {
        StringBuilder answerContent = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(response.body().charStream())) {
            String line;
            while ((line = reader.readLine()) != null) {
                String value = line == null ? "" : line.trim();
                if (value.length() == 0 || value.startsWith(":")) {
                    continue;
                }
                String json = value.startsWith("data:") ? value.substring(5).trim() : value;
                if ("[DONE]".equals(json)) {
                    continue;
                }
                appendChunkContent(answerContent, json, onContent);
            }
        }
        return answerContent.toString();
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
        return parseJsonResponse(responseBody, null);
    }

    private String parseJsonResponse(String responseBody, Consumer<String> onContent)
    {
        StringBuilder answerContent = new StringBuilder();
        appendChunkContent(answerContent, responseBody, onContent);
        return answerContent.toString();
    }

    private void appendChunkContent(StringBuilder answerContent, String json)
    {
        appendChunkContent(answerContent, json, null);
    }

    private void appendChunkContent(StringBuilder answerContent, String json, Consumer<String> onContent)
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
                    appendContent(answerContent, delta.getContent(), onContent);
                    continue;
                }
                Message message = choice.getMessage();
                if (message != null && message.getContent() != null) {
                    appendContent(answerContent, message.getContent(), onContent);
                }
            }
        } catch (JsonProcessingException e) {
            log.debug("忽略无法解析的大模型流式片段：{}", truncate(json, 200));
        }
    }

    private void appendContent(StringBuilder answerContent, String content, Consumer<String> onContent)
    {
        answerContent.append(content);
        if (onContent != null && StringUtils.isNotEmpty(content)) {
            onContent.accept(content);
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

    private GenerateOutlineAiResponse parseOutlineResponse(String answer)
    {
        String json = extractJson(answer);
        try {
            GenerateOutlineAiResponse response = objectMapper.readValue(json, GenerateOutlineAiResponse.class);
            if (response.getNodes() != null && !response.getNodes().isEmpty()) {
                return response;
            }
        } catch (Exception e) {
            GenerateOutlineAiResponse markdownResponse = parseMarkdownOutlineResponse(answer);
            if (markdownResponse.getNodes() != null && !markdownResponse.getNodes().isEmpty()) {
                return markdownResponse;
            }
            log.warn("大模型大纲解析失败，使用mock兜底。原始响应：{}", truncate(answer, 500));
        }
        return mockAiProvider.generateOutline(new GenerateOutlineAiRequest());
    }

    private String extractJson(String answer)
    {
        String value = StringUtils.defaultString(answer).trim();
        if (value.startsWith("```")) {
            value = value.replaceFirst("^```(?:json|JSON|markdown)?\\s*", "");
            value = value.replaceFirst("\\s*```$", "");
        }
        int objectStart = value.indexOf('{');
        int objectEnd = value.lastIndexOf('}');
        if (objectStart >= 0 && objectEnd > objectStart) {
            return value.substring(objectStart, objectEnd + 1);
        }
        return value;
    }

    private GenerateOutlineAiResponse parseMarkdownOutlineResponse(String answer)
    {
        GenerateOutlineAiResponse response = new GenerateOutlineAiResponse();
        List<GenerateOutlineAiResponse.OutlineNode> roots = new ArrayList<GenerateOutlineAiResponse.OutlineNode>();
        GenerateOutlineAiResponse.OutlineNode[] stack = new GenerateOutlineAiResponse.OutlineNode[4];
        int[] sortNums = new int[4];
        String[] lines = stripMarkdownFence(answer).split("\\r?\\n");
        for (String line : lines) {
            String value = line == null ? "" : line.trim();
            Matcher matcher = MARKDOWN_HEADING_PATTERN.matcher(value);
            if (!matcher.matches()) {
                continue;
            }
            int markdownLevel = matcher.group(1).length();
            int outlineLevel = markdownLevel <= 1 ? 1 : Math.min(markdownLevel - 1, 3);
            String title = matcher.group(2).trim();
            if (StringUtils.isBlank(title)) {
                continue;
            }
            GenerateOutlineAiResponse.OutlineNode node = new GenerateOutlineAiResponse.OutlineNode();
            node.setLevel(outlineLevel);
            node.setTitle(title);
            node.setWordLimit(outlineLevel >= 3 ? 500 : 300);
            sortNums[outlineLevel]++;
            node.setSortNum(sortNums[outlineLevel]);
            for (int i = outlineLevel + 1; i < sortNums.length; i++) {
                sortNums[i] = 0;
                stack[i] = null;
            }
            if (outlineLevel <= 1 || stack[outlineLevel - 1] == null) {
                roots.add(node);
            } else {
                stack[outlineLevel - 1].getChildren().add(node);
            }
            stack[outlineLevel] = node;
        }
        response.setNodes(roots);
        return response;
    }

    private String stripMarkdownFence(String answer)
    {
        String value = StringUtils.defaultString(answer).trim();
        value = value.replaceFirst("^```(?:markdown|MARKDOWN|json|JSON)?\\s*", "");
        value = value.replaceFirst("\\s*```$", "");
        return value.replaceAll("(?m)^```\\s*$", "").replaceAll("(?m)^markdown\\s*$", "");
    }

    private String toHeadingMarkdown(int outlineLevel, String title)
    {
        int markdownLevel = Math.min(Math.max(outlineLevel + 1, 2), 4);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < markdownLevel; i++) {
            builder.append('#');
        }
        return builder.append(' ').append(title).append("\n\n").toString();
    }

    private String unescapeJsonText(String value)
    {
        return StringUtils.defaultString(value)
                .replace("\\\"", "\"")
                .replace("\\n", "\n")
                .replace("\\r", "")
                .replace("\\\\", "\\");
    }

    private class OutlineStreamMarkdownAdapter
    {
        private final Consumer<String> consumer;
        private final StringBuilder buffer = new StringBuilder();
        private int emittedJsonTitleCount = 0;
        private int emittedMarkdownHeadingCount = 0;

        private OutlineStreamMarkdownAdapter(Consumer<String> consumer)
        {
            this.consumer = consumer;
        }

        private void accept(String chunk)
        {
            if (StringUtils.isEmpty(chunk)) {
                return;
            }
            buffer.append(chunk);
            emitJsonTitles();
            emitMarkdownHeadings(false);
        }

        private void flush()
        {
            emitJsonTitles();
            emitMarkdownHeadings(true);
        }

        private void emitJsonTitles()
        {
            if (consumer == null) {
                return;
            }
            List<String> headings = new ArrayList<String>();
            Matcher matcher = JSON_TITLE_PATTERN.matcher(buffer);
            while (matcher.find()) {
                int outlineLevel = 1;
                try {
                    outlineLevel = Integer.parseInt(matcher.group(1));
                } catch (NumberFormatException ignored) {
                }
                headings.add(toHeadingMarkdown(outlineLevel, unescapeJsonText(matcher.group(2)).trim()));
            }
            for (int i = emittedJsonTitleCount; i < headings.size(); i++) {
                consumer.accept(headings.get(i));
            }
            emittedJsonTitleCount = headings.size();
        }

        private void emitMarkdownHeadings(boolean includeLastLine)
        {
            if (consumer == null || emittedJsonTitleCount > 0) {
                return;
            }
            String[] lines = stripMarkdownFence(buffer.toString()).split("\\r?\\n", includeLastLine ? -1 : 0);
            int completeLineCount = lines.length;
            if (!includeLastLine && buffer.length() > 0 && buffer.charAt(buffer.length() - 1) != '\n') {
                completeLineCount = Math.max(completeLineCount - 1, 0);
            }
            List<String> headings = new ArrayList<String>();
            for (int i = 0; i < completeLineCount; i++) {
                String value = lines[i] == null ? "" : lines[i].trim();
                Matcher matcher = MARKDOWN_HEADING_PATTERN.matcher(value);
                if (matcher.matches()) {
                    int markdownLevel = Math.min(Math.max(matcher.group(1).length(), 2), 4);
                    StringBuilder heading = new StringBuilder();
                    for (int j = 0; j < markdownLevel; j++) {
                        heading.append('#');
                    }
                    headings.add(heading.append(' ').append(matcher.group(2).trim()).append("\n\n").toString());
                }
            }
            for (int i = emittedMarkdownHeadingCount; i < headings.size(); i++) {
                consumer.accept(headings.get(i));
            }
            emittedMarkdownHeadingCount = headings.size();
        }
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
