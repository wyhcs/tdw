package com.ruoyi.tdw.mapper;

import java.util.List;
import com.ruoyi.tdw.domain.TdwDuplicateFile;

public interface TdwDuplicateFileMapper
{
    TdwDuplicateFile selectDuplicateFileById(Long id);

    TdwDuplicateFile selectSourceFileByTaskId(Long taskId);

    List<TdwDuplicateFile> selectCompareFilesByTaskId(Long taskId);

    int countCompareFilesByTaskId(Long taskId);

    List<TdwDuplicateFile> selectDuplicateFileList(TdwDuplicateFile query);

    int insertDuplicateFile(TdwDuplicateFile file);

    int updateDuplicateFile(TdwDuplicateFile file);

    int deleteDuplicateFileByIds(Long[] ids);

    int deleteByTaskIds(Long[] taskIds);
}
