package com.cloudinary.android;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DefaultImmediateRequestsRunner implements ImmediateRequestsRunner {
    private static final String TAG = "DefaultImmediateRequestsRunner";

    protected static final Map<String, Future> tasks = new ConcurrentHashMap<>();
    private final ExecutorService executor;
    private final RequestProcessor requestProcessor;

    DefaultImmediateRequestsRunner(RequestProcessor requestProcessor) {
        this.executor = new ThreadPoolExecutor(4, 4,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());

        this.requestProcessor = requestProcessor;
    }

    @Override
    public synchronized void runRequest(final Context context, final UploadRequest uploadRequest) {
        final ImmediateRequestParams params = new ImmediateRequestParams();
        uploadRequest.populateParamsFromFields(params);

        // mark as an immediate tasks:
        params.putBoolean("immediate", true);

        final String requestId = uploadRequest.getRequestId();
        tasks.put(requestId, executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    requestProcessor.processRequest(context, params);
                } finally {
                    tasks.remove(requestId);
                }
            }
        }));
    }

    @Override
    public synchronized boolean cancelRequest(String requestId) {
        Future task = tasks.remove(requestId);
        if (task != null) {
            task.cancel(true);
            return true;
        }

        return false;
    }

    @Override
    public synchronized int cancelAllRequests() {
        for (Future task : tasks.values()) {
            task.cancel(true);
        }

        int size = tasks.size();
        tasks.clear();
        return size;
    }

    private static final class ImmediateRequestParams implements RequestParams {
        private final Map<String, Object> map = new HashMap<>();

        @Override
        public void putString(String key, String value) {
            if (value == null) {
                map.remove(key);
            } else {
                map.put(key, value);
            }
        }

        @Override
        public void putInt(String key, int value) {
            map.put(key, value);
        }

        @Override
        public void putLong(String key, long value) {
            map.put(key, value);
        }

        @Override
        public void putBoolean(String key, boolean value) {
            map.put(key, value);
        }

        @Override
        public String getString(String key, String defaultValue) {
            return map.containsKey(key) ? map.get(key).toString() : null;
        }

        @Override
        public int getInt(String key, int defaultValue) {
            return map.containsKey(key) ? (int) map.get(key) : defaultValue;
        }

        @Override
        public long getLong(String key, long defaultValue) {
            return map.containsKey(key) ? (long) map.get(key) : defaultValue;
        }

        @Override
        public boolean getBoolean(String key, boolean defaultValue) {
            return map.containsKey(key) ? (boolean) map.get(key) : defaultValue;
        }
    }
}
