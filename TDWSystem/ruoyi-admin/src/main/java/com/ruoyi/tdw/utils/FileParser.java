package com.ruoyi.tdw.utils;
import org.apache.pdfbox.text.TextPosition;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileParser {
    private static final int MAX_CHARS = 500;
    // 扩展称呼模式，支持前后有描述的情况（如"尊敬的各位专家："）
    private static final Pattern OPENING_PATTERN = Pattern.compile(
            "(.*?)((上午好|下午好|晚上好|同志|代表|委员|常委|领导|各位|嘉宾|同胞|群众|乡亲|朋友|女士|先生|专家|学者|企业家|来宾|媒体|指战员|干警|战友|官兵|武警|国际友人|外国朋友|使节|外交官|审议人|列席|与会人员|青年|同学|团员|青年代表|学子|宗教|信教|教职|人员|尊敬)(.*?))[：:]\\s*",
            Pattern.CASE_INSENSITIVE
    );
    // 日期模式：匹配各种格式的日期（含括号和不含括号）
    private static final Pattern DATE_PATTERN = Pattern.compile(
            "^\\s*(\\(|（)?\\d{4}[-年.]{1}\\d{1,2}[-月.]{1}\\d{1,2}(日)?(\\)|）)?\\s*$",
            Pattern.CASE_INSENSITIVE
    );
    // 扩展日期模式：匹配包含日期的行（如标题后紧跟的日期行）
    private static final Pattern DATE_CONTAIN_PATTERN = Pattern.compile(
            "(.*?)(\\(|（)?\\d{4}[-年.]{1}\\d{1,2}[-月.]{1}\\d{1,2}(日)?(\\)|）)?(.*?)",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * 解析文件内容（入口方法）
     */
    public static String parseFileContent(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(fileName);

        try (InputStream inputStream = file.getInputStream()) {
            switch (fileExtension.toLowerCase()) {
                case "txt":
                    return extractContent(parseTxtFile(inputStream));
                case "docx":
                    return extractContent(parseDocxFile(inputStream));
                case "doc":
                    return extractContent(parseDocFile(inputStream));
                case "pdf":
                    return extractContent(parsePdfFile(inputStream));
                default:
                    throw new UnsupportedOperationException("不支持的文件类型: " + fileExtension);
            }
        }
    }

    /**
     * 从完整文本中提取讲话稿正文前500字（从第4行开始）
     */
    /**
     * 处理段落列表，提取有效内容
     */
    private static String extractContent(List<String> paragraphs) {
//        System.out.println("###########");
//        System.out.println(paragraphs);
        StringBuilder content = new StringBuilder();
        boolean foundOpening = false;
        int validParagraphCount = 0;

        for (String para : paragraphs) {


            // 2. 跳过日期行（精确匹配纯日期）
            if (DATE_PATTERN.matcher(para).matches()) {
                continue;
            }

            // 3. 跳过包含日期的行（如标题后紧跟的混合行）
            if (DATE_CONTAIN_PATTERN.matcher(para).matches()) {
                continue;
            }

            // 4. 跳过包含称呼的行（忽略中间可能的名字）
            if (!foundOpening) {
                if (OPENING_PATTERN.matcher(para).matches()) {
                    foundOpening = true;
                }
                continue;
            }

            // 从第二个有效段落开始提取
            validParagraphCount++;
            if (validParagraphCount >= 1) {
                // 去除开场词
                String cleanedPara = OPENING_PATTERN.matcher(para).replaceAll("");
                content.append(cleanedPara).append(" ");
                // 达到500字时停止
                if (content.length() >= MAX_CHARS) {
                    break;
                }
            }
        }
        if (content.length()==0 ){
            foundOpening = false;
            int validParagraph = 0;
            for (String para : paragraphs) {
                if (!foundOpening) {
                    if (DATE_CONTAIN_PATTERN.matcher(para).matches()) {
                        foundOpening = true;
                    }
                    continue;
                }
                validParagraph++;
                if (validParagraph > 2) {
                    content.append(para);
                }
            }
        }

        if (content.length()==0 ){
            int validParagraph = 0;
            for (String para : paragraphs) {
                validParagraph++;
                if (validParagraph >= 2) {
                    content.append(para);
                }
            }
        }

        // 截取前500字
        return content.length() > MAX_CHARS ? content.substring(0, MAX_CHARS) : content.toString();
    }

    private static List<String>  parseTxtFile(InputStream inputStream) throws IOException {
        List<String> paragraphs = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    paragraphs.add(line.toString());
                }
            }
        }
        return paragraphs;
    }

    private static List<String>  parseDocxFile(InputStream inputStream) throws IOException {
        List<String> paragraphs = new ArrayList<>();
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            for (XWPFParagraph para : document.getParagraphs()) {
                String text = para.getText();
                if (text != null && !text.trim().isEmpty()) {
                    paragraphs.add(text);
                }
            }
            return paragraphs;
        }
    }

    private static List<String> parseDocFile(InputStream inputStream) throws IOException {
        List<String> paragraphs = new ArrayList<>();

        // 使用HWPFDocument处理.doc文件
        try (HWPFDocument document = new HWPFDocument(inputStream)) {
            // 获取文档的全部内容范围
            Range range = document.getRange();

            // 遍历所有段落
            for (int i = 0; i < range.numParagraphs(); i++) {
                Paragraph para = range.getParagraph(i);
                // 获取段落文本（包含换行符等格式符）
                String text = para.text();

                // 清理文本：去除段落末尾的特殊符号（如段落标记）
                if (text != null) {
                    text = text.replaceAll("\\r|\\n", "").trim();
                }

                // 过滤空段落
                if (text != null && !text.isEmpty()) {
                    paragraphs.add(text);
                }
            }
        }

        return paragraphs;
    }
    private static List<String>  parsePdfFile(InputStream inputStream) throws IOException {
        List<String> paragraphs = new ArrayList<>();
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper(){
                @Override
                protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
                    super.writeString(text, textPositions);
                    if (!text.trim().isEmpty()) {
                        paragraphs.add(text);
                    }
                }
            };
            stripper.getText(document);
        }
        return paragraphs;
    }

    /**
     * 从文件名中提取标题（去除扩展名）
     */
    public static String extractTitleFromFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "未命名文档";
        }
        int dotIndex = fileName.lastIndexOf(".");
        return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
    }

    /**
     * 获取文件扩展名
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    public static void zipFiles(List<File> files, String zipPath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(zipPath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            for (File file : files) {
                if (!file.exists()) continue;
                ZipEntry zipEntry = new ZipEntry(file.getName());
                zos.putNextEntry(zipEntry);
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                }
                zos.closeEntry();
            }
        }
    }
}