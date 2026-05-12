package com.ruoyi.tdw.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PDF文档文本提取工具类（精确保留字体、字号、居中、首行缩进等格式）
 */
public class PdfToHtmlConverter extends PDFTextStripper {

    private StringBuilder htmlBuilder;
    private float lastYPosition = -1;
    private boolean isNewParagraph = true;
    private TextStyle currentStyle;
    private ParagraphStyle currentParagraphStyle;
    private StringBuilder currentParagraphBuilder;
    private float pageWidth = 0;
    private float firstLineIndent = 0;

    public PdfToHtmlConverter() throws IOException {
        super();
        this.htmlBuilder = new StringBuilder();
        this.currentStyle = new TextStyle();
        this.currentParagraphStyle = new ParagraphStyle();
        this.currentParagraphBuilder = new StringBuilder();
        this.setSortByPosition(true);
        this.setAddMoreFormatting(true);
    }

    public static String extractTextWithStyles(String filePath) throws Exception {
        PDDocument document = PDDocument.load(new File(filePath));
        PdfToHtmlConverter converter = new PdfToHtmlConverter();

        converter.htmlBuilder.append("<!DOCTYPE html>\n");
        converter.htmlBuilder.append("<html>\n<head>\n");
        converter.htmlBuilder.append("<meta charset=\"UTF-8\">\n");
        converter.htmlBuilder.append("<title>PDF文档转换</title>\n");
        converter.htmlBuilder.append("<style>\n");
        converter.htmlBuilder.append(generateBaseCss());
        converter.htmlBuilder.append("</style>\n");
        converter.htmlBuilder.append("</head>\n<body>\n");

        // 开始处理文档
        converter.getText(document);

        // 确保最后一段被处理
        converter.finalizeCurrentParagraph();

        converter.htmlBuilder.append("</body>\n</html>");
        document.close();

        return converter.htmlBuilder.toString();
    }

    @Override
    protected void startPage(PDPage page) throws IOException {
        super.startPage(page);
        // 重置页面相关状态
        lastYPosition = -1;
        if (page != null && page.getMediaBox() != null) {
            pageWidth = page.getMediaBox().getWidth();
        } else {
            pageWidth = 595; // A4宽度默认值（单位：点）
        }
        firstLineIndent = 0;
    }

    @Override
    protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
        if (text == null || text.trim().isEmpty() || textPositions == null || textPositions.isEmpty()) {
            return;
        }

        // 获取当前文本位置的Y坐标和X坐标
        TextPosition firstPosition = textPositions.get(0);
        float currentY = firstPosition.getY();
        float currentX = firstPosition.getX();

        // 初始化lastYPosition
        if (lastYPosition == -1) {
            lastYPosition = currentY;
        }

        // 检测是否需要新段落（Y坐标变化较大）
        boolean shouldStartNewParagraph = false;
        float lineHeight = getAverageLineHeight(textPositions);

        if (Math.abs(currentY - lastYPosition) > lineHeight * 1.2) {
            shouldStartNewParagraph = true;
        }

        // 如果当前是文档开头，开始新段落
        if (isNewParagraph) {
            shouldStartNewParagraph = true;
        }

        if (shouldStartNewParagraph) {
            finalizeCurrentParagraph();

            // 分析段落样式
            analyzeParagraphStyle(textPositions);

            currentParagraphBuilder.append("<p style=\"").append(currentParagraphStyle.toCss()).append("\">");
            isNewParagraph = false;
        }

        // 处理文本内容
        processTextContent(text, textPositions);

        lastYPosition = currentY;
    }

    /**
     * 分析段落样式（对齐方式、缩进等）
     */
    /**
     * 分析段落样式（对齐方式、缩进等）
     */
    private void analyzeParagraphStyle(List<TextPosition> textPositions) {
        if (textPositions == null || textPositions.isEmpty()) {
            return;
        }

        TextPosition firstPos = textPositions.get(0);
        TextPosition lastPos = textPositions.get(textPositions.size() - 1);

        float leftMargin = firstPos.getX();
        float rightMargin = pageWidth - (lastPos.getX() + lastPos.getWidth());
        float textWidth = lastPos.getX() + lastPos.getWidth() - firstPos.getX();

        // 改进的对齐方式检测逻辑
//        if (isCentered(textPositions)) {
        currentParagraphStyle.setAlignment("justify"); // 居中
//        } else if (isRightAligned(textPositions)) {
//            currentParagraphStyle.setAlignment("right"); // 右对齐
//        } else if (isJustified(textPositions)) {
//            currentParagraphStyle.setAlignment("justify"); // 两端对齐
//        } else {
//            currentParagraphStyle.setAlignment("left"); // 左对齐
//        }

        // 检测首行缩进（如果左缩进大于20点）
        if (leftMargin > 20) {
            currentParagraphStyle.setFirstLineIndent(leftMargin / 2 + "pt");
        } else {
            currentParagraphStyle.setFirstLineIndent("0");
        }

        // 设置左右边距（转换为相对单位）
        currentParagraphStyle.setMarginLeft((leftMargin / 5) + "px");
        currentParagraphStyle.setMarginRight((rightMargin / 5) + "px");
    }

    /**
     * 判断是否为居中文本
     */
    private boolean isCentered(List<TextPosition> textPositions) {
        if (textPositions.size() < 2) return false;

        TextPosition first = textPositions.get(0);
        TextPosition last = textPositions.get(textPositions.size() - 1);

        float textWidth = last.getX() + last.getWidth() - first.getX();
        float centerX = pageWidth / 2;
        float textCenterX = first.getX() + textWidth / 2;

        // 文本中心与页面中心的偏差在5%以内认为是居中
        return Math.abs(textCenterX - centerX) < pageWidth * 0.05;
    }

    /**
     * 判断是否为右对齐文本
     */
    private boolean isRightAligned(List<TextPosition> textPositions) {
        if (textPositions.size() < 2) return false;

        TextPosition last = textPositions.get(textPositions.size() - 1);
        float textEnd = last.getX() + last.getWidth();

        // 文本结束位置接近页面右边界（偏差在5%以内）
        return Math.abs(textEnd - pageWidth) < pageWidth * 0.05;
    }

    /**
     * 判断是否为两端对齐文本
     */
    private boolean isJustified(List<TextPosition> textPositions) {
        if (textPositions.size() < 3) return false;

        // 计算字符间距的均匀性
        float totalSpacing = 0;
        int spacingCount = 0;

        for (int i = 1; i < textPositions.size(); i++) {
            TextPosition prev = textPositions.get(i - 1);
            TextPosition curr = textPositions.get(i);
            float spacing = curr.getX() - (prev.getX() + prev.getWidth());
            if (spacing > 0) {
                totalSpacing += spacing;
                spacingCount++;
            }
        }

        if (spacingCount == 0) return false;

        float avgSpacing = totalSpacing / spacingCount;

        // 检查间距的均匀性（标准差较小）
        float variance = 0;
        for (int i = 1; i < textPositions.size(); i++) {
            TextPosition prev = textPositions.get(i - 1);
            TextPosition curr = textPositions.get(i);
            float spacing = curr.getX() - (prev.getX() + prev.getWidth());
            if (spacing > 0) {
                variance += Math.pow(spacing - avgSpacing, 2);
            }
        }

        float stdDev = (float) Math.sqrt(variance / spacingCount);

        // 间距相对均匀且文本接近页面宽度
        TextPosition first = textPositions.get(0);
        TextPosition last = textPositions.get(textPositions.size() - 1);
        float textWidth = last.getX() + last.getWidth() - first.getX();

        return stdDev < avgSpacing * 0.3 && textWidth > pageWidth * 0.8;
    }

    /**
     * 判断是否为左对齐文本
     */
    private boolean isLeftAligned(List<TextPosition> textPositions) {
        if (textPositions.isEmpty()) return false;

        TextPosition first = textPositions.get(0);

        // 文本开始位置接近页面左边界（偏差在5%以内）
        return first.getX() < pageWidth * 0.05;
    }

    /**
     * 计算平均行高
     */
    private float getAverageLineHeight(List<TextPosition> textPositions) {
        if (textPositions.isEmpty()) {
            return 15.0f; // 默认行高
        }

        float totalHeight = 0;
        for (TextPosition tp : textPositions) {
            totalHeight += tp.getHeight();
        }
        return totalHeight / textPositions.size();
    }

    /**
     * 处理文本内容
     */
    private void processTextContent(String text, List<TextPosition> textPositions) {
        // 获取第一个文本位置的样式
        TextPosition firstPosition = textPositions.get(0);
        TextStyle newStyle = getTextStyle(firstPosition);

        // 检查样式是否变化
        if (!newStyle.equals(currentStyle)) {
            // 关闭之前的span
            if (currentStyle != null) {
                currentParagraphBuilder.append("</span>");
            }
            // 开始新的span
            currentParagraphBuilder.append("<span style=\"").append(newStyle.toCss()).append("\">");
            currentStyle = newStyle;
        }

        // 添加文本内容
        currentParagraphBuilder.append(escapeHtml(text));
    }

    /**
     * 完成当前段落处理
     */
    private void finalizeCurrentParagraph() {
        if (!isNewParagraph) {
            // 关闭当前样式span
            if (currentStyle != null) {
                currentParagraphBuilder.append("</span>");
                currentStyle = null;
            }

            currentParagraphBuilder.append("</p>\n");
            htmlBuilder.append(currentParagraphBuilder);
            currentParagraphBuilder.setLength(0); // 清空StringBuilder

            // 重置段落样式
            currentParagraphStyle = new ParagraphStyle();
        }
        isNewParagraph = true;
    }

    @Override
    protected void writeLineSeparator() throws IOException {
        // 行分隔符 - 在段落内换行
        if (!isNewParagraph) {
            currentParagraphBuilder.append("<br/>");
        }
    }

    @Override
    protected void writeWordSeparator() throws IOException {
        // 单词分隔符（空格）
        if (!isNewParagraph) {
            currentParagraphBuilder.append(" ");
        }
    }

    @Override
    protected void endDocument(PDDocument document) throws IOException {
        // 文档结束时确保当前段落被处理
        finalizeCurrentParagraph();
    }

    /**
     * 获取文本样式
     */
    private TextStyle getTextStyle(TextPosition textPosition) {
        TextStyle style = new TextStyle();

        try {
            // 字体名称
            PDFont font = textPosition.getFont();
            if (font != null) {
                String fontName = getFontName(font);
                style.setFontFamily(fontName);
            }

            // 字号（转换为磅值）
            float fontSize = textPosition.getFontSizeInPt();
            if (fontSize > 0) {
                style.setFontSize(fontSize);
            }

            // 粗体检测
            try {
                String fontName = textPosition.getFont().getName();
                if (fontName != null) {
                    String lowerName = fontName.toLowerCase();
                    if (lowerName.contains("bold") || lowerName.contains("black") ||
                            lowerName.contains("heavy") || lowerName.contains("700") ||
                            lowerName.contains("800") || lowerName.contains("900")) {
                        style.setBold(true);
                    }
                }

                // 通过字符宽度判断粗体
                String text = textPosition.getUnicode();
                if (text != null && text.length() > 0) {
                    float expectedWidth = textPosition.getFont().getStringWidth(text) / 1000 * textPosition.getFontSize();
                    if (textPosition.getWidth() > expectedWidth * 1.15) {
                        style.setBold(true);
                    }
                }
            } catch (Exception e) {
                // 忽略错误
            }

            // 斜体检测
            try {
                String fontName = textPosition.getFont().getName();
                if (fontName != null && (fontName.toLowerCase().contains("italic") ||
                        fontName.toLowerCase().contains("oblique"))) {
                    style.setItalic(true);
                }
            } catch (Exception e) {
                // 忽略错误
            }

        } catch (Exception e) {
            // 忽略样式提取错误
        }

        return style;
    }

    /**
     * 获取字体名称
     */
    private String getFontName(PDFont font) {
        if (font == null) {
            return "Arial, sans-serif";
        }

        String fontName = "";
        try {
            fontName = font.getName();
            if (fontName == null || fontName.isEmpty()) {
                return "Arial, sans-serif";
            }

            // 移除字体名称中的前缀
            if (fontName.contains("+")) {
                fontName = fontName.substring(fontName.indexOf("+") + 1);
            }
        } catch (Exception e) {
            return "Arial, sans-serif";
        }

        // 映射常见字体
        Map<String, String> fontMapping = new HashMap<>();
        fontMapping.put("仿宋_GB2312", "仿宋_GB2312");
        fontMapping.put("黑体", "黑体");
        fontMapping.put("宋体", "宋体");
        fontMapping.put("仿宋", "仿宋");
        fontMapping.put("楷体", "楷体");
        fontMapping.put("楷体_GB2312", "楷体_GB2312");
        fontMapping.put("微软雅黑", "微软雅黑");
        fontMapping.put("华文宋体", "华文宋体");
        fontMapping.put("华文黑体", "华文黑体");

        // 英文字体映射
        fontMapping.put("Times New Roman", "Times New Roman, serif");
        fontMapping.put("Arial", "Arial, Helvetica, sans-serif");
        fontMapping.put("Calibri", "Calibri, sans-serif");
        fontMapping.put("Tahoma", "Tahoma, Geneva, sans-serif");
        fontMapping.put("Verdana", "Verdana, Geneva, sans-serif");

        return fontMapping.getOrDefault(fontName, fontName + ", sans-serif");
    }

    /**
     * 文本样式类
     */
    private static class TextStyle {
        private String fontFamily = "Arial, sans-serif";
        private float fontSize = 12.0f;
        private boolean bold = false;
        private boolean italic = false;

        public String toCss() {
            List<String> styles = new ArrayList<>();

            styles.add("font-family: " + fontFamily);
            styles.add("font-size: " + fontSize + "pt");

            if (bold) {
                styles.add("font-weight: bold");
            }

            if (italic) {
                styles.add("font-style: italic");
            }

            return String.join("; ", styles);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;

            TextStyle other = (TextStyle) obj;
            return Float.compare(other.fontSize, fontSize) == 0 &&
                    bold == other.bold &&
                    italic == other.italic &&
                    java.util.Objects.equals(fontFamily, other.fontFamily);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(fontFamily, fontSize, bold, italic);
        }

        // Getter和Setter方法
        public String getFontFamily() { return fontFamily; }
        public void setFontFamily(String fontFamily) { this.fontFamily = fontFamily; }
        public float getFontSize() { return fontSize; }
        public void setFontSize(float fontSize) { this.fontSize = fontSize; }
        public boolean isBold() { return bold; }
        public void setBold(boolean bold) { this.bold = bold; }
        public boolean isItalic() { return italic; }
        public void setItalic(boolean italic) { this.italic = italic; }
    }

    /**
     * 段落样式类
     */
    private static class ParagraphStyle {
        private String alignment = "left";
        private String firstLineIndent;
        private String marginLeft;
        private String marginRight;

        public String toCss() {
            List<String> styles = new ArrayList<>();

            styles.add("text-align: " + alignment);

            if (firstLineIndent != null) {
                styles.add("text-indent: " + firstLineIndent);
            }

            if (marginLeft != null) {
                styles.add("margin-left: " + marginLeft);
            }

            if (marginRight != null) {
                styles.add("margin-right: " + marginRight);
            }

            styles.add("line-height: 1.6");
            styles.add("margin: 8px 0");

            return String.join("; ", styles);
        }

        // Getter和Setter方法
        public String getAlignment() { return alignment; }
        public void setAlignment(String alignment) { this.alignment = alignment; }
        public String getFirstLineIndent() { return firstLineIndent; }
        public void setFirstLineIndent(String firstLineIndent) { this.firstLineIndent = firstLineIndent; }
        public String getMarginLeft() { return marginLeft; }
        public void setMarginLeft(String marginLeft) { this.marginLeft = marginLeft; }
        public String getMarginRight() { return marginRight; }
        public void setMarginRight(String marginRight) { this.marginRight = marginRight; }
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
                "font-family: Arial, sans-serif; " +
                "line-height: 1.6; " +
                "color: #000000; " +
                "margin: 20px; " +
                "background: #ffffff; " +
                "} " +
                "p { " +
                "margin: 8px 0; " +
                "line-height: 1.6; " +
                "} " +
                "span { " +
                "white-space: pre-wrap; " +
                "} " +
                "br { " +
                "display: block; " +
                "content: ''; " +
                "margin: 2px 0; " +
                "}";
    }
}