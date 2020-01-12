package com.cloudinary.android.preprocess;

import android.content.Context;
import android.net.Uri;

/**
 * Returns the encoded video target file. Note: It doesn't do the actual encoding process.
 */
public class VideoEncoder implements ResourceEncoder<Uri> {

    private final String targetFilePath;

    public VideoEncoder(String targetFilePath) {
        this.targetFilePath = targetFilePath;
    }

    @Override
    public String encode(Context context, Uri resource) {
        return targetFilePath;
    }
}
