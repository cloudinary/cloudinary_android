package com.cloudinary.android.preprocess;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;

import com.cloudinary.android.preprocess.Preprocess;
import com.cloudinary.android.preprocess.PreprocessChain;

/**
 * Preprocess for rotating. Send an instance to {@link PreprocessChain#addStep(Preprocess)} to rotate an image.
 */
public class Rotate implements Preprocess<Bitmap> {

    private int rotationAngle;

    public Rotate(int rotationAngle) {
        this.rotationAngle = rotationAngle;
    }

    /**
     * Execute the preprocessing phase. This will rotate the image by the rotation angle property.
     * If the rotation angle is a multiple of 360, then the original resource bitmap will be returned.
     *
     * @param context  Android context.
     * @param resource The bitmap to rotate.
     * @return The rotated bitmap.
     */
    @Override
    public Bitmap execute(Context context, Bitmap resource) {
        Matrix matrix = new Matrix();
        matrix.setRotate(rotationAngle);

        return Bitmap.createBitmap(resource, 0, 0, resource.getWidth(), resource.getHeight(), matrix, false);
    }
}
