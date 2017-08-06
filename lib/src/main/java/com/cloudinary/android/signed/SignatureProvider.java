package com.cloudinary.android.signed;

import android.content.Context;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.UploadRequest;

import java.util.Map;

/**
 * Provide an implementation of this class to {@link MediaManager#init(Context, SignatureProvider, Map)} to enable signed uploads.
 * Note: If an api key and secret are provided to the library this interface is not required.
 */
public interface SignatureProvider {
    /**
     * This method will be called when a {@link UploadRequest} is about the start a signed upload.
     * @param options The options provided for the upload. Needed for signing.
     * @return A {@link Signature} object containing the generated signature, the api key and the signature's timestamp.
     */
    Signature provideSignature(Map options);

    /**
     * Return a name for logging purposes.
     */
    String getName();
}
