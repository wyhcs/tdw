package com.ruoyi.tdw.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * Private gallery.
 */
public class TdwGallery extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private String galleryName;
    private String description;
    private Integer imageCount;
    private String coverUrl;
    private String delFlag;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getGalleryName() { return galleryName; }
    public void setGalleryName(String galleryName) { this.galleryName = galleryName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getImageCount() { return imageCount; }
    public void setImageCount(Integer imageCount) { this.imageCount = imageCount; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
