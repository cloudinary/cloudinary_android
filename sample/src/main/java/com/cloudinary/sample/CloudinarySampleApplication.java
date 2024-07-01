package com.cloudinary.sample;

import android.app.Application;

import com.cloudinary.sample.local_storage.AssetRepository;
import com.facebook.drawee.backends.pipeline.Fresco;


public class CloudinarySampleApplication extends Application {
    private AssetRepository assetRepository;
    public static String APP_NAME = "cloudinary_sample_app";
    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);
        assetRepository = AssetRepository.getInstance(this);
    }

    public AssetRepository getAssetRepository() {
        return assetRepository;
    }


}