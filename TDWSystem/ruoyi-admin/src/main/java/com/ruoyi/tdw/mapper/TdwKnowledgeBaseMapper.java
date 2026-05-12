package com.ruoyi.tdw.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.tdw.domain.TdwKnowledgeBase;

public interface TdwKnowledgeBaseMapper
{
    List<TdwKnowledgeBase> selectKnowledgeBaseList(TdwKnowledgeBase query);
    TdwKnowledgeBase selectKnowledgeBaseById(@Param("knowledgeId") Long knowledgeId);
    int insertKnowledgeBase(TdwKnowledgeBase knowledgeBase);
    int updateKnowledgeBase(TdwKnowledgeBase knowledgeBase);
    int deleteKnowledgeBaseById(@Param("knowledgeId") Long knowledgeId);
}
