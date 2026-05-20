package com.ruoyi.tdw.service;

import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import com.ruoyi.tdw.domain.TdwGallery;
import com.ruoyi.tdw.domain.TdwGalleryImage;
import com.ruoyi.tdw.domain.TdwToolConvertRecord;
import org.springframework.web.multipart.MultipartFile;

public interface ITdwToolService
{
    List<TdwToolConvertRecord> selectConvertRecordList(TdwToolConvertRecord query);

    TdwToolConvertRecord selectConvertRecordById(Long id);

    TdwToolConvertRecord convertPdfToWord(MultipartFile file) throws IOException;

    void downloadConvertFile(Long id, HttpServletResponse response) throws IOException;

    int deleteConvertRecordByIds(Long[] ids);

    List<TdwGallery> selectGalleryList(TdwGallery query);

    TdwGallery selectGalleryById(Long id);

    int insertGallery(TdwGallery gallery);

    int updateGallery(TdwGallery gallery);

    int deleteGalleryByIds(Long[] ids);

    List<TdwGalleryImage> selectGalleryImageList(TdwGalleryImage query);

    TdwGalleryImage selectGalleryImageById(Long id);

    TdwGalleryImage uploadImage(Long galleryId, String imageName, String description, MultipartFile file) throws IOException;

    List<TdwGalleryImage> extractDocImage(Long galleryId, MultipartFile file) throws IOException;

    int updateGalleryImage(TdwGalleryImage image);

    int deleteGalleryImageByIds(Long[] ids);
}
