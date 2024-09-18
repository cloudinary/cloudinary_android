package com.cloudinary.sample.local_storage;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AssetRepository {
    private static AssetRepository instance;
    private AssetModelDao assetModelDao;
    private ExecutorService executorService;

    private AssetRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        assetModelDao = db.assetModelDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public static synchronized AssetRepository getInstance(Context context) {
        if (instance == null) {
            instance = new AssetRepository(context.getApplicationContext());
        }
        return instance;
    }

    public void insert(final AssetModelEntity assetModel) {
        executorService.execute(() -> assetModelDao.insert(assetModel));
    }

    public AssetModelEntity fetchByPublicId(final String publicId) {
        return assetModelDao.fetchByPublicId(publicId);
    }

    public LiveData<List<AssetModelEntity>> fetchAll() {
        return assetModelDao.fetchAll();
    }

    public void deleteAll() {
        executorService.execute(assetModelDao::deleteAll);
    }
}
