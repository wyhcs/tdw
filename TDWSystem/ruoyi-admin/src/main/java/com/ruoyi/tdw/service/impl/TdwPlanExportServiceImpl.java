package com.ruoyi.tdw.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.tdw.domain.TdwBids;
import com.ruoyi.tdw.domain.TdwContents;
import com.ruoyi.tdw.domain.TdwOutlines;
import com.ruoyi.tdw.domain.dto.TdwPlanExportRequest;
import com.ruoyi.tdw.domain.dto.TdwPlanExportResult;
import com.ruoyi.tdw.mapper.TdwBidsMapper;
import com.ruoyi.tdw.mapper.TdwContentsMapper;
import com.ruoyi.tdw.service.ITdwDownloadService;
import com.ruoyi.tdw.service.ITdwOutlinesService;
import com.ruoyi.tdw.service.ITdwPlanExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * AI方案HTML导出。
 */
@Service
public class TdwPlanExportServiceImpl implements ITdwPlanExportService
{
    @Autowired
    private TdwBidsMapper tdwBidsMapper;

    @Autowired
    private TdwContentsMapper tdwContentsMapper;

    @Autowired
    private ITdwOutlinesService tdwOutlinesService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ITdwDownloadService downloadService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public TdwPlanExportResult exportHtml(TdwPlanExportRequest request) throws IOException
    {
        if (request == null || request.getBidId() == null) {
            throw new IllegalArgumentException("bidId不能为空");
        }
        TdwBids bid = tdwBidsMapper.selectTdwBidsById(request.getBidId());
        if (bid == null) {
            throw new IllegalArgumentException("方案不存在");
        }

        List<TdwOutlines> outlineTree = tdwOutlinesService.selectOutlineTree(request.getBidId());
        String html = buildHtml(bid, outlineTree, Boolean.TRUE.equals(request.getIncludeEmptyOutline()));
        String safeTitle = safeFileName(bid.getTitle() == null ? "AI方案" : bid.getTitle());
        String fileName = safeTitle + "_" + request.getBidId() + "_" + System.currentTimeMillis() + ".html";
        File dir = new File(RuoYiConfig.getProfile() + File.separator + "download" + File.separator + "bid");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, fileName);
        Files.write(file.toPath(), html.getBytes(StandardCharsets.UTF_8));

        TdwPlanExportResult result = new TdwPlanExportResult();
        result.setFileName("bid/" + fileName);
        result.setFileUrl(Constants.RESOURCE_PREFIX + "/download/bid/" + fileName);
        result.setDownloadName(fileName);
        result.setFileFormat("html");
        downloadService.recordGeneratedFile("plan", request.getBidId(), fileName, "html", result.getFileUrl(), file.length(), "AI方案HTML导出");
        return result;
    }

    private String buildHtml(TdwBids bid, List<TdwOutlines> outlineTree, boolean includeEmptyOutline)
    {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\">");
        html.append("<title>").append(escape(bid.getTitle())).append("</title>");
        html.append("<style>");
        html.append("body{font-family:Arial,'Microsoft YaHei',sans-serif;line-height:1.8;color:#303133;padding:32px;}");
        html.append("h1{font-size:26px;text-align:center;margin:0 0 28px;}h2{font-size:22px;margin-top:28px;}h3{font-size:18px;margin-top:20px;}h4{font-size:16px;margin-top:16px;}");
        html.append("table{border-collapse:collapse;width:100%;margin:10px 0 18px;}th,td{border:1px solid #dcdfe6;padding:8px;text-align:left;}th{background:#f5f7fa;}");
        html.append(".diagram{border:1px solid #ebeef5;padding:12px;margin:10px 0 18px;background:#fafafa;}.mermaid{white-space:pre-wrap;background:#f5f7fa;padding:10px;}");
        html.append("</style></head><body>");
        html.append("<h1>").append(escape(bid.getTitle())).append("</h1>");
        for (TdwOutlines outline : outlineTree) {
            appendOutline(html, outline, includeEmptyOutline);
        }
        html.append("</body></html>");
        return html.toString();
    }

    private void appendOutline(StringBuilder html, TdwOutlines outline, boolean includeEmptyOutline)
    {
        if (outline == null) {
            return;
        }
        List<TdwContents> contents = tdwContentsMapper.selectTdwContentsByOutlineId(outline.getId());
        if (includeEmptyOutline || !contents.isEmpty() || hasVisibleChild(outline)) {
            String tag = outline.getLevel() == 1 ? "h2" : outline.getLevel() == 2 ? "h3" : "h4";
            html.append("<").append(tag).append(">")
                    .append(escape(outline.getTitle()))
                    .append("</").append(tag).append(">");
            for (TdwContents content : contents) {
                appendContent(html, content);
            }
        }
        for (TdwOutlines child : outline.getChildren()) {
            appendOutline(html, child, includeEmptyOutline);
        }
    }

    private boolean hasVisibleChild(TdwOutlines outline)
    {
        if (outline.getChildren() == null || outline.getChildren().isEmpty()) {
            return false;
        }
        return true;
    }

    private void appendContent(StringBuilder html, TdwContents content)
    {
        Map<String, Object> data = parseContent(content.getContent());
        if (content.getContentType() != null && content.getContentType() == 2) {
            appendTable(html, data);
        } else if (content.getContentType() != null && content.getContentType() == 3) {
            appendDiagram(html, data);
        } else {
            Object text = firstValue(data, "text", "content");
            html.append("<p>").append(escape(text == null ? content.getContent() : String.valueOf(text))).append("</p>");
        }
    }

    private void appendTable(StringBuilder html, Map<String, Object> data)
    {
        Object title = firstValue(data, "title", "name");
        if (title != null) {
            html.append("<p><strong>").append(escape(String.valueOf(title))).append("</strong></p>");
        }
        Object headersObj = data.get("headers");
        Object rowsObj = data.get("rows");
        html.append("<table>");
        if (headersObj instanceof List) {
            html.append("<thead><tr>");
            for (Object header : (List<?>) headersObj) {
                html.append("<th>").append(escape(String.valueOf(header))).append("</th>");
            }
            html.append("</tr></thead>");
        }
        if (rowsObj instanceof List) {
            html.append("<tbody>");
            for (Object row : (List<?>) rowsObj) {
                html.append("<tr>");
                if (row instanceof List) {
                    for (Object cell : (List<?>) row) {
                        html.append("<td>").append(escape(String.valueOf(cell))).append("</td>");
                    }
                } else {
                    html.append("<td>").append(escape(String.valueOf(row))).append("</td>");
                }
                html.append("</tr>");
            }
            html.append("</tbody>");
        }
        html.append("</table>");
    }

    private void appendDiagram(StringBuilder html, Map<String, Object> data)
    {
        html.append("<div class=\"diagram\">");
        Object title = firstValue(data, "title", "name");
        Object description = data.get("description");
        Object imageUrl = firstValue(data, "imageUrl", "url");
        Object mermaid = data.get("mermaid");
        if (title != null) {
            html.append("<p><strong>").append(escape(String.valueOf(title))).append("</strong></p>");
        }
        if (description != null) {
            html.append("<p>").append(escape(String.valueOf(description))).append("</p>");
        }
        if (imageUrl != null && String.valueOf(imageUrl).trim().length() > 0) {
            html.append("<img src=\"").append(escape(String.valueOf(imageUrl))).append("\" style=\"max-width:100%;\" />");
        }
        if (mermaid != null && String.valueOf(mermaid).trim().length() > 0) {
            html.append("<pre class=\"mermaid\">").append(escape(String.valueOf(mermaid))).append("</pre>");
        }
        html.append("</div>");
    }

    private Map<String, Object> parseContent(String content)
    {
        try {
            return objectMapper.readValue(content, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return java.util.Collections.<String, Object>singletonMap("text", content);
        }
    }

    private Object firstValue(Map<String, Object> data, String first, String second)
    {
        Object value = data.get(first);
        return value == null ? data.get(second) : value;
    }

    private void insertDownloadRecord(Long bidId, String downloadName, String fileUrl)
    {
        try {
            jdbcTemplate.update(
                    "insert into bid_download_record(module_type, biz_id, file_id, download_name, download_type, file_format, generate_status, download_count, owner_id, create_by, create_time, update_by, update_time, remark, del_flag) values(?, ?, null, ?, ?, ?, ?, 0, null, '', now(), '', now(), ?, '0')",
                    "plan", bidId, downloadName, "plan_export", "html", "success", fileUrl);
        } catch (Exception ignored) {
            // 下载中心表未初始化时，不阻断HTML导出。
        }
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

    private String safeFileName(String title)
    {
        return title.replaceAll("[\\\\/:*?\"<>|\\s]+", "_");
    }
}
