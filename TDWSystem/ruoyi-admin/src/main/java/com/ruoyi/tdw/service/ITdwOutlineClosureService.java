package com.ruoyi.tdw.service;

import java.util.List;
import com.ruoyi.tdw.domain.TdwOutlineClosure;

/**
 * 大纲树形闭包关系，存储所有祖先后代关系Service接口
 * 
 * @author ruoyi
 * @date 2026-03-28
 */
public interface ITdwOutlineClosureService 
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
     * 批量删除大纲树形闭包关系，存储所有祖先后代关系
     * 
     * @param ids 需要删除的大纲树形闭包关系，存储所有祖先后代关系主键集合
     * @return 结果
     */
    public int deleteTdwOutlineClosureByIds(Long[] ids);

    /**
     * 删除大纲树形闭包关系，存储所有祖先后代关系信息
     * 
     * @param id 大纲树形闭包关系，存储所有祖先后代关系主键
     * @return 结果
     */
    public int deleteTdwOutlineClosureById(Long id);
}
