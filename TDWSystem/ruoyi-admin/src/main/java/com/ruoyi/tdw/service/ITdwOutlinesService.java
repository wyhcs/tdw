package com.ruoyi.tdw.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruoyi.tdw.domain.TdwOutlines;
import com.ruoyi.tdw.domain.dto.TdwOutlineGenerateRequest;
import com.ruoyi.tdw.domain.dto.TdwOutlineInsertRequest;
import com.ruoyi.tdw.domain.dto.TdwOutlineMoveRequest;
import com.ruoyi.tdw.domain.dto.TdwOutlineSortRequest;

/**
 * 大纲节点Service接口
 * 
 * @author ruoyi
 * @date 2026-03-28
 */
public interface ITdwOutlinesService 
{
    /**
     * 查询大纲节点
     * 
     * @param id 大纲节点主键
     * @return 大纲节点
     */
    public TdwOutlines selectTdwOutlinesById(Long id);

    /**
     * 查询大纲节点列表
     * 
     * @param tdwOutlines 大纲节点
     * @return 大纲节点集合
     */
    public List<TdwOutlines> selectTdwOutlinesList(TdwOutlines tdwOutlines);

    /**
     * 新增大纲节点
     * 
     * @param tdwOutlines 大纲节点
     * @return 结果
     */
    public int insertTdwOutlines(TdwOutlines tdwOutlines);

    /**
     * 修改大纲节点
     * 
     * @param tdwOutlines 大纲节点
     * @return 结果
     */
    public int updateTdwOutlines(TdwOutlines tdwOutlines);

    /**
     * 批量删除大纲节点
     * 
     * @param ids 需要删除的大纲节点主键集合
     * @return 结果
     */
    public int deleteTdwOutlinesByIds(Long[] ids);

    /**
     * 删除大纲节点信息
     * 
     * @param id 大纲节点主键
     * @return 结果
     */
    public int deleteTdwOutlinesById(Long id);

    Long generateOutline(Long id) throws IOException;

    Long generateOutline(Long id, List<Long> knowledgeFileIds) throws IOException;

    Long generateOutline(TdwOutlineGenerateRequest request) throws IOException;

    List<TdwOutlines> selectOutlineTree(Long bidId);

    int updateOutlineTitle(TdwOutlines tdwOutlines);

    TdwOutlines insertOutlineNode(TdwOutlineInsertRequest request);

    int deleteOutlineById(Long outlineId);

    int moveOutline(TdwOutlineMoveRequest request);

    int sortOutlines(TdwOutlineSortRequest request);

    TdwOutlines selectLevelSortNumById(Long id);

    int insertOutlineLeval1(TdwOutlines tdwOutlines);

    int insertOutlineLeval2(TdwOutlines tdwOutlines);

    int insertOutlineLeval3(TdwOutlines tdwOutlines);

    int deleteOutlineLeval(TdwOutlines tdwOutlines);
}
