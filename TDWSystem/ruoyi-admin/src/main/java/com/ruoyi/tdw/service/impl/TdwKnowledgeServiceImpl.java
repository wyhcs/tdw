package com.ruoyi.tdw.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.tdw.ai.prompt.TdwPromptTemplateService;
import com.ruoyi.tdw.ai.service.TdwAiService;
import com.ruoyi.tdw.domain.TdwKnowledgeBase;
import com.ruoyi.tdw.domain.TdwKnowledgeChunk;
import com.ruoyi.tdw.domain.TdwKnowledgeFile;
import com.ruoyi.tdw.mapper.TdwKnowledgeBaseMapper;
import com.ruoyi.tdw.mapper.TdwKnowledgeChunkMapper;
import com.ruoyi.tdw.mapper.TdwKnowledgeFileMapper;
import com.ruoyi.tdw.service.ITdwKnowledgeService;
import com.ruoyi.tdw.utils.FileParser;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TdwKnowledgeServiceImpl implements ITdwKnowledgeService
{
    private static final long MAX_KNOWLEDGE_FILE_SIZE = 100 * 1024 * 1024L;
    private static final String[] KNOWLEDGE_FILE_EXTENSIONS = new String[] { "doc", "docx", "pdf" };

    @Autowired
    private TdwKnowledgeBaseMapper knowledgeBaseMapper;
    @Autowired
    private TdwKnowledgeFileMapper knowledgeFileMapper;
    @Autowired
    private TdwKnowledgeChunkMapper knowledgeChunkMapper;
    @Autowired
    private TdwAiService aiService;
    @Autowired
    private TdwPromptTemplateService promptTemplateService;
    @Autowired
    private ObjectMapper objectMapper;

    @Value("${tdw.knowledge.core-parse-prompt-path:prompts/knowledge-core-parse.md}")
    private String coreParsePromptPath;

    @Value("${tdw.knowledge.max-parse-chars:60000}")
    private int maxParseChars;

    @Override
    public List<TdwKnowledgeBase> selectKnowledgeBaseList(TdwKnowledgeBase query)
    {
        return knowledgeBaseMapper.selectKnowledgeBaseList(query);
    }

    @Override
    public TdwKnowledgeBase selectKnowledgeBaseById(Long knowledgeId)
    {
        return knowledgeBaseMapper.selectKnowledgeBaseById(knowledgeId);
    }

    @Override
    public int insertKnowledgeBase(TdwKnowledgeBase knowledgeBase)
    {
        if (knowledgeBase.getKnowledgeName() == null || knowledgeBase.getKnowledgeName().trim().length() == 0) {
            throw new IllegalArgumentException("知识库名称不能为空");
        }
        knowledgeBase.setStatus(knowledgeBase.getStatus() == null ? "normal" : knowledgeBase.getStatus());
        knowledgeBase.setFileCount(0);
        knowledgeBase.setCreateTime(DateUtils.getNowDate());
        return knowledgeBaseMapper.insertKnowledgeBase(knowledgeBase);
    }

    @Override
    public int renameKnowledgeBase(TdwKnowledgeBase knowledgeBase)
    {
        if (knowledgeBase.getKnowledgeId() == null) {
            throw new IllegalArgumentException("knowledgeId不能为空");
        }
        if (knowledgeBase.getKnowledgeName() == null || knowledgeBase.getKnowledgeName().trim().length() == 0) {
            throw new IllegalArgumentException("知识库名称不能为空");
        }
        knowledgeBase.setUpdateTime(DateUtils.getNowDate());
        return knowledgeBaseMapper.updateKnowledgeBase(knowledgeBase);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteKnowledgeBase(Long knowledgeId)
    {
        knowledgeChunkMapper.deleteChunksByKnowledgeId(knowledgeId);
        knowledgeFileMapper.deleteKnowledgeFilesByKnowledgeId(knowledgeId);
        return knowledgeBaseMapper.deleteKnowledgeBaseById(knowledgeId);
    }

    @Override
    public List<TdwKnowledgeFile> selectKnowledgeFileList(TdwKnowledgeFile query)
    {
        return knowledgeFileMapper.selectKnowledgeFileList(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TdwKnowledgeFile uploadKnowledgeFile(MultipartFile file, Long knowledgeId, String fileUsage, String isTemplate)
    {
        if (knowledgeBaseMapper.selectKnowledgeBaseById(knowledgeId) == null) {
            throw new IllegalArgumentException("知识库不存在");
        }
        validateUploadFile(file, KNOWLEDGE_FILE_EXTENSIONS, MAX_KNOWLEDGE_FILE_SIZE);
        String fileName = file.getOriginalFilename();
        String fileUrl = saveKnowledgeFile(file);
        TdwKnowledgeFile knowledgeFile = new TdwKnowledgeFile();
        knowledgeFile.setKnowledgeId(knowledgeId);
        knowledgeFile.setFileName(fileName);
        knowledgeFile.setOriginalName(fileName);
        knowledgeFile.setFileUrl(fileUrl);
        knowledgeFile.setFileType(resolveFileType(fileName));
        knowledgeFile.setFileSize(file.getSize());
        knowledgeFile.setFileUsage(fileUsage == null || fileUsage.trim().length() == 0 ? "material" : fileUsage);
        knowledgeFile.setIsTemplate("1".equals(isTemplate) || "true".equalsIgnoreCase(isTemplate) ? "1" : "0");
        knowledgeFile.setParseStatus("uploaded");
        knowledgeFile.setImageStatus("none");
        knowledgeFile.setChunkCount(0);
        knowledgeFile.setCreateTime(DateUtils.getNowDate());
        knowledgeFileMapper.insertKnowledgeFile(knowledgeFile);
        refreshFileCount(knowledgeId);
        return knowledgeFile;
    }

    @Override
    public int parseKnowledgeFile(Long knowledgeFileId)
    {
        TdwKnowledgeFile file = requireFile(knowledgeFileId);
        file.setParseStatus("parsing");
        file.setUpdateTime(DateUtils.getNowDate());
        knowledgeFileMapper.updateKnowledgeFile(file);

        try {
            String documentText = readUploadedFileText(file);
            String prompt = buildCoreParsePrompt(file, documentText);
            String answer = aiService.extractText(prompt, documentText, "knowledge-core-parse");
            List<KnowledgeParseChunk> chunks = parseAiChunks(answer);
            if (chunks.isEmpty()) {
                chunks.add(new KnowledgeParseChunk("核心内容", StringUtils.abbreviate(documentText, 1800), "text"));
            }

            knowledgeChunkMapper.deleteChunksByFileId(knowledgeFileId);
            int index = 1;
            for (KnowledgeParseChunk chunk : chunks) {
                insertChunk(file, index++, chunk.getTitle(), chunk.getContent(), chunk.getType());
            }
            file.setParseStatus("success");
            file.setChunkCount(chunks.size());
            file.setRemark("");
        } catch (Exception e) {
            file.setParseStatus("fail");
            file.setRemark(StringUtils.abbreviate(e.getMessage(), 480));
            file.setUpdateTime(DateUtils.getNowDate());
            knowledgeFileMapper.updateKnowledgeFile(file);
            throw new IllegalStateException("知识库文件解析失败：" + e.getMessage(), e);
        }
        file.setUpdateTime(DateUtils.getNowDate());
        return knowledgeFileMapper.updateKnowledgeFile(file);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int extractImagesMock(Long knowledgeFileId)
    {
        TdwKnowledgeFile file = requireFile(knowledgeFileId);
        insertChunk(file, 99, "抽图结果", "Mock抽图：生成1张系统架构图说明，可用于内容结构图或Mermaid生成。", "image");
        file.setImageStatus("success");
        file.setUpdateTime(DateUtils.getNowDate());
        return knowledgeFileMapper.updateKnowledgeFile(file);
    }

    @Override
    public int renameKnowledgeFile(TdwKnowledgeFile knowledgeFile)
    {
        if (knowledgeFile == null || knowledgeFile.getKnowledgeFileId() == null) {
            throw new IllegalArgumentException("knowledgeFileId不能为空");
        }
        if (StringUtils.isBlank(knowledgeFile.getFileName())) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        TdwKnowledgeFile old = requireFile(knowledgeFile.getKnowledgeFileId());
        old.setFileName(knowledgeFile.getFileName());
        old.setUpdateTime(DateUtils.getNowDate());
        return knowledgeFileMapper.updateKnowledgeFile(old);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteKnowledgeFile(Long knowledgeFileId)
    {
        TdwKnowledgeFile file = requireFile(knowledgeFileId);
        knowledgeChunkMapper.deleteChunksByFileId(knowledgeFileId);
        int rows = knowledgeFileMapper.deleteKnowledgeFileById(knowledgeFileId);
        refreshFileCount(file.getKnowledgeId());
        return rows;
    }

    @Override
    public List<TdwKnowledgeFile> selectTemplateFiles(Long knowledgeId)
    {
        TdwKnowledgeFile query = new TdwKnowledgeFile();
        query.setKnowledgeId(knowledgeId);
        return knowledgeFileMapper.selectTemplateFiles(query);
    }

    @Override
    public List<TdwKnowledgeChunk> selectKnowledgeChunks(TdwKnowledgeChunk query)
    {
        return knowledgeChunkMapper.selectKnowledgeChunkList(query);
    }

    @Override
    public String buildKnowledgeContext(List<Long> knowledgeFileIds, List<Long> knowledgeChunkIds)
    {
        List<TdwKnowledgeChunk> chunks = new ArrayList<TdwKnowledgeChunk>();
        if (knowledgeFileIds != null && !knowledgeFileIds.isEmpty()) {
            chunks.addAll(knowledgeChunkMapper.selectChunksByFileIds(knowledgeFileIds));
        }
        if (knowledgeChunkIds != null && !knowledgeChunkIds.isEmpty()) {
            chunks.addAll(knowledgeChunkMapper.selectChunksByIds(knowledgeChunkIds));
        }
        return chunks.stream()
                .map(item -> item.getChunkTitle() + "：" + item.getChunkContent())
                .limit(12)
                .collect(Collectors.joining("\n"));
    }

    private void validateUploadFile(MultipartFile file, String[] extensions, long maxSize)
    {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        String extension = resolveFileType(file.getOriginalFilename());
        boolean allowed = false;
        for (String item : extensions) {
            if (item.equalsIgnoreCase(extension)) {
                allowed = true;
                break;
            }
        }
        if (!allowed) {
            throw new IllegalArgumentException("仅支持 " + String.join("/", extensions) + " 格式文件");
        }
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("单个文件大小不能超过100MB");
        }
    }

    private String saveKnowledgeFile(MultipartFile file)
    {
        String uploadDir = RuoYiConfig.getUploadPath() + File.separator + "knowledge";
        try {
            String fileName = FileUploadUtils.extractFilename(file);
            File desc = FileUploadUtils.getAbsoluteFile(uploadDir, fileName);
            file.transferTo(desc);
            return FileUploadUtils.getPathFileName(uploadDir, fileName);
        } catch (IOException e) {
            throw new IllegalStateException("知识库文件保存失败：" + e.getMessage(), e);
        }
    }

    private String readUploadedFileText(TdwKnowledgeFile file) throws IOException
    {
        File localFile = resolveLocalFile(file.getFileUrl());
        if (localFile == null || !localFile.exists()) {
            throw new IllegalStateException("文件未找到，请重新上传后解析");
        }
        String originalName = StringUtils.defaultIfBlank(file.getOriginalName(), file.getFileName());
        String text = FileParser.parseFileText(localFile, originalName, maxParseChars);
        if (StringUtils.isBlank(text)) {
            throw new IllegalStateException("未读取到可解析的文档文本");
        }
        return text;
    }

    private File resolveLocalFile(String fileUrl)
    {
        if (StringUtils.isBlank(fileUrl)) {
            return null;
        }
        String normalized = fileUrl.replace('\\', '/');
        if (normalized.startsWith(Constants.RESOURCE_PREFIX + "/")) {
            String relative = normalized.substring((Constants.RESOURCE_PREFIX + "/").length()).replace('/', File.separatorChar);
            return new File(RuoYiConfig.getProfile(), relative);
        }
        return new File(normalized);
    }

    private String buildCoreParsePrompt(TdwKnowledgeFile file, String documentText)
    {
        Map<String, String> variables = new HashMap<String, String>();
        variables.put("fileName", StringUtils.defaultString(file.getFileName()));
        variables.put("fileType", StringUtils.defaultString(file.getFileType()));
        variables.put("documentText", StringUtils.defaultString(documentText));
        return promptTemplateService.render(coreParsePromptPath, variables);
    }

    private List<KnowledgeParseChunk> parseAiChunks(String answer)
    {
        List<KnowledgeParseChunk> chunks = new ArrayList<KnowledgeParseChunk>();
        String value = StringUtils.defaultString(answer).trim();
        if (StringUtils.isBlank(value)) {
            return chunks;
        }
        try {
            JsonNode root = objectMapper.readTree(extractJson(value));
            JsonNode items = root.has("chunks") ? root.get("chunks") : root;
            if (items != null && items.isArray()) {
                for (JsonNode item : items) {
                    String title = text(item, "title", "核心内容");
                    String content = text(item, "content", "");
                    String type = text(item, "type", "text");
                    if (StringUtils.isNotBlank(content)) {
                        chunks.add(new KnowledgeParseChunk(StringUtils.abbreviate(title, 80), StringUtils.abbreviate(content, 5000), normalizeChunkType(type)));
                    }
                }
            }
        } catch (Exception ignored) {
            chunks.addAll(parsePlainChunks(value));
        }
        return chunks;
    }

    private List<KnowledgeParseChunk> parsePlainChunks(String answer)
    {
        List<KnowledgeParseChunk> chunks = new ArrayList<KnowledgeParseChunk>();
        String[] blocks = StringUtils.defaultString(answer).split("\\n(?=#{1,4}\\s+|[一二三四五六七八九十]+[、.．])");
        for (String block : blocks) {
            String value = block.replaceFirst("^#{1,4}\\s+", "").trim();
            if (StringUtils.isBlank(value)) {
                continue;
            }
            int end = value.indexOf('\n');
            String title = end > 0 ? value.substring(0, end).trim() : "核心内容";
            String content = end > 0 ? value.substring(end + 1).trim() : value;
            if (StringUtils.isNotBlank(content)) {
                chunks.add(new KnowledgeParseChunk(StringUtils.abbreviate(title, 80), StringUtils.abbreviate(content, 5000), "text"));
            }
            if (chunks.size() >= 8) {
                break;
            }
        }
        return chunks;
    }

    private String extractJson(String answer)
    {
        String value = StringUtils.defaultString(answer).trim();
        if (value.startsWith("```")) {
            value = value.replaceFirst("^```(?:json|JSON)?\\s*", "");
            value = value.replaceFirst("\\s*```$", "");
        }
        int objectStart = value.indexOf('{');
        int objectEnd = value.lastIndexOf('}');
        if (objectStart >= 0 && objectEnd > objectStart) {
            return value.substring(objectStart, objectEnd + 1);
        }
        int arrayStart = value.indexOf('[');
        int arrayEnd = value.lastIndexOf(']');
        if (arrayStart >= 0 && arrayEnd > arrayStart) {
            return value.substring(arrayStart, arrayEnd + 1);
        }
        return value;
    }

    private String text(JsonNode item, String field, String defaultValue)
    {
        JsonNode node = item == null ? null : item.get(field);
        return node == null || node.isNull() ? defaultValue : node.asText(defaultValue);
    }

    private String normalizeChunkType(String type)
    {
        return "image".equalsIgnoreCase(type) ? "image" : "text";
    }

    private TdwKnowledgeFile requireFile(Long knowledgeFileId)
    {
        TdwKnowledgeFile file = knowledgeFileMapper.selectKnowledgeFileById(knowledgeFileId);
        if (file == null) {
            throw new IllegalArgumentException("知识库文件不存在");
        }
        return file;
    }

    private void insertChunk(TdwKnowledgeFile file, int index, String title, String content, String type)
    {
        TdwKnowledgeChunk chunk = new TdwKnowledgeChunk();
        chunk.setKnowledgeId(file.getKnowledgeId());
        chunk.setKnowledgeFileId(file.getKnowledgeFileId());
        chunk.setChunkIndex(index);
        chunk.setChunkTitle(title);
        chunk.setChunkContent(content);
        chunk.setChunkType(type);
        chunk.setCreateTime(DateUtils.getNowDate());
        knowledgeChunkMapper.insertKnowledgeChunk(chunk);
    }

    private void refreshFileCount(Long knowledgeId)
    {
        TdwKnowledgeFile query = new TdwKnowledgeFile();
        query.setKnowledgeId(knowledgeId);
        TdwKnowledgeBase base = new TdwKnowledgeBase();
        base.setKnowledgeId(knowledgeId);
        base.setFileCount(knowledgeFileMapper.selectKnowledgeFileList(query).size());
        base.setUpdateTime(DateUtils.getNowDate());
        knowledgeBaseMapper.updateKnowledgeBase(base);
    }

    private String resolveFileType(String fileName)
    {
        if (fileName == null || !fileName.contains(".")) {
            return "unknown";
        }
        String extension = FilenameUtils.getExtension(fileName);
        if (StringUtils.isBlank(extension)) {
            return "unknown";
        }
        return extension.toLowerCase(Locale.ROOT);
    }

    private static class KnowledgeParseChunk
    {
        private final String title;
        private final String content;
        private final String type;

        private KnowledgeParseChunk(String title, String content, String type)
        {
            this.title = title;
            this.content = content;
            this.type = type;
        }

        public String getTitle()
        {
            return title;
        }

        public String getContent()
        {
            return content;
        }

        public String getType()
        {
            return type;
        }
    }
}
