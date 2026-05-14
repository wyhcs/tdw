package com.ruoyi.tdw.service;

import java.util.Map;

import com.ruoyi.tdw.domain.dto.TdwPlanAddNodeRequest;
import com.ruoyi.tdw.domain.dto.TdwPlanAiTextResult;
import com.ruoyi.tdw.domain.dto.TdwPlanDeleteNodeRequest;
import com.ruoyi.tdw.domain.dto.TdwPlanSortRequest;
import com.ruoyi.tdw.domain.dto.TdwPlanTitleRequest;
import com.ruoyi.tdw.domain.dto.TdwPlanWordLimitRequest;
import com.ruoyi.tdw.domain.dto.TdwPlanWordPresetRequest;
import com.ruoyi.tdw.domain.dto.TdwPlanWritingAiRequest;
import com.ruoyi.tdw.domain.dto.TdwPlanWritingRuleRequest;

/**
 * AI方案目录生成后编辑Service。
 */
public interface ITdwPlanOutlineService
{
    Map<String, Object> getOverview(Long bidId);

    Map<String, Object> applyWordPreset(Long bidId, TdwPlanWordPresetRequest request);

    Map<String, Object> updateNodeWordLimit(Long bidId, Long outlineId, TdwPlanWordLimitRequest request);

    Map<String, Object> batchUpdateWordLimit(Long bidId, Long outlineId, TdwPlanWordLimitRequest request);

    Map<String, Object> updateTitle(Long bidId, Long outlineId, TdwPlanTitleRequest request);

    Map<String, Object> updateWritingDirection(Long bidId, Long outlineId, TdwPlanWritingRuleRequest request);

    Map<String, Object> updateWritingRequirement(Long bidId, Long outlineId, TdwPlanWritingRuleRequest request);

    Map<String, Object> updateGlobalWritingRequirement(Long bidId, TdwPlanWritingRuleRequest request);

    Map<String, Object> addSibling(Long bidId, Long outlineId, TdwPlanAddNodeRequest request);

    Map<String, Object> addChild(Long bidId, Long outlineId, TdwPlanAddNodeRequest request);

    Map<String, Object> addParagraph(Long bidId, Long outlineId, TdwPlanAddNodeRequest request);

    Map<String, Object> deleteNodes(Long bidId, TdwPlanDeleteNodeRequest request);

    Map<String, Object> sortNodes(Long bidId, TdwPlanSortRequest request);

    Map<String, Object> finalizeOutline(Long bidId);

    TdwPlanAiTextResult generateWritingDirection(Long bidId, Long outlineId, TdwPlanWritingAiRequest request);

    TdwPlanAiTextResult generateWritingRequirement(Long bidId, Long outlineId, TdwPlanWritingAiRequest request);
}
