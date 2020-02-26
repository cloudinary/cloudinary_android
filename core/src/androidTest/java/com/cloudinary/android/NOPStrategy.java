package com.cloudinary.android;

import android.content.Context;

import com.cloudinary.android.BackgroundRequestStrategy;
import com.cloudinary.android.UploadRequest;

class NOPStrategy implements BackgroundRequestStrategy {
    @Override
    public void init(Context context) {

    }

    @Override
    public void doDispatch(UploadRequest request) {

    }

    @Override
    public void executeRequestsNow(int howMany) {

    }

    @Override
    public boolean cancelRequest(String requestId) {
        return false;
    }

    @Override
    public int cancelAllRequests() {
        return 0;
    }

    @Override
    public int getPendingImmediateJobsCount() {
        return 0;
    }

    @Override
    public int getRunningJobsCount() {
        return 0;
    }
}
