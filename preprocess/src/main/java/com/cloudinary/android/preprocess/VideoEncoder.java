package com.cloudinary.android.preprocess;

import android.content.Context;
import android.net.Uri;

/**
 * Returns the encoded video target file. Note: It doesn't do the actual encoding process.
 */
public class VideoEncoder implements ResourceEncoder<Uri> {

    @Override
    public String encode(Context context, Uri resource) {
        return resource.toString();
    }
}
