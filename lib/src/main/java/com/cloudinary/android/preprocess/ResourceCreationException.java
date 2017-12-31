package com.cloudinary.android.preprocess;

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
