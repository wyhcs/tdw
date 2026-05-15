package com.ruoyi.tdw.ai.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruoyi.tdw.ai.dto.GenerateContentAiRequest;
import com.ruoyi.tdw.ai.dto.GenerateContentAiResponse;
import com.ruoyi.tdw.ai.dto.GenerateOutlineAiRequest;
import com.ruoyi.tdw.ai.dto.GenerateOutlineAiResponse;
import com.ruoyi.tdw.ai.provider.AiProviderFactory;
import com.ruoyi.tdw.ai.service.TdwAiService;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TdwAiServiceImpl implements TdwAiService
{
    @Autowired
    private AiProviderFactory aiProviderFactory;

    @Override
    public GenerateOutlineAiResponse generateOutline(GenerateOutlineAiRequest request)
    {
        return aiProviderFactory.getProvider().generateOutline(request);
    }

    @Override
    public GenerateOutlineAiResponse generateOutline(GenerateOutlineAiRequest request, Consumer<String> outlineMarkdownConsumer)
    {
        return aiProviderFactory.getProvider().generateOutline(request, outlineMarkdownConsumer);
    }

    @Override
    public GenerateContentAiResponse generateContentBlocks(GenerateContentAiRequest request)
    {
        return aiProviderFactory.getProvider().generateContentBlocks(request);
    }

    @Override
    public String optimizeContent(GenerateContentAiRequest request)
    {
        return aiProviderFactory.getProvider().optimizeContent(request);
    }

    @Override
    public String expandContent(GenerateContentAiRequest request)
    {
        return aiProviderFactory.getProvider().expandContent(request);
    }

    @Override
    public String shortenContent(GenerateContentAiRequest request)
    {
        return aiProviderFactory.getProvider().shortenContent(request);
    }

    @Override
    public String generateTenderResponse(GenerateContentAiRequest request)
    {
        return aiProviderFactory.getProvider().generateTenderResponse(request);
    }

    @Override
    public String checkQuality(GenerateContentAiRequest request)
    {
        return aiProviderFactory.getProvider().checkQuality(request);
    }

    @Override
    public String buildDuplicateText(GenerateContentAiRequest request) throws JsonProcessingException
    {
        return aiProviderFactory.getProvider().buildDuplicateText(request);
    }

    @Override
    public String extractText(String prompt, String inputText, String taskType)
    {
        return aiProviderFactory.getProvider().extractText(prompt, inputText, taskType);
    }
}
