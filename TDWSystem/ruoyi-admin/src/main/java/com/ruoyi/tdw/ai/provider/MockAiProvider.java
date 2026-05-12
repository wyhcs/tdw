package com.ruoyi.tdw.ai.provider;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.tdw.ai.dto.GenerateContentAiRequest;
import com.ruoyi.tdw.ai.dto.GenerateContentAiResponse;
import com.ruoyi.tdw.ai.dto.GenerateOutlineAiRequest;
import com.ruoyi.tdw.ai.dto.GenerateOutlineAiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Mock AI provider for local development.
 */
@Component
public class MockAiProvider implements AiProvider
{
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public String getName()
    {
        return "mock";
    }

    @Override
    public GenerateOutlineAiResponse generateOutline(GenerateOutlineAiRequest request)
    {
        GenerateOutlineAiResponse response = new GenerateOutlineAiResponse();
        List<GenerateOutlineAiResponse.OutlineNode> chapters = new ArrayList<GenerateOutlineAiResponse.OutlineNode>();
        chapters.add(chapter(1, "项目理解与总体响应",
                section(1, "项目背景与需求分析",
                        item(1, "项目建设背景"),
                        item(2, "核心需求响应")),
                section(2, "总体实施思路",
                        item(1, "总体架构设计"),
                        item(2, "实施路径规划"))));
        chapters.add(chapter(2, "技术方案与实施保障",
                section(1, "技术路线",
                        item(1, "关键技术路线"),
                        item(2, "数据处理方案")),
                section(2, "质量与进度保障",
                        item(1, "质量保障措施"),
                        item(2, "进度计划安排"))));
        response.setNodes(chapters);
        return response;
    }

    @Override
    public GenerateContentAiResponse generateContentBlocks(GenerateContentAiRequest request)
    {
        GenerateContentAiResponse response = new GenerateContentAiResponse();
        List<GenerateContentAiResponse.ContentBlock> blocks = new ArrayList<GenerateContentAiResponse.ContentBlock>();
        blocks.add(block(1, textContent(request)));
        if (Boolean.TRUE.equals(request.getIncludeTable())) {
            blocks.add(block(2, tableContent(request)));
        }
        if (Boolean.TRUE.equals(request.getIncludeDiagram())) {
            blocks.add(block(3, diagramContent(request)));
        }
        response.setBlocks(blocks);
        return response;
    }

    @Override
    public String optimizeContent(GenerateContentAiRequest request)
    {
        return prefixExisting(request, "已优化：");
    }

    @Override
    public String expandContent(GenerateContentAiRequest request)
    {
        return prefixExisting(request, "已扩写：") + " 补充了实施背景、关键步骤、风险控制和交付成果。";
    }

    @Override
    public String shortenContent(GenerateContentAiRequest request)
    {
        String text = request.getExistingContent() == null ? "" : request.getExistingContent();
        return text.length() > 80 ? text.substring(0, 80) : text;
    }

    @Override
    public String generateTenderResponse(GenerateContentAiRequest request)
    {
        return "根据招标文件解析结果生成响应内容：" + safe(request.getTenderParseResult());
    }

    @Override
    public String checkQuality(GenerateContentAiRequest request)
    {
        return "质检项：" + safe(request.getQualityItem()) + "；检查结论：Mock通过，建议后续接入真实规则与模型。";
    }

    @Override
    public String buildDuplicateText(GenerateContentAiRequest request) throws JsonProcessingException
    {
        GenerateContentAiResponse response = generateContentBlocks(request);
        List<String> texts = new ArrayList<String>();
        for (GenerateContentAiResponse.ContentBlock block : response.getBlocks()) {
            texts.add(objectMapper.writeValueAsString(block.getContent()));
        }
        return String.join("\n", texts);
    }

    private GenerateOutlineAiResponse.OutlineNode chapter(int sortNum, String title, GenerateOutlineAiResponse.OutlineNode... sections)
    {
        GenerateOutlineAiResponse.OutlineNode node = node(1, sortNum, title, 300);
        for (GenerateOutlineAiResponse.OutlineNode section : sections) {
            node.getChildren().add(section);
        }
        return node;
    }

    private GenerateOutlineAiResponse.OutlineNode section(int sortNum, String title, GenerateOutlineAiResponse.OutlineNode... items)
    {
        GenerateOutlineAiResponse.OutlineNode node = node(2, sortNum, title, 300);
        for (GenerateOutlineAiResponse.OutlineNode item : items) {
            node.getChildren().add(item);
        }
        return node;
    }

    private GenerateOutlineAiResponse.OutlineNode item(int sortNum, String title)
    {
        return node(3, sortNum, title, 500);
    }

    private GenerateOutlineAiResponse.OutlineNode node(int level, int sortNum, String title, int wordLimit)
    {
        GenerateOutlineAiResponse.OutlineNode node = new GenerateOutlineAiResponse.OutlineNode();
        node.setLevel(level);
        node.setSortNum(sortNum);
        node.setTitle(title);
        node.setWordLimit(wordLimit);
        return node;
    }

    private GenerateContentAiResponse.ContentBlock block(Integer contentType, Map<String, Object> content)
    {
        GenerateContentAiResponse.ContentBlock block = new GenerateContentAiResponse.ContentBlock();
        block.setContentType(contentType);
        block.setContent(content);
        return block;
    }

    private Map<String, Object> textContent(GenerateContentAiRequest request)
    {
        Map<String, Object> root = new LinkedHashMap<String, Object>();
        root.put("text", "围绕“" + safe(request.getOutlineTitle()) + "”生成的正文内容。" + requirementSuffix(request) + knowledgeSuffix(request.getKnowledgeContext()));
        Map<String, Object> format = new LinkedHashMap<String, Object>();
        format.put("fontSize", 14);
        format.put("bold", false);
        root.put("format", format);
        return root;
    }

    private Map<String, Object> tableContent(GenerateContentAiRequest request)
    {
        Map<String, Object> root = new LinkedHashMap<String, Object>();
        root.put("title", safe(request.getOutlineTitle()) + "要点表");
        List<String> headers = new ArrayList<String>();
        headers.add("序号");
        headers.add("内容");
        headers.add("说明");
        root.put("headers", headers);

        List<List<String>> rows = new ArrayList<List<String>>();
        rows.add(row("1", "总体思路", "结合招标要求形成可执行方案"));
        rows.add(row("2", "实施要点", "明确任务分工、进度安排和质量控制"));
        root.put("rows", rows);
        return root;
    }

    private Map<String, Object> diagramContent(GenerateContentAiRequest request)
    {
        Map<String, Object> root = new LinkedHashMap<String, Object>();
        root.put("title", safe(request.getOutlineTitle()) + "结构图");
        root.put("description", "展示“" + safe(request.getOutlineTitle()) + "”相关模块之间的关系");
        root.put("imageUrl", "");
        root.put("diagramType", "architecture");
        root.put("mermaid", "graph TD; A[输入资料] --> B[" + safe(request.getOutlineTitle()) + "]; B --> C[成果输出];");
        return root;
    }

    private List<String> row(String a, String b, String c)
    {
        List<String> row = new ArrayList<String>();
        row.add(a);
        row.add(b);
        row.add(c);
        return row;
    }

    private String prefixExisting(GenerateContentAiRequest request, String prefix)
    {
        return prefix + safe(request.getExistingContent());
    }

    private String requirementSuffix(GenerateContentAiRequest request)
    {
        return request.getRequirement() == null || request.getRequirement().trim().length() == 0
                ? ""
                : " 补充要求：" + request.getRequirement().trim();
    }

    private String knowledgeSuffix(String knowledgeContext)
    {
        return knowledgeContext == null || knowledgeContext.trim().length() == 0
                ? ""
                : " 已参考知识库资料：" + knowledgeContext.trim();
    }

    private String safe(String text)
    {
        return text == null ? "" : text;
    }
}
