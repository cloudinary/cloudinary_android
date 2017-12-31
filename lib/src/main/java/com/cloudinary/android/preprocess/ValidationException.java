package com.cloudinary.android.preprocess;

import android.content.Context;

/**
 * Thrown inside implementations of {@link Preprocess#execute(Context, Object)} if the resource fails validation (see {@link DimensionsValidator})
 */
public class ValidationException extends PreprocessException {
    public ValidationException(String message) {
        super(message);
    }
}
