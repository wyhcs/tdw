package com.ruoyi.tdw.service;

import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import com.ruoyi.tdw.domain.TdwDownloadRecord;

public interface ITdwDownloadService
{
    List<TdwDownloadRecord> selectDownloadRecordList(TdwDownloadRecord query);

    TdwDownloadRecord selectDownloadRecordById(Long id);

    TdwDownloadRecord recordGeneratedFile(String sourceModule, Long sourceId, String fileName, String fileType, String fileUrl, Long fileSize, String remark);

    TdwDownloadRecord exportReport(String sourceModule, Long sourceId) throws IOException;

    void downloadFile(Long id, HttpServletResponse response) throws IOException;

    int deleteDownloadRecordByIds(Long[] ids);
}
