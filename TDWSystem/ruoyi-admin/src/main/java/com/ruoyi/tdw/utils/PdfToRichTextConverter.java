package com.ruoyi.tdw.utils;

import com.vladsch.flexmark.util.ast.Node;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PdfToRichTextConverter {

    public String convertToRichText(File dest) {
        // 用于存储提取的文本行
        List<String> textLines = new ArrayList<>();
        // 用于存储行间距信息，帮助识别段落
        List<Float> lineSpacings = new ArrayList<>();
        // 结果字符串构建器
        StringBuilder markDown = new StringBuilder();

        try (PDDocument document = PDDocument.load(dest)) {
            // 自定义PDF文本提取器，收集行信息和行间距
            PDFTextStripper stripper = new PDFTextStripper() {
                private float lastY = -1;

                @Override
                protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
                    if (textPositions.isEmpty()) return;

                    float currentY = textPositions.get(0).getY();
                    // 计算行间距（仅当不是第一行时）
                    if (lastY != -1) {
                        lineSpacings.add(lastY - currentY);
                    }
                    lastY = currentY;

                    textLines.add(text);
                }
            };

            // 执行文本提取
            stripper.getText(document);

            // 处理提取的文本，识别段落
            String processedText = processParagraphs(textLines, lineSpacings);

            // 使用Flexmark进行转换
            MutableDataSet options = new MutableDataSet();
            options.set(HtmlRenderer.SOFT_BREAK, "\n");
            options.set(HtmlRenderer.ESCAPE_HTML, true);

            Parser parser = Parser.builder(options).build();
            HtmlRenderer renderer = HtmlRenderer.builder(options).build();

            // 解析处理后的文本
            Node documentNode = parser.parse(processedText);
            markDown.append(renderer.render(documentNode));

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return markDown.toString();
    }

    /**
     * 处理文本行，根据行间距识别段落并添加适当的换行
     */
    private String processParagraphs(List<String> textLines, List<Float> lineSpacings) {
        if (textLines.isEmpty()) return "";

        // 计算平均行间距，用于判断段落边界
        float avgSpacing = calculateAverageSpacing(lineSpacings);
        // 段落间距阈值（平均行间距的1.5倍）
        float paragraphThreshold = avgSpacing * 1.5f;

        StringBuilder processed = new StringBuilder();
        processed.append(textLines.get(0));

        // 遍历文本行，根据行间距添加段落分隔
        for (int i = 1; i < textLines.size(); i++) {
            // 检查是否是段落分隔
            boolean isParagraphBreak = (i-1 < lineSpacings.size() &&
                    lineSpacings.get(i-1) > paragraphThreshold);

            if (isParagraphBreak) {
                // 段落之间添加两个换行符
                processed.append("\n\n").append(textLines.get(i));
            } else {
                // 同一段落内的行添加一个空格
                processed.append(" ").append(textLines.get(i));
            }
        }

        return processed.toString();
    }

    /**
     * 计算平均行间距
     */
    private float calculateAverageSpacing(List<Float> spacings) {
        if (spacings.isEmpty()) return 10.0f; // 默认值

        float sum = 0;
        for (float spacing : spacings) {
            sum += spacing;
        }
        return sum / spacings.size();
    }

}
