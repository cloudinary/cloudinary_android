package com.cloudinary.android.preprocess;

import android.content.Context;
import android.graphics.Bitmap;

import com.cloudinary.android.preprocess.Preprocess;
import com.cloudinary.android.preprocess.ValidationException;

/**
 * A preprocess step for validating the dimensions of a given bitmap.
 */
public class DimensionsValidator implements Preprocess<Bitmap> {
    private final int minWidth;
    private final int minHeight;
    private final int maxWidth;
    private final int maxHeight;

    /**
     * Create an instance with the chosen parameters. Anything outside the specified bounds will fail
     * validation.
     *
     * @param minWidth  Minimum allowed width
     * @param minHeight Minimum allowed height
     * @param maxWidth  Maximum allowed width
     * @param maxHeight Maximum allowed height
     */
    public DimensionsValidator(int minWidth, int minHeight, int maxWidth, int maxHeight) {
        this.minWidth = minWidth;
        this.minHeight = minHeight;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bitmap execute(Context context, Bitmap resource) throws ValidationException {

        if (resource.getWidth() > maxWidth || resource.getWidth() < minWidth ||
                resource.getHeight() > maxHeight || resource.getHeight() < minHeight)
            throw new ValidationException("Resource dimensions are invalid");

        return resource;
    }
}
