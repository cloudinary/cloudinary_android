package com.cloudinary.android.uploadwidget.ui;

import android.net.Uri;

public class Image {

    public Uri sourceUri;
    public Uri resultUri;

    public Image(Uri sourceUri) {
        this.sourceUri = sourceUri;
    }
}
