package com.cloudinary.android;

import android.content.Context;

import com.cloudinary.android.callback.UploadStatus;

/**
 * Handles the processing of an upload request.
 */
interface RequestProcessor {

    /**
     * Process a single request, this runs after verifying all the policies and conditions are met. For internal use.
     * @param context Android context.
     * @param params Collection of params, usually populated by {@link UploadRequest#populateParamsFromFields(RequestParams)}
     */
    UploadStatus processRequest(Context context, RequestParams params);
}
