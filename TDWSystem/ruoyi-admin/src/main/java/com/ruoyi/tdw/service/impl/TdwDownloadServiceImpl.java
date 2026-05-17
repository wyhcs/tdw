package com.ruoyi.tdw.service.impl;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.tdw.domain.TdwDownloadRecord;
import com.ruoyi.tdw.mapper.TdwDownloadRecordMapper;
import com.ruoyi.tdw.service.ITdwDownloadService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TdwDownloadServiceImpl implements ITdwDownloadService
{
    @Autowired
    private TdwDownloadRecordMapper downloadRecordMapper;

    @Override
    public List<TdwDownloadRecord> selectDownloadRecordList(TdwDownloadRecord query)
    {
        return downloadRecordMapper.selectDownloadRecordList(query);
    }

    @Override
    public TdwDownloadRecord selectDownloadRecordById(Long id)
    {
        return downloadRecordMapper.selectDownloadRecordById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TdwDownloadRecord recordGeneratedFile(String sourceModule, Long sourceId, String fileName, String fileType, String fileUrl, Long fileSize, String remark)
    {
        TdwDownloadRecord record = new TdwDownloadRecord();
        record.setModuleType(sourceModule);
        record.setBizId(sourceId);
        record.setDownloadName(fileName);
        record.setDownloadType(StringUtils.defaultIfBlank(sourceModule, "common") + "_export");
        record.setFileFormat(fileType);
        record.setGenerateStatus("success");
        record.setFileUrl(fileUrl);
        record.setFileSize(fileSize == null ? 0L : fileSize);
        record.setDownloadCount(0);
        record.setCreateTime(DateUtils.getNowDate());
        record.setUpdateTime(DateUtils.getNowDate());
        record.setRemark(buildRemark(remark, fileUrl));
        record.setDelFlag("0");
        downloadRecordMapper.insertDownloadRecord(record);
        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TdwDownloadRecord exportReport(String sourceModule, Long sourceId) throws IOException
    {
        if (StringUtils.isBlank(sourceModule)) {
            throw new IllegalArgumentException("sourceModule不能为空");
        }
        if (sourceId == null) {
            throw new IllegalArgumentException("sourceId不能为空");
        }
        String fileType = "html";
        String safeModule = safeFileName(sourceModule);
        String fileName = safeModule + "_report_" + sourceId + "_" + System.currentTimeMillis() + ".html";
        File dir = new File(RuoYiConfig.getProfile() + File.separator + "download" + File.separator + "bid");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, fileName);
        String html = buildReportHtml(sourceModule, sourceId);
        Files.write(file.toPath(), html.getBytes(StandardCharsets.UTF_8));
        String fileUrl = Constants.RESOURCE_PREFIX + "/download/bid/" + fileName;
        return recordGeneratedFile(sourceModule, sourceId, fileName, fileType, fileUrl, file.length(), moduleText(sourceModule) + "报告导出");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void downloadFile(Long id, HttpServletResponse response) throws IOException
    {
        TdwDownloadRecord record = downloadRecordMapper.selectDownloadRecordById(id);
        if (record == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "下载记录不存在");
            return;
        }
        File file = resolveFile(resolveFileUrl(record));
        if (file == null || !file.exists() || !file.isFile()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "文件不存在");
            return;
        }
        String fileName = StringUtils.isBlank(record.getFileName()) ? file.getName() : record.getFileName();
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));
        response.setContentLengthLong(file.length());
        Files.copy(file.toPath(), response.getOutputStream());
        response.flushBuffer();
        downloadRecordMapper.increaseDownloadCount(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteDownloadRecordByIds(Long[] ids)
    {
        return downloadRecordMapper.deleteDownloadRecordByIds(ids);
    }

    private File resolveFile(String fileUrl)
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

    private String resolveFileUrl(TdwDownloadRecord record)
    {
        if (record == null) {
            return null;
        }
        if (StringUtils.isNotBlank(record.getFileUrl())) {
            return record.getFileUrl();
        }
        String remarkUrl = extractRemarkFileUrl(record.getRemark());
        if (StringUtils.isNotBlank(remarkUrl)) {
            return remarkUrl;
        }
        if (StringUtils.isNotBlank(record.getDownloadName())) {
            return Constants.RESOURCE_PREFIX + "/download/bid/" + record.getDownloadName();
        }
        return null;
    }

    private String buildRemark(String remark, String fileUrl)
    {
        String cleanRemark = StringUtils.defaultString(remark);
        if (StringUtils.isBlank(fileUrl)) {
            return cleanRemark;
        }
        String fileUrlPart = "fileUrl=" + fileUrl;
        if (StringUtils.isBlank(cleanRemark)) {
            return fileUrlPart;
        }
        return fileUrlPart + "\n" + cleanRemark;
    }

    private String extractRemarkFileUrl(String remark)
    {
        if (StringUtils.isBlank(remark)) {
            return null;
        }
        String marker = "fileUrl=";
        int index = remark.indexOf(marker);
        if (index < 0) {
            return null;
        }
        String value = remark.substring(index + marker.length());
        int lineBreak = value.indexOf('\n');
        if (lineBreak >= 0) {
            value = value.substring(0, lineBreak);
        }
        return value.trim();
    }

    private String buildReportHtml(String sourceModule, Long sourceId)
    {
        String title = moduleText(sourceModule) + "报告";
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\">");
        html.append("<title>").append(escape(title)).append("</title>");
        html.append("<style>body{font-family:Arial,'Microsoft YaHei',sans-serif;line-height:1.8;color:#303133;padding:32px;}table{border-collapse:collapse;width:100%;}td,th{border:1px solid #dcdfe6;padding:8px;}th{background:#f5f7fa;}</style>");
        html.append("</head><body>");
        html.append("<h1>").append(escape(title)).append("</h1>");
        html.append("<table><tr><th>来源模块</th><td>").append(escape(moduleText(sourceModule))).append("</td></tr>");
        html.append("<tr><th>业务ID</th><td>").append(sourceId).append("</td></tr>");
        html.append("<tr><th>生成时间</th><td>").append(DateUtils.getTime()).append("</td></tr></table>");
        html.append("<p>该文件由下载中心统一生成并登记，后续可替换为对应模块的完整报告模板。</p>");
        html.append("</body></html>");
        return html.toString();
    }

    private String moduleText(String sourceModule)
    {
        if ("plan".equals(sourceModule)) return "AI方案";
        if ("tender".equals(sourceModule)) return "AI标书";
        if ("quality".equals(sourceModule)) return "AI质检";
        if ("duplicate".equals(sourceModule)) return "方案查重";
        if ("knowledge".equals(sourceModule)) return "知识库";
        return sourceModule;
    }

    private String safeFileName(String value)
    {
        return value == null ? "download" : value.replaceAll("[\\\\/:*?\"<>|\\s]+", "_");
    }

    private String escape(String value)
    {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
