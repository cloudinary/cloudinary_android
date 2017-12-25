package com.cloudinary.android.preprocess;

import android.content.Context;

/**
 * Implement this interface to pass to {@link PreprocessChain#saveWith(ResourceEncoder)} for custom resource encoding behavior.
 * (See {@link BitmapEncoder} for an example)
 *
 * @param <T>
 */
public interface ResourceEncoder<T> {
    /**
     * Finalizes the preprocess - This method saves the new resource T to a file.
     *
     * @param context  Android context
     * @param resource The resource (after processing) to save to file.
     * @return The filepath of the newly created file.
     * @throws ErrorCreatingNewBitmapException
     */
    String encode(Context context, T resource) throws ErrorCreatingNewBitmapException;
}
