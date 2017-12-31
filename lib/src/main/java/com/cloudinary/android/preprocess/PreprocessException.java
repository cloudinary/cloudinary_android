package com.cloudinary.android.preprocess;

/**
 * Base exception for exceptions thrown during the preprocessing phase.
 */
public class PreprocessException extends Exception {
    /**
     * {@inheritDoc}
     */
    public PreprocessException() {
    }

    /**
     * {@inheritDoc}
     */
    public PreprocessException(String message) {
        super(message);
    }
}
