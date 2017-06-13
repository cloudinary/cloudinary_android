package com.cloudinary.android.sample.persist;

import com.cloudinary.android.sample.app.MainApplication;
import com.cloudinary.android.sample.model.Image;

import java.util.Date;
import java.util.List;

public class ImageRepository {
    private static final Object lockObject = new Object();
    private static ImageRepository _instance;
    private final CloudinarySqliteHelper helper;

    private ImageRepository() {
        this.helper = new CloudinarySqliteHelper(MainApplication.get());
    }

    public static ImageRepository getInstance() {
        if (_instance == null) {
            synchronized (lockObject) {
                if ((_instance == null)) {
                    _instance = new ImageRepository();
                }
            }
        }

        return _instance;
    }

    public void imageUploaded(String requestId, String publicId, String deleteToken, int width, int height, Date timestamp) {
        helper.setUploadResultParams(requestId, publicId, width, height, deleteToken, timestamp);
    }

    public boolean uploadQueued(Image image) {
        String localUri = image.getLocalUri();
        String requestId = image.getRequestId();
        return helper.insertNewImage(localUri, requestId);
    }

    public List<Image> listImages() {
        return helper.readImages();
    }

    public void clear() {
        helper.deleteAllImages();
    }
}

