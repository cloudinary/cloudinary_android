package com.cloudinary.android.preprocess;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;

import com.cloudinary.android.preprocess.Preprocess;
import com.cloudinary.android.preprocess.PreprocessChain;
import com.cloudinary.android.preprocess.PreprocessException;

/**
 * Preprocess for cropping. Send an instance to {@link PreprocessChain#addStep(Preprocess)} to crop an image.
 * Points must form a diagonal within the bounds of the image.
 * If the points form the same diagonal size as the original image, it will be returned unchanged.
 */
public class Crop implements Preprocess<Bitmap> {

    private Point p1;
    private Point p2;

    /**
     * Create a new crop preprocess.
     *
     * @param p1 First point that form the diagonal.
     * @param p2 Second point that form the diagonal.
     */
    public Crop(Point p1, Point p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    /**
     * Execute the preprocessing phase. This will crop the image if needed, making sure that the points form a
     * diagonal within the bounds of the bitmap.
     *
     * @param context  Android context
     * @param resource The Bitmap to crop
     * @return The cropped bitmap (or the original bitmap if the points form the same diagonal size).
     */
    @Override
    public Bitmap execute(Context context, Bitmap resource) throws PreprocessException {
        checkDiagonal();

        int startX, startY, width, height;
        if (p1.x < p2.x) {
            startX = p1.x;
            width = p2.x - p1.x;
        } else {
            startX = p2.x;
            width = p1.x - p2.x;
        }

        if (p1.y < p2.y) {
            startY = p1.y;
            height = p2.y - p1.y;
        } else {
            startY = p2.y;
            height = p1.y - p2.y;
        }
        checkOutOfBounds(startX, startY, width, height, resource);

        return Bitmap.createBitmap(resource, startX, startY, width, height);
    }

    private void checkDiagonal() throws PreprocessException {
        if (p1.x == p2.x || p1.y == p2.y) {
            throw new PreprocessException("Points do not make a diagonal");
        }
    }

    private void checkOutOfBounds(int startX, int startY, int width, int height, Bitmap resource) throws PreprocessException {
        boolean isOutOfBounds = false;
        if (startX < 0 || startX > resource.getWidth()) {
            isOutOfBounds = true;
        } else if (width > resource.getWidth() || startX + width > resource.getWidth()) {
            isOutOfBounds = true;
        } else if (startY < 0 || startY > resource.getHeight()) {
            isOutOfBounds = true;
        } else if (height > resource.getHeight() || startY + height > resource.getHeight()) {
            isOutOfBounds = true;
        }

        if (isOutOfBounds) {
            throw new PreprocessException("Out of bounds");
        }
    }
}
