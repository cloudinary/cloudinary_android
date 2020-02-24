package com.cloudinary.android.preprocess;

import android.content.Context;
import android.graphics.Bitmap;

import com.cloudinary.android.preprocess.Preprocess;
import com.cloudinary.android.preprocess.PreprocessChain;

/**
 * Preprocess implementation for resizing. Send an instance to {@link PreprocessChain#addStep(Preprocess)}
 * to scale down any image larger then {@link #height}/{@link #width}. The scaling retains aspect ratio while
 * making sure the height and width are within the requested maximum bounds. If the original image is smaller
 * than {@link #height} and {@link #width}, it will be returned unchanged.
 */
public class Limit implements Preprocess<Bitmap> {

    private final int width;
    private final int height;

    /**
     * Create a new Resize preprocess
     *
     * @param width  Maximum allowed width for the image. Will scale down to comply.
     * @param height Maximum allowed height for the image. Will scale down to comply.
     */
    public Limit(int width, int height) {
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
            double widthRatio = (double) width / resource.getWidth();
            double heightRatio = (double) height / resource.getHeight();
            if (heightRatio > widthRatio) {
                return Bitmap.createScaledBitmap(resource, width, (int) Math.round(widthRatio * resource.getHeight()), true);
            } else {
                return Bitmap.createScaledBitmap(resource, (int) Math.round(heightRatio * resource.getWidth()), height, true);
            }
        }

        return resource;
    }
}
