package com.ruoyi.tdw.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruoyi.tdw.ai.dto.GenerateContentAiRequest;
import com.ruoyi.tdw.ai.dto.GenerateContentAiResponse;
import com.ruoyi.tdw.ai.dto.GenerateOutlineAiRequest;
import com.ruoyi.tdw.ai.dto.GenerateOutlineAiResponse;

public interface TdwAiService
{
    GenerateOutlineAiResponse generateOutline(GenerateOutlineAiRequest request);

    GenerateContentAiResponse generateContentBlocks(GenerateContentAiRequest request);

    String optimizeContent(GenerateContentAiRequest request);

    String expandContent(GenerateContentAiRequest request);

    String shortenContent(GenerateContentAiRequest request);

    String generateTenderResponse(GenerateContentAiRequest request);

    String checkQuality(GenerateContentAiRequest request);

    String buildDuplicateText(GenerateContentAiRequest request) throws JsonProcessingException;

    String extractText(String prompt, String inputText, String taskType);
}
