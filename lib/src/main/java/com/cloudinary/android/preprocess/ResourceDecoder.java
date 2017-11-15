package com.cloudinary.android.preprocess;

import android.content.Context;

import com.cloudinary.android.payload.Payload;
import com.cloudinary.android.payload.PayloadNotFoundException;

/**
 * Implement this interface to pass to {@link PreprocessChain#loadWith(ResourceDecoder)} for custom resource loading behavior.
 * (See {@link DefaultBitmapDecoder} for an example)
 *
 * @param <T>
 */
public interface ResourceDecoder<T> {
    /**
     * Prepare the resource for preprocessing. This method should extract a concrete resource with type T from the generic payload.
     * T is later passed on to {@link Preprocess#execute(Context, Object)} down the chain.
     *
     * @param context Android context.
     * @param payload Payload to extract the resource from
     * @return The extract concrete resource of type T
     * @throws PayloadNotFoundException
     * @throws CouldNotDecodePayloadException
     */
    T decode(Context context, Payload payload) throws PayloadNotFoundException, CouldNotDecodePayloadException;
}
