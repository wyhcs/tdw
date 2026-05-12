package com.ruoyi.tdw.utils;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.*;
import org.docx4j.model.styles.StyleUtil;

import javax.xml.bind.JAXBElement;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Word纯文本提取工具类（支持.doc和.docx格式）
 * 只提取文字内容，忽略图片、表格等非文本元素
 */
public class DocToHtmlConverter  {

    public static String extractTextWithStyles(String filePath) throws Exception {
        if (filePath.toLowerCase().endsWith(".doc")) {
            return extractTextFromDoc(filePath);
        } else if (filePath.toLowerCase().endsWith(".docx")) {
            return extractTextFromDocx(filePath);
        } else {
            throw new IllegalArgumentException("不支持的文件格式，仅支持.doc和.docx格式");
        }
    }

    /**
     * 处理.doc格式文档
     */
    private static String extractTextFromDoc(String filePath) throws IOException {
        FileInputStream fis = new FileInputStream(filePath);
        POIFSFileSystem poifs = new POIFSFileSystem(fis);
        HWPFDocument document = new HWPFDocument(poifs);

        StringBuilder htmlBuilder = new StringBuilder();

        htmlBuilder.append("<!DOCTYPE html>\n");
        htmlBuilder.append("<html>\n<head>\n");
        htmlBuilder.append("<meta charset=\"UTF-8\">\n");
        htmlBuilder.append("<title>Word文档转换</title>\n");
        htmlBuilder.append("<style>\n");
        htmlBuilder.append(generateBaseCss());
        htmlBuilder.append("</style>\n");
        htmlBuilder.append("</head>\n<body>\n");

        // 获取文档范围
        Range range = document.getRange();

        // 处理段落
        for (int i = 0; i < range.numParagraphs(); i++) {
            Paragraph paragraph = range.getParagraph(i);
            processDocParagraph(paragraph, htmlBuilder, document);
        }

        htmlBuilder.append("</body>\n</html>");

        document.close();
        fis.close();

        return htmlBuilder.toString();
    }

    /**
     * 处理.doc段落
     */
    private static void processDocParagraph(Paragraph paragraph, StringBuilder htmlBuilder, HWPFDocument document) {
        // 获取段落对齐方式
        String alignment = getDocParagraphAlignment(paragraph);

        htmlBuilder.append("<p style=\"");
        htmlBuilder.append("margin: 8px 0; line-height: 1.8; ");
        htmlBuilder.append(alignment);

        // 处理缩进
        String indentStyle = getDocParagraphIndent(paragraph);
        if (indentStyle != null) {
            htmlBuilder.append("; ").append(indentStyle);
        }
        htmlBuilder.append("\">");

        // 处理字符运行
        for (int j = 0; j < paragraph.numCharacterRuns(); j++) {
            CharacterRun run = paragraph.getCharacterRun(j);
            processDocCharacterRun(run, htmlBuilder);
        }

        htmlBuilder.append("</p>\n");
    }

    /**
     * 获取.doc段落对齐方式
     */
    private static String getDocParagraphAlignment(Paragraph paragraph) {
        int alignment = paragraph.getJustification();
        switch (alignment) {
            case 1: return "text-align: center";
            case 2: return "text-align: right";
            case 3: return "text-align: justify";
            case 0:
            default: return "text-align: left";
        }
    }

    /**
     * 获取.doc段落缩进
     */
    private static String getDocParagraphIndent(Paragraph paragraph) {
        List<String> indentStyles = new ArrayList<>();

        // 左缩进 - 修正单位转换
        int leftIndent = paragraph.getIndentFromLeft();
        if (leftIndent > 0) {
            // 1/20点 = 1像素，但实际需要根据DPI调整
            int pixels = twipsToPixels(leftIndent);
            indentStyles.add("margin-left: " + pixels + "px");
        }

        // 首行缩进
        int firstLineIndent = paragraph.getFirstLineIndent();
        if (firstLineIndent != 0) {
            int pixels = twipsToPixels(firstLineIndent);
            indentStyles.add("text-indent: " + pixels + "px");
        }

        // 右缩进
        int rightIndent = paragraph.getIndentFromRight();
        if (rightIndent > 0) {
            int pixels = twipsToPixels(rightIndent);
            indentStyles.add("margin-right: " + pixels + "px");
        }

        if (!indentStyles.isEmpty()) {
            return String.join("; ", indentStyles);
        }
        return null;
    }

    /**
     * 将twips转换为像素（更准确的转换）
     * 1英寸 = 1440 twips = 96像素（在96 DPI下）
     */
    private static int twipsToPixels(int twips) {
        // 1440 twips = 1英寸 = 96像素
        return (int) Math.round(twips * 96.0 / 1440);
    }

    /**
     * 处理.doc字符运行
     */
    private static void processDocCharacterRun(CharacterRun run, StringBuilder htmlBuilder) {
        String text = run.text();
        if (text != null && !text.trim().isEmpty()) {
            String runStyle = getDocRunStyle(run);
            htmlBuilder.append("<span style=\"").append(runStyle).append("\">");
            htmlBuilder.append(escapeHtml(text));
            htmlBuilder.append("</span>");
        }
    }

    /**
     * 获取.doc运行样式
     */
    private static String getDocRunStyle(CharacterRun run) {
        List<String> styles = new ArrayList<>();

        // 字体
        String fontName = run.getFontName();
        if (fontName != null && !fontName.trim().isEmpty()) {
            styles.add("font-family:" + mapChineseFont(fontName));
        }

        // 字号
        int fontSize = run.getFontSize() / 2; // 半磅转换为磅
        if (fontSize > 0) {
            styles.add("font-size:" + fontSize + "pt");
        }

        // 加粗
        if (run.isBold()) {
            styles.add("font-weight:bold");
        }

        // 斜体
        if (run.isItalic()) {
            styles.add("font-style:italic");
        }

        // 删除线
        if (run.isMarkedInserted()) {
            styles.add("text-decoration:line-through");
        }

        // 颜色 - .doc格式的颜色处理较为复杂，这里简化处理
        int colorIndex = run.getColor();
        if (colorIndex > 0) {
            String color = getDocColor(colorIndex);
            if (color != null) {
                styles.add("color:" + color);
            }
        }

        return String.join(";", styles);
    }

    /**
     * 获取.doc颜色
     */
    private static String getDocColor(int colorIndex) {
        Map<Integer, String> colorMap = new HashMap<>();
        colorMap.put(1, "#000000"); // 黑色
        colorMap.put(2, "#0000FF"); // 蓝色
        colorMap.put(3, "#00FF00"); // 绿色
        colorMap.put(4, "#FF0000"); // 红色
        colorMap.put(5, "#FFFF00"); // 黄色
        colorMap.put(6, "#FF00FF"); // 洋红
        colorMap.put(7, "#00FFFF"); // 青色
        colorMap.put(8, "#FFFFFF"); // 白色
        colorMap.put(9, "#000080"); // 深蓝
        colorMap.put(10, "#008000"); // 深绿
        colorMap.put(11, "#800000"); // 深红
        colorMap.put(12, "#808000"); // 深黄
        colorMap.put(13, "#800080"); // 深紫
        colorMap.put(14, "#008080"); // 深青
        colorMap.put(15, "#C0C0C0"); // 浅灰
        colorMap.put(16, "#808080"); // 深灰

        return colorMap.get(colorIndex);
    }

    /**
     * 处理.docx格式文档（原有代码）
     */
    private static String extractTextFromDocx(String filePath) throws Exception {
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new File(filePath));
        StringBuilder htmlBuilder = new StringBuilder();

        htmlBuilder.append("<!DOCTYPE html>\n");
        htmlBuilder.append("<html>\n<head>\n");
        htmlBuilder.append("<meta charset=\"UTF-8\">\n");
        htmlBuilder.append("<title>Word文档转换</title>\n");
        htmlBuilder.append("<style>\n");
        htmlBuilder.append(generateBaseCss());
        htmlBuilder.append("</style>\n");
        htmlBuilder.append("</head>\n<body>\n");

        Body body = wordMLPackage.getMainDocumentPart().getJaxbElement().getBody();

        if (body != null) {
            for (Object content : body.getContent()) {
                if (content instanceof JAXBElement) {
                    Object element = ((JAXBElement<?>) content).getValue();
                    if (element instanceof P) {
                        processParagraph((P) element, htmlBuilder, wordMLPackage);
                    }
                } else if (content instanceof P) {
                    processParagraph((P) content, htmlBuilder, wordMLPackage);
                }
            }
        }

        htmlBuilder.append("</body>\n</html>");
        return htmlBuilder.toString();
    }

    /**
     * 处理段落
     */
    private static void processParagraph(P paragraph, StringBuilder htmlBuilder,
                                         WordprocessingMLPackage wordMLPackage) {
        String alignment = getParagraphAlignment(paragraph);
        String indentStyle = getParagraphIndent(paragraph);

        htmlBuilder.append("<p style=\"");
        htmlBuilder.append("margin: 8px 0; line-height: 1.8; ");
        htmlBuilder.append(alignment);
        if (indentStyle != null) {
            htmlBuilder.append("; ").append(indentStyle);
        }
        htmlBuilder.append("\">");

        boolean hasText = false;

        for (Object runObj : paragraph.getContent()) {
            if (runObj instanceof JAXBElement) {
                Object element = ((JAXBElement<?>) runObj).getValue();
                if (element instanceof R) {
                    hasText = processRun((R) element, htmlBuilder, wordMLPackage) || hasText;
                }
            } else if (runObj instanceof R) {
                hasText = processRun((R) runObj, htmlBuilder, wordMLPackage) || hasText;
            } else if (runObj instanceof R.Tab) {
                // 处理制表符
                htmlBuilder.append("&nbsp;&nbsp;&nbsp;&nbsp;");
                hasText = true;
            }
        }

        // 如果段落没有文本内容，添加一个空格保持结构
        if (!hasText) {
            htmlBuilder.append("&nbsp;");
        }

        htmlBuilder.append("</p>\n");
    }

    /**
     * 获取段落对齐方式
     */
    private static String getParagraphAlignment(P paragraph) {
        PPr pPr = paragraph.getPPr();
        if (pPr != null) {
            Jc jc = pPr.getJc();
            if (jc != null && jc.getVal() != null) {
                String val = jc.getVal().toString().toLowerCase();
                switch (val) {
                    case "center": return "text-align: center";
                    case "right": return "text-align: right";
                    case "both": return "text-align: justify";
                    case "left": return "text-align: left";
                    case "start": return "text-align: left";
                    case "end": return "text-align: right";
                    case "distribute": return "text-align: justify";
                }
            }
        }
        return "text-align: left";
    }

    /**
     * 获取段落缩进
     */
    private static String getParagraphIndent(P paragraph) {
        PPr pPr = paragraph.getPPr();
        if (pPr != null && pPr.getInd() != null) {
            PPrBase.Ind ind = pPr.getInd();
            List<String> indentStyles = new ArrayList<>();

            if (ind.getLeft() != null) {
                int leftIndent = ind.getLeft().intValue() / 20; // twips to pixels
                indentStyles.add("margin-left: " + leftIndent + "px");
            }

            if (ind.getFirstLine() != null) {
                int firstLineIndent = ind.getFirstLine().intValue() / 20;
                indentStyles.add("text-indent: " + firstLineIndent + "px");
            }

            if (ind.getRight() != null) {
                int rightIndent = ind.getRight().intValue() / 20;
                indentStyles.add("margin-right: " + rightIndent + "px");
            }

            if (!indentStyles.isEmpty()) {
                return String.join("; ", indentStyles);
            }
        }
        return null;
    }

    /**
     * 处理文本运行（Run）
     */
    private static boolean processRun(R run, StringBuilder htmlBuilder,
                                      WordprocessingMLPackage wordMLPackage) {
        boolean hasText = false;

        for (Object textObj : run.getContent()) {
            if (textObj instanceof JAXBElement) {
                Object element = ((JAXBElement<?>) textObj).getValue();
                if (element instanceof Text) {
                    processTextElement((Text) element, run, htmlBuilder);
                    hasText = true;
                }
            } else if (textObj instanceof Text) {
                processTextElement((Text) textObj, run, htmlBuilder);
                hasText = true;
            } else if (textObj instanceof R.Tab) {
                htmlBuilder.append("&nbsp;&nbsp;&nbsp;&nbsp;");
                hasText = true;
            }
            // 忽略图片等其他元素
        }

        return hasText;
    }

    /**
     * 处理文本元素
     */
    private static void processTextElement(Text text, R run, StringBuilder htmlBuilder) {
        String textContent = text.getValue();
        if (textContent != null && !textContent.trim().isEmpty()) {
            String runStyle = getRunStyle(run);
            htmlBuilder.append("<span style=\"").append(runStyle).append("\">");
            htmlBuilder.append(escapeHtml(textContent));
            htmlBuilder.append("</span>");
        }
    }

    /**
     * 获取运行样式（字体、字号、颜色等）
     */
    private static String getRunStyle(R run) {
        List<String> styles = new ArrayList<>();
        RPr rPr = run.getRPr();

        if (rPr != null) {
            // 字体
            String fontFamily = getFontFamily(rPr);
            if (fontFamily != null) {
                styles.add("font-family:" + fontFamily);
            }

            // 字号 - 精确处理
            String fontSize = getFontSize(rPr);
            if (fontSize != null) {
                styles.add("font-size:" + fontSize);
            }

            // 加粗
            if (rPr.getB() != null && rPr.getB().isVal()) {
                styles.add("font-weight:bold");
            }

            // 斜体
            if (rPr.getI() != null && rPr.getI().isVal()) {
                styles.add("font-style:italic");
            }

            // 下划线
            if (rPr.getU() != null && rPr.getU().getVal() != null) {
                styles.add("text-decoration:underline");
            }

            // 删除线
            if (rPr.getStrike() != null && rPr.getStrike().isVal()) {
                styles.add("text-decoration:line-through");
            }

            // 颜色
            String color = getColor(rPr);
            if (color != null) {
                styles.add("color:" + color);
            }

            // 背景色
            String backgroundColor = getBackgroundColor(rPr);
            if (backgroundColor != null) {
                styles.add("background-color:" + backgroundColor);
            }

        }

        return String.join(";", styles);
    }

    /**
     * 获取字体家族
     */
    private static String getFontFamily(RPr rPr) {
        if (rPr != null && rPr.getRFonts() != null) {
            RFonts fonts = rPr.getRFonts();

            // 获取字体名称的优先级顺序
            String[] fontProperties = {
                    fonts.getAscii(),
                    fonts.getEastAsia(),
                    fonts.getHAnsi(),
                    fonts.getCs(),
                    String.valueOf(fonts.getAsciiTheme()),
                    String.valueOf(fonts.getEastAsiaTheme()),
                    String.valueOf(fonts.getHAnsiTheme())
            };

            for (String fontName : fontProperties) {
                if (fontName != null && !fontName.trim().isEmpty()) {
                    return fontName;
//                    return mapChineseFont(fontName);
                }
            }
        }
        return null;
    }

    /**
     * 映射中文字体到Web安全字体
     */
    private static String mapChineseFont(String fontName) {
        if (fontName == null) return null;

        Map<String, String> fontMapping = new HashMap<>();
        // 中文字体映射
        fontMapping.put("仿宋_GB2312", "仿宋_GB2312, serif");
        fontMapping.put("黑体", "黑体, sans-serif");
        fontMapping.put("宋体", "宋体, serif");
        fontMapping.put("仿宋", "仿宋, serif");
        fontMapping.put("楷体", "楷体, serif");
        fontMapping.put("楷体_GB2312", "楷体_GB2312, serif");
        fontMapping.put("微软雅黑", "微软雅黑, sans-serif");
        fontMapping.put("华文宋体", "华文宋体, serif");
        fontMapping.put("华文黑体", "华文黑体, sans-serif");

        // 英文字体映射
        fontMapping.put("Times New Roman", "Times New Roman, serif");
        fontMapping.put("Arial", "Arial, Helvetica, sans-serif");
        fontMapping.put("Calibri", "Calibri, sans-serif");
        fontMapping.put("Tahoma", "Tahoma, Geneva, sans-serif");
        fontMapping.put("Verdana", "Verdana, Geneva, sans-serif");

        return fontMapping.getOrDefault(fontName, fontName);
    }

    /**
     * 获取字号 - 修正版
     */
    private static String getFontSize(RPr rPr) {
        if (rPr != null && rPr.getSz() != null) {
            HpsMeasure size = rPr.getSz();
            if (size.getVal() != null) {
                // 半磅值直接转换为磅值
                int halfPoints = size.getVal().intValue();
                double points = halfPoints / 2.0;
                return points + "pt";
            }
        }

        // 默认字号
        if (rPr != null && rPr.getSzCs() != null) {
            HpsMeasure sizeCs = rPr.getSzCs();
            if (sizeCs.getVal() != null) {
                int halfPoints = sizeCs.getVal().intValue();
                double points = halfPoints / 2.0;
                return points + "pt";
            }
        }

        return null;
    }

    /**
     * 获取文字颜色
     */
    private static String getColor(RPr rPr) {
        if (rPr != null && rPr.getColor() != null) {
            Color color = rPr.getColor();
            if (color.getVal() != null) {
                String colorVal = color.getVal();
                // 处理自动颜色
                if ("auto".equalsIgnoreCase(colorVal)) {
                    return "inherit";
                }
                // 处理6位十六进制颜色
                if (colorVal.length() == 6) {
                    return "#" + colorVal;
                }
                // 处理颜色名称
                return colorVal;
            }
        }
        return null;
    }

    /**
     * 获取背景颜色
     */
    private static String getBackgroundColor(RPr rPr) {
        if (rPr != null && rPr.getHighlight() != null) {
            Highlight highlight = rPr.getHighlight();
            if (highlight.getVal() != null) {
                String colorVal = highlight.getVal().toString();
                Map<String, String> highlightColors = new HashMap<>();
                highlightColors.put("yellow", "#FFFF00");
                highlightColors.put("green", "#00FF00");
                highlightColors.put("cyan", "#00FFFF");
                highlightColors.put("magenta", "#FF00FF");
                highlightColors.put("blue", "#0000FF");
                highlightColors.put("red", "#FF0000");
                highlightColors.put("darkBlue", "#000080");
                highlightColors.put("darkCyan", "#008080");
                highlightColors.put("darkGreen", "#008000");
                highlightColors.put("darkMagenta", "#800080");
                highlightColors.put("darkRed", "#800000");
                highlightColors.put("darkYellow", "#808000");
                highlightColors.put("darkGray", "#808080");
                highlightColors.put("lightGray", "#C0C0C0");
                highlightColors.put("black", "#000000");
                highlightColors.put("white", "#FFFFFF");
                highlightColors.put("none", "transparent");

                return highlightColors.getOrDefault(colorVal.toLowerCase(), "#FFFF00");
            }
        }
        return null;
    }

    /**
     * HTML转义
     */
    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /**
     * 生成基础CSS样式
     */
    private static String generateBaseCss() {
        return "body { " +
                "font-family: SimSun, serif; " +
                "line-height: 1.6; " +
                "color: #000000; " +
                "margin: 20px; " +
                "background: #ffffff; " +
                "} " +
                "p { " +
                "margin: 8px 0; " +
                "line-height: 1.8; " +
                "} " +
                "span { " +
                "white-space: pre-wrap; " +
                "font-size: inherit; " +
                "font-family: inherit; " +
                "} " +
                ".indent-2 { text-indent: 2em; } " + // 添加2字符缩进类
                ".indent-4 { text-indent: 4em; } " + // 添加4字符缩进类
                ".center { text-align: center; } " +
                ".right { text-align: right; } " +
                ".justify { text-align: justify; } " +
                ".left { text-align: left; }";
    }

}

