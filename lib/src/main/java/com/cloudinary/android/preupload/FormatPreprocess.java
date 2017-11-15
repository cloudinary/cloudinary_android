package com.cloudinary.android.preupload;

import android.content.Context;
import android.graphics.Bitmap;

import com.cloudinary.android.payload.ErrorCreatingNewBitmapException;

/**
 * Preprocess phase to re-encode the resource with specific format and quality before uploading
 */
public class FormatPreprocess extends ImagePreprocess {

    private final Format format;
    private final int quality;

    /**
     * Create a new format processing phase
     *
     * @param format  The format to encode the bitmap
     * @param quality The quality to use when encoding the bitmap
     */
    public FormatPreprocess(Format format, int quality) {
        this.format = format;
        this.quality = quality;
    }

    /**
     * Override default finalize behaviour to save with the given quality and format
     * @param context Android context
     * @param resource The bitmap to save
     * @return The filepath of the saved bitmap
     * @throws ErrorCreatingNewBitmapException
     */
    @Override
    public String finalize(Context context, Bitmap resource) throws ErrorCreatingNewBitmapException {
        return saveFile(context, resource, quality, format);
    }
}
