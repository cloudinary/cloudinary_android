package com.cloudinary.android;

import android.content.Context;

interface RequestProcessorInterface {

    /***
     * Process a single request, this runs after verifying all the policies and conditions are met. For internal use.
     * @param context Android context.
     * @param params Collection of params, usually populated by {@link UploadRequest#populateParamsFromFields(ParamsAdaptable)}
     */
    UploadStatus processRequest(Context context, ParamsAdaptable params);
}
