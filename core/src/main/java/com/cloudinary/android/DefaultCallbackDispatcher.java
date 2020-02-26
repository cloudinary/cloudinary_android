package com.cloudinary.android;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.core.util.Pools;

import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.ListenerService;
import com.cloudinary.android.callback.UploadCallback;
import com.cloudinary.android.callback.UploadResult;
import com.cloudinary.android.callback.UploadStatus;
import com.cloudinary.utils.StringUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * {@inheritDoc}
 */
class DefaultCallbackDispatcher implements CallbackDispatcher {
    private static final int START_MESSAGE = 0;
    private static final int ERROR_MESSAGE = 1;
    private static final int PROGRESS_MESSAGE = 2;
    private static final int RESCHEDULE_MESSAGE = 3;
    private static final int SUCCESS_MESSAGE = 4;

    private static final String TAG = "DefaultCallbackDispatcher";

    private final Map<Integer, UploadCallbackWrapper> callbackRegistry;
    private final Map<String, UploadResult> pendingResults;
    private Class<?> listenerServiceClass = null;
    private ReentrantReadWriteLock readWriteLock;
    private Handler handler;
    private boolean isListenerServiceAlreadyRegistered = false;

    DefaultCallbackDispatcher(Context context) {
        callbackRegistry = new ConcurrentHashMap<>();
        pendingResults = new ConcurrentHashMap<>();
        initListenerClass(context);
        readWriteLock = new ReentrantReadWriteLock();

        // Main thread handler for all callback calls
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                CallbackMessage callbackMessage = (CallbackMessage) msg.obj;
                String requestId = callbackMessage.requestId;
                switch (msg.what) {
                    case START_MESSAGE:
                        callbackMessage.callback.onStart(requestId);
                        break;
                    case ERROR_MESSAGE:
                        callbackMessage.callback.onError(requestId, callbackMessage.error);
                        break;
                    case PROGRESS_MESSAGE:
                        callbackMessage.callback.onProgress(requestId, callbackMessage.bytes, callbackMessage.totalBytes);
                        break;
                    case RESCHEDULE_MESSAGE:
                        callbackMessage.callback.onReschedule(requestId, callbackMessage.error);
                        break;
                    case SUCCESS_MESSAGE:
                        callbackMessage.callback.onSuccess(requestId, callbackMessage.resultData);
                        break;
                }

                if (msg.what != PROGRESS_MESSAGE) {
                    Logger.d(TAG, String.format("Dispatching callback for request %s. Type: %d", requestId, msg.what));
                }

                callbackMessage.recycle();
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void registerCallback(String requestId, UploadCallback callback) {
        readWriteLock.writeLock().lock();
        try {
            if (callback != null) {
                Logger.d(TAG, String.format("Registered callback %s", callback.getClass().getSimpleName()));
                if (callback instanceof ListenerService) {
                    Logger.d(TAG, "Listener service registered.");
                    isListenerServiceAlreadyRegistered = true;
                }
                int callbackId = System.identityHashCode(callback);
                UploadCallbackWrapper uploadCallbackWrapper = new UploadCallbackWrapper(callback);
                uploadCallbackWrapper.addRequestId(requestId);
                callbackRegistry.put(callbackId, uploadCallbackWrapper);
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void registerCallback(UploadCallback callback) {
        readWriteLock.writeLock().lock();
        try {
            if (callback != null) {
                Logger.d(TAG, String.format("Registered callback %s", callback.getClass().getSimpleName()));
                if (callback instanceof ListenerService) {
                    Logger.d(TAG, "Listener service registered.");
                    isListenerServiceAlreadyRegistered = true;
                }
                callbackRegistry.put(System.identityHashCode(callback), new UploadCallbackWrapper(callback));
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void unregisterCallback(UploadCallback callback) {
        if (callback != null) {
            Logger.d(TAG, String.format("Unregistered callback %s", callback.getClass().getSimpleName()));
            if (callback instanceof ListenerService) {
                Logger.d(TAG, "Listener service unregistered.");
                isListenerServiceAlreadyRegistered = false;
            }
            callbackRegistry.remove(System.identityHashCode(callback));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void wakeListenerServiceWithRequestStart(Context appContext, String requestId) {
        Logger.d(TAG, String.format("wakeListenerServiceWithRequestStart, listenerClass: %s, alreadyRegistered: %s", listenerServiceClass, isListenerServiceAlreadyRegistered));

        // if the service is already 'awake' it will receive events via callback. If we send the intent the event will be received twice.
        if (listenerServiceClass != null && !isListenerServiceAlreadyRegistered) {
            appContext.startService(new Intent(appContext, listenerServiceClass)
                    .setAction(MediaManager.ACTION_REQUEST_STARTED)
                    .putExtra(MediaManager.INTENT_EXTRA_REQUEST_ID, requestId));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void wakeListenerServiceWithRequestFinished(Context appContext, String requestId, UploadStatus uploadStatus) {
        Logger.d(TAG, String.format("wakeListenerServiceWithRequestFinished, listenerClass: %s, alreadyRegistered: %s", listenerServiceClass, isListenerServiceAlreadyRegistered));

        // if the service is already 'awake' it will receive events via callback. If we send the intent the event will be received twice.
        if (listenerServiceClass != null && !isListenerServiceAlreadyRegistered) {
            appContext.startService(new Intent(appContext, listenerServiceClass)
                    .setAction(MediaManager.ACTION_REQUEST_FINISHED)
                    .putExtra(MediaManager.INTENT_EXTRA_REQUEST_ID, requestId)
                    .putExtra(MediaManager.INTENT_EXTRA_REQUEST_RESULT_STATUS, uploadStatus));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispatchStart(String requestId) {
        dispatchMessage(requestId, START_MESSAGE, CallbackMessage.obtain());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispatchProgress(String requestId, long bytes, long totalBytes) {
        CallbackMessage callbackMessage = CallbackMessage.obtain();
        callbackMessage.bytes = bytes;
        callbackMessage.totalBytes = totalBytes;
        dispatchMessage(requestId, PROGRESS_MESSAGE, callbackMessage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispatchError(Context context, String requestId, ErrorInfo error) {
        pendingResults.put(requestId, new UploadResult(null, error));
        CallbackMessage callbackMessage = CallbackMessage.obtain();
        callbackMessage.error = error;
        dispatchMessage(requestId, ERROR_MESSAGE, callbackMessage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispatchReschedule(Context context, String requestId, ErrorInfo error) {
        CallbackMessage callbackMessage = CallbackMessage.obtain();
        callbackMessage.error = error;
        dispatchMessage(requestId, RESCHEDULE_MESSAGE, callbackMessage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispatchSuccess(Context context, String requestId, Map resultData) {
        pendingResults.put(requestId, new UploadResult(resultData, null));
        CallbackMessage callbackMessage = CallbackMessage.obtain();
        callbackMessage.resultData = resultData;
        dispatchMessage(requestId, SUCCESS_MESSAGE, callbackMessage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UploadResult popPendingResult(String requestId) {
        return pendingResults.remove(requestId);
    }

    private void dispatchMessage(String requestId, int what, CallbackMessage callbackMessage) {
        readWriteLock.readLock().lock();
        try {
            for (UploadCallbackWrapper wrapper : callbackRegistry.values()) {
                if (wrapper != null && wrapper.matches(requestId)) {
                    // for each registered callback we send a message (different instance for each)
                    CallbackMessage copy = CallbackMessage.obtain(callbackMessage);
                    copy.callback = wrapper.callback;
                    copy.requestId = requestId;
                    handler.obtainMessage(what, copy).sendToTarget();
                }
            }
        } finally {
            // recycle original message (not used by handler)
            callbackMessage.recycle();
            readWriteLock.readLock().unlock();
        }
    }

    private void initListenerClass(Context context) {
        PackageManager packageManager = context.getPackageManager();
        String packageName = context.getPackageName();
        String className = null;
        try {
            ApplicationInfo info = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            if (info != null && info.metaData != null) {
                className = (String) info.metaData.get("cloudinaryCallbackService");
                if (StringUtils.isNotBlank(className)) {
                    listenerServiceClass = Class.forName(className);
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            Logger.e(TAG, String.format("Package name not found: %s", packageName));
        } catch (ClassNotFoundException e) {
            Logger.e(TAG, String.format("Listener class name not found: %s", className));
        }
    }

    private final static class UploadCallbackWrapper {
        private final UploadCallback callback;
        private final Set<String> requestIds;

        private UploadCallbackWrapper(UploadCallback callback) {
            this.callback = callback;
            requestIds = new HashSet<>();
        }

        private void addRequestId(String requestId) {
            requestIds.add(requestId);
        }

        boolean matches(String requestId) {
            return requestIds.isEmpty() || requestIds.contains(requestId);
        }
    }

    private final static class CallbackMessage {
        private static final Pools.SynchronizedPool<CallbackMessage> sPool = new Pools.SynchronizedPool<>(100);
        private UploadCallback callback;
        private String requestId;
        private long bytes;
        private long totalBytes;
        private ErrorInfo error;
        private Map resultData;

        static CallbackMessage obtain() {
            CallbackMessage instance = sPool.acquire();
            return (instance != null) ? instance : new CallbackMessage();
        }

        static CallbackMessage obtain(CallbackMessage callbackMessage) {
            CallbackMessage instance = obtain();
            instance.requestId = callbackMessage.requestId;
            instance.callback = callbackMessage.callback;
            instance.bytes = callbackMessage.bytes;
            instance.totalBytes = callbackMessage.totalBytes;
            instance.error = callbackMessage.error;
            instance.resultData = callbackMessage.resultData;
            return instance;
        }

        void recycle() {
            callback = null;
            requestId = null;
            bytes = -1;
            totalBytes = -1;
            error = null;
            resultData = null;
            sPool.release(this);
        }

    }
}
