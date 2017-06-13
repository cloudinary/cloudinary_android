package com.cloudinary.android.payload;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Base64;

/***
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

    void fromUri(String uri) {
        data = decode(Uri.parse(uri).getHost());
    }
}
