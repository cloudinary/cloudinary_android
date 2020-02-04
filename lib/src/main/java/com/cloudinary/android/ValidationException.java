package com.cloudinary.android;

import android.content.Context;

/**
 * Thrown inside implementations of {@link Preprocess#execute(Context, Object)} if the resource fails validation.
 */
public class ValidationException extends PreprocessException {
    public ValidationException(String message) {
        super(message);
    }
}
