package com.cloudinary.android.preprocess;

import android.content.Context;
import android.graphics.Bitmap;

import com.cloudinary.android.preprocess.ResourceCreationException;
import com.cloudinary.android.preprocess.ResourceEncoder;
import com.cloudinary.utils.StringUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Encodes the bitmap to a file. Allows configuration of quality and format.
 */
public class BitmapEncoder implements ResourceEncoder<Bitmap> {
    private static final Format DEFAULT_FORMAT = Format.WEBP;
    private static final int DEFAULT_QUALITY = 100;
    private final Format format;
    private final int quality;

    /**
     * Create a new bitmap encoder with the default specifications.
     */
    public BitmapEncoder() {
        this(DEFAULT_FORMAT, DEFAULT_QUALITY);
    }

    /**
     * Create a new bitmap encoder with the given specifications.
     *
     * @param format  The format to encode the bitmap
     * @param quality The quality to use when encoding the bitmap
     */
    public BitmapEncoder(Format format, int quality) {
        this.format = format;
        this.quality = quality;
    }

    /**
     * Encodes the given bitmap to a file using the supplied format and quality settings.
     * If no configuration is supplied, default settings will be used.
     *
     * @param context  Android context
     * @param resource The resource (after processing) to save to file.
     * @return
     * @throws ResourceCreationException if the resource cannot be saved to a file
     */
    @Override
    public String encode(Context context, Bitmap resource) throws ResourceCreationException {
        return saveFile(context, resource, quality, format);
    }

    protected final String saveFile(Context context, Bitmap resource, int quality, Format format) throws ResourceCreationException {
        FileOutputStream fos = null;
        String fileName = UUID.randomUUID().toString();
        String file = null;
        try {

            fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            resource.compress(adaptFormat(format), quality, fos);
            resource.recycle();
            file = fileName;
        } catch (java.io.FileNotFoundException e) {
            throw new ResourceCreationException("Could not create new file");
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                    if (StringUtils.isBlank(file)) {
                        // failed, delete the file just in case it's there:
                        context.deleteFile(fileName);
                    }
                } catch (IOException ignored) {
                }
            }
        }

        return file;
    }

    private Bitmap.CompressFormat adaptFormat(Format format) {
        switch (format) {
            case WEBP:
                return Bitmap.CompressFormat.WEBP;
            case JPEG:
                return Bitmap.CompressFormat.JPEG;
            case PNG:
            default:
                return Bitmap.CompressFormat.PNG;
        }
    }

    public enum Format {
        WEBP,
        JPEG,
        PNG,
    }
}
