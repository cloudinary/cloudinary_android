package com.cloudinary.android.preprocess;

import android.content.Context;

/**
 * Preprocess to run on a resource before uploading to Cloudinary. Pass an implementation of this interface to {@link PreprocessChain#addStep(Preprocess)}
 * to run preprocessing and validation steps on a resource (see {@link ScaleDownIfLargerThan}, {@link DimensionsValidator} for examples).
 *
 * @param <T> The class representing the resource - This is the type on which the preprocessing will run (e.g. {@link android.graphics.Bitmap})
 */
public interface Preprocess<T> {

    /**
     * Execute the given preprocess on the resource
     *
     * @param context  Android context
     * @param resource the resource as prepared by a {@link ResourceDecoder}
     * @return A resource of type T after processing.
     */
    T execute(Context context, T resource) throws PreprocessException;
}
