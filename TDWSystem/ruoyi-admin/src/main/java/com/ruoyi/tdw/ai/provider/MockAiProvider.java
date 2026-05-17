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
        if (isTeachingResourceProject(request.getProjectName(), request.getRequirement())) {
            response.setNodes(teachingResourceOutline());
            return response;
        }
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
        return prefixExisting(request, "已改写：");
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

    @Override
    public String extractText(String prompt, String inputText, String taskType)
    {
        String text = safe(inputText);
        String type = safe(taskType);
        if (type.contains("project-name")) {
            return extractProjectName(text);
        }
        if (type.contains("purchase-requirement")) {
            return extractPurchaseRequirement(text);
        }
        if (type.contains("extra-attachment")) {
            return extractExtraAttachment(text);
        }
        if (type.contains("-field-")) {
            return extractPlanField(text, type);
        }
        if (type.contains("score-from-purchase")) {
            return extractScoreItemsFromPurchase(text);
        }
        if (type.contains("score-technical")) {
            return extractTechnicalScoreItems(text);
        }
        if (type.contains("score-full")) {
            return extractFullScoreItems(text);
        }
        return truncatePlain(text, 1200);
    }

    private String extractPlanField(String text, String taskType)
    {
        if (taskType.contains("procurementNeed")) {
            return extractPurchaseRequirement(text);
        }
        if (taskType.contains("goodsInfo")) {
            return extractSectionLike(text, new String[] { "货物信息", "采购清单", "设备清单", "技术参数", "规格型号", "供货范围", "交付要求" }, 2200);
        }
        if (taskType.contains("engineeringOverview")) {
            return extractSectionLike(text, new String[] { "工程概况", "项目概况", "建设地点", "建设规模", "工程范围", "工期要求", "质量目标" }, 1800);
        }
        if (taskType.contains("engineeringList")) {
            return extractSectionLike(text, new String[] { "工程量清单", "分部分项", "清单编码", "项目特征", "计量单位", "工程量" }, 2200);
        }
        if (taskType.contains("drawingParse")) {
            return extractSectionLike(text, new String[] { "图纸", "施工图", "设计说明", "图号", "平面图", "剖面图" }, 1800);
        }
        if (taskType.contains("supervisionOverview")) {
            return extractSectionLike(text, new String[] { "监理概况", "监理范围", "服务范围", "监理目标", "监理服务", "监理工作内容" }, 1800);
        }
        if (taskType.contains("projectOverview")) {
            return extractSectionLike(text, new String[] { "项目概况", "项目说明", "建设背景", "建设目标", "建设内容", "项目范围" }, 1800);
        }
        if (taskType.contains("technicalRequirement")) {
            return extractSectionLike(text, new String[] { "技术要求", "功能要求", "性能要求", "技术规格", "用户需求书", "采购需求" }, 2200);
        }
        if (taskType.contains("equipmentList")) {
            return extractSectionLike(text, new String[] { "设备清单", "设备配置", "硬件设备", "软件清单", "模块清单", "规格型号" }, 2200);
        }
        return truncatePlain(text, 1200);
    }

    private String extractProjectName(String text)
    {
        if (text.contains("高等学校教学资源管理、推荐和分析智慧平台开发")) {
            return "高等学校教学资源管理、推荐和分析智慧平台开发";
        }
        String[] markers = new String[] { "项目名称", "项目名", "采购项目名称", "招标项目名称" };
        for (String marker : markers) {
            String value = afterMarkerLine(text, marker);
            if (value.length() > 0) {
                return trimChineseLength(value, 50);
            }
        }
        String first = firstMeaningfulLine(text);
        return trimChineseLength(first, 50);
    }

    private String extractPurchaseRequirement(String text)
    {
        if (isTeachingResourceProject(text, text)) {
            return "采购要求如下：\n"
                    + "一、项目解读\n"
                    + "（一）通过整合教学资源、提供个性化推荐、分析学生行为和数据，以及支持决策分析，建立一个高效、智能和个性化的教学资源管理、推荐和分析智慧平台。\n"
                    + "（二）平台包含主要的子系统：统一用户管理、权限管理应用、基础信息管理、资源管理、接口管理、资源智能推荐管理系统、高等教育工作情况可视化系统、教师教研管理系统和基础服务。";
        }
        return extractSectionLike(text, new String[] { "采购需求", "采购要求", "项目采购内容与要求", "服务要求", "技术规格要求", "技术要求", "项目需求" }, 2200);
    }

    private String extractExtraAttachment(String text)
    {
        if (isTeachingResourceProject(text, text)) {
            return "【附件类型】\n评分办法附件\n\n【是否属于实质性附件】\n是\n\n【附件核心有效内容】\n\n"
                    + "一、条款名称：技术参数\n"
                    + "- 条款性质：评分项（含实质性参数）\n"
                    + "- 具体要求：\n"
                    + "  1. 投标产品与招标文件规定的技术参数和要求的满足程度，完全响应得51分\n"
                    + "  2. 标记为“★”为实质性参数，一项不满足视为无效投标\n"
                    + "  3. 无标记的指标项参数有一项不满足扣3分；扣完为止\n"
                    + "- 涉及关键数值：\n"
                    + "  - 分值：51.0分\n"
                    + "  - 扣分标准：无标记指标项一项不满足扣3分\n"
                    + "- 风险提示：\n"
                    + "  - 实质性参数（★）不满足将导致无效投标";
        }
        String section = extractSectionLike(text, new String[] { "附件", "附表", "技术规范书", "偏离表", "评分标准", "评分细则", "合同专用条款" }, 2200);
        if (section.length() == 0) {
            return "";
        }
        return "【附件类型】\n招投标相关附件\n\n【是否属于实质性附件】\n是\n\n【附件核心有效内容】\n\n" + section;
    }

    private String extractFullScoreItems(String text)
    {
        if (isTeachingResourceProject(text, text)) {
            return "技术参数（51.0分）\n"
                    + "投标产品与招标文件规定的技术参数和要求的满足程度，完全响应得51分；标记为“★”为实质性参数，一项不满足视为无效投标。无标记的指标项参数有一项不满足扣3分；扣完为止。\n\n"
                    + "项目实施阶段方案（9分）\n"
                    + "供应商应结合本项目的特点提供项目实施阶段方案包括但不限于：需求分析阶段、系统实施准备阶段、系统培训阶段、系统初验测试阶段、系统试运行阶段、系统终验阶段。\n\n"
                    + "可视化开发技术方案（6分）\n"
                    + "供应商结合本项目特点提供可视化开发技术方案，包括统计的实时数据情况、前端实时页面的多功能展示窗口、系统数据架构、系统应用架构。\n\n"
                    + "服务方案（6分）\n"
                    + "供应商应结合本项目的特点提供服务方案，包括质量管理、组织架构、实施的进度计划方案、保障措施方案、技术设计方案、系统开发方案。";
        }
        return extractSectionLike(text, new String[] { "评分标准", "评分细则", "评审细则", "评标办法", "技术评分", "综合评分" }, 2600);
    }

    private String extractScoreItemsFromPurchase(String text)
    {
        if (isTeachingResourceProject(text, text)) {
            return extractFullScoreItems(text);
        }
        String section = extractSectionLike(text, new String[] { "技术参数", "项目实施阶段方案", "服务方案", "技术方案", "质量要求", "服务要求", "实施要求" }, 2200);
        if (section.length() == 0) {
            return "";
        }
        return section;
    }

    private String extractTechnicalScoreItems(String text)
    {
        String full = text.length() == 0 ? extractFullScoreItems(text) : text;
        if (full.contains("技术参数") || full.contains("项目实施阶段方案")) {
            return "技术参数（51.0分）\n"
                    + "投标产品与招标文件规定的技术参数和要求的满足程度，完全响应得51分；标记为“★”为实质性参数，一项不满足视为无效投标。无标记的指标项参数有一项不满足扣3分；扣完为止。\n\n"
                    + "项目实施阶段方案（9分）\n"
                    + "供应商应结合本项目的特点提供项目实施阶段方案，包括需求分析阶段、系统实施准备阶段、系统培训阶段、系统初验测试阶段、系统试运行阶段、系统终验阶段。\n\n"
                    + "可视化开发技术方案（6分）\n"
                    + "供应商结合本项目特点提供可视化开发技术方案，包括统计的实时数据情况、前端实时页面的多功能展示窗口、系统数据架构、系统应用架构。\n\n"
                    + "服务方案（6分）\n"
                    + "供应商应结合本项目的特点提供服务方案，包括质量管理、组织架构、实施的进度计划方案、保障措施方案、技术设计方案、系统开发方案。";
        }
        StringBuilder result = new StringBuilder();
        String[] lines = full.split("\\r?\\n");
        for (String line : lines) {
            if (line.contains("技术") || line.contains("方案") || line.contains("服务") || line.contains("实施") || line.contains("措施")) {
                if (result.length() > 0) {
                    result.append('\n');
                }
                result.append(line);
            }
        }
        return result.length() == 0 ? full : result.toString();
    }

    private String extractSectionLike(String text, String[] markers, int length)
    {
        String compact = text == null ? "" : text.trim();
        for (String marker : markers) {
            int index = compact.indexOf(marker);
            if (index >= 0) {
                int end = Math.min(compact.length(), index + length);
                return compact.substring(index, end).trim();
            }
        }
        return truncatePlain(compact, Math.min(length, 1200));
    }

    private String afterMarkerLine(String text, String marker)
    {
        int index = text.indexOf(marker);
        if (index < 0) {
            return "";
        }
        String rest = text.substring(index + marker.length()).replaceFirst("^[：:：\\s]+", "");
        int end = rest.indexOf('\n');
        String line = end >= 0 ? rest.substring(0, end) : rest;
        return line.replaceAll("[；;。].*$", "").trim();
    }

    private String firstMeaningfulLine(String text)
    {
        String[] lines = text.split("\\r?\\n");
        for (String line : lines) {
            String value = line.trim();
            if (value.length() >= 4 && !value.contains("目录") && !value.contains("公告")) {
                return value;
            }
        }
        return "";
    }

    private String trimChineseLength(String text, int max)
    {
        String value = text == null ? "" : text.trim();
        return value.length() > max ? value.substring(0, max) : value;
    }

    private String truncatePlain(String text, int max)
    {
        String value = text == null ? "" : text.trim();
        return value.length() > max ? value.substring(0, max) : value;
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
        String text = teachingResourceParagraph(request);
        if (text == null) {
            text = "【" + safeStyle(request) + "】围绕“" + safe(request.getOutlineTitle()) + "”生成的正文内容。"
                    + requirementSuffix(request) + knowledgeSuffix(request.getKnowledgeContext());
        }
        root.put("text", text);
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

    private boolean isTeachingResourceProject(String projectName, String requirement)
    {
        String text = safe(projectName) + " " + safe(requirement);
        return text.contains("教学资源管理")
                || text.contains("智慧平台")
                || text.contains("统一用户管理")
                || text.contains("用户管理功能")
                || text.contains("权限管理功能")
                || text.contains("课程资源管理");
    }

    private List<GenerateOutlineAiResponse.OutlineNode> teachingResourceOutline()
    {
        List<GenerateOutlineAiResponse.OutlineNode> chapters = new ArrayList<GenerateOutlineAiResponse.OutlineNode>();
        chapters.add(chapter(1, "技术参数",
                section(1, "实质性参数响应",
                        item(1, "用户管理功能需求"),
                        item(2, "权限管理功能需求"),
                        item(3, "课程资源管理功能实现"),
                        item(4, "技术参数对照表")),
                section(2, "系统功能与接口响应",
                        item(1, "基础信息管理功能实现"),
                        item(2, "资源智能推荐能力"),
                        item(3, "数据接口与集成能力"))));
        chapters.add(chapter(2, "项目实施服务方案",
                section(1, "需求分析阶段",
                        item(1, "需求调研与范围确认"),
                        item(2, "业务流程梳理"),
                        item(3, "数据资源盘点")),
                section(2, "实施准备与部署阶段",
                        item(1, "实施组织与职责分工"),
                        item(2, "系统部署与联调"),
                        item(3, "测试试运行与验收"))));
        chapters.add(chapter(3, "质量保障与售后服务方案",
                section(1, "质量保障措施",
                        item(1, "质量控制机制"),
                        item(2, "风险识别与应急处理"),
                        item(3, "安全与数据保护")),
                section(2, "售后服务承诺",
                        item(1, "响应机制与服务流程"),
                        item(2, "培训支持与知识转移"),
                        item(3, "持续优化与运维保障"))));
        return chapters;
    }

    private String teachingResourceParagraph(GenerateContentAiRequest request)
    {
        String title = safe(request.getOutlineTitle());
        if (!isTeachingResourceProject(request.getTenderParseResult(), request.getRequirement())
                && !isTeachingResourceProject(title, request.getKnowledgeContext())
                && !looksLikePlanOutlineTitle(title)) {
            return null;
        }
        if (title.contains("用户管理")) {
            return "用户管理功能实现以统一身份认证与行为数据融合为双核心，构建覆盖全生命周期的管理闭环。系统支持多角色用户注册与实名认证，集成多种认证方式，确保身份真实性与操作可追溯性。用户信息采用集中式存储架构，实现跨子系统信息自动同步与动态更新，保障数据一致性。\n\n登录过程全程记录时间、终端类型、访问路径、操作行为按模块、功能、动作粒度进行结构化采集，形成完整操作日志链。学习行为数据通过资源访问频次、停留时长、互动类型、路径偏好等维度进行标准化归集，与用户画像模型深度耦合，支撑后续推荐与分析。所有管理动作均嵌入权限校验机制，与权限管理应用实时联动，确保操作权限动态生效。";
        }
        if (title.contains("权限管理")) {
            return "权限管理功能实现以角色为中心构建多层级管控体系，预设教学管理者、院系负责人、教师、学生等角色权限模板，支持按组织架构、岗位职责、业务场景进行自定义权限配置。覆盖功能菜单、数据范围、操作行为三个维度，权限分配过程纳入统一用户管理体系，与身份认证服务实时联动，确保权限变更后即时生效。\n\n系统内置权限变更审计模块，完整记录授权授予、调整、回收的操作时间、操作人、操作对象及变更前后的权限状态。审计日志不可篡改且支持按条件追溯查询。所有权限策略均基于平台基础服务模块实现，依托已部署的标准化接口管理机制完成跨子系统权限同步，保障资源智能推荐、教研管理、可视化分析等业务模块权限策略的一致性与连续性。";
        }
        if (title.contains("课程资源")) {
            return "课程资源管理功能实现以机构与人员信息管理、教学基础数据维护为双主线，构建覆盖全生命周期的数据治理体系。机构层级结构维护支持多级单位关系动态配置，实现高校院系所中心等组织单元的树状映射与权限绑定；师生基础档案管理集成身份核验、信息变更留痕、档案版本控制等机制，确保数据真实可溯。\n\n课程信息标准化管理内置教育部课程分类编码规则，支持课程属性、学分学时、开课学期等字段的结构化录入与批量校验；教学资源分类目录维护采用多维度标签体系，兼容文本、音视频、实验包等资源类型，支持按学科、专业、课程、难度等维度交叉索引与权限隔离。所有数据操作均通过统一元数据管理平台进行字段定义、格式约束与质量校验，与统一用户管理、权限管理应用深度耦合，保障数据在各子系统间的一致性与安全性。";
        }
        if (title.contains("技术参数对照")) {
            return "技术参数对照围绕招标文件的实质性要求和一般功能要求逐条响应。系统建设内容覆盖统一用户管理、权限管理、资源管理、接口管理、资源智能推荐、可视化分析、教师教研管理和基础服务等模块，投标方案在功能边界、数据流转、实施交付、质量控制和售后服务等方面保持一致响应。";
        }
        if (title.contains("需求")) {
            return "需求分析阶段将围绕采购需求、评分标准、现有业务流程和数据资源现状展开。项目组会对教学资源管理、用户权限、资源推荐、可视化分析、接口集成等核心场景进行梳理，形成需求确认记录、功能边界说明和实施优先级清单，为后续设计、开发、联调和验收提供可追溯依据。";
        }
        if (title.contains("部署") || title.contains("实施")) {
            return "项目实施以阶段化交付为主线，建立计划管理、环境准备、部署联调、培训试运行和验收移交的协同机制。各阶段产出与招标要求保持对应关系，关键节点纳入质量检查清单，问题闭环通过记录、定位、修复、复核的流程完成，确保系统按既定目标稳定落地。";
        }
        if (title.contains("质量") || title.contains("风险") || title.contains("安全")) {
            return "质量保障措施覆盖需求确认、设计评审、开发自检、联调测试、试运行巡检和验收复核全过程。系统通过问题台账、版本记录、权限审计、数据备份和安全加固机制降低交付风险。对于运行异常、数据同步异常、权限策略异常等情况，建立响应、定位、修复和复盘流程，保障平台持续稳定运行。";
        }
        if (title.contains("售后") || title.contains("培训") || title.contains("运维")) {
            return "售后服务以应用可持续运行为目标，提供系统使用培训、运维支持、配置调整、故障排查、数据备份恢复和安全更新等服务。服务过程保留工单记录和处理结论，常见问题沉淀为知识条目，便于学校管理人员和使用人员快速掌握平台操作与维护方法。";
        }
        return "本节围绕“" + title + "”进行响应，结合招标文件中的平台建设目标、教学资源管理场景、用户权限体系、智能推荐能力和可视化分析要求，形成可执行、可验证、可交付的服务方案内容。";
    }

    private boolean looksLikePlanOutlineTitle(String title)
    {
        return title.contains("需求")
                || title.contains("实施")
                || title.contains("部署")
                || title.contains("质量")
                || title.contains("风险")
                || title.contains("安全")
                || title.contains("售后")
                || title.contains("培训")
                || title.contains("运维")
                || title.contains("技术参数");
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

    private String safeStyle(GenerateContentAiRequest request)
    {
        String style = request == null ? "" : request.getWritingStyle();
        return style == null || style.trim().length() == 0 ? "通用型" : style.trim();
    }
}
