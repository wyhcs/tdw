package com.ruoyi.tdw.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTInd;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class PdfToDocxConverter {
    public static void convertPdfToDocx(String pdfPath, String docxPath, String pdftotext)
            throws IOException, InterruptedException {
        // 1. 使用pdftotext提取文本
        String rawText = extractTextWithPdftotext(pdfPath, pdftotext);
        // 2. 处理文本，识别段落
        List<String> paragraphs = processRawText(rawText.toString());

        // 3. 创建DOCX文档并添加格式化段落
        createDocxWithFormattedParagraphs(paragraphs, docxPath);
    }

    public static String extractTextWithPdftotext(String pdfPath, String pdftotext)
            throws IOException, InterruptedException {
        // 创建临时文本文件
        File tempTextFile = File.createTempFile("pdftext", ".txt");
        tempTextFile.deleteOnExit();

        // 构建pdftotext命令
        ProcessBuilder pb = new ProcessBuilder(
                pdftotext,
                "-layout",  // 保持原始布局（有助于段落识别）
                "-enc", "UTF-8",  // 使用UTF-8编码
                pdfPath,
                tempTextFile.getAbsolutePath()
        );

        // 执行命令
        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new IOException("pdftotext执行失败，退出码: " + exitCode);
        }

        // 读取生成的文本文件
        return new String(Files.readAllBytes(tempTextFile.toPath()), StandardCharsets.UTF_8);
    }

    public static List<String> processRawText(String rawText) {
        List<String> paragraphs = new ArrayList<>();

        // 按空行分割段落（pdftotext通常用空行分隔段落）
        String[] rawParagraphs = rawText.split("\\n\\s*\\n");

        for (String rawPara : rawParagraphs) {
            // 清理段落内容：移除多余空白、换行符等
            String cleanedPara = rawPara.trim()
                    .replaceAll("\\r?\\n", " ") // 替换换行符为空格
                    .replaceAll("\\s+", " ");  // 合并多个空格

            if (!cleanedPara.isEmpty()) {
                paragraphs.add(cleanedPara.replaceAll(" ", ""));
            }
        }

        return paragraphs;
    }

    public static void createDocxWithFormattedParagraphs(List<String> paragraphs, String docxPath)
            throws IOException {
        try (XWPFDocument document = new XWPFDocument()) {
            // 创建文档样式
            XWPFParagraph paragraphStyle = document.createParagraph();
            paragraphStyle.setIndentationFirstLine(400); // 首行缩进400单位（约2字符）

            // 添加每个段落
            for (String paraText : paragraphs) {
                XWPFParagraph paragraph = document.createParagraph();

                // 设置段落样式（首行缩进）
                CTP ctp = paragraph.getCTP();
                CTInd ind = ctp.isSetPPr() ? ctp.getPPr().getInd() : ctp.addNewPPr().addNewInd();
                ind.setFirstLine(400); // 首行缩进400单位（约2字符）

                // 创建文本运行并设置内容
                XWPFRun run = paragraph.createRun();
                run.setText(paraText);
                run.setFontSize(12); // 设置字体大小
                run.setFontFamily("宋体"); // 设置中文字体
            }

            // 保存文档
            try (FileOutputStream out = new FileOutputStream(docxPath)) {
                document.write(out);
            }
        }
    }
}

