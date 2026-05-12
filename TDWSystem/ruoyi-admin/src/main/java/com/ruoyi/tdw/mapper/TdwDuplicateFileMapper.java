package com.ruoyi.tdw.mapper;

import java.util.List;
import com.ruoyi.tdw.domain.TdwDuplicateFile;

public interface TdwDuplicateFileMapper
{
    TdwDuplicateFile selectDuplicateFileById(Long id);

    List<TdwDuplicateFile> selectDuplicateFileList(TdwDuplicateFile query);

    int insertDuplicateFile(TdwDuplicateFile file);

    int deleteByTaskIds(Long[] taskIds);
}
