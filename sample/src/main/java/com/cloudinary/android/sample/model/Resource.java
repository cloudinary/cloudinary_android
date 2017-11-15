package com.cloudinary.android.sample.model;

import java.io.Serializable;
import java.util.Date;

public class Resource implements Serializable {
    private String localUri;
    private String name;
    private String cloudinaryPublicId;
    private String requestId;
    private String deleteToken;
    private Date statusTimestamp;
    private String resourceType;
    private String lastErrorDesc;
    private int lastErrorCode;
    private UploadStatus status;

    public Resource() {
    }

    public Resource(String localUri, String name, String type) {
        this.localUri = localUri;
        this.name = name;
        this.resourceType = type;
    }

    public static void copyFields(Resource src, Resource dest) {
        dest.setStatus(src.getStatus());
        dest.setLastErrorDesc(src.getLastErrorDesc());
        dest.setDeleteToken(src.getDeleteToken());
        dest.setResourceType(src.getResourceType());
        dest.setCloudinaryPublicId(src.getCloudinaryPublicId());
        dest.setLocalUri(src.getLocalUri());
        dest.setRequestId(src.getRequestId());
        dest.setStatusTimestamp(src.getStatusTimestamp());
        dest.setName(src.getName());
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

    public String getLastErrorDesc() {
        return lastErrorDesc;
    }

    public void setLastErrorDesc(String lastErrorDesc) {
        this.lastErrorDesc = lastErrorDesc;
    }

    public void setLastError(int errorCode) {
        lastErrorCode = errorCode;
    }

    public UploadStatus getStatus() {
        return status;
    }

    public void setStatus(UploadStatus status) {
        this.status = status;
    }

    public int getLastErrorCode() {
        return lastErrorCode;
    }

    public void setLastErrorCode(int lastErrorCode) {
        this.lastErrorCode = lastErrorCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public enum UploadStatus {
        QUEUED,
        UPLOADING,
        UPLOADED,
        RESCHEDULED,
        FAILED,
        CANCELLED,
    }
}
