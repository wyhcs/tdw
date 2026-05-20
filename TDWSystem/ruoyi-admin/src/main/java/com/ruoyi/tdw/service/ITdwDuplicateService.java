package com.ruoyi.tdw.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import com.ruoyi.tdw.domain.TdwDuplicateFile;
import com.ruoyi.tdw.domain.TdwDuplicateResult;
import com.ruoyi.tdw.domain.TdwDuplicateTask;
import com.ruoyi.tdw.domain.TdwDownloadRecord;
import com.ruoyi.tdw.domain.dto.TdwDuplicateRunRequest;
import org.springframework.web.multipart.MultipartFile;

public interface ITdwDuplicateService
{
    List<TdwDuplicateTask> selectDuplicateTaskList(TdwDuplicateTask query);

    TdwDuplicateTask selectDuplicateTaskById(Long id);

    TdwDuplicateTask createDuplicateTask(TdwDuplicateTask task, MultipartFile file) throws IOException;

    TdwDuplicateFile uploadCompareFile(Long taskId, MultipartFile file) throws IOException;

    List<TdwDuplicateFile> uploadCompareFiles(Long taskId, MultipartFile[] files) throws IOException;

    List<TdwDuplicateResult> runDuplicateTask(Long taskId);

    List<TdwDuplicateResult> runDuplicateTask(Long taskId, TdwDuplicateRunRequest request);

    int deleteDuplicateTaskByIds(Long[] ids);

    int deleteDuplicateFileByIds(Long[] ids);

    int updateCompareLibraries(Long taskId, List<Long> compareLibraryIds);

    List<TdwDuplicateFile> selectDuplicateFileList(TdwDuplicateFile query);

    List<TdwDuplicateResult> selectDuplicateResultList(TdwDuplicateResult query);

    Map<String, Object> selectDuplicateReport(Long taskId) throws IOException;

    TdwDownloadRecord exportDuplicateReport(Long taskId) throws IOException;
}
