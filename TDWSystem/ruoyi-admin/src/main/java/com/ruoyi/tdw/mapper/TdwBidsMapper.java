package com.ruoyi.tdw.mapper;

import java.util.List;
import com.ruoyi.tdw.domain.TdwBids;

/**
 * 【请填写功能名称】Mapper接口
 * 
 * @author ruoyi
 * @date 2026-03-28
 */
public interface TdwBidsMapper 
{
    /**
     * 查询【请填写功能名称】
     * 
     * @param id 【请填写功能名称】主键
     * @return 【请填写功能名称】
     */
    public TdwBids selectTdwBidsById(Long id);

    /**
     * 查询【请填写功能名称】列表
     * 
     * @param tdwBids 【请填写功能名称】
     * @return 【请填写功能名称】集合
     */
    public List<TdwBids> selectTdwBidsList(TdwBids tdwBids);

    /**
     * 新增【请填写功能名称】
     * 
     * @param tdwBids 【请填写功能名称】
     * @return 结果
     */
    public int insertTdwBids(TdwBids tdwBids);

    /**
     * 修改【请填写功能名称】
     * 
     * @param tdwBids 【请填写功能名称】
     * @return 结果
     */
    public int updateTdwBids(TdwBids tdwBids);

    /**
     * 删除【请填写功能名称】
     * 
     * @param id 【请填写功能名称】主键
     * @return 结果
     */
    public int deleteTdwBidsById(Long id);

    /**
     * 批量删除【请填写功能名称】
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTdwBidsByIds(Long[] ids);

    String getTitle(Long id);

}
