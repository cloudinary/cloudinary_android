package com.cloudinary.android.preprocess;

import android.content.Context;
import android.graphics.Bitmap;

/**
 * Preprocess implementation for resizing. Send an instance to {@link PreprocessChain#addStep(Preprocess)}
 * to scale down any image larger then {@link #height}/{@link #width}. The scaling retains aspect ratio while
 * making sure the height and width are within the requested maximum bounds.
 */
public class ScaleDownIfLargerThan implements Preprocess<Bitmap> {

    private final int width;
    private final int height;

    /**
     * Create a new Resize preprocess
     *
     * @param width  Maximum allowed width for the image. Will scale down to comply.
     * @param height Maximum allowed height for the image. Will scale down to comply.
     */
    public ScaleDownIfLargerThan(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Execute the preprocessing phase. This will check the dimensions and scale down the image if needed, making sure
     * both height and width are within maximum bounds.
     *
     * @param context  Android context
     * @param resource The Bitmap to resize
     * @return The scaled down bitmap (or the original bitmap if it's within bounds).
     */
    @Override
    public Bitmap execute(Context context, Bitmap resource) {
        if (resource.getWidth() > width || resource.getHeight() > height) {
            int newWidth;
            int newHeight;
            if (resource.getWidth() > resource.getHeight()) {
                newHeight = (int) Math.floor(((float) resource.getHeight() / resource.getWidth()) * width);
                newWidth = width;
            } else {
                newWidth = (int) Math.floor(((float) resource.getWidth() / resource.getHeight()) * height);
                newHeight = height;
            }

            return Bitmap.createScaledBitmap(resource, newWidth, newHeight, true);
        }

        return resource;
    }
}
