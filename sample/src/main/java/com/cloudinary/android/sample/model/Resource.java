package com.cloudinary.android.sample.model;

import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.sample.core.CloudinaryHelper;

import java.io.Serializable;
import java.util.Date;

public class Resource implements Serializable {
    private String localUri;
    private String cloudinaryPublicId;
    private String requestId;
    private String deleteToken;
    private Date statusTimestamp;
    private String resourceType;
    private String lastError;
    private UploadStatus status;

    public Resource() {
    }

    public Resource(String localUri) {
        this.localUri = localUri;
    }

    public static void copyFields(Resource src, Resource dest) {
        dest.setStatus(src.getStatus());
        dest.setLastError(src.getLastError());
        dest.setDeleteToken(src.getDeleteToken());
        dest.setResourceType(src.getResourceType());
        dest.setCloudinaryPublicId(src.getCloudinaryPublicId());
        dest.setLocalUri(src.getLocalUri());
        dest.setRequestId(src.getRequestId());
        dest.setStatusTimestamp(src.getStatusTimestamp());
    }

    public String getLocalUri() {
        return localUri;
    }

    public void setLocalUri(String localUri) {
        this.localUri = localUri;
    }

    public String getCloudinaryPublicId() {
        return cloudinaryPublicId;
    }

    public void setCloudinaryPublicId(String cloudinaryPublicId) {
        this.cloudinaryPublicId = cloudinaryPublicId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getDeleteToken() {
        return deleteToken;
    }

    public void setDeleteToken(String deleteToken) {
        this.deleteToken = deleteToken;
    }

    public Date getStatusTimestamp() {
        return statusTimestamp;
    }

    public void setStatusTimestamp(Date uploadTimestamp) {
        this.statusTimestamp = uploadTimestamp;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(int errorCode) {
        if (errorCode == ErrorInfo.NO_ERROR) {
            lastError = null;
        } else {
            setLastError(CloudinaryHelper.getPrettyErrorMessage(errorCode));
        }
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public UploadStatus getStatus() {
        return status;
    }

    public void setStatus(UploadStatus status) {
        this.status = status;
    }

    public enum UploadStatus {
        QUEUED,
        UPLOADING,
        UPLOADED,
        RESCHEDULED,
        FAILED,
    }
}
