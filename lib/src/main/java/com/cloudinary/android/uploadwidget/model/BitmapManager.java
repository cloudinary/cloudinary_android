package com.cloudinary.android.uploadwidget.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import androidx.collection.LruCache;

import com.cloudinary.android.uploadwidget.utils.BitmapUtils;
import com.cloudinary.android.uploadwidget.utils.MediaType;
import com.cloudinary.android.uploadwidget.utils.UriUtils;
import com.cloudinary.utils.StringUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.UUID;
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

    /**
     * Return a bitmap manager instance.
     */
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
    public void load(final Context context, final Uri uri, final int width, final int height, final LoadCallback callback) {
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
     * Get a video file's thumbnail.
     * @param context Android context.
     * @param uri Uri of the video file.
     * @param width Thumbnail's width.
     * @param height Thumbnail's height.
     * @param callback The callback to be called when loading the thumbnail.
     */
    public void thumbnail(final Context context, final Uri uri, final int width, final int height, final LoadCallback callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (UriUtils.getMediaType(context, uri) == MediaType.VIDEO) {
                        String hash = getHash(uri.toString() + width + height);
                        Bitmap bitmap = memoryCache.get(hash);
                        if (bitmap == null) {
                            bitmap = UriUtils.getVideoThumbnail(context, uri);
                            memoryCache.put(hash, bitmap);
                        }
                        Dimensions dimensions = BitmapUtils.getBitmapDimensions(context, uri);

                        onLoadSuccess(bitmap, dimensions, callback);
                    } else {
                        onLoadFailed(callback);
                    }
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
    public void save(final Context context, final Bitmap bitmap, final SaveCallback callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                FileOutputStream fos = null;
                String fileName = UUID.randomUUID().toString();
                try {
                    fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    Uri bitmapUri = Uri.fromFile(context.getFileStreamPath(fileName));

                    onSaveSuccess(bitmapUri, callback);
                } catch (Exception e) {
                    onSaveFailed(callback);
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                            if (StringUtils.isBlank(fileName)) {
                                // failed, delete the file just in case it's there:
                                context.deleteFile(fileName);
                            }
                        } catch (IOException ignored) {
                        }
                    }
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

    private void onLoadSuccess(final Bitmap bitmap, final Dimensions dimensions, final LoadCallback callback) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onSuccess(bitmap, dimensions);
                }
            }
        });
    }

    private void onLoadFailed(final LoadCallback callback) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onFailure();
                }
            }
        });
    }

    private void onSaveSuccess(final Uri resultUri, final SaveCallback callback) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onSuccess(resultUri);
                }
            }
        });
    }

    private void onSaveFailed(final SaveCallback callback) {
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
        String hash = "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(plaintext.getBytes());
            hash = new String(digest.digest());
        } catch (Exception ignored) {
        }

        return hash;
    }

    /**
     * Callback for loading a bitmap.
     */
    public interface LoadCallback {

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
    public interface SaveCallback {

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
