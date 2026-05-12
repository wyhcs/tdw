package com.ruoyi.tdw.utils;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.*;

import javax.xml.bind.JAXBElement;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Word纯文本提取工具类（精确保留字体字号样式）
 * 只提取文字内容，忽略图片、表格等非文本元素
 */
public class DocxToHtmlConverter {

    public static String extractTextWithStyles(String filePath) throws Exception {
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new File(filePath));
        StringBuilder htmlBuilder = new StringBuilder();

        htmlBuilder.append("<!DOCTYPE html>\n");
        htmlBuilder.append("<html>\n<head>\n");
        htmlBuilder.append("<meta charset=\"UTF-8\">\n");
//        htmlBuilder.append("<title>Word文档转换</title>\n");
//        htmlBuilder.append("<style>\n");
//        htmlBuilder.append(generateBaseCss());
//        htmlBuilder.append("</style>\n");
        htmlBuilder.append("</head>\n<body>\n");

        // 获取文档主体内容
        Body body = wordMLPackage.getMainDocumentPart().getJaxbElement().getBody();

        if (body != null) {
            for (Object content : body.getContent()) {
                if (content instanceof JAXBElement) {
                    Object element = ((JAXBElement<?>) content).getValue();
                    if (element instanceof P) {
                        processParagraph((P) element, htmlBuilder, wordMLPackage);
                    }
                    // 忽略其他元素（表格、图片等）
                } else if (content instanceof P) {
                    processParagraph((P) content, htmlBuilder, wordMLPackage);
                }
                // 忽略表格、图片等其他元素
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
                int leftIndent = ind.getLeft().intValue();
                int pixels = dxaToPixels(leftIndent); // 使用正确的单位转换
                indentStyles.add("margin-left: " + pixels + "px");
            }

            if (ind.getFirstLine() != null) {
                int firstLineIndent = ind.getFirstLine().intValue();
                int pixels = dxaToPixels(firstLineIndent);
                indentStyles.add("text-indent: " + pixels + "px");
            }

            if (ind.getRight() != null) {
                int rightIndent = ind.getRight().intValue();
                int pixels = dxaToPixels(rightIndent);
                indentStyles.add("margin-right: " + pixels + "px");
            }

            if (!indentStyles.isEmpty()) {
                return String.join("; ", indentStyles);
            }
        }
        return null;
    }

    /**
     * 将DXA（二十分之一磅）转换为像素
     * 1英寸 = 1440 DXA = 96像素（在96 DPI下）
     */
    private static int dxaToPixels(int dxa) {
        // 1440 DXA = 1英寸 = 96像素
        return (int) Math.round(dxa * 96.0 / 1440);
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