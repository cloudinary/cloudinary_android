package com.cloudinary.android;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.cloudinary.Cloudinary;
import com.cloudinary.Url;
import com.cloudinary.android.payload.ByteArrayPayload;
import com.cloudinary.android.payload.FilePayload;
import com.cloudinary.android.payload.LocalUriPayload;
import com.cloudinary.android.payload.Payload;
import com.cloudinary.android.payload.ResourcePayload;
import com.cloudinary.utils.StringUtils;

import java.util.Map;

/***
 * Main class used as entry point to any operation against Cloudinary. Use {@link CldAndroid#get()} to get an instance.
 * Must be initialized before use, see {@link #init(Context, SignatureProvider, Map)}.
 */
public class CldAndroid {
    public static final String INTENT_EXTRA_REQUEST_ID = "INTENT_EXTRA_REQUEST_ID";
    public static final String INTENT_EXTRA_REQUEST_RESULT_STATUS = "INTENT_EXTRA_REQUEST_RESULT_STATUS";
    public static final String ACTION_REQUEST_STARTED = "com.cloudinary.ACTION_REQUEST_STARTED";
    public static final String ACTION_REQUEST_FINISHED = "com.cloudinary.ACTION_REQUEST_FINISHED";
    private static final String TAG = CldAndroid.class.getSimpleName();

    private static CldAndroid _instance;

    private final com.cloudinary.Cloudinary cloudinary;
    private final RequestDispatcherInterface requestDispatcher;
    private final RequestProcessorInterface requestProcessor;
    private final CallbackDispatcher callbackDispatcher;
    private final BackgroundRequestStrategy strategy;
    private final SignatureProvider signatureProvider;
    private final UploadCallback callback;

    private GlobalUploadPolicy globalUploadPolicy = GlobalUploadPolicy.defaultPolicy();

    private CldAndroid(@NonNull Context context, @Nullable SignatureProvider provider, @Nullable Map<String, Object> config) {
        // use context to initialize components but DO NOT store it
        strategy = BackgroundStrategyProvider.provideStrategy();
        callbackDispatcher = new CallbackDispatcher(context);
        requestDispatcher = new RequestDispatcher(strategy);
        requestProcessor = new RequestProcessor(callbackDispatcher);
        strategy.init(context);
        this.signatureProvider = provider;

        String cloudinaryUrl = Utils.cloudinaryUrlFromContext(context);
        if (config != null) {
            cloudinary = new Cloudinary(config);
        } else if (StringUtils.isNotBlank(cloudinaryUrl)) {
            cloudinary = new Cloudinary(cloudinaryUrl);
        } else {
            cloudinary = new Cloudinary();
        }

        callback = new UploadCallback() {

            @Override
            public void onStart(String requestId) {
            }

            @Override
            public void onProgress(String requestId, long bytes, long totalBytes) {
            }

            @Override
            public void onSuccess(String requestId, Map resultData) {
                queueRoomFreed();
            }

            @Override
            public void onError(String requestId, String error) {
                queueRoomFreed();
            }

            @Override
            public void onReschedule(String requestId, String errorMessage) {
                queueRoomFreed();
            }
        };

        callbackDispatcher.registerCallback(callback);
    }

    /***
     * Setup the library with the required parameters. A flavor of init() must be called once before CldAndroid can be used, preferably in an implementation of {@link Application#onCreate()}.
     * @param context Android context for initializations. Does not get cached.
     */
    public static void init(@NonNull Context context) {
        init(context, null, null);
    }

    /***
     * Setup the library with the required parameters. A flavor of init() must be called once before CldAndroid can be used, preferably in an implementation of {@link Application#onCreate()}.
     * @param context Android context for initializations. Does not get cached.
     * @param config Cloudinary configuration parameters. If not supplied a cloudinary-url metadata must exist in the manifest.
     */
    public static void init(@NonNull Context context, @Nullable Map config) {
        init(context, null, config);
    }

    /***
     * Setup the library with the required parameters. A flavor of init() must be called once before CldAndroid can be used, preferably in an implementation of {@link Application#onCreate()}.
     * @param context Android context for initializations. Does not get cached.
     * @param provider A signature provider. Needed if using signed uploads.
     */
    public static void init(@NonNull Context context, @Nullable SignatureProvider provider) {
        init(context, provider, null);
    }

    /***
     * Setup the library with the required parameters. A flavor of init() must be called once before CldAndroid can be used, preferably in an implementation of {@link Application#onCreate()}.
     * @param context Android context for initializations. Does not get cached.
     * @param provider A signature provider. Needed if using signed uploads.
     * @param config Cloudinary configuration parameters. If not supplied a cloudinary-url metadata must exist in the manifest.
     */
    public static void init(@NonNull Context context, @Nullable SignatureProvider provider, @Nullable Map config) {
        synchronized (CldAndroid.class) {
            //noinspection ConstantConditions
            if (context == null) {
                throw new IllegalArgumentException("context cannot be null.");
            }


            if (_instance == null) {
                _instance = new CldAndroid(context, provider, config);
            } else {
                throw new IllegalStateException("CldAndroid is already initialized");
            }
        }
    }

    /***
     * Entry point for any operation against Cloudinary
     * @return An instance of the CldAndroid class to run operations against cloudinary.
     */
    public static CldAndroid get() {
        if (_instance == null) {
            throw new IllegalStateException("Must call init() before accessing Cloudinary.");
        }

        return _instance;
    }

    public static void setLogLevel(LogLevel logLevel) {
        Logger.logLevel = logLevel;
    }

    /***
     * Called every time a request finishes, meaning there's room for new requests.
     */
    private void queueRoomFreed() {
        int room = getGlobalUploadPolicy().getMaxConcurrentRequests() - strategy.getPendingImmediateJobsCount() - strategy.getRunningJobsCount();
        Logger.d(TAG, String.format("queueRoomFreed called, there's room for %d requests.", room));
        if (room > 0) {
            strategy.executeRequestsNow(room);
        }
    }

    /***
     * Get an instance of the Cloudinary class for raw operations (not wrapped).
     * @return A Pre-configured {@link com.cloudinary.Cloudinary} instance
     */

    public Cloudinary getCloudinary() {
        return cloudinary;
    }

    /***
     * Get a Cloudinary Url object used to construct urls to access and transform pre-uploaded resources.
     */
    public Url url() {
        return cloudinary.url();
    }

    /***
     * Cancel an upload request.
     * @param requestId Id of the request to cancel.
     * @return True if the request was found and cancelled successfully.
     */
    public boolean cancelRequest(String requestId) {
        return strategy.cancelRequest(requestId);
    }

    /***
     * * Cancel all upload requests.
     * @return The count of canceled requests and running jobs.
     */
    public int cancelAllRequests() {
        return strategy.cancelAllRequests();
    }

    /***
     * Entry point to start an upload of a raw resource.
     * @param rawResourceId Android R generated raw resource identifier
     * @return {@link UploadRequest} instance. Setup the request and call {@link UploadRequest#dispatch()} to start uploading.
     */
    public UploadRequest upload(int rawResourceId) {
        return buildUploadRequest(new ResourcePayload(rawResourceId));
    }

    /***
     * Entry point to start an upload of a uri.
     * @param uri Android R generated raw resource identifier
     * @return {@link UploadRequest} instance. Setup the request and call {@link UploadRequest#dispatch()} to start uploading.
     */
    public UploadRequest upload(Uri uri) {
        return buildUploadRequest(new LocalUriPayload(uri));
    }

    /***
     * Entry point to start an upload of a byte array.
     * @param bytes A byte array containing image/video/raw data to upload.
     * @return {@link UploadRequest} instance. Setup the request and call {@link UploadRequest#dispatch()} to start uploading.
     */
    public UploadRequest upload(byte[] bytes) {
        return buildUploadRequest(new ByteArrayPayload(bytes));
    }

    /***
     * Entry point to start an upload of a file.
     * @param filePath An absolute file path to upload.
     * @return {@link UploadRequest} instance. Setup the request and call {@link UploadRequest#dispatch()} to start uploading.
     */
    public UploadRequest upload(String filePath) {
        return buildUploadRequest(new FilePayload(filePath));
    }

    /***
     * Entry point to start an upload of a generic payload. Only use this with custom payloads.
     * @param payload The payload to upload
     * @return {@link UploadRequest} instance. Setup the request and call {@link UploadRequest#dispatch()} to start uploading.
     */
    public UploadRequest upload(Payload payload) {
        return buildUploadRequest(payload);
    }

    /***
     * Return the global upload policy.
     */
    public GlobalUploadPolicy getGlobalUploadPolicy() {
        return globalUploadPolicy;
    }

    /***
     * Setup the global upload policy for Cloudinary.
     * @param globalUploadPolicy The policy to set. See {@link RequestUploadPolicy.Builder}.
     * */
    public void setGlobalUploadPolicy(GlobalUploadPolicy globalUploadPolicy) {
        this.globalUploadPolicy = globalUploadPolicy;
    }

    /***
     * Register a callback for state changes and results of requests.
     * @param callback The callback to activate upon state changes and results.
     */
    public void registerCallback(UploadCallback callback) {
        callbackDispatcher.registerCallback(callback);
    }

    /***
     * Register a callback for state changes and results for a specific request.
     * @param requestId The id of the request.
     * @param callback The callback to activate upon state changes and results.
     */
    void registerCallback(String requestId, UploadCallback callback) {
        callbackDispatcher.registerCallback(requestId, callback);
    }

    /***
     * Unregister a callback
     * @param callback The callback to unregister.
     */
    public void unregisterCallback(UploadCallback callback) {
        callbackDispatcher.unregisterCallback(callback);
    }

    /***
     * Fetch a pending result. In case the app wasn't awake when the upload stopped, successfully or not, the result can be fetched here. Assuming
     * the app wakes up through a the callback service defined in the manifest, it should get the full results by calling this method.
     * Note: the result is cleared once this method is called.
     * @param requestId Id of the request to fetch results for.
     * @return The upload result.
     */
    public UploadResult popPendingResult(String requestId) {
        return callbackDispatcher.popPendingResult(requestId);
    }

    private UploadRequest buildUploadRequest(Payload payload) {
        UploadContext<Payload> payloadUploadContext = new UploadContext<>(payload, requestDispatcher);
        return new UploadRequest(payloadUploadContext, null);
    }

    /***
     * Process a single request, this runs after verifying all the policies and conditions are met. For internal use.
     * @param context Android context.
     */
    UploadStatus processRequest(Context context, ParamsAdaptable params) {
        return requestProcessor.processRequest(context, params);
    }

    boolean hasCredentials() {
        return StringUtils.isNotBlank(cloudinary.config.apiKey) && StringUtils.isNotBlank(cloudinary.config.apiSecret);
    }

    SignatureProvider getSignatureProvider() {
        return signatureProvider;
    }
}
