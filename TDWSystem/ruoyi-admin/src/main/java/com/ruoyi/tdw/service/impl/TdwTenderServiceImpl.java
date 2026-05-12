package com.ruoyi.tdw.service.impl;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.tdw.domain.TdwBids;
import com.ruoyi.tdw.domain.TdwTenderFile;
import com.ruoyi.tdw.domain.TdwTenderParseReport;
import com.ruoyi.tdw.mapper.TdwBidsMapper;
import com.ruoyi.tdw.mapper.TdwTenderFileMapper;
import com.ruoyi.tdw.mapper.TdwTenderParseReportMapper;
import com.ruoyi.tdw.service.ITdwTenderService;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TdwTenderServiceImpl implements ITdwTenderService
{
    @Autowired
    private TdwBidsMapper tdwBidsMapper;

    @Autowired
    private TdwTenderFileMapper tenderFileMapper;

    @Autowired
    private TdwTenderParseReportMapper parseReportMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<TdwBids> selectTenderBidList(TdwBids query)
    {
        return tenderFileMapper.selectTenderBidList(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createTenderProject(String title, String category, String note, MultipartFile file) throws IOException
    {
        if (title == null || title.trim().length() == 0) {
            throw new IllegalArgumentException("项目名称不能为空");
        }
        TdwBids bid = new TdwBids();
        bid.setTitle(title.trim());
        bid.setCategory(category);
        bid.setNote(note);
        bid.setStatus(1L);
        bid.setCreatedTime(new Date());
        tdwBidsMapper.insertTdwBids(bid);

        TdwTenderFile tenderFile = null;
        if (file != null && !file.isEmpty()) {
            tenderFile = uploadTenderFile(bid.getId(), file);
        }
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("bid", bid);
        result.put("file", tenderFile);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TdwTenderFile uploadTenderFile(Long bidId, MultipartFile file) throws IOException
    {
        if (bidId == null) {
            throw new IllegalArgumentException("bidId不能为空");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("招标文件不能为空");
        }
        String fileUrl = FileUploadUtils.upload(RuoYiConfig.getUploadPath(), file);
        TdwTenderFile tenderFile = new TdwTenderFile();
        tenderFile.setBidId(bidId);
        tenderFile.setOriginalName(file.getOriginalFilename());
        tenderFile.setFileName(FilenameUtils.getName(fileUrl));
        tenderFile.setFileUrl(fileUrl);
        tenderFile.setFileSize(file.getSize());
        tenderFile.setFileType(FilenameUtils.getExtension(file.getOriginalFilename()));
        tenderFile.setParseStatus("uploaded");
        tenderFile.setCreateTime(DateUtils.getNowDate());
        tenderFile.setUpdateTime(DateUtils.getNowDate());
        tenderFileMapper.insertTenderFile(tenderFile);
        return tenderFile;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TdwTenderParseReport mockParse(Long tenderFileId)
    {
        TdwTenderFile tenderFile = tenderFileMapper.selectById(tenderFileId);
        if (tenderFile == null) {
            throw new IllegalArgumentException("招标文件不存在");
        }
        TdwBids bid = tdwBidsMapper.selectTdwBidsById(tenderFile.getBidId());
        String projectName = bid == null ? "标书项目" : bid.getTitle();

        Map<String, Object> report = new HashMap<String, Object>();
        report.put("projectName", projectName);
        report.put("requirementSummary", "Mock解析：需围绕招标文件要求响应技术方案、实施计划、质量保障、服务承诺和商务条款。");
        report.put("scoreItems", "技术方案响应; 实施组织与进度; 质量与风险控制; 售后服务承诺; 商务响应");
        report.put("sourceFileName", tenderFile.getOriginalName());

        TdwTenderParseReport parseReport = new TdwTenderParseReport();
        parseReport.setBidId(tenderFile.getBidId());
        parseReport.setTenderFileId(tenderFile.getId());
        parseReport.setProjectName(projectName);
        parseReport.setRequirementSummary(String.valueOf(report.get("requirementSummary")));
        parseReport.setScoreItems(String.valueOf(report.get("scoreItems")));
        try {
            parseReport.setReportContent(objectMapper.writeValueAsString(report));
        } catch (Exception e) {
            parseReport.setReportContent("{}");
        }
        parseReport.setParseStatus("success");
        parseReport.setCreateTime(DateUtils.getNowDate());
        parseReport.setUpdateTime(DateUtils.getNowDate());
        parseReportMapper.insertParseReport(parseReport);
        tenderFileMapper.updateParseStatus(tenderFile.getId(), "success", parseReport.getId());
        return parseReport;
    }

    @Override
    public List<TdwTenderFile> selectFilesByBidId(Long bidId)
    {
        return tenderFileMapper.selectByBidId(bidId);
    }

    @Override
    public TdwTenderParseReport selectReportById(Long id)
    {
        return parseReportMapper.selectById(id);
    }

    @Override
    public TdwTenderParseReport selectReportByFileId(Long tenderFileId)
    {
        return parseReportMapper.selectByTenderFileId(tenderFileId);
    }

    @Override
    public TdwTenderParseReport selectLatestReportByBidId(Long bidId)
    {
        return parseReportMapper.selectLatestByBidId(bidId);
    }
}
