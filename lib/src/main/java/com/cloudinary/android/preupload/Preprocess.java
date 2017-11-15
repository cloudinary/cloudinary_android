package com.cloudinary.android.preupload;

import android.content.Context;

import com.cloudinary.android.payload.CouldNotDecodePayloadException;
import com.cloudinary.android.payload.ErrorCreatingNewBitmapException;
import com.cloudinary.android.payload.Payload;
import com.cloudinary.android.payload.PayloadNotFoundException;

/**
 * Preporcess to run on resources before uploading to Cloudinary.
 *
 * @param <T> The class representing the resource - This is the type on which the preprocessing will run (e.g. {@link android.graphics.Bitmap})
 */
public interface Preprocess<T> {
    /**
     * Prepare the resource for preprocessing. This method should extract a concrete resource with type T from the generic payload. T
     * his is later passed on to {@link #execute(Context, Object)} down the chain.
     * @param context Android context.
     * @param payload Payload to extract the resource from
     * @return The extract concrete resource of type T
     * @throws PayloadNotFoundException
     * @throws CouldNotDecodePayloadException
     */
    T prepare(Context context, Payload payload) throws PayloadNotFoundException, CouldNotDecodePayloadException;

    /**
     * Execute the given preprocess on the resource
     *
     * @param context  Android context
     * @param resource the resource as prepared in @link {@link #prepare(Context, Payload).
     * @return A resource of type T after processing.
     */
    T execute(Context context, T resource);

    /**
     * Finalizes the preprocess - This method saves the new resource to a file.
     *
     * @param context  Android context
     * @param resource The resource (after processing) to save to file.
     * @return The filepath of the newly created file.
     * @throws ErrorCreatingNewBitmapException
     */
    String finalize(Context context, T resource) throws ErrorCreatingNewBitmapException;
}
