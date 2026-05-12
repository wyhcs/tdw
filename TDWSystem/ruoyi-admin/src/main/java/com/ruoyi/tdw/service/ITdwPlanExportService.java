package com.ruoyi.tdw.service;

import java.io.IOException;

import com.ruoyi.tdw.domain.dto.TdwPlanExportRequest;
import com.ruoyi.tdw.domain.dto.TdwPlanExportResult;

/**
 * AI方案导出服务。
 */
public interface ITdwPlanExportService
{
    TdwPlanExportResult exportHtml(TdwPlanExportRequest request) throws IOException;
}
