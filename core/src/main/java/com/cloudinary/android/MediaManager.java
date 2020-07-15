package com.cloudinary.android;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cloudinary.Cloudinary;
import com.cloudinary.Configuration;
import com.cloudinary.Url;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.cloudinary.android.callback.UploadResult;
import com.cloudinary.android.callback.UploadStatus;
import com.cloudinary.android.download.DownloadRequestBuilder;
import com.cloudinary.android.download.DownloadRequestBuilderFactory;
import com.cloudinary.android.payload.ByteArrayPayload;
import com.cloudinary.android.payload.FilePayload;
import com.cloudinary.android.payload.LocalUriPayload;
import com.cloudinary.android.payload.Payload;
import com.cloudinary.android.payload.ResourcePayload;
import com.cloudinary.android.policy.GlobalUploadPolicy;
import com.cloudinary.android.policy.UploadPolicy;
import com.cloudinary.android.signed.SignatureProvider;
import com.cloudinary.utils.StringUtils;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.cloudinary.android.ResponsiveUrl.Preset;

/**
 * Main class used as entry point to any operation against Cloudinary. Use {@link MediaManager#get()} to get an instance.
 * Must be initialized before use, see {@link #init(Context, SignatureProvider, Map)}.
 */
public class MediaManager {
    public static final String VERSION = "1.30.0";
    public static final String INTENT_EXTRA_REQUEST_ID = "INTENT_EXTRA_REQUEST_ID";
    public static final String INTENT_EXTRA_REQUEST_RESULT_STATUS = "INTENT_EXTRA_REQUEST_RESULT_STATUS";
    public static final String ACTION_REQUEST_STARTED = "com.cloudinary.ACTION_REQUEST_STARTED";
    public static final String ACTION_REQUEST_FINISHED = "com.cloudinary.ACTION_REQUEST_FINISHED";
    private static final String TAG = MediaManager.class.getSimpleName();

    private static MediaManager _instance;

    private final com.cloudinary.Cloudinary cloudinary;
    private final RequestDispatcher requestDispatcher;
    private final RequestProcessor requestProcessor;
    private final CallbackDispatcher callbackDispatcher;
    private final SignatureProvider signatureProvider;
    private final ImmediateRequestsRunner immediateRequestsRunner;

    private final ExecutorService executor;

    private GlobalUploadPolicy globalUploadPolicy = GlobalUploadPolicy.defaultPolicy();
    private DownloadRequestBuilderFactory downloadRequestBuilderFactory;

    private MediaManager(@NonNull Context context, @Nullable SignatureProvider signatureProvider, @Nullable Map config) {
        executor = new ThreadPoolExecutor(4, 4,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());

        // use context to initialize components but DO NOT store it
        BackgroundRequestStrategy strategy = BackgroundStrategyProvider.provideStrategy();
        callbackDispatcher = new DefaultCallbackDispatcher(context);
        requestProcessor = new DefaultRequestProcessor(callbackDispatcher);
        immediateRequestsRunner = new DefaultImmediateRequestsRunner(requestProcessor);
        requestDispatcher = new DefaultRequestDispatcher(strategy, immediateRequestsRunner);

        strategy.init(context);
        this.signatureProvider = signatureProvider;

        String cloudinaryUrl = Utils.cloudinaryUrlFromContext(context);
        if (config != null) {
            cloudinary = new Cloudinary(config);
        } else if (StringUtils.isNotBlank(cloudinaryUrl)) {
            cloudinary = new Cloudinary(cloudinaryUrl);
        } else {
            cloudinary = new Cloudinary();
        }

        callbackDispatcher.registerCallback(new UploadCallback() {

            @Override
            public void onStart(String requestId) {
            }

            @Override
            public void onProgress(String requestId, long bytes, long totalBytes) {
            }

            @Override
            public void onSuccess(String requestId, Map resultData) {
                requestDispatcher.queueRoomFreed();
            }

            @Override
            public void onError(String requestId, ErrorInfo error) {
                requestDispatcher.queueRoomFreed();
            }

            @Override
            public void onReschedule(String requestId, ErrorInfo error) {
                requestDispatcher.queueRoomFreed();
            }
        });
    }

    /**
     * Setup the library with the required parameters. A flavor of init() must be called once before MediaManager can be used, preferably in an implementation of {@link Application#onCreate()}.
     *
     * @param context Android context for initializations. Does not get cached.
     */
    public static void init(@NonNull Context context) {
        init(context, null, (Map) null);
    }

    /**
     * Setup the library with the required parameters. A flavor of init() must be called once before MediaManager can be used, preferably in an implementation of {@link Application#onCreate()}.
     *
     * @param context Android context for initializations. Does not get cached.
     * @param config  Cloudinary configuration parameters. If not supplied a cloudinary-url metadata must exist in the manifest.
     */
    public static void init(@NonNull Context context, @Nullable Map config) {
        init(context, null, config);
    }

    /**
     * Setup the library with the required parameters. A flavor of init() must be called once before MediaManager can be used, preferably in an implementation of {@link Application#onCreate()}.
     *
     * @param context Android context for initializations. Does not get cached.
     * @param config  Cloudinary configuration parameters. If not supplied a cloudinary-url metadata must exist in the manifest.
     */
    public static void init(@NonNull Context context, @Nullable Configuration config) {
        Map<String, Object> map = null;
        if (config != null) {
            map = config.asMap();

        }
        init(context, null, map);
    }

    /**
     * Setup the library with the required parameters. A flavor of init() must be called once before MediaManager can be used, preferably in an implementation of {@link Application#onCreate()}.
     *
     * @param context           Android context for initializations. Does not get cached.
     * @param signatureProvider A signature provider. Needed if using signed uploads.
     */
    public static void init(@NonNull Context context, @Nullable SignatureProvider signatureProvider) {
        init(context, signatureProvider, (Map) null);
    }

    /**
     * Setup the library with the required parameters. A flavor of init() must be called once before MediaManager can be used, preferably in an implementation of {@link Application#onCreate()}.
     *
     * @param context  Android context for initializations. Does not get cached.
     * @param provider A signature provider. Needed if using signed uploads.
     * @param config   Cloudinary configuration parameters. If not supplied a cloudinary-url metadata must exist in the manifest.
     */
    public static void init(@NonNull Context context, @Nullable SignatureProvider provider, @Nullable Map config) {
        synchronized (MediaManager.class) {
            //noinspection ConstantConditions
            if (context == null) {
                throw new IllegalArgumentException("context cannot be null.");
            }


            if (_instance == null) {
                _instance = new MediaManager(context, provider, config);
            } else {
                throw new IllegalStateException("MediaManager is already initialized");
            }
        }
    }

    /**
     * Setup the library with the required parameters. A flavor of init() must be called once before MediaManager can be used, preferably in an implementation of {@link Application#onCreate()}.
     *
     * @param context  Android context for initializations. Does not get cached.
     * @param provider A signature provider. Needed if using signed uploads.
     * @param config   Cloudinary configuration parameters. If not supplied a cloudinary-url metadata must exist in the manifest.
     */
    public static void init(@NonNull Context context, @Nullable SignatureProvider provider, @Nullable Configuration config) {
        Map map = null;
        if (config != null) {
            map = config.asMap();
        }
        init(context, provider, map);
    }

    /**
     * Entry point for any operation against Cloudinary
     *
     * @return An instance of the MediaManager class to run operations against cloudinary.
     */
    public static MediaManager get() {
        if (_instance == null) {
            throw new IllegalStateException("Must call init() before accessing Cloudinary.");
        }

        return _instance;
    }

    /**
     * Set the log level. In order to affect initialization logging this can be set before calling {@link MediaManager#init(Context)}.
     *
     * @param logLevel The log level to set, see {@link LogLevel}.
     */
    public static void setLogLevel(LogLevel logLevel) {
        Logger.logLevel = logLevel;
    }

    /**
     * Get an instance of the Cloudinary class for raw operations (not wrapped).
     *
     * @return A Pre-configured {@link com.cloudinary.Cloudinary} instance
     */

    public Cloudinary getCloudinary() {
        return cloudinary;
    }

    /**
     * Get a Cloudinary Url object used to construct urls to access and transform pre-uploaded resources.
     */
    public Url url() {
        Url url = cloudinary.url();

        // set https as default for android P and up - in P the default policy fails all http
        // requests
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            url.secure(true);
        }

        return url;
    }

    /**
     * Cancel an upload request.
     *
     * @param requestId Id of the request to cancel.
     * @return True if the request was found and cancelled successfully.
     */
    public boolean cancelRequest(String requestId) {
        return immediateRequestsRunner.cancelRequest(requestId) || requestDispatcher.cancelRequest(requestId);
    }

    /**
     * * Cancel all upload requests.
     *
     * @return The count of canceled requests and running jobs.
     */
    public int cancelAllRequests() {
        return requestDispatcher.cancelAllRequests() + immediateRequestsRunner.cancelAllRequests();
    }

    /**
     * Entry point to start an upload of a raw resource.
     *
     * @param rawResourceId Android R generated raw resource identifier
     * @return {@link UploadRequest} instance. Setup the request and call {@link UploadRequest#dispatch()} to start uploading.
     */
    public UploadRequest upload(int rawResourceId) {
        return buildUploadRequest(new ResourcePayload(rawResourceId));
    }

    /**
     * Entry point to start an upload of a uri.
     *
     * @param uri Android R generated raw resource identifier
     * @return {@link UploadRequest} instance. Setup the request and call {@link UploadRequest#dispatch()} to start uploading.
     */
    public UploadRequest upload(Uri uri) {
        return buildUploadRequest(new LocalUriPayload(uri));
    }

    /**
     * Entry point to start an upload of a byte array.
     *
     * @param bytes A byte array containing image/video/raw data to upload.
     * @return {@link UploadRequest} instance. Setup the request and call {@link UploadRequest#dispatch()} to start uploading.
     */
    public UploadRequest upload(byte[] bytes) {
        return buildUploadRequest(new ByteArrayPayload(bytes));
    }

    /**
     * Entry point to start an upload of a file.
     *
     * @param filePath An absolute file path to upload.
     * @return {@link UploadRequest} instance. Setup the request and call {@link UploadRequest#dispatch()} to start uploading.
     */
    public UploadRequest upload(String filePath) {
        return buildUploadRequest(new FilePayload(filePath));
    }

    /**
     * Entry point to start an upload of a generic payload. Only use this with custom payloads.
     *
     * @param payload The payload to upload
     * @return {@link UploadRequest} instance. Setup the request and call {@link UploadRequest#dispatch()} to start uploading.
     */
    public UploadRequest upload(Payload payload) {
        return buildUploadRequest(payload);
    }

    /**
     * Return the global upload policy.
     */
    public GlobalUploadPolicy getGlobalUploadPolicy() {
        return globalUploadPolicy;
    }

    /**
     * Setup the global upload policy for Cloudinary.
     *
     * @param globalUploadPolicy The policy to set. See {@link UploadPolicy.Builder}.
     */
    public void setGlobalUploadPolicy(GlobalUploadPolicy globalUploadPolicy) {
        this.globalUploadPolicy = globalUploadPolicy;
    }

    /**
     * Register a callback for state changes and results of requests.
     *
     * @param callback The callback to activate upon state changes and results.
     */
    public void registerCallback(UploadCallback callback) {
        callbackDispatcher.registerCallback(callback);
    }

    /**
     * Register a callback for state changes and results for a specific request.
     *
     * @param requestId The id of the request.
     * @param callback  The callback to activate upon state changes and results.
     */
    void registerCallback(String requestId, UploadCallback callback) {
        callbackDispatcher.registerCallback(requestId, callback);
    }

    /**
     * Unregister a callback
     *
     * @param callback The callback to unregister.
     */
    public void unregisterCallback(UploadCallback callback) {
        callbackDispatcher.unregisterCallback(callback);
    }

    /**
     * Fetch a pending result. In case the app wasn't awake when the upload stopped, successfully or not, the result can be fetched here. Assuming
     * the app wakes up through a the callback service defined in the manifest, it should get the full results by calling this method.
     * Note: the result is cleared once this method is called.
     *
     * @param requestId Id of the request to fetch results for.
     * @return The upload result.
     */
    public UploadResult popPendingResult(String requestId) {
        return callbackDispatcher.popPendingResult(requestId);
    }

    void dispatchRequestError(Context context, String requestId, ErrorInfo error) {
        callbackDispatcher.dispatchError(context, requestId, error);
    }

    private UploadRequest<Payload> buildUploadRequest(Payload payload) {
        UploadContext<Payload> payloadUploadContext = new UploadContext<>(payload, requestDispatcher);
        return new UploadRequest<>(payloadUploadContext);
    }

    /**
     * Create a new responsive url generator instance.
     *
     * @param autoWidth  Specifying true will adjust the image width to the view width
     * @param autoHeight Specifying true will adjust the image height to the view height
     * @param cropMode   Crop mode to use in the transformation. See <a href="https://cloudinary.com/documentation/image_transformation_reference#crop_parameter">here</a>).
     * @param gravity    Gravity to use in the transformation. See <a href="https://cloudinary.com/documentation/image_transformation_reference#gravity_parameter">here</a>).
     */
    public ResponsiveUrl responsiveUrl(boolean autoWidth, boolean autoHeight, @NonNull String cropMode, @NonNull String gravity) {
        return new ResponsiveUrl(this.cloudinary, autoWidth, autoHeight, cropMode, gravity);
    }

    /**
     * Create a new responsive url generator instance.
     * @param preset A predefined set of responsive parameters, see {@link Preset}.
     * @return The responsive url generator. Use {@link ResponsiveUrl#generate} to build the final
     * url.
     */
    public ResponsiveUrl responsiveUrl(@NonNull Preset preset) {
        return preset.get(this.getCloudinary());
    }

    /**
     * Create a new responsive url.
     *
     * @param view     The view to adapt the resource dimensions to.
     * @param baseUrl  A url to be used as a base to the responsive transformation. This url can
     *                 contain any configurations and transformations. The generated responsive
     *                 transformation will be chained as the last transformation in the url.
     *                 Important: When generating using a base url, it's preferable to not include
     *                 any cropping/scaling in the original transformations.
     * @param preset   A predefined set of responsive parameters, see {@link Preset}.
     * @param callback Callback to called when the modified Url is ready.
     */
    public void responsiveUrl(View view, Url baseUrl, Preset preset, ResponsiveUrl.Callback callback) {
        preset.get(this.getCloudinary()).generate(baseUrl, view, callback);
    }

    /**
     * Create a new responsive url.
     *
     * @param view     The view to adapt the resource dimensions to.
     * @param publicId The public id of the cloudinary resource
     * @param preset   A predefined set of responsive parameters, see {@link Preset}.
     * @param callback Callback to called when the modified Url is ready.
     */
    public void responsiveUrl(View view, String publicId, Preset preset, ResponsiveUrl.Callback callback) {
        preset.get(this.getCloudinary()).generate(publicId, view, callback);
    }

    /**
     * Process a single request, this runs after verifying all the policies and conditions are met. For internal use.
     *
     * @param context Android context.
     */
    UploadStatus processRequest(Context context, RequestParams params) {
        return requestProcessor.processRequest(context, params);
    }

    boolean hasCredentials() {
        return StringUtils.isNotBlank(cloudinary.config.apiKey) && StringUtils.isNotBlank(cloudinary.config.apiSecret);
    }

    SignatureProvider getSignatureProvider() {
        return signatureProvider;
    }

    void execute(Runnable runnable) {
        executor.execute(runnable);
    }

    /**
     * Set a {@link DownloadRequestBuilderFactory} factory that will construct the
     * {@link DownloadRequestBuilder} instance, to be used when creating download requests
     * using {@link #download(Context)}.
     */
    public void setDownloadRequestBuilderFactory(DownloadRequestBuilderFactory factory) {
        downloadRequestBuilderFactory = factory;
    }

    /**
     * Create a new {@link DownloadRequestBuilder} to be used to create a download request.
     * @param context Android context
     * @return The {@link DownloadRequestBuilder} that will create the download request.
     */
    public DownloadRequestBuilder download(@NonNull Context context) {
        if (downloadRequestBuilderFactory == null) {
            throw new IllegalStateException("Must set a factory before downloading.");
        }

        return downloadRequestBuilderFactory.createDownloadRequestBuilder(context);
    }
}
