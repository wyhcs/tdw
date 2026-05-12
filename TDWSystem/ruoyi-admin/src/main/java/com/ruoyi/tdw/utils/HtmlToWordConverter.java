package com.ruoyi.tdw.utils;

import com.aspose.words.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class HtmlToWordConverter {

    /**
     * HTML转Word并保存到指定路径
     */
    public static void convertHtmlToWord(String html, String outputDocx, String outputPdf) throws IOException {
        XWPFDocument doc = new XWPFDocument();

        org.jsoup.nodes.Document parse = org.jsoup.Jsoup.parse(html);
        Element body = parse.body();

        if (body != null) {
            // 处理body下的所有直接子元素，确保每个段落单独成行
            List<Element> childElements = body.children().stream()
                    .filter(el -> "p".equals(el.tagName()) || el.tagName().startsWith("h") || el.tagName().startsWith("ol")
                            || el.tagName().startsWith("ul") || el.tagName().startsWith("li") || "table".equals(el.tagName()))
                    .collect(Collectors.toList());

            for (Element child : childElements) {
                handleElement(doc, child);
            }
        }

        // 写入文件并释放资源
        try (FileOutputStream out = new FileOutputStream(outputDocx)) {
            doc.write(out);
        } finally {
            if (doc != null) {
                doc.close();
            }
        }

        try {
            // 加载Word文档
            com.aspose.words.Document pdfSave = new com.aspose.words.Document(new FileInputStream(outputDocx));

            // 配置PDF保存选项
            PdfSaveOptions saveOptions = new PdfSaveOptions();

            // 关键设置：确保高质量输出和格式保持
            saveOptions.setUseHighQualityRendering(true);
            saveOptions.setJpegQuality(100);
            saveOptions.setOptimizeOutput(true);

            // 设置PDF合规性（根据需求选择）
//             saveOptions.setCompliance(PdfCompliance.PDF_A_1B); // 用于归档
            saveOptions.setCompliance(PdfCompliance.PDF_17); // PDF 1.7标准

            // 字体设置 - 确保字体正确嵌入和替换
            FontSettings fontSettings = new FontSettings();
            FontSubstitutionSettings substitution = fontSettings.getSubstitutionSettings();

            // 中文字体替换规则（针对Linux环境优化）
            TableSubstitutionRule tableSubstitution = substitution.getTableSubstitution();

//            // 设置字体替换优先级
            tableSubstitution.addSubstitutes("仿宋_GB2312", new String[]{"SimSun_GB2312", "FangSong", "SimSun", "SimHei", "Microsoft YaHei", "WenQuanYi Micro Hei", "Heiti TC"});
            tableSubstitution.addSubstitutes("黑体", new String[]{"SimHei", "Microsoft YaHei", "WenQuanYi Micro Hei", "Heiti TC", "SimSun"});
            tableSubstitution.addSubstitutes("宋体", new String[]{"SimSun", "Microsoft YaHei", "WenQuanYi Micro Hei", "Heiti TC", "SimHei"});
            tableSubstitution.addSubstitutes("仿宋", new String[]{"FangSong", "SimSun", "Microsoft YaHei", "WenQuanYi Micro Hei"});
            tableSubstitution.addSubstitutes("楷体", new String[]{"KaiTi", "SimSun", "Microsoft YaHei", "WenQuanYi Micro Hei"});
            tableSubstitution.addSubstitutes("楷体_GB2312", new String[]{"KaiTi_GB2312", "KaiTi", "SimSun", "Microsoft YaHei", "WenQuanYi Micro Hei"});
            tableSubstitution.addSubstitutes("微软雅黑", new String[]{"Microsoft YaHei", "SimHei", "SimSun", "WenQuanYi Micro Hei"});
            tableSubstitution.addSubstitutes("华文宋体", new String[]{"STSong", "SimSun", "Microsoft YaHei", "WenQuanYi Micro Hei"});
            tableSubstitution.addSubstitutes("华文黑体", new String[]{"STHeiti", "SimHei", "Microsoft YaHei", "WenQuanYi Micro Hei"});
            tableSubstitution.addSubstitutes("方正小标宋简体", new String[]{"FZXBSJW", "SimHei", "Microsoft YaHei", "WenQuanYi Micro Hei"});

            // 英文字体替换规则
            tableSubstitution.addSubstitutes("Times New Roman", new String[]{"Times", "Times New Roman", "SimSun_GB2312", "Serif", "DejaVu Serif", "Liberation Serif"});
            tableSubstitution.addSubstitutes("Arial", new String[]{"Arial", "Helvetica", "Sans-serif", "DejaVu Sans", "Liberation Sans"});
            tableSubstitution.addSubstitutes("Calibri", new String[]{"SimSun_GB2312", "Calibri", "Arial", "Helvetica", "Sans-serif", "DejaVu Sans"});
            tableSubstitution.addSubstitutes("Tahoma", new String[]{"SimSun_GB2312", "Tahoma", "Arial", "Helvetica", "Sans-serif", "DejaVu Sans"});
            tableSubstitution.addSubstitutes("Verdana", new String[]{"SimSun_GB2312", "Verdana", "Arial", "Helvetica", "Sans-serif", "DejaVu Sans"});

            // 添加通用字体回退规则
            tableSubstitution.addSubstitutes("serif", new String[]{"SimSun_GB2312", "SimSun"});
            tableSubstitution.addSubstitutes("sans-serif", new String[]{"SimSun_GB2312", "SimSun"});
            tableSubstitution.addSubstitutes("monospace", new String[]{"SimSun_GB2312", "SimSun"});
            tableSubstitution.addSubstitutes("serif", new String[]{"SimSun_GB2312", "Times New Roman", "Times", "SimSun", "SimHei", "DejaVu Serif"});
            tableSubstitution.addSubstitutes("sans-serif", new String[]{"SimSun_GB2312", "Arial", "Helvetica", "Microsoft YaHei", "WenQuanYi Micro Hei", "DejaVu Sans"});
            tableSubstitution.addSubstitutes("monospace", new String[]{"SimSun_GB2312", "Courier New", "Courier", "Monaco", "DejaVu Sans Mono"});

            substitution.getDefaultFontSubstitution().setDefaultFontName("SimSun_GB2312");
            substitution.getDefaultFontSubstitution().setEnabled(true);
            // 应用字体设置到文档
            pdfSave.setFontSettings(fontSettings);

            // 设置PDF保存选项
            saveOptions.setEmbedFullFonts(true);

            // 保留Word文档的布局和格式
            saveOptions.setPreserveFormFields(true);

            // 保存为PDF
            FileOutputStream os = new FileOutputStream(outputPdf);
            pdfSave.save(os, saveOptions);
            os.close();

            System.out.println("Word转PDF成功完成，已保持所有样式格式");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Word转PDF过程中出现错误: " + e.getMessage());
        }
    }

    private static void handleElement(XWPFDocument doc, Element element) {
        String tagName = element.tagName();
        XWPFParagraph paragraph = doc.createParagraph();
        List<String> styles = new ArrayList<>();
        styles.addAll(getElementStyles(element));

        if ("p".equals(tagName) || tagName.startsWith("h")) {
            handleParagraph(doc, paragraph, element, styles);
        } else if ("ol".equals(tagName)) {
            handleOrderedList(doc, element);
        } else if ("ul".equals(tagName)) {
            handleUnorderedList(doc, element);
        } else {
            handleOtherTags(doc, paragraph, element, styles);
        }
    }

    private static void handleOrderedList(XWPFDocument doc, Element ol) {
        // 获取 start 属性
        String startAttr = ol.attr("start");
        int start = 1;
        if (StringUtils.isNotBlank(startAttr)) {
            try {
                start = Integer.parseInt(startAttr.trim());
            } catch (NumberFormatException ignored) {}
        }

        // 尝试从第一个 li 的 p 中获取 font-size / font-family（若有），否则使用默认
        double fontPt = 17.5;
        String eastAsiaFont = "仿宋_GB2312";
        String asciiFont = "Times New Roman";

        Optional<Element> firstLi = ol.children().stream().filter(el -> "li".equals(el.tagName())).findFirst();
        if (firstLi.isPresent()) {
            Element li = firstLi.get();
            // 找到第一个 p 元素或直接使用 li 的 style
            Element p = li.selectFirst("p");
            Element source = p != null ? p : li;
            String fs = getStyleValue(source.attr("style"), "font-size");
            if (StringUtils.isNotBlank(fs)) {
                fontPt = parseFontSizeToPt(fs);
            }
            String ff = getStyleValue(source.attr("style"), "font-family");
            if (StringUtils.isNotBlank(ff)) {
                // 取第一个字体作为 EastAsia（简化）
                String[] parts = ff.split(",");
                String ff0 = parts[0].replaceAll("\"", "").trim();
                if (!ff0.isEmpty()) eastAsiaFont = ff0;
            }
        }

        BigInteger numId = addNumbering(doc, start, fontPt, eastAsiaFont, asciiFont);

        List<Element> items = ol.children().stream()
                .filter(el -> "li".equals(el.tagName()))
                .collect(Collectors.toList());

        for (Element li : items) {
            XWPFParagraph paragraph = doc.createParagraph();
            paragraph.setNumID(numId);
            handleParagraph(doc, paragraph, li, getElementStyles(li));
        }
    }

    private static void handleUnorderedList(XWPFDocument doc, Element ul) {
        // 检查第一个 li 以获取字体/字号
        double fontPt = 17.5;
        String eastAsiaFont = "仿宋_GB2312";
        String asciiFont = "Times New Roman";

        Optional<Element> firstLi = ul.children().stream().filter(el -> "li".equals(el.tagName())).findFirst();
        if (firstLi.isPresent()) {
            Element li = firstLi.get();
            Element p = li.selectFirst("p");
            Element source = p != null ? p : li;
            String fs = getStyleValue(source.attr("style"), "font-size");
            if (StringUtils.isNotBlank(fs)) fontPt = parseFontSizeToPt(fs);
            String ff = getStyleValue(source.attr("style"), "font-family");
            if (StringUtils.isNotBlank(ff)) {
                String[] parts = ff.split(",");
                String ff0 = parts[0].replaceAll("\"", "").trim();
                if (!ff0.isEmpty()) eastAsiaFont = ff0;
            }
        }

        BigInteger numId = addBulletNumbering(doc, fontPt, eastAsiaFont, asciiFont);

        List<Element> items = ul.children().stream()
                .filter(el -> "li".equals(el.tagName()))
                .collect(Collectors.toList());

        for (Element li : items) {
            XWPFParagraph paragraph = doc.createParagraph();
            paragraph.setNumID(numId);
            handleParagraph(doc, paragraph, li, getElementStyles(li));
        }
    }

    private static double parseFontSizeToPt(String value) {
        if (StringUtils.isBlank(value)) return 17.5;
        value = value.trim().toLowerCase();
        try {
            if (value.endsWith("pt")) {
                return Double.parseDouble(value.replace("pt", "").trim());
            } else if (value.endsWith("px")) {
                double px = Double.parseDouble(value.replace("px", "").trim());
                return px / 1.3333333333; // px -> pt 的常见转换
            } else {
                // 可能是纯数字（视为 pt）
                return Double.parseDouble(value);
            }
        } catch (Exception e) {
            return 17.5;
        }
    }

    // 创建有序编号
    private static BigInteger addNumbering(XWPFDocument doc, int start, double fontPt, String eastAsia, String asciiFont) {
        XWPFNumbering numbering = doc.createNumbering();

        // 生成比较安全的唯一 abstractNumId 与 numId
        BigInteger abstractNumId = BigInteger.valueOf(Math.abs(new Random().nextInt(Integer.MAX_VALUE)));
        CTAbstractNum abstractNum = CTAbstractNum.Factory.newInstance();
        abstractNum.setAbstractNumId(abstractNumId);

        CTLvl lvl = abstractNum.addNewLvl();
        lvl.setIlvl(BigInteger.ZERO);
        lvl.addNewNumFmt().setVal(STNumberFormat.DECIMAL);
        lvl.addNewLvlText().setVal("%1.");
        lvl.addNewStart().setVal(BigInteger.valueOf(start));

        // 设置级别的运行属性（rPr）：字体 & 字号（half-points）
        CTRPr rpr = lvl.isSetRPr() ? lvl.getRPr() : lvl.addNewRPr();
        CTFonts lvlFonts = rpr.addNewRFonts();
        lvlFonts.setEastAsia(eastAsia);
        lvlFonts.setAscii(asciiFont);
        lvlFonts.setHAnsi(asciiFont);

        BigInteger halfPoints = BigInteger.valueOf(Math.round(fontPt * 2.0));
        rpr.addNewSz().setVal(halfPoints);
        rpr.addNewSzCs().setVal(halfPoints);

        XWPFAbstractNum xwpfAbstractNum = new XWPFAbstractNum(abstractNum);
        BigInteger newAbstractNumID = numbering.addAbstractNum(xwpfAbstractNum);
        return numbering.addNum(newAbstractNumID);
    }

    // 创建无序项目符号
    private static BigInteger addBulletNumbering(XWPFDocument doc, double fontPt, String eastAsia, String asciiFont) {
        XWPFNumbering numbering = doc.createNumbering();

        BigInteger abstractNumId = BigInteger.valueOf(Math.abs(new Random().nextInt(Integer.MAX_VALUE)));
        CTAbstractNum abstractNum = CTAbstractNum.Factory.newInstance();
        abstractNum.setAbstractNumId(abstractNumId);

        CTLvl lvl = abstractNum.addNewLvl();
        lvl.setIlvl(BigInteger.ZERO);
        lvl.addNewNumFmt().setVal(STNumberFormat.BULLET);
        lvl.addNewLvlText().setVal("•");
        lvl.addNewStart().setVal(BigInteger.ONE);

        // 级别运行属性：字体 & 字号
        CTRPr rpr = lvl.isSetRPr() ? lvl.getRPr() : lvl.addNewRPr();
        CTFonts lvlFonts = rpr.addNewRFonts();
        lvlFonts.setEastAsia(eastAsia);
        lvlFonts.setAscii(asciiFont);
        lvlFonts.setHAnsi(asciiFont);

        BigInteger halfPoints = BigInteger.valueOf(Math.round(fontPt * 2.0));
        rpr.addNewSz().setVal(halfPoints);
        rpr.addNewSzCs().setVal(halfPoints);

        XWPFAbstractNum xwpfAbstractNum = new XWPFAbstractNum(abstractNum);
        BigInteger newAbstractNumID = numbering.addAbstractNum(xwpfAbstractNum);
        return numbering.addNum(newAbstractNumID);
    }

    private static void handleParagraph(XWPFDocument doc, XWPFParagraph paragraph, Element element, List<String> styles) {
        List<Node> nodes = element.childNodes();

        for (Node node : nodes) {
            if (node instanceof TextNode) {
                handleTextNode((TextNode) node, paragraph, styles);
            } else if (node instanceof Element) {
                handleElementNode(doc, paragraph, (Element) node, styles);
            }
        }
        // 处理段落样式（对齐、首行缩进等）
        handleParagraphStyle(paragraph, element);
    }

    private static void handleParagraphStyle(XWPFParagraph paragraph, Element element) {
        String textAlign = getStyleValue(element.attr("style"), "text-align").toLowerCase();
        // 处理文本对齐
        if ("center".equals(textAlign)) {
            paragraph.setAlignment(ParagraphAlignment.CENTER);
        } else if ("right".equals(textAlign)) {
            paragraph.setAlignment(ParagraphAlignment.RIGHT);
        }else if ("left".equals(textAlign)) {
            paragraph.setAlignment(ParagraphAlignment.LEFT);
        }else if ("justify".equals(textAlign)) {
            paragraph.setAlignment(ParagraphAlignment.BOTH);
        } else {
            paragraph.setAlignment(ParagraphAlignment.BOTH);
        }

        String textIndent = getStyleValue(element.attr("style"), "text-indent").toLowerCase();
        // 处理首行缩进
        if (!"".equals(textIndent) || textIndent != null ){
            if (textIndent.endsWith("pt")) {
                paragraph.setIndentationFirstLine(Integer.parseInt(textIndent.replace("px", "").trim()) * 20);
            } else if (textIndent.endsWith("px")) {
                paragraph.setIndentationFirstLine((int) Math.round(Integer.parseInt(textIndent.replace("px", "").trim()) / 1.333 * 20));
            }else {
                paragraph.setIndentationFirstLine(600);
            }
        }else {
            paragraph.setIndentationFirstLine(600);
        }
    }

    private static void handleTextNode(TextNode textNode, XWPFParagraph paragraph, List<String> styles) {
        String text = textNode.text().replaceAll("&nbsp;", " ").trim();
        if (StringUtils.isNotBlank(text)) {
            XWPFRun run = paragraph.createRun();
            run.setText(text);
            // 设置默认字体颜色为黑色，避免与背景色混淆
            run.setColor("000000");
            run.setFontSize(17.5);

            // 设置中文字体为仿宋_GB2312，英文字体为Times New Roman
            CTRPr rPr = run.getCTR().getRPr();
            if (rPr == null) {
                rPr = run.getCTR().addNewRPr();
            }

            CTFonts ctFonts = rPr.addNewRFonts();
            // 中文字体
            ctFonts.setEastAsia("仿宋_GB2312");
            // 英文字体（包括数字和符号）
            ctFonts.setAscii("Times New Roman");
            ctFonts.setHAnsi("Times New Roman");
            // 设置行距类型为多倍行距
//            paragraph.setSpacingLineRule(LineSpacingRule.AUTO);

            // 设置行距值（1.5倍行距对应240，2倍行距对应320）
            paragraph.setSpacingBetween(1.5);
            setFontStyle(styles, run, paragraph);
        }
    }

    private static void handleElementNode(XWPFDocument doc, XWPFParagraph paragraph, Element element, List<String> styles) {
        List<String> childStyles = getElementStyles(element);
        styles.addAll(childStyles);

        List<Node> nodes = element.childNodes();
        for (Node node : nodes) {
            if (node instanceof TextNode) {
                handleTextNode((TextNode) node, paragraph, styles);
            } else if (node instanceof Element) {
                handleElementNode(doc, paragraph, (Element) node, styles);
            }
        }

        styles.removeAll(childStyles);
    }

    private static List<String> getElementStyles(Element element) {
        List<String> styles = new ArrayList<>();
        String styleAttr = element.attr("style");

        if (StringUtils.isNotBlank(styleAttr)) {
            // 拆分样式并过滤空值
            Arrays.stream(styleAttr.split(";"))
                    .filter(StringUtils::isNotBlank)
                    .forEach(styles::add);
        }

        styles.add(addTagStyle(element.tagName(), element));
        return styles;
    }

    private static void handleOtherTags(XWPFDocument doc, XWPFParagraph paragraph, Element element, List<String> styles) {
        List<String> childStyles = getElementStyles(element);
        styles.addAll(childStyles);
        List<Node> nodes = element.childNodes();
        for (Node node : nodes) {
            if (node instanceof TextNode) {
                handleTextNode((TextNode) node, paragraph, styles);
            } else if (node instanceof Element) {
                handleElementNode(doc, paragraph, (Element) node, styles);
            }
        }

        styles.removeAll(childStyles);
    }

    private static String getStyleValue(String style, String key) {
        if (StringUtils.isBlank(style)) return "";

        String[] styles = style.split(";");
        for (String s : styles) {
            if (s.contains(key)) {
                String[] parts = s.split(":", 2);
                return parts.length > 1 ? parts[1].trim() : "";
            }
        }
        return "";
    }

    private static String addTagStyle(String tagName, Element element) {
        List<String> spanStyles = new ArrayList<>();

        switch (tagName) {
            case "font":
                if (StringUtils.isNotBlank(element.attr("face"))) {
                    spanStyles.add("face:" + element.attr("face"));
                }
                if (StringUtils.isNotBlank(element.attr("size"))) {
                    spanStyles.add("size:" + element.attr("size"));
                }
                if (StringUtils.isNotBlank(element.attr("color"))) {
                    spanStyles.add("color:" + element.attr("color"));
                }
                break;
            case "strike":
                spanStyles.add("strike:");
                break;
            case "br":
                spanStyles.add("br:");
                break;
            case "u":
                spanStyles.add("u:");
                break;
            case "i":
                spanStyles.add("i:");
                break;
            case "b":
            case "strong":
                spanStyles.add("b:");
                break;
            case "a":
                if (StringUtils.isNotBlank(element.attr("href"))) {
                    spanStyles.add("a:" + element.attr("href"));
                }
                break;
            case "span":
                String spanStyle = element.attr("style");
                if (StringUtils.isNotBlank(spanStyle)) {
                    // 同时保留字体颜色和背景色，不互相覆盖
                    if (spanStyle.equals("color")) {
                        spanStyles.add("color:" + getStyleValue(spanStyle, "color"));
                    }
                    if (spanStyle.equals("background-color")) {
                        spanStyles.add("background-color:" + getStyleValue(spanStyle, "background-color"));
                    }
                    if (spanStyle.equals("font-size")) {
                        spanStyles.add("font-size:" + getStyleValue(spanStyle, "font-size"));
                    }
                }
                break;
        }

        // 优先处理font-family
        String fontFamily = getStyleValue(element.attr("style"), "font-family");
        if (StringUtils.isNotBlank(fontFamily)) {
            spanStyles.add("font-family:" + "仿宋_GB2312");
            spanStyles.add("font-family:" + fontFamily);
//            if (fontFamily.toLowerCase().contains("times") ||
//                    fontFamily.toLowerCase().contains("arial") ||
//                    (!fontFamily.contains("仿宋") && !fontFamily.contains("黑体") && !fontFamily.contains("楷体") &&
//                            !fontFamily.contains("方正")  && !fontFamily.contains("微软雅黑")  && !fontFamily.toLowerCase().contains("simhei") )
//            ){
//                spanStyles.add("font-family:" + "仿宋_GB2312");
//            }else {
//                spanStyles.add("font-family:" + fontFamily);
//            }
        }
        return String.join(";", spanStyles);
    }

    private static void setFontStyle(List<String> styles, XWPFRun run, XWPFParagraph paragraph) {
        // 默认字体配置
        String defaultChineseFont = "仿宋_GB2312";
        String defaultEnglishFont = "Times New Roman";
        // 记录是否已设置字体颜色
        boolean hasSetFontColor = false;

//        System.out.println(styles);

        for (String styleValue : styles) {
            if (StringUtils.isBlank(styleValue)) continue;
            // 处理包含多个样式的情况（如span标签）
            String[] multipleStyles = styleValue.split(";");
            for (String s : multipleStyles) {
                if (StringUtils.isBlank(s)) continue;
                String[] styleParts = s.split(":", 2);
                if (styleParts.length < 2) continue;
                String style = styleParts[0].trim().toLowerCase();
                String value = styleParts[1].trim();
                switch (style) {
                    case "p":
//                        System.out.println("@@@@@@@@@@");

//                        setFontFamily(run, defaultChineseFont, defaultEnglishFont);
//                        run.setFontSize(17.5);
                        // 获取段落属性，如果不存在则创建

                        break;
                    case "h1":

//                        run.setBold(true);
//                        run.setColor("000000");
//                        hasSetFontColor = true;
//                        setFontFamily(run, "FZXiaoBiaoSong-B05S", defaultEnglishFont);
//                        run.setFontSize(22);
                        break;
                    case "h2":
////                        run.setBold(true);
//                        run.setColor("000000");
//                        hasSetFontColor = true;
//                        setFontFamily(run, "黑体", defaultEnglishFont);
//                        run.setFontSize(17.5);
                        break;
                    case "h3":
//                        run.setBold(true);
//                        run.setColor("000000");
//                        hasSetFontColor = true;
//                        setFontFamily(run, defaultChineseFont, defaultEnglishFont);
//                        run.setFontSize(16);
                        break;
                    case "line-height":
                        try {
                            run.setTextPosition(Integer.parseInt(value));
                        } catch (NumberFormatException e) {
                            // 忽略无效值
                        }
                        break;
                    case "font-family":
                        String[] fonts = value.split(",");
                        String chineseFont = fonts.length > 0 ?
                                fonts[0].trim().replaceAll("\"", "") : defaultChineseFont;
                        String englishFont = fonts.length > 1 ?
                                fonts[1].trim().replaceAll("\"", "") : defaultEnglishFont;
                        setFontFamily(run, chineseFont, englishFont);
                        break;
                    case "font-size":
                        run.setFontSize(fontSizeConvert(value));
                        break;
                    case "color":
                        // 显式设置字体颜色
                        // 显式设置字体颜色
                        String colorValue = value.trim();
                        String hexColor;

                        // 检查是否是颜色名（如 "blue"），转换为十六进制
                        if (isColorName(colorValue)) {
                            hexColor = getHexFromColorName(colorValue);
                        } else {
                            // 处理十六进制颜色（移除 # 符号）
                            hexColor = colorValue.replaceAll("#", "");
                        }

                        // 确保十六进制颜色是6位（某些库要求）
                        if (hexColor.length() == 3) { // 处理简写形式如 #FFF
                            hexColor = expandShortHex(hexColor);
                        }

                        run.setColor(hexColor);
                        hasSetFontColor = true;
                        break;
//                        run.setColor(value.replaceAll("#", ""));
//                        hasSetFontColor = true;
//                        break;
                    case "strike":
                        run.setStrikeThrough(true);
                        break;
                    case "br":
                        run.addCarriageReturn();
                        break;
                    case "u":
                        run.setUnderline(UnderlinePatterns.SINGLE);
                        break;
                    case "i":
                        run.setItalic(true);
                        break;
                    case "b":
                        run.setBold(true);
                        break;
                    case "background-color":
                        // 仅设置背景色，不影响字体颜色
                        CTRPr rPr = run.getCTR().getRPr();
                        if (rPr == null) {
                            rPr = run.getCTR().addNewRPr();
                        }
                        CTHighlight highlight =  rPr.addNewHighlight();
                        highlight.setVal(getBackground(value));
                        break;
                    case "a":
                        // 处理超链接
                        run.setColor("0563C1");
                        hasSetFontColor = true;
                        run.setUnderline(UnderlinePatterns.SINGLE);
                        break;
                }
            }
        }

        // 如果没有设置过字体颜色，确保使用默认黑色
        if (!hasSetFontColor) {
            run.setColor("000000");
        }
    }

    private static void setFontFamily(XWPFRun run, String chineseFont, String englishFont) {
        CTRPr rPr = run.getCTR().getRPr();
        if (rPr == null) {
            rPr = run.getCTR().addNewRPr();
        }

        CTFonts ctFonts = rPr.addNewRFonts();

        // 分别设置中文字体和英文/数字字体
        ctFonts.setEastAsia(chineseFont);  // 中文字体
        ctFonts.setAscii(englishFont);     // 英文/数字字体
        ctFonts.setHAnsi(englishFont);     // 高ANSI字符集
    }

    private static Double fontSizeConvert(String level) {
        if (StringUtils.isBlank(level)) {
            return 17.5; // 默认字号
        }
        // 支持pt单位的直接转换
        if (level.endsWith("pt")) {
            try {
                return Double.parseDouble(level.replace("pt", "").trim());
            } catch (NumberFormatException e) {
                // 忽略无效格式
            }
        }
        // 像素转换逻辑
        switch (level) {
            case "1": return 7.0;
            case "2": return 8.0;
            case "3": return 9.0;
            case "4": return 10.0;
            case "5": return 14.0;
            case "6": return 18.0;
            case "7": return 28.0;
            case "8": return 36.0;
            case "9": return 48.0;
            case "10": return 72.0;
            default: return 12.0;
        }
    }

    // 判断是否为颜色名（非十六进制格式）
    private static boolean isColorName(String value) {
        // 简单判断：如果不是以#开头且包含字母，则视为颜色名
        return !value.startsWith("#") && value.matches(".*[a-zA-Z].*");
    }

    // 颜色名转十六进制
    private static String getHexFromColorName(String colorName) {
        // 常用颜色映射表
        Map<String, String> colorMap = new HashMap<>();
        colorMap.put("black", "000000");
        colorMap.put("white", "FFFFFF");
        colorMap.put("red", "FF0000");
        colorMap.put("green", "008000");
        colorMap.put("blue", "0000FF");
        colorMap.put("yellow", "FFFF00");
        colorMap.put("gray", "808080");
        colorMap.put("orange", "FFA500");
        colorMap.put("purple", "800080");

        // 转为小写并获取对应十六进制，默认黑色
        return colorMap.getOrDefault(colorName.toLowerCase(), "000000");
    }

    // 扩展简写的十六进制（如 F00 -> FF0000）
    private static String expandShortHex(String shortHex) {
        StringBuilder sb = new StringBuilder();
        for (char c : shortHex.toCharArray()) {
            sb.append(c).append(c);
        }
        return sb.toString();
    }

    // 背景色转换，特别处理用户示例中的颜色
    private static STHighlightColor.Enum getBackground(String color) {
        if (StringUtils.isBlank(color)) return STHighlightColor.NONE;

        color = color.replaceAll(" ", "").toLowerCase();

        // 处理示例中特殊的背景色
        if ("#169179".equals(color)) {
            return STHighlightColor.GREEN;
        }
        if ("#e03e2d".equals(color)) {
            return STHighlightColor.RED;
        }

        // 处理标准颜色
        if ("yellow".equals(color) || "rgb(255,255,0)".equals(color) || "#ffff00".equals(color)) {
            return STHighlightColor.YELLOW;
        } else if ("lime".equals(color) || "rgb(0,255,0)".equals(color) || "#00ff00".equals(color)) {
            return STHighlightColor.GREEN;
        } else if ("aqua".equals(color) || "rgb(0,255,255)".equals(color) || "#00ffff".equals(color)) {
            return STHighlightColor.CYAN;
        } else if ("fuchsia".equals(color) || "rgb(255,0,255)".equals(color) || "#ff00ff".equals(color)) {
            return STHighlightColor.MAGENTA;
        } else if ("blue".equals(color) || "rgb(0,0,255)".equals(color) || "#0000ff".equals(color)) {
            return STHighlightColor.BLUE;
        } else if ("red".equals(color) || "rgb(255,0,0)".equals(color) || "#ff0000".equals(color)) {
            return STHighlightColor.RED;
        }

        // 处理其他十六进制颜色
        if (color.startsWith("#")) {
            try {
                // 简单的颜色近似算法
                int r = Integer.parseInt(color.substring(1, 3), 16);
                int g = Integer.parseInt(color.substring(3, 5), 16);
                int b = Integer.parseInt(color.substring(5, 7), 16);

                if (r > g && r > b) return STHighlightColor.RED;
                if (g > r && g > b) return STHighlightColor.GREEN;
                if (b > r && b > g) return STHighlightColor.BLUE;
                return STHighlightColor.YELLOW;
            } catch (Exception e) {
                return STHighlightColor.NONE;
            }
        }

        return STHighlightColor.NONE;
    }
}
