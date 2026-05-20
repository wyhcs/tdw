package com.ruoyi.tdw.service.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.tdw.ai.prompt.TdwPromptTemplateService;
import com.ruoyi.tdw.ai.service.TdwAiService;
import com.ruoyi.tdw.domain.TdwGallery;
import com.ruoyi.tdw.domain.TdwGalleryImage;
import com.ruoyi.tdw.domain.TdwToolConvertRecord;
import com.ruoyi.tdw.mapper.TdwGalleryImageMapper;
import com.ruoyi.tdw.mapper.TdwGalleryMapper;
import com.ruoyi.tdw.mapper.TdwToolConvertRecordMapper;
import com.ruoyi.tdw.service.ITdwToolService;
import com.ruoyi.tdw.utils.PdfToDocxConverter;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Picture;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TdwToolServiceImpl implements ITdwToolService
{
    private static final long MAX_PDF_CONVERT_SIZE = 50 * 1024 * 1024L;
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024L;
    private static final long MAX_DOC_EXTRACT_SIZE = 50 * 1024 * 1024L;
    private static final String TOOL_TYPE_PDF_TO_WORD = "pdf_to_word";
    private static final String[] PDF_EXTENSIONS = new String[] { "pdf" };
    private static final String[] IMAGE_EXTENSIONS = new String[] { "png", "jpg", "jpeg" };
    private static final String[] DOC_EXTENSIONS = new String[] { "doc", "docx" };

    @Autowired
    private TdwToolConvertRecordMapper convertRecordMapper;

    @Autowired
    private TdwGalleryMapper galleryMapper;

    @Autowired
    private TdwGalleryImageMapper galleryImageMapper;

    @Autowired
    private TdwAiService aiService;

    @Autowired
    private TdwPromptTemplateService promptTemplateService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${tdw.gallery.image-summary-prompt-path:prompts/gallery-image-summary.md}")
    private String imageSummaryPromptPath;

    @Value("${tdw.gallery.doc-image-summary-prompt-path:prompts/gallery-doc-image-summary.md}")
    private String docImageSummaryPromptPath;

    @Value("${ruoyi.pdfToTextPath:}")
    private String pdfToTextPath;

    @Override
    public List<TdwToolConvertRecord> selectConvertRecordList(TdwToolConvertRecord query)
    {
        if (query == null) {
            query = new TdwToolConvertRecord();
        }
        query.setToolType(TOOL_TYPE_PDF_TO_WORD);
        return convertRecordMapper.selectConvertRecordList(query);
    }

    @Override
    public TdwToolConvertRecord selectConvertRecordById(Long id)
    {
        return convertRecordMapper.selectConvertRecordById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TdwToolConvertRecord convertPdfToWord(MultipartFile file) throws IOException
    {
        validateUploadFile(file, PDF_EXTENSIONS, MAX_PDF_CONVERT_SIZE, "PDF文档不能为空", "文件大小不能超过50MB");
        String originalName = StringUtils.defaultString(file.getOriginalFilename(), "PDF文档.pdf");
        String sourceUrl = FileUploadUtils.upload(convertSourceDir(), file);
        File sourceFile = resolveProfileFile(sourceUrl);
        if (sourceFile == null || !sourceFile.exists()) {
            throw new IOException("PDF源文件保存失败");
        }

        String outputName = StringUtils.defaultIfBlank(FilenameUtils.getBaseName(originalName), "PDF转Word") + ".docx";
        String storageName = DateUtils.datePath() + "/" + IdUtils.fastSimpleUUID() + ".docx";
        File outputFile = FileUploadUtils.getAbsoluteFile(convertOutputDir(), storageName);
        try {
            PdfToDocxConverter.convertPdfToDocx(sourceFile.getAbsolutePath(), outputFile.getAbsolutePath(), pdfToTextPath);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("PDF转Word任务被中断", e);
        }

        String outputUrl = FileUploadUtils.getPathFileName(convertOutputDir(), storageName);
        TdwToolConvertRecord record = new TdwToolConvertRecord();
        record.setToolType(TOOL_TYPE_PDF_TO_WORD);
        record.setOriginalName(originalName);
        record.setSourceUrl(sourceUrl);
        record.setSourceSize(file.getSize());
        record.setSourceType("pdf");
        record.setOutputName(outputName);
        record.setOutputUrl(outputUrl);
        record.setOutputSize(outputFile.length());
        record.setOutputType("docx");
        record.setConvertStatus("success");
        record.setCreateTime(DateUtils.getNowDate());
        record.setUpdateTime(DateUtils.getNowDate());
        record.setDelFlag("0");
        convertRecordMapper.insertConvertRecord(record);
        return record;
    }

    @Override
    public void downloadConvertFile(Long id, HttpServletResponse response) throws IOException
    {
        TdwToolConvertRecord record = convertRecordMapper.selectConvertRecordById(id);
        if (record == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "转换记录不存在");
            return;
        }
        File file = resolveProfileFile(record.getOutputUrl());
        if (file == null || !file.exists() || !file.isFile()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "转换文件不存在");
            return;
        }
        String fileName = StringUtils.defaultIfBlank(record.getOutputName(), file.getName());
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));
        response.setContentLengthLong(file.length());
        Files.copy(file.toPath(), response.getOutputStream());
        response.flushBuffer();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteConvertRecordByIds(Long[] ids)
    {
        return convertRecordMapper.deleteConvertRecordByIds(ids);
    }

    @Override
    public List<TdwGallery> selectGalleryList(TdwGallery query)
    {
        return galleryMapper.selectGalleryList(query);
    }

    @Override
    public TdwGallery selectGalleryById(Long id)
    {
        return galleryMapper.selectGalleryById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertGallery(TdwGallery gallery)
    {
        if (gallery == null || StringUtils.isBlank(gallery.getGalleryName())) {
            throw new IllegalArgumentException("图库名称不能为空");
        }
        gallery.setImageCount(0);
        gallery.setCoverUrl("");
        gallery.setCreateTime(DateUtils.getNowDate());
        gallery.setUpdateTime(DateUtils.getNowDate());
        gallery.setDelFlag("0");
        return galleryMapper.insertGallery(gallery);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateGallery(TdwGallery gallery)
    {
        if (gallery == null || gallery.getId() == null) {
            throw new IllegalArgumentException("图库ID不能为空");
        }
        gallery.setUpdateTime(DateUtils.getNowDate());
        return galleryMapper.updateGallery(gallery);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteGalleryByIds(Long[] ids)
    {
        galleryImageMapper.deleteByGalleryIds(ids);
        return galleryMapper.deleteGalleryByIds(ids);
    }

    @Override
    public List<TdwGalleryImage> selectGalleryImageList(TdwGalleryImage query)
    {
        return galleryImageMapper.selectGalleryImageList(query);
    }

    @Override
    public TdwGalleryImage selectGalleryImageById(Long id)
    {
        return galleryImageMapper.selectGalleryImageById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TdwGalleryImage uploadImage(Long galleryId, String imageName, String description, MultipartFile file) throws IOException
    {
        requireGallery(galleryId);
        validateUploadFile(file, IMAGE_EXTENSIONS, MAX_IMAGE_SIZE, "图片不能为空", "单个图片大小不能超过5MB");
        String originalName = StringUtils.defaultString(file.getOriginalFilename());
        String baseName = StringUtils.defaultIfBlank(imageName, FilenameUtils.getBaseName(originalName));
        String extension = FilenameUtils.getExtension(originalName);
        String fileUrl = FileUploadUtils.upload(galleryUploadDir(), file);
        TdwGalleryImage image = new TdwGalleryImage();
        image.setGalleryId(galleryId);
        image.setOriginalName(originalName);
        image.setImageUrl(fileUrl);
        image.setImageSize(file.getSize());
        image.setImageType(normalizeImageExtension(extension, file.getContentType()));
        fillImageSize(image, file);
        GalleryImageSummary summary = summarizeImage(imageSummaryPromptPath, "上传图片", originalName,
                baseName, StringUtils.defaultString(description), image);
        image.setImageName(summary.getTitle());
        image.setDescription(StringUtils.defaultIfBlank(description, "AI总结：" + summary.getTitle()));
        image.setImageTags(summary.getKeywordsText());
        image.setCreateTime(DateUtils.getNowDate());
        image.setUpdateTime(DateUtils.getNowDate());
        image.setDelFlag("0");
        galleryImageMapper.insertGalleryImage(image);
        galleryMapper.updateImageStats(galleryId);
        return image;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<TdwGalleryImage> extractDocImage(Long galleryId, MultipartFile file) throws IOException
    {
        requireGallery(galleryId);
        validateUploadFile(file, DOC_EXTENSIONS, MAX_DOC_EXTRACT_SIZE, "文档不能为空", "文档大小不能超过50MB");
        String originalName = StringUtils.defaultString(file.getOriginalFilename());
        String baseName = StringUtils.defaultIfBlank(FilenameUtils.getBaseName(originalName), "文档抽图");
        List<ExtractedPicture> pictures = extractPictures(file);
        if (pictures.isEmpty()) {
            throw new IllegalArgumentException("未从文档中抽取到可用图片");
        }
        List<TdwGalleryImage> result = new ArrayList<TdwGalleryImage>();
        int index = 1;
        for (ExtractedPicture picture : pictures) {
            if (!isReadableImage(picture.getData())) {
                continue;
            }
            TdwGalleryImage image = new TdwGalleryImage();
            image.setGalleryId(galleryId);
            image.setOriginalName(originalName);
            image.setImageUrl(saveGalleryImageBytes(picture.getData(), picture.getExtension()));
            image.setImageSize((long) picture.getData().length);
            image.setImageType(picture.getExtension());
            fillImageSize(image, picture.getData());
            GalleryImageSummary summary = summarizeImage(docImageSummaryPromptPath, "文档抽图", originalName,
                    baseName + "-图片" + index, "从文档中自动抽取的图库图片", image);
            image.setImageName(summary.getTitle());
            image.setDescription("AI总结：" + summary.getTitle());
            image.setImageTags(summary.getKeywordsText());
            image.setCreateTime(DateUtils.getNowDate());
            image.setUpdateTime(DateUtils.getNowDate());
            image.setDelFlag("0");
            galleryImageMapper.insertGalleryImage(image);
            result.add(image);
            index++;
        }
        if (result.isEmpty()) {
            throw new IllegalArgumentException("文档中图片格式暂不支持预览");
        }
        galleryMapper.updateImageStats(galleryId);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateGalleryImage(TdwGalleryImage image)
    {
        if (image == null || image.getId() == null) {
            throw new IllegalArgumentException("图片ID不能为空");
        }
        TdwGalleryImage old = galleryImageMapper.selectGalleryImageById(image.getId());
        image.setUpdateTime(DateUtils.getNowDate());
        int rows = galleryImageMapper.updateGalleryImage(image);
        if (old != null) {
            galleryMapper.updateImageStats(old.getGalleryId());
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteGalleryImageByIds(Long[] ids)
    {
        Set<Long> galleryIds = new LinkedHashSet<Long>();
        if (ids != null) {
            for (Long id : ids) {
                TdwGalleryImage image = galleryImageMapper.selectGalleryImageById(id);
                if (image != null) {
                    galleryIds.add(image.getGalleryId());
                }
            }
        }
        int rows = galleryImageMapper.deleteGalleryImageByIds(ids);
        for (Long galleryId : galleryIds) {
            galleryMapper.updateImageStats(galleryId);
        }
        return rows;
    }

    private void fillImageSize(TdwGalleryImage image, MultipartFile file)
    {
        try {
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            if (bufferedImage != null) {
                image.setWidth(bufferedImage.getWidth());
                image.setHeight(bufferedImage.getHeight());
            }
        } catch (Exception e) {
            image.setWidth(null);
            image.setHeight(null);
        }
    }

    private void fillImageSize(TdwGalleryImage image, byte[] data)
    {
        try {
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(data));
            if (bufferedImage != null) {
                image.setWidth(bufferedImage.getWidth());
                image.setHeight(bufferedImage.getHeight());
            }
        } catch (Exception e) {
            image.setWidth(null);
            image.setHeight(null);
        }
    }

    private TdwGallery requireGallery(Long galleryId)
    {
        if (galleryId == null) {
            throw new IllegalArgumentException("galleryId不能为空");
        }
        TdwGallery gallery = galleryMapper.selectGalleryById(galleryId);
        if (gallery == null) {
            throw new IllegalArgumentException("图库不存在");
        }
        return gallery;
    }

    private void validateUploadFile(MultipartFile file, String[] allowedExtensions, long maxSize, String emptyMessage, String maxSizeMessage)
    {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(emptyMessage);
        }
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (!isAllowedExtension(extension, allowedExtensions)) {
            throw new IllegalArgumentException("仅支持 " + StringUtils.join(allowedExtensions, "/") + " 格式文件");
        }
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException(maxSizeMessage);
        }
    }

    private boolean isAllowedExtension(String extension, String[] allowedExtensions)
    {
        if (StringUtils.isBlank(extension) || allowedExtensions == null) {
            return false;
        }
        for (String item : allowedExtensions) {
            if (item.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }

    private List<ExtractedPicture> extractPictures(MultipartFile file) throws IOException
    {
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        if ("docx".equalsIgnoreCase(extension)) {
            return extractDocxPictures(file);
        }
        return extractDocPictures(file);
    }

    private List<ExtractedPicture> extractDocxPictures(MultipartFile file) throws IOException
    {
        List<ExtractedPicture> pictures = new ArrayList<ExtractedPicture>();
        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
            for (XWPFPictureData pictureData : document.getAllPictures()) {
                byte[] data = pictureData.getData();
                if (data == null || data.length == 0) {
                    continue;
                }
                pictures.add(new ExtractedPicture(data, normalizeImageExtension(pictureData.suggestFileExtension(),
                        pictureData.getPackagePart() == null ? null : pictureData.getPackagePart().getContentType())));
            }
        }
        return pictures;
    }

    private List<ExtractedPicture> extractDocPictures(MultipartFile file) throws IOException
    {
        List<ExtractedPicture> pictures = new ArrayList<ExtractedPicture>();
        try (HWPFDocument document = new HWPFDocument(file.getInputStream())) {
            for (Picture picture : document.getPicturesTable().getAllPictures()) {
                byte[] data = picture.getContent();
                if (data == null || data.length == 0) {
                    continue;
                }
                pictures.add(new ExtractedPicture(data, normalizeImageExtension(picture.suggestFileExtension(),
                        picture.getMimeType())));
            }
        }
        return pictures;
    }

    private boolean isReadableImage(byte[] data)
    {
        if (data == null || data.length == 0) {
            return false;
        }
        try {
            return ImageIO.read(new ByteArrayInputStream(data)) != null;
        } catch (IOException e) {
            return false;
        }
    }

    private String saveGalleryImageBytes(byte[] data, String extension) throws IOException
    {
        String fileName = DateUtils.datePath() + "/" + IdUtils.fastSimpleUUID() + "." + normalizeImageExtension(extension, null);
        File desc = FileUploadUtils.getAbsoluteFile(galleryUploadDir(), fileName);
        try (FileOutputStream outputStream = new FileOutputStream(desc)) {
            outputStream.write(data);
        }
        return FileUploadUtils.getPathFileName(galleryUploadDir(), fileName);
    }

    private String galleryUploadDir()
    {
        return RuoYiConfig.getUploadPath() + File.separator + "gallery";
    }

    private String convertSourceDir()
    {
        return RuoYiConfig.getProfile() + File.separator + "tool" + File.separator + "pdf-to-word" + File.separator + "source";
    }

    private String convertOutputDir()
    {
        return RuoYiConfig.getProfile() + File.separator + "tool" + File.separator + "pdf-to-word" + File.separator + "word";
    }

    private File resolveProfileFile(String fileUrl)
    {
        if (StringUtils.isBlank(fileUrl)) {
            return null;
        }
        String normalized = fileUrl.replace("\\", "/");
        if (normalized.startsWith(Constants.RESOURCE_PREFIX + "/")) {
            String relative = normalized.substring((Constants.RESOURCE_PREFIX + "/").length());
            return new File(RuoYiConfig.getProfile(), relative);
        }
        if (normalized.startsWith("/")) {
            return new File(RuoYiConfig.getProfile(), normalized.substring(1));
        }
        return new File(RuoYiConfig.getProfile(), normalized);
    }

    private String normalizeImageExtension(String extension, String contentType)
    {
        String value = StringUtils.defaultString(extension).toLowerCase(Locale.ROOT);
        if ("jpeg".equals(value)) {
            return "jpg";
        }
        if ("png".equals(value) || "jpg".equals(value)) {
            return value;
        }
        String type = StringUtils.defaultString(contentType).toLowerCase(Locale.ROOT);
        if (type.contains("png")) {
            return "png";
        }
        if (type.contains("jpeg") || type.contains("jpg")) {
            return "jpg";
        }
        return "jpg";
    }

    private GalleryImageSummary summarizeImage(String promptPath, String sourceType, String originalName,
                                               String defaultTitle, String description, TdwGalleryImage image)
    {
        GalleryImageSummary fallback = fallbackSummary(sourceType, defaultTitle);
        try {
            Map<String, String> variables = new HashMap<String, String>();
            variables.put("sourceType", StringUtils.defaultString(sourceType));
            variables.put("originalName", StringUtils.defaultString(originalName));
            variables.put("defaultTitle", StringUtils.defaultString(defaultTitle));
            variables.put("description", StringUtils.defaultString(description));
            variables.put("imageType", StringUtils.defaultString(image.getImageType()));
            variables.put("imageSize", image.getImageSize() == null ? "" : String.valueOf(image.getImageSize()));
            variables.put("width", image.getWidth() == null ? "" : String.valueOf(image.getWidth()));
            variables.put("height", image.getHeight() == null ? "" : String.valueOf(image.getHeight()));
            String prompt = promptTemplateService.render(promptPath, variables);
            String answer = aiService.extractText(prompt, prompt, "gallery-image-summary");
            GalleryImageSummary parsed = parseGallerySummary(answer, fallback);
            return parsed == null ? fallback : parsed;
        } catch (Exception e) {
            return fallback;
        }
    }

    private GalleryImageSummary fallbackSummary(String sourceType, String defaultTitle)
    {
        String title = StringUtils.abbreviate(StringUtils.defaultIfBlank(defaultTitle, "图库图片"), 40);
        List<String> keywords = new ArrayList<String>();
        if ("文档抽图".equals(sourceType)) {
            keywords.add("文档抽图");
            keywords.add("标书素材");
            keywords.add("图片引用");
        } else {
            keywords.add("上传图片");
            keywords.add("私人图库");
            keywords.add("标书素材");
        }
        return new GalleryImageSummary(title, keywords);
    }

    private GalleryImageSummary parseGallerySummary(String answer, GalleryImageSummary fallback)
    {
        String value = StringUtils.defaultString(answer).trim();
        if (StringUtils.isBlank(value)) {
            return fallback;
        }
        try {
            JsonNode root = objectMapper.readTree(extractJson(value));
            String title = text(root, "title", fallback.getTitle());
            List<String> keywords = new ArrayList<String>();
            JsonNode keywordNode = root.get("keywords");
            if (keywordNode != null && keywordNode.isArray()) {
                for (JsonNode item : keywordNode) {
                    if (StringUtils.isNotBlank(item.asText())) {
                        keywords.add(StringUtils.abbreviate(item.asText().trim(), 30));
                    }
                }
            } else if (keywordNode != null && keywordNode.isTextual()) {
                for (String item : keywordNode.asText().split("[,，、\\s]+")) {
                    if (StringUtils.isNotBlank(item)) {
                        keywords.add(StringUtils.abbreviate(item.trim(), 30));
                    }
                }
            }
            if (keywords.isEmpty()) {
                keywords.addAll(fallback.getKeywords());
            }
            return new GalleryImageSummary(StringUtils.abbreviate(StringUtils.defaultIfBlank(title, fallback.getTitle()), 40), keywords);
        } catch (Exception e) {
            return fallback;
        }
    }

    private String extractJson(String answer)
    {
        String value = StringUtils.defaultString(answer).trim();
        if (value.startsWith("```")) {
            value = value.replaceFirst("^```(?:json|JSON)?\\s*", "");
            value = value.replaceFirst("\\s*```$", "");
        }
        int objectStart = value.indexOf('{');
        int objectEnd = value.lastIndexOf('}');
        if (objectStart >= 0 && objectEnd > objectStart) {
            return value.substring(objectStart, objectEnd + 1);
        }
        return value;
    }

    private String text(JsonNode node, String field, String defaultValue)
    {
        JsonNode value = node == null ? null : node.get(field);
        return value == null || value.isNull() ? defaultValue : value.asText(defaultValue);
    }

    private static class ExtractedPicture
    {
        private final byte[] data;
        private final String extension;

        private ExtractedPicture(byte[] data, String extension)
        {
            this.data = data;
            this.extension = extension;
        }

        private byte[] getData()
        {
            return data;
        }

        private String getExtension()
        {
            return extension;
        }
    }

    private static class GalleryImageSummary
    {
        private final String title;
        private final List<String> keywords;

        private GalleryImageSummary(String title, List<String> keywords)
        {
            this.title = title;
            this.keywords = keywords == null ? new ArrayList<String>() : keywords;
        }

        private String getTitle()
        {
            return title;
        }

        private List<String> getKeywords()
        {
            return keywords;
        }

        private String getKeywordsText()
        {
            return StringUtils.join(keywords, ",");
        }
    }
}
