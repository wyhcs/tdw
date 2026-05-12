package com.ruoyi.tdw.service;

import java.io.IOException;
import java.util.List;
import com.ruoyi.tdw.domain.TdwDuplicateFile;
import com.ruoyi.tdw.domain.TdwDuplicateResult;
import com.ruoyi.tdw.domain.TdwDuplicateTask;
import org.springframework.web.multipart.MultipartFile;

public interface ITdwDuplicateService
{
    List<TdwDuplicateTask> selectDuplicateTaskList(TdwDuplicateTask query);

    TdwDuplicateTask selectDuplicateTaskById(Long id);

    TdwDuplicateTask createDuplicateTask(TdwDuplicateTask task, MultipartFile file) throws IOException;

    TdwDuplicateFile uploadCompareFile(Long taskId, MultipartFile file) throws IOException;

    List<TdwDuplicateResult> runDuplicateTask(Long taskId);

    int deleteDuplicateTaskByIds(Long[] ids);

    List<TdwDuplicateFile> selectDuplicateFileList(TdwDuplicateFile query);

    List<TdwDuplicateResult> selectDuplicateResultList(TdwDuplicateResult query);
}
