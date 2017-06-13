package com.cloudinary.android;

public class InvalidParamsException extends IllegalArgumentException {
    public InvalidParamsException(String message, Throwable cause) {
        super(message, cause);
    }
}
