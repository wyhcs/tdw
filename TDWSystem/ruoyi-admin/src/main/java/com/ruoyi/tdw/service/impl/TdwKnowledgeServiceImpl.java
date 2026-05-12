package com.ruoyi.tdw.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.tdw.domain.TdwKnowledgeBase;
import com.ruoyi.tdw.domain.TdwKnowledgeChunk;
import com.ruoyi.tdw.domain.TdwKnowledgeFile;
import com.ruoyi.tdw.mapper.TdwKnowledgeBaseMapper;
import com.ruoyi.tdw.mapper.TdwKnowledgeChunkMapper;
import com.ruoyi.tdw.mapper.TdwKnowledgeFileMapper;
import com.ruoyi.tdw.service.ITdwKnowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TdwKnowledgeServiceImpl implements ITdwKnowledgeService
{
    @Autowired
    private TdwKnowledgeBaseMapper knowledgeBaseMapper;
    @Autowired
    private TdwKnowledgeFileMapper knowledgeFileMapper;
    @Autowired
    private TdwKnowledgeChunkMapper knowledgeChunkMapper;

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
        String fileName = file == null ? "mock-template.docx" : file.getOriginalFilename();
        TdwKnowledgeFile knowledgeFile = new TdwKnowledgeFile();
        knowledgeFile.setKnowledgeId(knowledgeId);
        knowledgeFile.setFileName(fileName);
        knowledgeFile.setFileType(resolveFileType(fileName));
        knowledgeFile.setFileSize(file == null ? 0L : file.getSize());
        knowledgeFile.setFileUsage(fileUsage == null || fileUsage.trim().length() == 0 ? "material" : fileUsage);
        knowledgeFile.setIsTemplate("1".equals(isTemplate) || "true".equalsIgnoreCase(isTemplate) ? "1" : "0");
        knowledgeFile.setParseStatus("uploaded");
        knowledgeFile.setImageStatus("none");
        knowledgeFile.setCreateTime(DateUtils.getNowDate());
        knowledgeFileMapper.insertKnowledgeFile(knowledgeFile);
        refreshFileCount(knowledgeId);
        return knowledgeFile;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int parseFileMock(Long knowledgeFileId)
    {
        TdwKnowledgeFile file = requireFile(knowledgeFileId);
        knowledgeChunkMapper.deleteChunksByFileId(knowledgeFileId);
        insertChunk(file, 1, "文档概述", "Mock解析：" + file.getFileName() + " 的总体要求、项目背景和关键目标。", "text");
        insertChunk(file, 2, "技术要求", "Mock解析：提取技术路线、实施方法、质量控制和交付要求。", "text");
        insertChunk(file, 3, "评分关注点", "Mock解析：建议大纲和内容重点响应评分标准、商务条款和实施保障。", "text");
        file.setParseStatus("success");
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
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }
}
