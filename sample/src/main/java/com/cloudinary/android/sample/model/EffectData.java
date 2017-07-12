package com.cloudinary.android.sample.model;

public class EffectData {
    private String thumbUrl;
    private String imageUrl;
    private String description;

    public EffectData(String thumbUrl, String imageUrl, String description) {
        this.thumbUrl = thumbUrl;
        this.imageUrl = imageUrl;
        this.description = description;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
