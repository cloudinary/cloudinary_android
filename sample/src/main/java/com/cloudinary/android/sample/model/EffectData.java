package com.cloudinary.android.sample.model;

import com.cloudinary.Transformation;

public class EffectData {
    private final String publicId;
    private final Transformation transformation;
    private final String name;
    private final String description;

    public EffectData(String publicId, Transformation transformation, String name, String description) {
        this.publicId = publicId;
        this.transformation = transformation;
        this.name = name;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public String getPublicId() {
        return publicId;
    }

    public Transformation getTransformation() {
        return transformation;
    }
}
