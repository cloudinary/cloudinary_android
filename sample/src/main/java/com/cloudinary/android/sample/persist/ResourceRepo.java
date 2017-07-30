package com.cloudinary.android.sample.persist;

import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.sample.app.MainApplication;
import com.cloudinary.android.sample.model.Resource;

import java.util.List;

public class ResourceRepo {
    private static final Object lockObject = new Object();
    private static ResourceRepo _instance;
    private final CloudinarySqliteHelper helper;

    private ResourceRepo() {
        this.helper = new CloudinarySqliteHelper(MainApplication.get());
    }

    public static ResourceRepo getInstance() {
        if (_instance == null) {
            synchronized (lockObject) {
                if ((_instance == null)) {
                    _instance = new ResourceRepo();
                }
            }
        }

        return _instance;
    }

    public Resource resourceRescheduled(String requestId, int error) {
        helper.setUploadResultParams(requestId, null, null, Resource.UploadStatus.RESCHEDULED, error);
        return helper.findByRequestId(requestId);
    }

    public Resource resourceFailed(String requestId, int error) {
        helper.setUploadResultParams(requestId, null, null, Resource.UploadStatus.FAILED, error);
        return helper.findByRequestId(requestId);
    }

    public Resource resourceUploaded(String requestId, String publicId, String deleteToken) {
        helper.setUploadResultParams(requestId, publicId, deleteToken, Resource.UploadStatus.UPLOADED, ErrorInfo.NO_ERROR);
        return helper.findByRequestId(requestId);
    }

    public Resource resourceQueued(Resource resource) {
        String localUri = resource.getLocalUri();
        String requestId = resource.getRequestId();
        helper.insertOrUpdateQueuedResource(localUri, requestId, resource.getResourceType(), Resource.UploadStatus.QUEUED);
        return helper.findByRequestId(requestId);
    }

    public Resource resourceUploading(String requestId) {
        helper.setUploadResultParams(requestId, null, null, Resource.UploadStatus.UPLOADING, ErrorInfo.NO_ERROR);
        return helper.findByRequestId(requestId);
    }

    public List<Resource> listAll() {
        return helper.listAll();
    }

    public void clear() {
        helper.deleteAllImages();
    }

    public String getLocalUri(String requestId) {
        return helper.getLocalUri(requestId);
    }

    public void delete(String imageLocalId) {
        helper.delete(imageLocalId);
    }

    public List<Resource> list(List<Resource.UploadStatus> statuses) {
        String[] strStatuses = new String[statuses.size()];
        for (int i = 0; i < statuses.size(); i++) {
            strStatuses[i] = statuses.get(i).name();
        }

        return helper.list(strStatuses);
    }
}

