package com.cloudinary.android.preprocess;

import com.cloudinary.android.preprocess.PreprocessException;

/**
 * Thrown if a resource cannot be created (saved) after running the preprocessing steps.
 */
public class ResourceCreationException extends PreprocessException {
    /**
     * {@inheritDoc}
     */
    public ResourceCreationException(String message) {
        super(message);
    }
}
