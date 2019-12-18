package com.cloudinary.android.uploadwidget.model;

import android.net.Uri;

public class Image {

    private Uri sourceUri;
    private Uri resultUri;

    public Image(Uri sourceUri) {
        this.sourceUri = sourceUri;
    }

    public Uri getSourceUri() {
        return sourceUri;
    }

    public void setSourceUri(Uri sourceUri) {
        this.sourceUri = sourceUri;
    }

    public Uri getResultUri() {
        return resultUri;
    }

    public void setResultUri(Uri resultUri) {
        this.resultUri = resultUri;
    }
}
