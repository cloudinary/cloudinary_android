package com.cloudinary.android;

import android.content.Context;
import android.support.v4.util.Pair;

import java.util.Map;

/***
 * Provide an implementation of this class to {@link CldAndroid#init(Context, SignatureProvider, Map)} to enable signed uploads.
 * Note: If an api key and secret are provided to the library this interface is not required.
 */
public interface SignatureProvider {
    /***
     * This method will be called when a {@link UploadRequest} is about the start a signed upload.
     * @param options The options provided for the upload. Needed for signing.
     * @return A pair containing the generated signature and the signature's timestamp.
     */
    Pair<String, Long> provideSignature(Map options);
}
