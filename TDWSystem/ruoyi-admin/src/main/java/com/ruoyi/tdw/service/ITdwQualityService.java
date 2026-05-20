package com.ruoyi.tdw.service;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import com.ruoyi.tdw.domain.TdwBids;
import com.ruoyi.tdw.domain.TdwQualityFramework;
import com.ruoyi.tdw.domain.TdwQualityItem;
import com.ruoyi.tdw.domain.TdwQualityResult;
import com.ruoyi.tdw.domain.TdwQualityTask;

public interface ITdwQualityService
{
    List<TdwBids> selectQualityProjectList(TdwBids query);

    Map<String, Object> createQualityProject(MultipartFile file) throws IOException;

    Map<String, Object> selectQualityProjectDetail(Long bidId);

    TdwQualityFramework uploadQualityFramework(Long bidId, MultipartFile file) throws IOException;

    TdwQualityFramework extractQualityFramework(Long bidId);

    List<TdwQualityFramework> selectQualityFrameworkList(TdwQualityFramework query);

    List<TdwQualityItem> selectQualityItemList(TdwQualityItem query);

    TdwQualityItem createQualityItem(TdwQualityItem item);

    int updateQualityItem(TdwQualityItem item);

    int deleteQualityItemByIds(Long[] ids);

    Map<String, Object> createQualityVersion(Long bidId, Long frameworkId, MultipartFile file) throws IOException;

    void exportQualityFramework(Long frameworkId, HttpServletResponse response) throws IOException;

    List<TdwQualityTask> selectQualityTaskList(TdwQualityTask query);

    TdwQualityTask selectQualityTaskById(Long id);

    TdwQualityTask createQualityTask(TdwQualityTask task);

    List<TdwQualityResult> runQualityTask(Long taskId);

    int deleteQualityTaskByIds(Long[] ids);

    List<TdwQualityResult> selectQualityResultList(TdwQualityResult query);
}
