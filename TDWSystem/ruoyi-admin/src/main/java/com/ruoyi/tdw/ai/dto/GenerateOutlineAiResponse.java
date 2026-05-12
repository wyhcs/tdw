package com.ruoyi.tdw.ai.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Structured outline response returned by AI providers.
 */
public class GenerateOutlineAiResponse
{
    private List<OutlineNode> nodes = new ArrayList<OutlineNode>();

    public List<OutlineNode> getNodes()
    {
        return nodes;
    }

    public void setNodes(List<OutlineNode> nodes)
    {
        this.nodes = nodes;
    }

    public static class OutlineNode
    {
        private int level;

        private String title;

        private int sortNum;

        private Integer wordLimit;

        private List<OutlineNode> children = new ArrayList<OutlineNode>();

        public int getLevel()
        {
            return level;
        }

        public void setLevel(int level)
        {
            this.level = level;
        }

        public String getTitle()
        {
            return title;
        }

        public void setTitle(String title)
        {
            this.title = title;
        }

        public int getSortNum()
        {
            return sortNum;
        }

        public void setSortNum(int sortNum)
        {
            this.sortNum = sortNum;
        }

        public Integer getWordLimit()
        {
            return wordLimit;
        }

        public void setWordLimit(Integer wordLimit)
        {
            this.wordLimit = wordLimit;
        }

        public List<OutlineNode> getChildren()
        {
            return children;
        }

        public void setChildren(List<OutlineNode> children)
        {
            this.children = children;
        }
    }
}
