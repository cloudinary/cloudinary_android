package com.cloudinary.android.payload;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.Base64;

import com.cloudinary.android.Logger;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * This class is used to handle uploading of images/videos as byte arrays
 */
public class ByteArrayPayload extends Payload<byte[]> {

    public static final String ENCODING_CHARSET = "UTF8";
    static final String URI_KEY = "bytes";
    private static final String TAG = ByteArrayPayload.class.getSimpleName();

    public ByteArrayPayload(byte[] data) {
        super(data);
    }

    public ByteArrayPayload() {
    }

    @NonNull
    private static String encode(byte[] data) {
        try {
            return new String(Base64.encode(data, Base64.URL_SAFE), ENCODING_CHARSET);
        } catch (UnsupportedEncodingException e) {
            Logger.e(TAG, "Cannot encode image bytes", e);

            // this will be addressed later through the request's flows and callbacks
            return null;
        }
    }

    private static byte[] decode(String encoded) {
        try {
            return Base64.decode(encoded.getBytes(ENCODING_CHARSET), Base64.URL_SAFE);
        } catch (UnsupportedEncodingException e) {
            Logger.e(TAG, "Cannot decode image bytes", e);

            // this will be addressed later through the request's flows and callbacks
            return null;
        }
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
    public Object prepare(Context context) throws EmptyByteArrayException {
        if (data == null || data.length < 1) {
            throw new EmptyByteArrayException();
        }

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
