package com.ruoyi.tdw.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.tdw.mapper.TdwOutlineClosureMapper;
import com.ruoyi.tdw.domain.TdwOutlineClosure;
import com.ruoyi.tdw.service.ITdwOutlineClosureService;

/**
 * 大纲树形闭包关系，存储所有祖先后代关系Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-03-28
 */
@Service
public class TdwOutlineClosureServiceImpl implements ITdwOutlineClosureService 
{
    @Autowired
    private TdwOutlineClosureMapper tdwOutlineClosureMapper;

    /**
     * 查询大纲树形闭包关系，存储所有祖先后代关系
     * 
     * @param id 大纲树形闭包关系，存储所有祖先后代关系主键
     * @return 大纲树形闭包关系，存储所有祖先后代关系
     */
    @Override
    public TdwOutlineClosure selectTdwOutlineClosureById(Long id)
    {
        return tdwOutlineClosureMapper.selectTdwOutlineClosureById(id);
    }

    /**
     * 查询大纲树形闭包关系，存储所有祖先后代关系列表
     * 
     * @param tdwOutlineClosure 大纲树形闭包关系，存储所有祖先后代关系
     * @return 大纲树形闭包关系，存储所有祖先后代关系
     */
    @Override
    public List<TdwOutlineClosure> selectTdwOutlineClosureList(TdwOutlineClosure tdwOutlineClosure)
    {
        return tdwOutlineClosureMapper.selectTdwOutlineClosureList(tdwOutlineClosure);
    }

    /**
     * 新增大纲树形闭包关系，存储所有祖先后代关系
     * 
     * @param tdwOutlineClosure 大纲树形闭包关系，存储所有祖先后代关系
     * @return 结果
     */
    @Override
    public int insertTdwOutlineClosure(TdwOutlineClosure tdwOutlineClosure)
    {
        return tdwOutlineClosureMapper.insertTdwOutlineClosure(tdwOutlineClosure);
    }

    /**
     * 修改大纲树形闭包关系，存储所有祖先后代关系
     * 
     * @param tdwOutlineClosure 大纲树形闭包关系，存储所有祖先后代关系
     * @return 结果
     */
    @Override
    public int updateTdwOutlineClosure(TdwOutlineClosure tdwOutlineClosure)
    {
        return tdwOutlineClosureMapper.updateTdwOutlineClosure(tdwOutlineClosure);
    }

    /**
     * 批量删除大纲树形闭包关系，存储所有祖先后代关系
     * 
     * @param ids 需要删除的大纲树形闭包关系，存储所有祖先后代关系主键
     * @return 结果
     */
    @Override
    public int deleteTdwOutlineClosureByIds(Long[] ids)
    {
        return tdwOutlineClosureMapper.deleteTdwOutlineClosureByIds(ids);
    }

    /**
     * 删除大纲树形闭包关系，存储所有祖先后代关系信息
     * 
     * @param id 大纲树形闭包关系，存储所有祖先后代关系主键
     * @return 结果
     */
    @Override
    public int deleteTdwOutlineClosureById(Long id)
    {
        return tdwOutlineClosureMapper.deleteTdwOutlineClosureById(id);
    }
}
