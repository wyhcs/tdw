package com.ruoyi.tdw.ai.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruoyi.tdw.ai.dto.GenerateContentAiRequest;
import com.ruoyi.tdw.ai.dto.GenerateContentAiResponse;
import com.ruoyi.tdw.ai.dto.GenerateOutlineAiRequest;
import com.ruoyi.tdw.ai.dto.GenerateOutlineAiResponse;

/**
 * AI provider abstraction. Real model providers should implement this interface
 * without exposing API keys in business code.
 */
public interface AiProvider
{
    String getName();

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
