package com.ruoyi.tdw.service.impl;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.tdw.mapper.TdwContentsMapper;
import com.ruoyi.tdw.mapper.TdwOutlineClosureMapper;
import com.ruoyi.tdw.mapper.TdwOutlinesMapper;
import com.ruoyi.tdw.mapper.TdwTenderFileMapper;
import com.ruoyi.tdw.mapper.TdwTenderParseReportMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.tdw.mapper.TdwBidsMapper;
import com.ruoyi.tdw.domain.TdwBids;
import com.ruoyi.tdw.service.ITdwBidsService;

/**
 * 【请填写功能名称】Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-03-28
 */
@Service
public class TdwBidsServiceImpl implements ITdwBidsService 
{
    @Autowired
    private TdwOutlinesMapper tdwOutlinesMapper;
    @Autowired
    private TdwOutlineClosureMapper tdwOutlineClosureMapper;
    @Autowired
    private TdwContentsMapper tdwContentsMapper;
    @Autowired
    private TdwBidsMapper tdwBidsMapper;
    @Autowired
    private TdwTenderFileMapper tdwTenderFileMapper;
    @Autowired
    private TdwTenderParseReportMapper tdwTenderParseReportMapper;

    /**
     * 查询【请填写功能名称】
     * 
     * @param id 【请填写功能名称】主键
     * @return 【请填写功能名称】
     */
    @Override
    public TdwBids selectTdwBidsById(Long id)
    {
        return tdwBidsMapper.selectTdwBidsById(id);
    }

    /**
     * 查询【请填写功能名称】列表
     * 
     * @param tdwBids 【请填写功能名称】
     * @return 【请填写功能名称】
     */
    @Override
    public List<TdwBids> selectTdwBidsList(TdwBids tdwBids)
    {
        return tdwBidsMapper.selectTdwBidsList(tdwBids);
    }

    /**
     * 新增【请填写功能名称】
     * 
     * @param tdwBids 【请填写功能名称】
     * @return 结果
     */
    @Override
    public int insertTdwBids(TdwBids tdwBids)
    {
        return tdwBidsMapper.insertTdwBids(tdwBids);
    }

    /**
     * 修改【请填写功能名称】
     * 
     * @param tdwBids 【请填写功能名称】
     * @return 结果
     */
    @Override
    public int updateTdwBids(TdwBids tdwBids)
    {
        return tdwBidsMapper.updateTdwBids(tdwBids);
    }

    /**
     * 批量删除【请填写功能名称】
     * 
     * @param ids 需要删除的【请填写功能名称】主键
     * @return 结果
     */
    @Override
    public int deleteTdwBidsByIds(Long[] ids)
    {
        return tdwBidsMapper.deleteTdwBidsByIds(ids);
    }

    /**
     * 删除【请填写功能名称】信息
     * 
     * @param id 【请填写功能名称】主键
     * @return 结果
     */
    @Override
    public int deleteTdwBidsById(Long id)
    {
        return tdwBidsMapper.deleteTdwBidsById(id);
    }

    @Override
    public int deleteBids(Long bidId) {
        // 2. 查询该项目下的所有大纲节点ID（一次性查出来，后面批量删）
        List<Long> nodeIds = tdwOutlinesMapper.selectIdsByBidId(bidId);

        if (!CollectionUtils.isEmpty(nodeIds)) {
            // 3. 批量删除所有内容块：所有子节点的内容都删掉
            tdwContentsMapper.batchDeleteByOutlineIds(nodeIds);
            // 4. 批量删除所有闭包关系
            tdwOutlineClosureMapper.batchDeleteByNodeIds(nodeIds);
            // 5. 批量删除所有大纲节点
            tdwOutlinesMapper.batchDeleteByIds(nodeIds);
        }
        // 6. 最后删除标书主表
        tdwTenderParseReportMapper.deleteByBidId(bidId);
        tdwTenderFileMapper.deleteByBidId(bidId);
        return tdwBidsMapper.deleteTdwBidsById(bidId);
    }
}
