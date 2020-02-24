package com.cloudinary.android.preprocess;

import android.content.Context;

import com.cloudinary.android.preprocess.Preprocess;
import com.cloudinary.android.preprocess.PreprocessException;

/**
 * Thrown inside implementations of {@link Preprocess#execute(Context, Object)} if the resource fails validation.
 */
public class ValidationException extends PreprocessException {
    public ValidationException(String message) {
        super(message);
    }
}
