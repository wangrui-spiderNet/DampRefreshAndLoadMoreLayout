package com.jzycc.layout.damprefreshandloadmorelayout.model;

/**
 * author Jzy(Xiaohuntun)
 * date 18-9-12
 */
public class ZhiHuDto {
    private String title;
    private String imageUrl;

    public ZhiHuDto(String title, String imageUrl) {
        this.title = title;
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
