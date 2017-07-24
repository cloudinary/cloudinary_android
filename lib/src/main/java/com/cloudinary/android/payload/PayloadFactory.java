package com.cloudinary.android.payload;

import android.net.Uri;

import com.cloudinary.android.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to register custom payload types and create instances from Payload URIs
 */
public class PayloadFactory {
    private static final String TAG = PayloadFactory.class.getSimpleName();
    private static Map<String, Class<? extends Payload>> types = new HashMap<>();

    static {
        types.put(ByteArrayPayload.URI_KEY, ByteArrayPayload.class);
        types.put(FilePayload.URI_KEY, FilePayload.class);
        types.put(LocalUriPayload.URI_KEY, LocalUriPayload.class);
        types.put(ResourcePayload.URI_KEY, ResourcePayload.class);
    }

    /**
     * Constructs the payload extracted from the uri.
     * @param uri Uri that contains the payload data and type
     * @return The constructed payload
     */
    public static Payload fromUri(String uri){
        Uri parsed = Uri.parse(uri);
        String scheme = parsed.getScheme();


        Class<? extends Payload> clazz = types.get(scheme);

        if (clazz == null){
            Logger.d(TAG, String.format("Unknown payload class, type not registered for scheme %s", scheme));
            return null;
        }

        try {
            Payload payload = clazz.newInstance();
            payload.fromUri(uri);
            return payload;
        } catch (IllegalAccessException e) {
            Logger.e(TAG, String.format("IllegalAccessException when loading uri: %s", uri), e);
        } catch (InstantiationException e) {
            Logger.e(TAG, String.format("InstantiationException when loading uri: %s", uri), e);
        }

        return null;
    }
}
