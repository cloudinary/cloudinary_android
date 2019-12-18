package com.cloudinary.android.uploadwidget.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.util.LruCache;

import com.cloudinary.android.preprocess.BitmapEncoder;
import com.cloudinary.android.preprocess.ResourceCreationException;

import java.security.MessageDigest;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Asynchronous bitmap manager that loads and saves bitmaps. The manager uses a {@link LruCache} to cache the bitmaps
 * for better performance.
 */
public class BitmapManager {

    private LruCache<String, Bitmap> memoryCache;
    private ExecutorService executor;
    private Handler mainThreadHandler;
    private static BitmapManager instance;

    public synchronized static BitmapManager get() {
        if (instance == null) {
            instance = new BitmapManager();
        }

        return instance;
    }

    private BitmapManager() {
        mainThreadHandler = new Handler(Looper.getMainLooper());
        executor = Executors.newFixedThreadPool(4);

        initMemoryCache();
    }

    /**
     * Load the uri and downsample the bitmap, adjusting it to the specified dimensions.
     *
     * @param context  Android context
     * @param uri      Uri of the image to be loaded
     * @param width    Width for the output bitmap to be adjusted to (not necessarily exact fit).
     * @param height   Height for the output bitmap to be adjusted to (not necessarily exact fit).
     * @param callback The callback to be called when loading the bitmap.
     */
    public void load(final Context context, final Uri uri, final int width, final int height, final LoadBitmapCallback callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String hash = getHash(uri.toString() + width + height);
                    Bitmap bitmap = memoryCache.get(hash);
                    if (bitmap == null) {
                        bitmap = BitmapUtils.decodeSampledBitmapFromUri(context, uri, width, height);
                        memoryCache.put(hash, bitmap);
                    }
                    Dimensions dimensions = BitmapUtils.getBitmapDimensions(context, uri);

                    onLoadSuccess(bitmap, dimensions, callback);
                } catch (Exception e) {
                    onLoadFailed(callback);
                }
            }
        });
    }

    /**
     * Save the bitmap into a file
     * @param context Android context.
     * @param bitmap Bitmap to save.
     * @param callback the callback to be called when saving the bitmap.
     */
    public void save(final Context context, final Bitmap bitmap, final SaveResultCallback callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    BitmapEncoder encoder = new BitmapEncoder(BitmapEncoder.Format.PNG, 100);
                    String fileName = encoder.encode(context, bitmap);
                    final Uri bitmapUri = Uri.fromFile(context.getFileStreamPath(fileName));

                    onSaveSuccess(bitmapUri, callback);
                } catch (ResourceCreationException e) {
                    onSaveFailed(callback);
                }
            }
        });
    }

    private void initMemoryCache() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;
        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    private void onLoadSuccess(final Bitmap bitmap, final Dimensions dimensions, final LoadBitmapCallback callback) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onSuccess(bitmap, dimensions);
                }
            }
        });
    }

    private void onLoadFailed(final LoadBitmapCallback callback) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onFailure();
                }
            }
        });
    }

    private void onSaveSuccess(final Uri resultUri, final SaveResultCallback callback) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onSuccess(resultUri);
                }
            }
        });
    }

    private void onSaveFailed(final SaveResultCallback callback) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onFailure();
                }
            }
        });
    }

    private String getHash(String plaintext) {
        String hashString = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(plaintext.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            hashString = hexString.toString();
        } catch (Exception ignored) {
        }

        return hashString;
    }

    /**
     * Callback for loading a bitmap.
     */
    public interface LoadBitmapCallback {

        /**
         * Called when the bitmap is loaded successfully.
         *
         * @param bitmap             The loaded bitmap.
         * @param originalDimensions The original bitmap's dimensions.
         */
        void onSuccess(Bitmap bitmap, Dimensions originalDimensions);

        /**
         * Called when failed to load the bitmap.
         */
        void onFailure();
    }

    /**
     * Callback for saving a bitmap into a file.
     */
    public interface SaveResultCallback {

        /**
         * Called when the bitmap was saved successfully.
         *
         * @param resultUri Result's file uri.
         */
        void onSuccess(Uri resultUri);

        /**
         * Called when failed to save the bitmap.
         */
        void onFailure();
    }

}
