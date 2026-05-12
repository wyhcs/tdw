package com.ruoyi.tdw.service.impl;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import com.ruoyi.tdw.ai.dto.GenerateOutlineAiRequest;
import com.ruoyi.tdw.ai.dto.GenerateOutlineAiResponse;
import com.ruoyi.tdw.ai.service.TdwAiService;
import com.ruoyi.tdw.domain.TdwBids;
import com.ruoyi.tdw.domain.TdwOutlineClosure;
import com.ruoyi.tdw.mapper.TdwBidsMapper;
import com.ruoyi.tdw.mapper.TdwContentsMapper;
import com.ruoyi.tdw.mapper.TdwOutlineClosureMapper;
import com.ruoyi.tdw.mapper.TdwTenderParseReportMapper;
import com.ruoyi.tdw.domain.dto.TdwOutlineGenerateRequest;
import com.ruoyi.tdw.domain.dto.TdwOutlineInsertRequest;
import com.ruoyi.tdw.domain.dto.TdwOutlineMoveRequest;
import com.ruoyi.tdw.domain.dto.TdwOutlineSortRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.tdw.mapper.TdwOutlinesMapper;
import com.ruoyi.tdw.domain.TdwOutlines;
import com.ruoyi.tdw.service.ITdwKnowledgeService;
import com.ruoyi.tdw.service.ITdwOutlinesService;

/**
 * 大纲节点Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-03-28
 */
@Service
public class TdwOutlinesServiceImpl implements ITdwOutlinesService 
{
    @Value("${llm.prompt.outline-service}")
    private String outlineService;     // 服务类

    @Value("${llm.prompt.outline-goods}")
    private String outlineGoods;  // 货物类

    @Value("${llm.prompt.outline-engineering}")
    private String outlineEngineering;// 工程类

    @Value("${llm.prompt.outline-supervision}")
    private String outlineSupervision;// 监理类

    @Value("${llm.prompt.outline-it}")
    private String outlineIt;// IT信息类

    @Value("${llm.prompt.outline-other}")
    private String outlineOther; // 其它类

    @Autowired
    private TdwOutlinesMapper tdwOutlinesMapper;
    @Autowired
    private TdwBidsMapper bidsMapper;
    @Autowired
    private TdwOutlinesMapper outlinesMapper;
    @Autowired
    private TdwOutlineClosureMapper tdwOutlineClosureMapper;
    @Autowired
    private TdwContentsMapper tdwContentsMapper;
    @Autowired
    private TdwAiService tdwAiService;
    @Autowired
    private ITdwKnowledgeService tdwKnowledgeService;
    @Autowired
    private TdwTenderParseReportMapper tenderParseReportMapper;

    // 临时的节点ID生成器，从10开始，和我们的测试ID对齐
    private AtomicLong nextNodeId = new AtomicLong(10);
    // 临时存储所有的大纲节点，用于后续生成闭包关系
    private List<TdwOutlines> allOutlines = new ArrayList<>();
    // 临时存储所有的闭包关系
    private List<TdwOutlineClosure> allClosures = new ArrayList<>();

    /**
     * 查询大纲节点
     *
     * @param id 大纲节点主键
     * @return 大纲节点
     */
    @Override
    public TdwOutlines selectTdwOutlinesById(Long id)
    {
        return tdwOutlinesMapper.selectTdwOutlinesById(id);
    }

    /**
     * 查询大纲节点列表
     *
     * @param tdwOutlines 大纲节点
     * @return 大纲节点
     */
    @Override
    public List<TdwOutlines> selectTdwOutlinesList(TdwOutlines tdwOutlines)
    {
        return tdwOutlinesMapper.selectTdwOutlinesList(tdwOutlines);
    }


    /**
     * 新增大纲节点
     *
     * @param tdwOutlines 大纲节点
     * @return 结果
     */
    @Override
    public int insertTdwOutlines(TdwOutlines tdwOutlines)
    {
        return tdwOutlinesMapper.insertTdwOutlines(tdwOutlines);
    }

    /**
     * 修改大纲节点
     *
     * @param tdwOutlines 大纲节点
     * @return 结果
     */
    @Override
    public int updateTdwOutlines(TdwOutlines tdwOutlines)
    {
        return tdwOutlinesMapper.updateTdwOutlines(tdwOutlines);
    }

    /**
     * 批量删除大纲节点
     *
     * @param ids 需要删除的大纲节点主键
     * @return 结果
     */
    @Override
    public int deleteTdwOutlinesByIds(Long[] ids)
    {
        return tdwOutlinesMapper.deleteTdwOutlinesByIds(ids);
    }

    /**
     * 删除大纲节点信息
     *
     * @param id 大纲节点主键
     * @return 结果
     */
    @Override
    public int deleteTdwOutlinesById(Long id)
    {
        return tdwOutlinesMapper.deleteTdwOutlinesById(id);
    }

    @Override
    public List<TdwOutlines> selectOutlineTree(Long bidId)
    {
        if (bidId == null) {
            throw new IllegalArgumentException("bid not found");
        }
        List<TdwOutlines> outlines = tdwOutlinesMapper.selectOutlinesByBidId(bidId);
        Map<Long, TdwOutlines> nodeMap = new LinkedHashMap<>();
        for (TdwOutlines outline : outlines) {
            outline.setChildren(new ArrayList<TdwOutlines>());
            nodeMap.put(outline.getId(), outline);
        }

        List<TdwOutlines> roots = new ArrayList<>();
        for (TdwOutlines outline : outlines) {
            if (outline.getParentId() == null) {
                roots.add(outline);
            } else {
                TdwOutlines parent = nodeMap.get(outline.getParentId());
                if (parent != null) {
                    parent.getChildren().add(outline);
                }
            }
        }
        sortTree(roots);
        return roots;
    }

    @Override
    public int updateOutlineTitle(TdwOutlines tdwOutlines)
    {
        if (tdwOutlines == null || tdwOutlines.getId() == null) {
            throw new IllegalArgumentException("bid not found");
        }
        if (tdwOutlines.getTitle() == null || tdwOutlines.getTitle().trim().length() == 0) {
            throw new IllegalArgumentException("bid not found");
        }
        return tdwOutlinesMapper.updateOutlineTitle(tdwOutlines.getId(), tdwOutlines.getTitle().trim());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TdwOutlines insertOutlineNode(TdwOutlineInsertRequest request)
    {
        if (request == null) {
            throw new IllegalArgumentException("bid not found");
        }
        if (request.getTitle() == null || request.getTitle().trim().length() == 0) {
            throw new IllegalArgumentException("bid not found");
        }

        TdwOutlines parent = null;
        TdwOutlines after = null;
        Long bidId = request.getBidId();
        Long parentId = request.getParentId();
        Integer level = request.getLevel();
        Integer sortNum = request.getSortNum();

        if (request.getAfterId() != null) {
            after = requireOutline(request.getAfterId());
            bidId = after.getBidId();
            parentId = after.getParentId();
            level = after.getLevel();
            sortNum = after.getSortNum() + 1;
        } else if (parentId != null) {
            parent = requireOutline(parentId);
            bidId = parent.getBidId();
            level = parent.getLevel() + 1;
        } else {
            if (bidId == null) {
            throw new IllegalArgumentException("bid not found");
            }
            level = 1;
        }

        if (level == null || level < 1 || level > 3) {
            throw new IllegalArgumentException("bid not found");
        }
        if (level == 1 && parentId != null) {
            throw new IllegalArgumentException("bid not found");
        }
        if (level > 1) {
            parent = parent == null ? requireOutline(parentId) : parent;
            if (parent.getLevel() + 1 != level) {
            throw new IllegalArgumentException("bid not found");
            }
        }

        int targetSort = resolveInsertSortNum(bidId, parentId, sortNum);
        shiftSiblingForInsert(bidId, parentId, targetSort);

        TdwOutlines outline = new TdwOutlines();
        outline.setBidId(bidId);
        outline.setParentId(parentId);
        outline.setLevel(level);
        outline.setTitle(request.getTitle().trim());
        outline.setSortNum(targetSort);
        outline.setWordLimit(request.getWordLimit() == null ? 300 : request.getWordLimit());
        tdwOutlinesMapper.insertTdwOutlines(outline);
        insertClosureForNode(outline.getId(), parentId);
        normalizeSiblingSort(bidId, parentId);
        return outline;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteOutlineById(Long outlineId)
    {
        TdwOutlines outline = requireOutline(outlineId);
        return deleteOutlineLeval(outline);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int moveOutline(TdwOutlineMoveRequest request)
    {
        if (request == null || request.getId() == null) {
            throw new IllegalArgumentException("bid not found");
        }
        TdwOutlines current = requireOutline(request.getId());
        List<Long> subtreeIds = tdwOutlineClosureMapper.selectDescendantIdsByAncestor(current.getId());

        TdwOutlines after = null;
        TdwOutlines parent = null;
        Long targetBidId = request.getBidId() == null ? current.getBidId() : request.getBidId();
        Long targetParentId = request.getParentId();
        Integer targetLevel = 1;
        Integer targetSortNum = request.getSortNum();

        if (request.getAfterId() != null) {
            if (subtreeIds.contains(request.getAfterId())) {
            throw new IllegalArgumentException("bid not found");
            }
            after = requireOutline(request.getAfterId());
            targetBidId = after.getBidId();
            targetParentId = after.getParentId();
            targetLevel = after.getLevel();
        } else if (targetParentId != null) {
            if (subtreeIds.contains(targetParentId)) {
            throw new IllegalArgumentException("bid not found");
            }
            parent = requireOutline(targetParentId);
            targetBidId = parent.getBidId();
            targetLevel = parent.getLevel() + 1;
        }

        if (!Objects.equals(current.getBidId(), targetBidId)) {
            throw new IllegalArgumentException("bid not found");
        }
        if (current.getLevel() != targetLevel) {
            throw new IllegalArgumentException("bid not found");
        }
        if (targetLevel < 1 || targetLevel > 3) {
            throw new IllegalArgumentException("bid not found");
        }

        Long oldParentId = current.getParentId();
        if (Objects.equals(oldParentId, targetParentId)) {
            List<TdwOutlines> siblings = tdwOutlinesMapper.selectSiblings(current.getBidId(), oldParentId);
            reorderForMove(siblings, current.getId(), request.getAfterId(), targetSortNum);
        } else {
            normalizeSiblingSortWithout(current.getBidId(), oldParentId, current.getId());
            List<TdwOutlines> targetSiblings = tdwOutlinesMapper.selectSiblings(targetBidId, targetParentId);
            int targetIndex = resolveMoveIndex(targetSiblings, request.getAfterId(), targetSortNum);
            tdwOutlinesMapper.updateParentAndSort(current.getId(), targetParentId, targetIndex + 1);
            targetSiblings.add(targetIndex, current);
            updateSiblingSort(targetSiblings);
            rebuildClosureForMovedSubtree(current.getId(), targetParentId, subtreeIds);
        }
        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int sortOutlines(TdwOutlineSortRequest request)
    {
        if (request == null || request.getOutlineIds() == null || request.getOutlineIds().isEmpty()) {
            throw new IllegalArgumentException("bid not found");
        }
        Long bidId = request.getBidId();
        Long parentId = request.getParentId();
        if (parentId != null) {
            TdwOutlines parent = requireOutline(parentId);
            bidId = parent.getBidId();
        }
        if (bidId == null) {
            throw new IllegalArgumentException("bid not found");
        }

        List<TdwOutlines> siblings = tdwOutlinesMapper.selectSiblings(bidId, parentId);
        Set<Long> exists = siblings.stream().map(TdwOutlines::getId).collect(Collectors.toSet());
        Set<Long> input = new LinkedHashSet<>(request.getOutlineIds());
        if (exists.size() != input.size() || !exists.equals(input)) {
            throw new IllegalArgumentException("bid not found");
        }
        for (int i = 0; i < request.getOutlineIds().size(); i++) {
            tdwOutlinesMapper.updateSortNum(request.getOutlineIds().get(i), i + 1);
        }
        return 1;
    }



    @Override
    public TdwOutlines selectLevelSortNumById(Long id) {
        return tdwOutlinesMapper.selectLevelSortNumById(id);
    }

    @Override
    public int insertOutlineLeval1(TdwOutlines tdwOutlines) {
        try {
            tdwOutlinesMapper.updateOutlineLeval1(tdwOutlines.getBidId(), tdwOutlines.getSortNum());
            tdwOutlinesMapper.insertTdwOutlines(tdwOutlines);
            tdwOutlineClosureMapper.insertOutlineClosureLeval1(tdwOutlines.getId());
            return 1;
        }catch (Exception e){
            throw new RuntimeException("insert outline failed", e);
        }
    }

    @Override
    public int insertOutlineLeval2(TdwOutlines tdwOutlines) {
        try {
            tdwOutlinesMapper.updateOutlineLeval2And3(tdwOutlines.getParentId(), tdwOutlines.getSortNum());
            tdwOutlinesMapper.insertTdwOutlines(tdwOutlines);
            tdwOutlineClosureMapper.insertOutlineClosureLeval2(tdwOutlines.getId(), tdwOutlines.getParentId());
            return 1;
        }catch (Exception e){
            throw new RuntimeException("insert outline failed", e);
        }
    }

    @Override
    public int insertOutlineLeval3(TdwOutlines tdwOutlines) {
        try {
            tdwOutlinesMapper.updateOutlineLeval2And3(tdwOutlines.getParentId(), tdwOutlines.getSortNum());
            tdwOutlinesMapper.insertTdwOutlines(tdwOutlines);
            Long newNodeId = tdwOutlines.getId();
            // 3. 【核心】查询父节点的所有祖先，自动拿到爷爷、太爷爷...所有的！
            List<TdwOutlineClosure> parentAncestors = tdwOutlineClosureMapper.selectAncestorByParentId(1108L);

            // 4. 生成新的闭包关系
            List<TdwOutlineClosure> newClosures = new ArrayList<>();
            // 加上新节点自己到自己的关系
            newClosures.add(new TdwOutlineClosure(newNodeId, newNodeId, 0L));

            for (TdwOutlineClosure ancestor : parentAncestors) {
                newClosures.add(new TdwOutlineClosure(
                        ancestor.getAncestor(),  // 祖先ID（爷爷/爸爸...）
                        newNodeId,               // 新节点ID
                        ancestor.getDepth() + 1L  // 深度+1
                ));
            }

            // 5. 批量插入闭包关系
            tdwOutlineClosureMapper.batchInsert(newClosures);
            return 1;
        }catch (Exception e){
            throw new RuntimeException("insert outline failed", e);
        }
    }

    private void sortTree(List<TdwOutlines> nodes)
    {
        nodes.sort((a, b) -> {
            int sort = Integer.compare(a.getSortNum(), b.getSortNum());
            if (sort != 0) {
                return sort;
            }
            if (a.getId() == null && b.getId() == null) {
                return 0;
            }
            if (a.getId() == null) {
                return 1;
            }
            if (b.getId() == null) {
                return -1;
            }
            return a.getId().compareTo(b.getId());
        });
        for (TdwOutlines node : nodes) {
            sortTree(node.getChildren());
        }
    }

    private TdwOutlines requireOutline(Long id)
    {
        if (id == null) {
            throw new IllegalArgumentException("bid not found");
        }
        TdwOutlines outline = tdwOutlinesMapper.selectTdwOutlinesById(id);
        if (outline == null) {
            throw new IllegalArgumentException("bid not found");
        }
        return outline;
    }

    private int resolveInsertSortNum(Long bidId, Long parentId, Integer sortNum)
    {
        Integer maxSortNum = tdwOutlinesMapper.selectMaxSortNum(bidId, parentId);
        int max = maxSortNum == null ? 0 : maxSortNum;
        if (sortNum == null || sortNum <= 0) {
            return max + 1;
        }
        return Math.min(sortNum, max + 1);
    }

    private void shiftSiblingForInsert(Long bidId, Long parentId, int sortNum)
    {
        if (parentId == null) {
            tdwOutlinesMapper.updateOutlineLeval1(bidId, sortNum);
        } else {
            tdwOutlinesMapper.updateOutlineLeval2And3(parentId, sortNum);
        }
    }

    private void insertClosureForNode(Long outlineId, Long parentId)
    {
        List<TdwOutlineClosure> closures = new ArrayList<>();
        closures.add(new TdwOutlineClosure(outlineId, outlineId, 0L));
        if (parentId != null) {
            List<TdwOutlineClosure> parentAncestors = tdwOutlineClosureMapper.selectAncestorByParentId(parentId);
            if (parentAncestors == null || parentAncestors.isEmpty()) {
                parentAncestors = new ArrayList<>();
                parentAncestors.add(new TdwOutlineClosure(parentId, parentId, 0L));
            }
            for (TdwOutlineClosure ancestor : parentAncestors) {
                closures.add(new TdwOutlineClosure(ancestor.getAncestor(), outlineId, ancestor.getDepth() + 1L));
            }
        }
        tdwOutlineClosureMapper.batchInsert(closures);
    }

    private void normalizeSiblingSort(Long bidId, Long parentId)
    {
        List<TdwOutlines> siblings = tdwOutlinesMapper.selectSiblings(bidId, parentId);
        updateSiblingSort(siblings);
    }

    private void normalizeSiblingSortWithout(Long bidId, Long parentId, Long excludeId)
    {
        List<TdwOutlines> siblings = tdwOutlinesMapper.selectSiblings(bidId, parentId);
        siblings.removeIf(item -> Objects.equals(item.getId(), excludeId));
        updateSiblingSort(siblings);
    }

    private void updateSiblingSort(List<TdwOutlines> siblings)
    {
        for (int i = 0; i < siblings.size(); i++) {
            tdwOutlinesMapper.updateSortNum(siblings.get(i).getId(), i + 1);
        }
    }

    private void reorderForMove(List<TdwOutlines> siblings, Long currentId, Long afterId, Integer sortNum)
    {
        TdwOutlines current = null;
        Iterator<TdwOutlines> iterator = siblings.iterator();
        while (iterator.hasNext()) {
            TdwOutlines item = iterator.next();
            if (Objects.equals(item.getId(), currentId)) {
                current = item;
                iterator.remove();
                break;
            }
        }
        if (current == null) {
            current = requireOutline(currentId);
        }
        int targetIndex = resolveMoveIndex(siblings, afterId, sortNum);
        siblings.add(targetIndex, current);
        updateSiblingSort(siblings);
    }

    private int resolveMoveIndex(List<TdwOutlines> siblings, Long afterId, Integer sortNum)
    {
        if (afterId != null) {
            for (int i = 0; i < siblings.size(); i++) {
                if (Objects.equals(siblings.get(i).getId(), afterId)) {
                    return i + 1;
                }
            }
            throw new IllegalArgumentException("bid not found");
        }
        if (sortNum == null || sortNum <= 0) {
            return siblings.size();
        }
        return Math.min(sortNum - 1, siblings.size());
    }

    private void rebuildClosureForMovedSubtree(Long rootId, Long targetParentId, List<Long> subtreeIds)
    {
        if (subtreeIds == null || subtreeIds.isEmpty()) {
            return;
        }
        List<TdwOutlineClosure> subtreeClosures = tdwOutlineClosureMapper.selectDescendantClosuresByAncestor(rootId);
        tdwOutlineClosureMapper.deleteExternalClosureByDescendantIds(subtreeIds);

        if (targetParentId == null) {
            return;
        }
        List<TdwOutlineClosure> parentAncestors = tdwOutlineClosureMapper.selectAncestorByParentId(targetParentId);
        if (parentAncestors == null || parentAncestors.isEmpty()) {
            parentAncestors = new ArrayList<>();
            parentAncestors.add(new TdwOutlineClosure(targetParentId, targetParentId, 0L));
        }
        List<TdwOutlineClosure> newClosures = new ArrayList<>();
        for (TdwOutlineClosure parentAncestor : parentAncestors) {
            for (TdwOutlineClosure subtreeClosure : subtreeClosures) {
                newClosures.add(new TdwOutlineClosure(
                        parentAncestor.getAncestor(),
                        subtreeClosure.getDescendant(),
                        parentAncestor.getDepth() + 1L + subtreeClosure.getDepth()
                ));
            }
        }
        if (!newClosures.isEmpty()) {
            tdwOutlineClosureMapper.batchInsert(newClosures);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteOutlineLeval(TdwOutlines tdwOutlines) {
        try {
            Long parentId = tdwOutlines.getParentId();
            Integer oldSortNum = tdwOutlines.getSortNum();
            // 闭包表直接查，不用递归！一次性拿到整个子树的所有节点
            List<Long> allSubNodeIds = tdwOutlineClosureMapper.selectDescendantIdsByAncestor(tdwOutlines.getId());
            if (allSubNodeIds == null || allSubNodeIds.isEmpty()) {
                allSubNodeIds = new ArrayList<>();
                allSubNodeIds.add(tdwOutlines.getId());
            }

            // 3. 批量删除所有内容块：所有子节点的内容都删掉
            tdwContentsMapper.batchDeleteByOutlineIds(allSubNodeIds);

            // 4. 批量删除所有闭包关系：所有和这些节点相关的关系都删掉
            tdwOutlineClosureMapper.batchDeleteByNodeIds(allSubNodeIds);

            // 5. 批量删除所有大纲节点
            tdwOutlinesMapper.batchDeleteByIds(allSubNodeIds);

            // 6. 调整同级节点的sort_num，补上删除的位置
            if (tdwOutlines.getLevel() == 1) {
                // 根节点（章级），调整根节点的排序
                tdwOutlinesMapper.updateOutlineLeval1Delete(tdwOutlines.getBidId(), oldSortNum);
            } else {
                // 非根节点，调整父节点下的排序
                tdwOutlinesMapper.updateOutlineLeval2And3Delete(parentId, oldSortNum);
            }
            return 1;
        }catch (Exception e){
            throw new RuntimeException("delete outline failed", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long generateOutline(Long bidId) throws IOException {
        return generateOutline(bidId, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long generateOutline(Long bidId, List<Long> knowledgeFileIds) throws IOException {
        TdwOutlineGenerateRequest request = new TdwOutlineGenerateRequest();
        request.setBidId(bidId);
        request.setMode("append");
        request.setKnowledgeFileIds(knowledgeFileIds);
        return generateOutline(request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long generateOutline(TdwOutlineGenerateRequest request) throws IOException {
        if (request == null || request.getBidId() == null) {
            throw new IllegalArgumentException("bid not found");
        }
        Long bidId = request.getBidId();
        String mode = request.getMode() == null || request.getMode().trim().length() == 0 ? "append" : request.getMode().trim().toLowerCase();
        if (!"overwrite".equals(mode) && !"append".equals(mode)) {
            throw new IllegalArgumentException("mode must be overwrite or append");
        }
        TdwBids tdwBids = bidsMapper.selectTdwBidsById(bidId);
        if (tdwBids == null) {
            throw new IllegalArgumentException("bid not found");
        }

        List<Long> knowledgeFileIds = mergeKnowledgeFileIds(tdwBids, request);
        String prompt = buildOutlinePrompt(tdwBids.getCategory(), tdwBids.getTitle(), buildOutlineRequirement(tdwBids, request));
        GenerateOutlineAiRequest aiRequest = new GenerateOutlineAiRequest();
        aiRequest.setBidId(bidId);
        aiRequest.setProjectName(tdwBids.getTitle());
        aiRequest.setProjectType(tdwBids.getCategory());
        aiRequest.setRequirement(prompt);
        aiRequest.setKnowledgeFileIds(knowledgeFileIds);
        aiRequest.setKnowledgeContext(tdwKnowledgeService.buildKnowledgeContext(knowledgeFileIds, null));

        GenerateOutlineAiResponse aiResponse = tdwAiService.generateOutline(aiRequest);
        // 2. 解析大模型返回的JSON
        if (aiResponse == null || aiResponse.getNodes() == null || aiResponse.getNodes().isEmpty()) {
            throw new IllegalStateException("AI outline is empty");
        }

        if ("overwrite".equals(mode)) {
            clearBidOutlines(bidId);
        }
        int rootSortOffset = "append".equals(mode) ? selectRootSortOffset(bidId) : 0;

        // ------------------------------
        // 内存里解析所有节点，生成临时节点结构
        // ------------------------------
        List<TempNode> allTempNodes = new ArrayList<>();
        AtomicLong nextTempId = new AtomicLong(0);
        for (GenerateOutlineAiResponse.OutlineNode aiNode : aiResponse.getNodes()) {
            parseTempNode(aiNode, null, 1, nextTempId, allTempNodes);
        }

        // ------------------------------
        // 批量插入所有大纲节点，自动回填自增ID
        // ------------------------------
        List<TdwOutlines> outlines = allTempNodes.stream().map(temp -> {
            TdwOutlines outline = new TdwOutlines();
            outline.setBidId(bidId);
            outline.setLevel(temp.getLevel());
            outline.setTitle(temp.getTitle());
            outline.setSortNum(temp.getParentTempId() == null ? temp.getSortNum() + rootSortOffset : temp.getSortNum());
            outline.setWordLimit(temp.getWordLimit() == null ? 300 : temp.getWordLimit());
            return outline;
        }).collect(Collectors.toList());
        // 批量插入，MyBatis自动把所有自增ID回填到outline的id字段

        outlinesMapper.batchInsert(outlines);

        // ------------------------------
        // 建立临时ID到真实ID的映射
        // ------------------------------
        Map<Long, Long> tempToRealId = new HashMap<>();
        for (int i = 0; i < allTempNodes.size(); i++) {
            TempNode tempNode = allTempNodes.get(i);
            TdwOutlines outline = outlines.get(i);
            tempToRealId.put(tempNode.getTempId(), outline.getId());
        }

        // ------------------------------
        // 批量更新所有节点的parent_id
        // ------------------------------
        for (int i = 0; i < allTempNodes.size(); i++) {
            TempNode tempNode = allTempNodes.get(i);
            TdwOutlines outline = outlines.get(i);
            if (tempNode.getParentTempId() != null) {
                Long realParentId = tempToRealId.get(tempNode.getParentTempId());
                outline.setParentId(realParentId);
            }
        }
        outlinesMapper.batchUpdateParentId(outlines);

        // ------------------------------
        // 内存里批量生成所有闭包关系，不用查数据库！
        // ------------------------------
        List<TdwOutlineClosure> closures = new ArrayList<>();
        // 遍历根节点，递归生成闭包
        for (TempNode rootNode : allTempNodes.stream()
                .filter(n -> n.getParentTempId() == null)
                .collect(Collectors.toList())) {
            generateClosureInMemory(rootNode, null, closures, tempToRealId);
        }

        // ------------------------------
        // 批量插入所有闭包关系
        // ------------------------------
        tdwOutlineClosureMapper.batchInsert(closures);
        return bidId;
    }

    private List<Long> mergeKnowledgeFileIds(TdwBids tdwBids, TdwOutlineGenerateRequest request)
    {
        Set<Long> ids = new LinkedHashSet<>();
        if (tdwBids.getTemplateId() != null) {
            ids.add(tdwBids.getTemplateId());
        }
        if (request.getTemplateFileIds() != null) {
            ids.addAll(request.getTemplateFileIds());
        }
        if (request.getKnowledgeFileIds() != null) {
            ids.addAll(request.getKnowledgeFileIds());
        }
        return new ArrayList<>(ids);
    }

    private String buildOutlineRequirement(TdwBids tdwBids, TdwOutlineGenerateRequest request)
    {
        StringBuilder requirement = new StringBuilder();
        if (tdwBids.getNote() != null && tdwBids.getNote().trim().length() > 0) {
            requirement.append(tdwBids.getNote().trim());
        }
        if (request.getRequirement() != null && request.getRequirement().trim().length() > 0) {
            if (requirement.length() > 0) {
                requirement.append("\n");
            }
            requirement.append(request.getRequirement().trim());
        }
        if (request.getTenderParseReportId() != null) {
            com.ruoyi.tdw.domain.TdwTenderParseReport report = tenderParseReportMapper.selectById(request.getTenderParseReportId());
            if (report != null) {
                if (requirement.length() > 0) {
                    requirement.append("\n");
                }
                requirement.append("招标文件解析结果：")
                        .append(report.getRequirementSummary() == null ? "" : report.getRequirementSummary())
                        .append("\n评分项：")
                        .append(report.getScoreItems() == null ? "" : report.getScoreItems())
                        .append("\n解析详情：")
                        .append(report.getReportContent() == null ? "" : report.getReportContent());
            }
        }
        return requirement.toString();
    }

    private void clearBidOutlines(Long bidId)
    {
        List<Long> outlineIds = tdwOutlinesMapper.selectIdsByBidId(bidId);
        if (outlineIds == null || outlineIds.isEmpty()) {
            return;
        }
        tdwContentsMapper.batchDeleteByOutlineIds(outlineIds);
        tdwOutlineClosureMapper.batchDeleteByNodeIds(outlineIds);
        tdwOutlinesMapper.batchDeleteByIds(outlineIds);
    }

    private int selectRootSortOffset(Long bidId)
    {
        Integer maxSortNum = tdwOutlinesMapper.selectMaxSortNum(bidId, null);
        return maxSortNum == null ? 0 : maxSortNum;
    }


    // ------------------------------
// 内存里解析临时节点
// ------------------------------
    private Long parseTempNode(GenerateOutlineAiResponse.OutlineNode llmNode, Long parentTempId, int level, AtomicLong nextTempId, List<TempNode> allTempNodes) {
        Long tempId = nextTempId.getAndIncrement();
        TempNode tempNode = new TempNode();
        tempNode.setTempId(tempId);
        tempNode.setParentTempId(parentTempId);
        tempNode.setLevel(level);
        tempNode.setTitle(llmNode.getTitle());
        tempNode.setSortNum(llmNode.getSortNum());
        tempNode.setWordLimit(llmNode.getWordLimit());
        allTempNodes.add(tempNode);

        // 閫掑綊澶勭悊瀛愯妭鐐?
        if (level < 3 && llmNode.getChildren() != null) {
            for (GenerateOutlineAiResponse.OutlineNode child : llmNode.getChildren()) {
                Long childTempId = parseTempNode(child, tempId, level + 1, nextTempId, allTempNodes);
                TempNode childNode = allTempNodes.stream()
                        .filter(n -> n.getTempId().equals(childTempId))
                        .findFirst().get();
                tempNode.getChildren().add(childNode);
            }
        }
        return tempId;
    }

    // ------------------------------
// 内存里递归生成闭包关系，完全不用查数据库
// ------------------------------
    private void generateClosureInMemory(TempNode node, List<Long> ancestors, List<TdwOutlineClosure> closures, Map<Long, Long> tempToRealId) {
        Long realNodeId = tempToRealId.get(node.getTempId());

        // 自己到自己的关系
        closures.add(new TdwOutlineClosure(realNodeId, realNodeId, 0L));

        // 祖先到当前节点的关系
        if (ancestors != null) {
            for (int i = 0; i < ancestors.size(); i++) {
                Long ancestorRealId = ancestors.get(i);
                int depth = ancestors.size() - i;
                closures.add(new TdwOutlineClosure(ancestorRealId, realNodeId, Long.parseLong(""+depth)));
            }
        }

        // 处理子节点
        if (!node.getChildren().isEmpty()) {
            List<Long> newAncestors = ancestors == null ? new ArrayList<>() : new ArrayList<>(ancestors);
            newAncestors.add(realNodeId);
            for (TempNode child : node.getChildren()) {
                generateClosureInMemory(child, newAncestors, closures, tempToRealId);
            }
        }
    }

    private static class TempNode {
        private Long tempId;
        private Long parentTempId;
        private int level;
        private String title;
        private int sortNum;
        private Integer wordLimit;
        private List<TempNode> children = new ArrayList<>();
        public void setTempId(Long tempId) {this.tempId = tempId;}
        public void setParentTempId(Long parentTempId) {this.parentTempId = parentTempId;}
        public void setLevel(int level) {this.level = level;}
        public void setTitle(String title) {this.title = title;}
        public void setSortNum(int sortNum) {this.sortNum = sortNum;}
        public void setWordLimit(Integer wordLimit) {this.wordLimit = wordLimit;}
        public void setChildren(List<TempNode> children) {this.children = children;}
        public Long getTempId() {return tempId;}
        public Long getParentTempId() {return parentTempId;}
        public int getLevel() {return level;}
        public String getTitle() {return title;}
        public int getSortNum() {return sortNum;}
        public Integer getWordLimit() {return wordLimit;}
        public List<TempNode> getChildren() {return children;}
    }

    public static class LlmOutlineResponse {
        private List<LlmNode> nodes;
        public List<LlmNode> getNodes() {return nodes;}
    }

    public static class LlmNode {
        private int level;
        private String title;
        private int sortNum;
        private List<LlmNode> children;
        public int getLevel() {return level;}

        public String getTitle() {return title;}
        public int getSortNum() {return sortNum;}
        public List<LlmNode> getChildren() {return children;}
    }

    /**
     * 构建大纲生成的提示词，支持6种项目类型
     * @param projectType 项目类型：service/goods/engineering/supervision/it/other
     * @param projectName 项目名称
     * @param scoreItems 评分项信息
     * @return 最终提示词
     */
    private String buildOutlinePrompt(String projectType, String projectName, String scoreItems) {
        Map<String, String> extraParams = null;
        // 1. 根据类型选择对应的模板
        String template;
        switch (projectType) {
            case "服务":
                template = outlineService;
                break;
            case "货物":
                template = outlineGoods;
                break;
            case "工程":
                template = outlineEngineering;
                // 工程类需要额外的工程量清单参数
                String quantityList = extraParams != null ? extraParams.get("quantityList") : "";
                template = template.replace("{quantityList}", quantityList);
                break;
            case "监理":
                template = outlineSupervision;
                // 监理类需要额外的工程范围参数
                break;
            case "IT信息":
                template = outlineIt;
                break;
            case "其它":
                template = outlineOther;
                break;
            default:
                template = outlineOther;
                break;
        }

        template = template.replace("{projectName}", projectName)
                .replace("{scoreItems}", scoreItems);

        return template;
    }

}
