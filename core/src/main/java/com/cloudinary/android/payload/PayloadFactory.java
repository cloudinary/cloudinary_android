package com.cloudinary.android.payload;

import android.net.Uri;

/**
 * This class is used to construct Payload instances from URIs.
 */
public class PayloadFactory {
    private static final String TAG = PayloadFactory.class.getSimpleName();

    /**
     * Constructs the payload extracted from the uri.
     *
     * @param uri Uri that contains the payload data and type
     * @return The constructed payload
     */
    public static Payload fromUri(String uri) {
        Uri parsed = Uri.parse(uri);
        String scheme = parsed.getScheme();

        // invalid payload
        if (scheme == null) {
            return null;
        }

        Payload payload;
        switch (scheme) {
            case ByteArrayPayload.URI_KEY:
                payload = new ByteArrayPayload();
                break;
            case FilePayload.URI_KEY:
                payload = new FilePayload();
                break;
            case LocalUriPayload.URI_KEY:
                payload = new LocalUriPayload();
                break;
            case ResourcePayload.URI_KEY:
                payload = new ResourcePayload();
                break;
            default:
                return null;
        }

        payload.loadData(parsed.getHost());

        return payload;
    }
}
