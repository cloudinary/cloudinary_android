package com.cloudinary.android;

import com.cloudinary.android.payload.Payload;

class UploadContext<T extends Payload> {
    private final T payload;
    private final RequestDispatcherInterface dispatcher;

    UploadContext(T payload, RequestDispatcherInterface requestDispatcher) {
        this.payload = payload;
        this.dispatcher = requestDispatcher;
    }

    public T getPayload() {
        return payload;
    }

    RequestDispatcherInterface getDispatcher() {
        return dispatcher;
    }
}
