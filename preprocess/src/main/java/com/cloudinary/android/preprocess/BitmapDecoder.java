package com.cloudinary.android.preprocess;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.cloudinary.android.payload.Payload;
import com.cloudinary.android.payload.PayloadNotFoundException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Decodes a bitmap from a payload. If given width and height the decoding process will take them
 * into account to decode the bitmap efficiently but will not necessarily resize the bitmap.
 */
public class BitmapDecoder implements ResourceDecoder<Bitmap> {
    private final int width;
    private final int height;


    /**
     * Create a new decoder.
     */
    public BitmapDecoder() {
        this(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    /**
     * Create a new decoder taking the required width and height into account to decode the bitmap efficiently.
     *
     * @param width  Required width
     * @param height Required height
     */
    public BitmapDecoder(int width, int height) {
        this.width = width;
        this.height = height;
    }

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
     * Decodes a bitmap from the given payload. If the bitmap is at least two times larger than the required
     * dimensions it will decode a version scaled down by a factor. Note: The dimensions of the decoded bitmap
     * will not necessarily be equal to {@link BitmapDecoder#width} and {@link BitmapDecoder#height}. For
     * exact resizing combine this decoder with {@link Limit} processing step, or use
     * {@link ImagePreprocessChain#limitDimensionsChain(int, int)}.
     *
     * @param context Android context.
     * @param payload Payload to extract the bitmap from
     * @return The decoded bitmap
     * @throws PayloadNotFoundException if the payload is not found
     * @throws PayloadDecodeException if the payload exists but cannot be decoded
     */
    @Override
    public Bitmap decode(Context context, Payload payload) throws PayloadNotFoundException, PayloadDecodeException {
        return bitmapFromPayload(context, payload, width, height);
    }

    protected final Bitmap bitmapFromPayload(Context context, Payload payload, int width, int height) throws PayloadNotFoundException, PayloadDecodeException {
        Object resource = payload.prepare(context);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = null;
        if (resource instanceof File) {
            BitmapFactory.decodeFile(((File) resource).getPath(), options);
            options.inSampleSize = calculateInSampleSize(options, width, height);
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFile(((File) resource).getPath(), options);
        } else if (resource instanceof InputStream) {
            InputStream is = null;
            try {
                BitmapFactory.decodeStream((InputStream) resource, null, options);
                options.inSampleSize = calculateInSampleSize(options, width, height);
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
            options.inSampleSize = calculateInSampleSize(options, width, height);
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
        }

        if (bitmap == null) {
            throw new PayloadDecodeException();
        }

        return bitmap;
    }
}
