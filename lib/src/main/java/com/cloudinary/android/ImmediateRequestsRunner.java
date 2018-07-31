package com.cloudinary.android;

import android.content.Context;

public interface ImmediateRequestsRunner {
    void dispatchRequest(Context context, UploadRequest uploadRequest);

    boolean cancelRequest(String requestId);

    int cancelAllRequests();
}
