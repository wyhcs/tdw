package com.ruoyi.tdw.mapper;

import java.util.List;
import java.util.Map;

import com.ruoyi.tdw.domain.TdwOutlines;
import org.apache.ibatis.annotations.Param;

/**
 * 大纲节点Mapper接口
 * 
 * @author ruoyi
 * @date 2026-03-28
 */
public interface TdwOutlinesMapper 
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
     * 删除大纲节点
     * 
     * @param id 大纲节点主键
     * @return 结果
     */
    public int deleteTdwOutlinesById(Long id);

    /**
     * 批量删除大纲节点
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTdwOutlinesByIds(Long[] ids);

    void batchInsert(List<TdwOutlines> outlines);

    void batchUpdateParentId(List<TdwOutlines> outlines);

    List<TdwOutlines> selectTdwOutlinesByLevel(@Param("bidId") long bidId, @Param("level") int level);

    List<TdwOutlines> selectContentTitleOutlinesByAncestor(@Param("outlineId") Long outlineId);

    List<TdwOutlines> selectOutlinesByBidId(@Param("bidId") Long bidId);

    List<TdwOutlines> selectSiblings(@Param("bidId") Long bidId, @Param("parentId") Long parentId);

    Integer selectMaxSortNum(@Param("bidId") Long bidId, @Param("parentId") Long parentId);

    TdwOutlines selectLevelSortNumById(Long id);

    int updateOutlineTitle(@Param("id") Long id, @Param("title") String title);

    int updateParentAndSort(@Param("id") Long id, @Param("parentId") Long parentId, @Param("sortNum") Integer sortNum);

    int updateSortNum(@Param("id") Long id, @Param("sortNum") Integer sortNum);

    void updateOutlineLeval1(@Param("bidId") long bidId, @Param("sortNum") int sortNum);

    void updateOutlineLeval2And3(@Param("parentId") Long parentId, @Param("sortNum") int sortNum);

    void updateOutlineLeval2And3Delete(@Param("parentId") Long parentId, @Param("sortNum") int sortNum);

    void updateOutlineLeval1Delete(@Param("bidId") long bidId, @Param("sortNum") int sortNum);

    void batchDeleteByIds(List<Long> allSubNodeIds);

    List<Long> selectIdsByBidId(Long bidId);
}
