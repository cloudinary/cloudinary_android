package com.cloudinary.android.preupload;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.cloudinary.android.payload.CouldNotDecodePayloadException;
import com.cloudinary.android.payload.ErrorCreatingNewBitmapException;
import com.cloudinary.android.payload.Payload;
import com.cloudinary.android.payload.PayloadNotFoundException;
import com.cloudinary.utils.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Base class for all image ({@link Bitmap}) preprocessing. Extends this class and override {@link #execute(Context, Bitmap)} for custom image preprocessing
 * Note: Using any variation of ImagePreprocess will re-save the resource with the default quality and format, ignoring
 * the original file quality and format. Use {@link FormatPreprocess} to choose specific quality and format.
 */
public abstract class ImagePreprocess implements Preprocess<Bitmap> {
    private static final Format DEFAULT_FORMAT = Format.WEBP;
    private static final int DEFAULT_QUALITY = 100;

    // Google reference method, see https://developer.android.com/topic/performance/graphics/load-bitmap.html
    static private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * Decodes a bitmap from the given payload. Override for custom bitmap decoding (see {@link ResizePreprocess})
     *
     * @param context Android context.
     * @param payload Payload to extract the bitmap from
     * @return Bitmap representation of the payload.
     * @throws PayloadNotFoundException
     * @throws CouldNotDecodePayloadException
     */
    @Override
    public Bitmap prepare(Context context, Payload payload) throws PayloadNotFoundException, CouldNotDecodePayloadException {
        return bitmapFromPayload(context, payload, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    /**
     * NOP - override to implement the actual processing required.
     * @param context Android context
     * @param resource the bitmap as prepared in @link {@link #prepare(Context, Payload).
     * @return The bitmap after the processing.
     */
    @Override
    public Bitmap execute(Context context, Bitmap resource) {
        return resource;
    }

    /**
     * Encodes the bitmap to a file using default format and quality. Override for custom encoding, to choose format etc (see {@link FormatPreprocess})
     * @param context Android context
     * @param resource The resource (after processing) to save to file.
     * @return Filepath of the saved file after processing
     * @throws ErrorCreatingNewBitmapException
     */
    @Override
    public String finalize(Context context, Bitmap resource) throws ErrorCreatingNewBitmapException {
        return saveFile(context, resource, DEFAULT_QUALITY, DEFAULT_FORMAT);
    }

    protected String saveFile(Context context, Bitmap resource, int quality, Format format) throws ErrorCreatingNewBitmapException {
        FileOutputStream fos = null;
        String fileName = UUID.randomUUID().toString();
        String file = null;
        try {

            fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            resource.compress(adaptFormat(format), quality, fos);
            resource.recycle();
            file = fileName;
        } catch (java.io.FileNotFoundException e) {
            throw new ErrorCreatingNewBitmapException("Could not create new file");
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

    protected Bitmap bitmapFromPayload(Context context, Payload payload, int maxWidth, int maxHeight) throws PayloadNotFoundException, CouldNotDecodePayloadException {
        Object resource = payload.prepare(context);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = null;
        if (resource instanceof File) {
            BitmapFactory.decodeFile(((File) resource).getPath(), options);
            options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight);
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFile(((File) resource).getPath(), options);
        } else if (resource instanceof InputStream) {
            InputStream is = null;
            try {
                BitmapFactory.decodeStream((InputStream) resource, null, options);
                options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight);
                options.inJustDecodeBounds = false;
                is = (InputStream) payload.prepare(context);
                bitmap = BitmapFactory.decodeStream(is, null, options);
            } finally {
                try {
                    ((InputStream) resource).close();
                } catch (IOException ignored) {
                }
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException ignored) {
                }
            }
        } else if (resource instanceof byte[]) {
            byte[] data = (byte[]) resource;
            BitmapFactory.decodeByteArray(data, 0, data.length, options);
            options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight);
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
        }

        if (bitmap == null) {
            throw new CouldNotDecodePayloadException();
        }

        return bitmap;
    }

    private Bitmap.CompressFormat adaptFormat(ResizePreprocess.Format format) {
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
