package com.cloudinary.android;

/**
 * An InvalidParamsException is thrown when the parameters passed to a request cannot not be
 * serialized. 
 */
public class InvalidParamsException extends IllegalArgumentException {
    public InvalidParamsException(String message, Throwable cause) {
        super(message, cause);
    }
}
