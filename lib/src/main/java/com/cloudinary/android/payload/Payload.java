package com.cloudinary.android.payload;

import android.content.Context;

/**
 * Representation of a resource to upload
 */
public abstract class Payload<T> {
    protected T data;

    public Payload(T data){
        this.data = data;
    }

    public Payload(){
    }

    /**
     * Constructs a uri of the data to upload, for serialization.
     * @return The string representation of the uri.
     */
    public abstract String toUri();

    /**
     * Get the length of the resource in bytes
     * @param context Android context.
     * @return The length
     * @throws PayloadNotFoundException in case the resource doesn't exist.
     */
    public abstract long getLength(Context context) throws PayloadNotFoundException;

    /**
     * Prepares the payload to upload to Cloudinary
     * @param context Android context
     * @return An object formatted for Cloudinary uploader. This can be one of InputStream, Byte array, File object or an absolute path (as string).
     * @throws PayloadNotFoundException If the request file or resource does not exist.
     */
    public abstract Object prepare(Context context) throws PayloadNotFoundException;

    /**
     * Initializes the payload data from the uri.
     * @param uri Uri that contains the payload data and type
     */
    abstract void loadData(String uri);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Payload<?> payload = (Payload<?>) o;

        return data != null ? data.equals(payload.data) : payload.data == null;

    }

    @Override
    public int hashCode() {
        return data != null ? data.hashCode() : 0;
    }

    public T getData() {
        return data;
    }
}
