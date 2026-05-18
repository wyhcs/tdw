package com.ruoyi.tdw.ai.provider;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    private static final Pattern CONTENT_TEXT_START_PATTERN = Pattern.compile("\"text\"\\s*:\\s*\"");

    private static final Pattern MARKDOWN_HEADING_PATTERN = Pattern.compile("^(#{1,6})\\s+(.+)$");

    @Autowired
    private ObjectMapper objectMapper;

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
        return generateContentBlocks(request, null);
    }

    @Override
    public GenerateContentAiResponse generateContentBlocks(GenerateContentAiRequest request, Consumer<String> contentTextConsumer)
    {
        return generateContentBlocks(request, contentTextConsumer, null);
    }

    @Override
    public GenerateContentAiResponse generateContentBlocks(GenerateContentAiRequest request, Consumer<String> contentTextConsumer, Consumer<Map<String, Object>> streamStatusConsumer)
    {
        String prompt = request == null ? "" : StringUtils.defaultIfBlank(request.getFinalPrompt(), request.getRequirement());
        if (StringUtils.isBlank(prompt)) {
            throw new IllegalArgumentException("content generation prompt is blank");
        }
        try {
            ContentTextStreamAdapter adapter = contentTextConsumer == null ? null : new ContentTextStreamAdapter(contentTextConsumer);
            String answer = callChatCompletion(prompt, adapter == null ? null : adapter::accept, streamStatusConsumer);
            if (adapter != null) {
                adapter.flush();
            }
            return parseContentResponse(answer);
        } catch (RuntimeException e) {
            log.warn("正文生成大模型调用失败，mock 兜底已禁用。promptKey={}",
                    request == null ? "" : request.getPromptKey(), e);
            emitStreamStatus(streamStatusConsumer, "error", "message", e.getMessage());
            throw e;
        }
    }

    private GenerateContentAiResponse parseContentResponse(String answer)
    {
        String value = StringUtils.defaultString(answer).trim();
        String json = extractJson(value);
        try {
            GenerateContentAiResponse response = objectMapper.readValue(json, GenerateContentAiResponse.class);
            if (response.getBlocks() != null && !response.getBlocks().isEmpty()) {
                return response;
            }
        } catch (Exception e) {
            log.debug("正文生成结果不是结构化内容块，按纯文本处理：{}", truncate(value, 200));
        }
        return textContentResponse(value);
    }

    private GenerateContentAiResponse textContentResponse(String text)
    {
        GenerateContentAiResponse response = new GenerateContentAiResponse();
        GenerateContentAiResponse.ContentBlock block = new GenerateContentAiResponse.ContentBlock();
        block.setContentType(1);
        Map<String, Object> content = new LinkedHashMap<String, Object>();
        content.put("text", StringUtils.defaultString(text));
        Map<String, Object> format = new LinkedHashMap<String, Object>();
        format.put("fontSize", 14);
        format.put("bold", false);
        content.put("format", format);
        block.setContent(content);
        List<GenerateContentAiResponse.ContentBlock> blocks = new ArrayList<GenerateContentAiResponse.ContentBlock>();
        blocks.add(block);
        response.setBlocks(blocks);
        return response;
    }

    @Override
    public String optimizeContent(GenerateContentAiRequest request)
    {
        return callChatCompletion(buildContentOperationPrompt(request, "请在保留核心含义的基础上改写并润色以下内容，直接输出改写后的正文："));
    }

    @Override
    public String expandContent(GenerateContentAiRequest request)
    {
        return callChatCompletion(buildContentOperationPrompt(request, "请扩写以下内容，使表达更完整、论述更充分，直接输出扩写后的正文："));
    }

    @Override
    public String shortenContent(GenerateContentAiRequest request)
    {
        return callChatCompletion(buildContentOperationPrompt(request, "请压缩以下内容，保留关键信息，直接输出精简后的正文："));
    }

    @Override
    public String generateTenderResponse(GenerateContentAiRequest request)
    {
        return callChatCompletion(buildContentOperationPrompt(request, "请根据以下要求生成投标响应内容，直接输出正文："));
    }

    @Override
    public String checkQuality(GenerateContentAiRequest request)
    {
        return callChatCompletion(buildContentOperationPrompt(request, "请检查以下标书内容的质量问题，并给出简洁的修改建议："));
    }

    @Override
    public String buildDuplicateText(GenerateContentAiRequest request) throws JsonProcessingException
    {
        return callChatCompletion(buildContentOperationPrompt(request, "请整理以下内容用于查重比对，直接输出整理后的文本："));
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
        return callChatCompletion(prompt, onContent, null);
    }

    private String callChatCompletion(String prompt, Consumer<String> onContent, Consumer<Map<String, Object>> streamStatusConsumer)
    {
        if (StringUtils.isBlank(apiKey)) {
            throw new IllegalStateException("大模型API Key未配置，请设置 TDW_GLM_API_KEY 或 tdw.ai.glm.api-key");
        }
        if (StringUtils.isBlank(prompt)) {
            return "";
        }

        List<Message> messages = new ArrayList<Message>();
        messages.add(new Message("user", prompt));
        boolean requestStream = stream || onContent != null;
        CompletionRequest completionRequest = new CompletionRequest(model, messages, requestStream, enableThinking);
        emitStreamStatus(streamStatusConsumer, "request", "provider", "glm", "model", model, "stream", requestStream, "apiUrl", apiUrl);

        try {
            String requestBody = objectMapper.writeValueAsString(completionRequest);
            Request httpRequest = new Request.Builder()
                    .url(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", requestStream ? "text/event-stream" : "application/json")
                    .post(RequestBody.create(requestBody, JSON))
                    .build();

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(connectTimeoutSeconds, TimeUnit.SECONDS)
                    .readTimeout(readTimeoutSeconds, TimeUnit.SECONDS)
                    .writeTimeout(readTimeoutSeconds, TimeUnit.SECONDS)
                    .build();

            try (Response response = client.newCall(httpRequest).execute()) {
                emitStreamStatus(streamStatusConsumer, "response", "code", response.code(),
                        "contentType", response.header("Content-Type", ""));
                if (response.body() == null) {
                    emitStreamStatus(streamStatusConsumer, "body_empty");
                    return "";
                }
                if (!response.isSuccessful()) {
                    String responseBody = response.body().string();
                    emitStreamStatus(streamStatusConsumer, "http_error", "code", response.code(),
                            "body", truncate(responseBody, 500));
                    throw new IllegalStateException("大模型请求失败：" + response.code() + "，" + truncate(responseBody, 500));
                }
                if (requestStream) {
                    return readStreamResponse(response, onContent, streamStatusConsumer);
                }
                String responseBody = response.body().string();
                emitStreamStatus(streamStatusConsumer, "json_done", "chars", responseBody.length());
                return parseJsonResponse(responseBody, onContent);
            }
        } catch (JsonProcessingException e) {
            emitStreamStatus(streamStatusConsumer, "error", "message", e.getMessage());
            throw new IllegalStateException("构造大模型请求失败：" + e.getMessage(), e);
        } catch (IOException e) {
            emitStreamStatus(streamStatusConsumer, "error", "message", e.getMessage());
            throw new IllegalStateException("大模型请求异常：" + e.getMessage(), e);
        }
    }

    private String readStreamResponse(Response response, Consumer<String> onContent, Consumer<Map<String, Object>> streamStatusConsumer) throws IOException
    {
        StringBuilder answerContent = new StringBuilder();
        long startedAt = System.currentTimeMillis();
        int chunkCount = 0;
        int charCount = 0;
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
                chunkCount++;
                charCount += json.length();
                if (chunkCount <= 5 || chunkCount % 20 == 0) {
                    emitStreamStatus(streamStatusConsumer, "chunk", "count", chunkCount, "length", json.length());
                }
                appendChunkContent(answerContent, json, onContent);
            }
        }
        emitStreamStatus(streamStatusConsumer, "stream_done", "chunks", chunkCount,
                "chars", charCount, "durationMs", System.currentTimeMillis() - startedAt);
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

    private void emitStreamStatus(Consumer<Map<String, Object>> consumer, String stage, Object... values)
    {
        if (consumer == null) {
            return;
        }
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("stage", stage);
        if (values != null) {
            for (int i = 0; i + 1 < values.length; i += 2) {
                payload.put(String.valueOf(values[i]), values[i + 1]);
            }
        }
        consumer.accept(payload);
    }

    private String buildContentOperationPrompt(GenerateContentAiRequest request, String instruction)
    {
        StringBuilder prompt = new StringBuilder(StringUtils.defaultString(instruction));
        appendPromptPart(prompt, "项目名称", request == null ? null : request.getProjectName());
        appendPromptPart(prompt, "目录标题", request == null ? null : request.getOutlineTitle());
        appendPromptPart(prompt, "招标要求", request == null ? null : request.getRequirement());
        appendPromptPart(prompt, "已有内容", request == null ? null : request.getExistingContent());
        appendPromptPart(prompt, "知识库参考", request == null ? null : request.getKnowledgeContext());
        appendPromptPart(prompt, "完整提示词", request == null ? null : request.getFinalPrompt());
        return prompt.toString();
    }

    private void appendPromptPart(StringBuilder prompt, String label, String value)
    {
        if (StringUtils.isBlank(value)) {
            return;
        }
        prompt.append("\n\n【").append(label).append("】\n").append(value);
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
            log.warn("大模型大纲解析失败，mock 兜底已禁用。原始响应：{}", truncate(answer, 500));
        }
        throw new IllegalStateException("outline model response cannot be parsed");
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

    private class ContentTextStreamAdapter
    {
        private final Consumer<String> consumer;
        private final StringBuilder buffer = new StringBuilder();
        private boolean textStarted = false;
        private boolean textComplete = false;
        private boolean escaping = false;
        private boolean plainMode = false;
        private int scanIndex = 0;

        private ContentTextStreamAdapter(Consumer<String> consumer)
        {
            this.consumer = consumer;
        }

        private void accept(String chunk)
        {
            if (StringUtils.isEmpty(chunk) || textComplete) {
                return;
            }
            buffer.append(chunk);
            emitText();
        }

        private void flush()
        {
            emitText();
        }

        private void emitText()
        {
            if (consumer == null || textComplete) {
                return;
            }
            if (!textStarted) {
                if (isPlainTextAnswer()) {
                    textStarted = true;
                    plainMode = true;
                    scanIndex = 0;
                } else {
                    Matcher matcher = CONTENT_TEXT_START_PATTERN.matcher(buffer);
                    if (!matcher.find()) {
                        return;
                    }
                    textStarted = true;
                    scanIndex = matcher.end();
                }
            }

            if (plainMode) {
                if (scanIndex < buffer.length()) {
                    String output = buffer.substring(scanIndex);
                    scanIndex = buffer.length();
                    if (StringUtils.isNotEmpty(output)) {
                        consumer.accept(output);
                    }
                }
                return;
            }

            StringBuilder output = new StringBuilder();
            while (scanIndex < buffer.length()) {
                char value = buffer.charAt(scanIndex++);
                if (escaping) {
                    appendEscaped(output, value);
                    escaping = false;
                    continue;
                }
                if (value == '\\') {
                    escaping = true;
                    continue;
                }
                if (value == '"') {
                    textComplete = true;
                    break;
                }
                output.append(value);
            }
            if (output.length() > 0) {
                consumer.accept(output.toString());
            }
        }

        private boolean isPlainTextAnswer()
        {
            for (int i = 0; i < buffer.length(); i++) {
                char value = buffer.charAt(i);
                if (Character.isWhitespace(value)) {
                    continue;
                }
                return value != '{' && value != '[' && value != '`';
            }
            return false;
        }

        private void appendEscaped(StringBuilder output, char value)
        {
            if (value == 'n') {
                output.append('\n');
            } else if (value == 'r') {
                output.append('\n');
            } else if (value == 't') {
                output.append('\t');
            } else {
                output.append(value);
            }
        }
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
