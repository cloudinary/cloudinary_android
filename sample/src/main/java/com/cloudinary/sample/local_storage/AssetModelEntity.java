package com.cloudinary.sample.local_storage;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "asset_items")
public class AssetModelEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String deliveryType;
    private String assetType;
    private String transformation;
    private String publicId;
    private String url;

    public AssetModelEntity(String publicId, String deliveryType, String assetType, String transformation, String url) {
        this.deliveryType = deliveryType;
        this.assetType = assetType;
        this.transformation = transformation;
        this.publicId = publicId;
        this.url = url;
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }

    public String getAssetType() {
        return assetType;
    }

    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }

    public String getTransformation() {
        return transformation;
    }

    public void setTransformation(String transformation) {
        this.transformation = transformation;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
