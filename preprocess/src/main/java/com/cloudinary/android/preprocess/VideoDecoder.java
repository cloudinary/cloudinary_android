package com.cloudinary.android.preprocess;

import android.content.Context;
import android.net.Uri;

import com.cloudinary.android.payload.FilePayload;
import com.cloudinary.android.payload.LocalUriPayload;
import com.cloudinary.android.payload.Payload;
import com.cloudinary.android.payload.PayloadNotFoundException;

import java.io.File;

/**
 * Returns the decoded video uri from a given payload. Payloads must be either {@link LocalUriPayload} or {@link FilePayload}.
 * Note: It doesn't do the actual decoding process.
 */
public class VideoDecoder implements ResourceDecoder<Uri> {

    /**
     * Returns the video uri.
     *
     * @param context Android context.
     * @param payload Payload to extract the resource from
     * @throws PayloadDecodeException if the payload is neither a {@link LocalUriPayload} nor {@link FilePayload}
     * @throws PayloadNotFoundException if the payload's resource cannot be found.
     */
    @Override
    public Uri decode(Context context, Payload payload) throws PayloadDecodeException, PayloadNotFoundException {
        Uri uri;
        if (payload instanceof LocalUriPayload) {
            uri = ((LocalUriPayload) payload).getData();
        } else if (payload instanceof FilePayload) {
            File file = (File) payload.prepare(context);
            uri = Uri.fromFile(file);
        } else {
            throw new PayloadDecodeException();
        }

        return uri;
    }

}
