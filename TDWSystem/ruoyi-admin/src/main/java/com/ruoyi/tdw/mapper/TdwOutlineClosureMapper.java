package com.ruoyi.tdw.mapper;

import java.util.List;
import java.util.Map;

import com.ruoyi.tdw.domain.TdwOutlineClosure;
import com.ruoyi.tdw.service.impl.TdwOutlinesServiceImpl;
import org.apache.ibatis.annotations.Param;

/**
 * 大纲树形闭包关系，存储所有祖先后代关系Mapper接口
 * 
 * @author ruoyi
 * @date 2026-03-28
 */
public interface TdwOutlineClosureMapper 
{
    /**
     * 查询大纲树形闭包关系，存储所有祖先后代关系
     * 
     * @param id 大纲树形闭包关系，存储所有祖先后代关系主键
     * @return 大纲树形闭包关系，存储所有祖先后代关系
     */
    public TdwOutlineClosure selectTdwOutlineClosureById(Long id);

    /**
     * 查询大纲树形闭包关系，存储所有祖先后代关系列表
     * 
     * @param tdwOutlineClosure 大纲树形闭包关系，存储所有祖先后代关系
     * @return 大纲树形闭包关系，存储所有祖先后代关系集合
     */
    public List<TdwOutlineClosure> selectTdwOutlineClosureList(TdwOutlineClosure tdwOutlineClosure);

    /**
     * 新增大纲树形闭包关系，存储所有祖先后代关系
     * 
     * @param tdwOutlineClosure 大纲树形闭包关系，存储所有祖先后代关系
     * @return 结果
     */
    public int insertTdwOutlineClosure(TdwOutlineClosure tdwOutlineClosure);

    /**
     * 修改大纲树形闭包关系，存储所有祖先后代关系
     * 
     * @param tdwOutlineClosure 大纲树形闭包关系，存储所有祖先后代关系
     * @return 结果
     */
    public int updateTdwOutlineClosure(TdwOutlineClosure tdwOutlineClosure);

    /**
     * 删除大纲树形闭包关系，存储所有祖先后代关系
     * 
     * @param id 大纲树形闭包关系，存储所有祖先后代关系主键
     * @return 结果
     */
    public int deleteTdwOutlineClosureById(Long id);

    /**
     * 批量删除大纲树形闭包关系，存储所有祖先后代关系
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTdwOutlineClosureByIds(Long[] ids);

    List<TdwOutlineClosure> selectAncestorByParentId(@Param("descendant") Long descendant);

    List<TdwOutlineClosure> selectDescendantClosuresByAncestor(@Param("ancestor") Long ancestor);

    void batchInsert(List<TdwOutlineClosure> closures);

    void insertOutlineClosureLeval1(@Param("outlineId") Long outlineId);

    void insertOutlineClosureLeval2(@Param("outlineId")Long outlineId, @Param("parentId")Long parentId);

    List<Long> selectDescendantIdsByAncestor(@Param("ancestor") Long ancestor);

    void batchDeleteByNodeIds(List<Long> allSubNodeIds);

    void deleteExternalClosureByDescendantIds(List<Long> descendantIds);
}
