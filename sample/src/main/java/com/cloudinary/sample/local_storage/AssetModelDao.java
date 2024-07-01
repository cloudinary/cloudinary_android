package com.cloudinary.sample.local_storage;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AssetModelDao {
    @Insert
    void insert(AssetModelEntity assetModel);

    @Query("SELECT * FROM asset_items WHERE publicId = :publicId LIMIT 1")
    AssetModelEntity fetchByPublicId(String publicId);

    @Query("SELECT * FROM asset_items")
    LiveData<List<AssetModelEntity>> fetchAll();

    @Query("DELETE FROM asset_items")
    void deleteAll();
}
