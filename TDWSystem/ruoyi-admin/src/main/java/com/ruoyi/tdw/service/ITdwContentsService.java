package com.ruoyi.tdw.service;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruoyi.tdw.domain.TdwContents;
import com.ruoyi.tdw.domain.dto.TdwContentGenerateRequest;
import com.ruoyi.tdw.domain.dto.TdwContentSortRequest;

/**
 * 内容块，支持文本/格/图片混排，随意增删改Service接口
 * 
 * @author ruoyi
 * @date 2026-03-28
 */
public interface ITdwContentsService 
{
    /**
     * 查询内容块，支持文本/格/图片混排，随意增删改
     * 
     * @param id 内容块，支持文本/格/图片混排，随意增删改主键
     * @return 内容块，支持文本/格/图片混排，随意增删改
     */
    public TdwContents selectTdwContentsById(Long id);

    /**
     * 查询内容块，支持文本/格/图片混排，随意增删改列表
     * 
     * @param tdwContents 内容块，支持文本/格/图片混排，随意增删改
     * @return 内容块，支持文本/格/图片混排，随意增删改集合
     */
    public List<TdwContents> selectTdwContentsList(TdwContents tdwContents);

    public List<TdwContents> selectTdwContentsByOutlineId(Long outlineId);

    public List<TdwContents> selectTdwContentsByOutlineIds(List<Long> outlineIds);

    /**
     * 新增内容块，支持文本/格/图片混排，随意增删改
     * 
     * @param tdwContents 内容块，支持文本/格/图片混排，随意增删改
     * @return 结果
     */
    public int insertTdwContents(TdwContents tdwContents);

    /**
     * 修改内容块，支持文本/格/图片混排，随意增删改
     * 
     * @param tdwContents 内容块，支持文本/格/图片混排，随意增删改
     * @return 结果
     */
    public int updateTdwContents(TdwContents tdwContents);

    /**
     * 批量删除内容块，支持文本/格/图片混排，随意增删改
     * 
     * @param ids 需要删除的内容块，支持文本/格/图片混排，随意增删改主键集合
     * @return 结果
     */
    public int deleteTdwContentsByIds(Long[] ids);

    /**
     * 删除内容块，支持文本/格/图片混排，随意增删改信息
     * 
     * @param id 内容块，支持文本/格/图片混排，随意增删改主键
     * @return 结果
     */
    public int deleteTdwContentsById(Long id);

    public int sortContents(TdwContentSortRequest request);

    public List<TdwContents> generateContentBlocks(TdwContentGenerateRequest request) throws JsonProcessingException;

    String generateContent(Long id) throws JsonProcessingException;

    String generateContent(Long id, List<Long> knowledgeFileIds, List<Long> knowledgeChunkIds) throws JsonProcessingException;

    List<TdwContents> getContent(Long id);
}
