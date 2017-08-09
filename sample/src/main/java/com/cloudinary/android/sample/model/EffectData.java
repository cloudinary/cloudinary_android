package com.cloudinary.android.sample.model;

public class EffectData {
    private final String thumbUrl;
    private final String imageUrl;
    private final String name;
    private final String description;

    public EffectData(String thumbUrl, String imageUrl, String name, String description) {
        this.thumbUrl = thumbUrl;
        this.imageUrl = imageUrl;
        this.name = name;
        this.description = description;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getName() {
        return name;
    }
}
