package com.cloudinary.android.preupload;

import android.content.Context;
import android.graphics.Bitmap;

import com.cloudinary.android.payload.CouldNotDecodePayloadException;
import com.cloudinary.android.payload.Payload;
import com.cloudinary.android.payload.PayloadNotFoundException;

/**
 * Preprocess implementation for resizing. Add this as a phase to {@link com.cloudinary.android.UploadRequest#preprocess(Preprocess)}
 * to scale down any image larger then {@link #maxHeight}/{@link #maxWidth}
 */
public class ResizePreprocess extends ImagePreprocess {

    private final int maxWidth;
    private final int maxHeight;

    /**
     * Create a new Resize preprocess
     *
     * @param maxWidth  Maximum allowed width for the image. Will scale down to comply.
     * @param maxHeight Maximum allowed height for the image. Will scale down to comply.
     */
    public ResizePreprocess(int maxWidth, int maxHeight) {
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

    /**
     * Overrides the default preparation to optimize bitmap loading (load a scaled down version of the bitmap if possible).
     * @param context Android context
     * @param payload Payload to load the Bitmap from
     * @return The Bitmap extracted from the payload
     * @throws PayloadNotFoundException
     * @throws CouldNotDecodePayloadException
     */
    @Override
    public Bitmap prepare(Context context, Payload payload) throws PayloadNotFoundException, CouldNotDecodePayloadException {
        return bitmapFromPayload(context, payload, maxWidth, maxHeight);
    }

    /**
     * Execute the preprocessing phase. This will check the dimensions and scale down the image if needed, making sure
     * both height and width are within maximum bounds.
     * @param context Android context
     * @param resource The Bitmap to resize
     * @return The scaled down bitmap (or the original bitmap if it's within bounds).
     */
    @Override
    public Bitmap execute(Context context, Bitmap resource) {
        if (resource.getWidth() > maxWidth || resource.getHeight() > maxHeight) {
            int newWidth;
            int newHeight;
            if (resource.getWidth() > resource.getHeight()) {
                newHeight = (int) Math.floor(((float) resource.getHeight() / resource.getWidth()) * maxWidth);
                newWidth = maxWidth;
            } else {
                newWidth = (int) Math.floor(((float) resource.getWidth() / resource.getHeight()) * maxHeight);
                newHeight = maxHeight;
            }

            return Bitmap.createScaledBitmap(resource, newWidth, newHeight, true);
        }

        return resource;
    }
}
