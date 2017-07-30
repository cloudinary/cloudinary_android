package com.cloudinary.android.payload;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Base64;

import java.util.Arrays;

/**
 * This class is used to handle uploading of images/videos as byte arrays
 */
public class ByteArrayPayload extends Payload<byte[]> {

    static final String URI_KEY = "bytes";

    public ByteArrayPayload(byte[] data) {
        super(data);
    }

    public ByteArrayPayload(){
    }

    @NonNull
    private static String encode(byte[] data) {
        return new String(Base64.encode(data, 0));
    }

    private static byte[] decode(String encoded){
        return Base64.decode(encoded, 0);
    }

    @Override
    public String toUri() {
        return URI_KEY + "://" + encode(data);
    }

    @Override
    public long getLength(Context context) {
        return data.length;
    }

    @Override
    public Object prepare(Context context) {
        return data;
    }

    void loadData(String encodedData) {
        data = decode(encodedData);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Payload<?> payload = (Payload<?>) o;

        return data != null ? Arrays.equals(data, (byte[]) payload.data) : payload.data == null;
    }

    @Override
    public int hashCode() {
        return data != null ? Arrays.hashCode(data) : 0;
    }
}
