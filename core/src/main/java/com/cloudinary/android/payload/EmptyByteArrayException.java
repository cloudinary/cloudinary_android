package com.cloudinary.android.payload;

public class EmptyByteArrayException extends PayloadNotFoundException {
    EmptyByteArrayException() {
        super("Byte array is empty.");
    }
}
