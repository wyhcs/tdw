package com.ruoyi.tdw.service;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.tdw.domain.TdwKnowledgeBase;
import com.ruoyi.tdw.domain.TdwKnowledgeChunk;
import com.ruoyi.tdw.domain.TdwKnowledgeFile;

public interface ITdwKnowledgeService
{
    List<TdwKnowledgeBase> selectKnowledgeBaseList(TdwKnowledgeBase query);
    TdwKnowledgeBase selectKnowledgeBaseById(Long knowledgeId);
    int insertKnowledgeBase(TdwKnowledgeBase knowledgeBase);
    int renameKnowledgeBase(TdwKnowledgeBase knowledgeBase);
    int deleteKnowledgeBase(Long knowledgeId);

    List<TdwKnowledgeFile> selectKnowledgeFileList(TdwKnowledgeFile query);
    TdwKnowledgeFile uploadKnowledgeFile(MultipartFile file, Long knowledgeId, String fileUsage, String isTemplate);
    int parseKnowledgeFile(Long knowledgeFileId);
    int extractImagesMock(Long knowledgeFileId);
    int renameKnowledgeFile(TdwKnowledgeFile knowledgeFile);
    int deleteKnowledgeFile(Long knowledgeFileId);

    List<TdwKnowledgeFile> selectTemplateFiles(Long knowledgeId);
    List<TdwKnowledgeChunk> selectKnowledgeChunks(TdwKnowledgeChunk query);
    String buildKnowledgeContext(List<Long> knowledgeFileIds, List<Long> knowledgeChunkIds);
}
