package com.ruoyi.tdw.mapper;

import java.util.List;
import com.ruoyi.tdw.domain.TdwContents;
import org.apache.ibatis.annotations.Param;

/**
 * 内容块，支持文本/格/图片混排，随意增删改Mapper接口
 * 
 * @author ruoyi
 * @date 2026-03-28
 */
public interface TdwContentsMapper 
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

    public List<TdwContents> selectTdwContentsByOutlineId(@Param("outlineId") Long outlineId);

    public Integer selectMaxSortOrder(@Param("outlineId") Long outlineId);

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
     * 删除内容块，支持文本/格/图片混排，随意增删改
     * 
     * @param id 内容块，支持文本/格/图片混排，随意增删改主键
     * @return 结果
     */
    public int deleteTdwContentsById(Long id);

    /**
     * 批量删除内容块，支持文本/格/图片混排，随意增删改
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTdwContentsByIds(Long[] ids);

    void batchDeleteByOutlineIds(List<Long> allSubNodeIds);

    int deleteByOutlineId(@Param("outlineId") Long outlineId);

    int updateSortOrder(@Param("id") Long id, @Param("sortOrder") Integer sortOrder);

    void batchInsert(List<TdwContents> allContents);

    List<TdwContents> getContent(Long id);
}
