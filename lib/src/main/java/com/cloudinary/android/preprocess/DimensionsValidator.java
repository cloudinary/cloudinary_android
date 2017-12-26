package com.cloudinary.android.preprocess;

import android.content.Context;
import android.graphics.Bitmap;

public class DimensionsValidator implements Preprocess<Bitmap> {
    private final int minWidth;
    private final int minHeight;
    private final int maxWidth;
    private final int maxHeight;

    public DimensionsValidator(int minWidth, int minHeight, int maxWidth, int maxHeight) {
        this.minWidth = minWidth;
        this.minHeight = minHeight;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

    @Override
    public Bitmap execute(Context context, Bitmap resource) throws ValidationException {

        if (resource.getWidth() > maxWidth || resource.getWidth() < minWidth ||
                resource.getHeight() > maxHeight || resource.getHeight() < minHeight)
            throw new ValidationException("Resource dimensions are invalid");

        return resource;
    }
}
