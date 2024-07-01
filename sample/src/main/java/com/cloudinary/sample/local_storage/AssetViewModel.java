package com.cloudinary.sample.local_storage;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class AssetViewModel extends AndroidViewModel {
    private AssetRepository repository;
    private LiveData<List<AssetModelEntity>> assetModels;

    public AssetViewModel(Application application) {
        super(application);
        repository = AssetRepository.getInstance(application);
        assetModels = repository.fetchAll();
    }

    public LiveData<List<AssetModelEntity>> getAssetModels() {
        return assetModels;
    }

    public void insert(AssetModelEntity model) {
        repository.insert(model);
    }
}
