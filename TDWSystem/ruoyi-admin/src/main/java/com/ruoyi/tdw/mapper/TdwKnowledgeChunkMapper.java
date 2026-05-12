package com.ruoyi.tdw.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.tdw.domain.TdwKnowledgeChunk;

public interface TdwKnowledgeChunkMapper
{
    List<TdwKnowledgeChunk> selectKnowledgeChunkList(TdwKnowledgeChunk query);
    List<TdwKnowledgeChunk> selectChunksByFileIds(@Param("fileIds") List<Long> fileIds);
    List<TdwKnowledgeChunk> selectChunksByIds(@Param("chunkIds") List<Long> chunkIds);
    int insertKnowledgeChunk(TdwKnowledgeChunk chunk);
    int deleteChunksByFileId(@Param("knowledgeFileId") Long knowledgeFileId);
    int deleteChunksByKnowledgeId(@Param("knowledgeId") Long knowledgeId);
}
