package com.ruoyi.tdw.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * Private gallery image.
 */
public class TdwGalleryImage extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long galleryId;
    private String imageName;
    private String originalName;
    private String imageUrl;
    private Long imageSize;
    private String imageType;
    private Integer width;
    private Integer height;
    private String description;
    private String imageTags;
    private String delFlag;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getGalleryId() { return galleryId; }
    public void setGalleryId(Long galleryId) { this.galleryId = galleryId; }
    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }
    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Long getImageSize() { return imageSize; }
    public void setImageSize(Long imageSize) { this.imageSize = imageSize; }
    public String getImageType() { return imageType; }
    public void setImageType(String imageType) { this.imageType = imageType; }
    public Integer getWidth() { return width; }
    public void setWidth(Integer width) { this.width = width; }
    public Integer getHeight() { return height; }
    public void setHeight(Integer height) { this.height = height; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageTags() { return imageTags; }
    public void setImageTags(String imageTags) { this.imageTags = imageTags; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
