package com.cloudinary.android;

import android.content.Context;

public interface ImmediateRequestsRunner {
    void runRequest(Context context, UploadRequest uploadRequest);

    boolean cancelRequest(String requestId);

    int cancelAllRequests();
}
