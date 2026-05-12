package com.ruoyi.tdw.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.tdw.domain.TdwKnowledgeFile;

public interface TdwKnowledgeFileMapper
{
    List<TdwKnowledgeFile> selectKnowledgeFileList(TdwKnowledgeFile query);
    TdwKnowledgeFile selectKnowledgeFileById(@Param("knowledgeFileId") Long knowledgeFileId);
    int insertKnowledgeFile(TdwKnowledgeFile knowledgeFile);
    int updateKnowledgeFile(TdwKnowledgeFile knowledgeFile);
    int deleteKnowledgeFilesByKnowledgeId(@Param("knowledgeId") Long knowledgeId);
    List<TdwKnowledgeFile> selectTemplateFiles(TdwKnowledgeFile query);
}
