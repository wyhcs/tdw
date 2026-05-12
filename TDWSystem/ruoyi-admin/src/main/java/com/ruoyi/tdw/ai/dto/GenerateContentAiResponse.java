package com.ruoyi.tdw.ai.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Structured content-block response returned by AI providers.
 */
public class GenerateContentAiResponse
{
    private List<ContentBlock> blocks = new ArrayList<ContentBlock>();

    public List<ContentBlock> getBlocks()
    {
        return blocks;
    }

    public void setBlocks(List<ContentBlock> blocks)
    {
        this.blocks = blocks;
    }

    public static class ContentBlock
    {
        private Integer contentType;

        private Map<String, Object> content;

        public Integer getContentType()
        {
            return contentType;
        }

        public void setContentType(Integer contentType)
        {
            this.contentType = contentType;
        }

        public Map<String, Object> getContent()
        {
            return content;
        }

        public void setContent(Map<String, Object> content)
        {
            this.content = content;
        }
    }
}
