package com.cloudinary.android.preprocess;

import android.content.Context;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.linkedin.android.litr.MediaTransformer;
import com.linkedin.android.litr.TransformationListener;
import com.linkedin.android.litr.analytics.TrackTransformationInfo;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Preprocess implementation for video transcoding.
 */
public class Transcode implements Preprocess<Uri> {

    private static final String TAG = Transcode.class.getSimpleName();
    private boolean isTransformationFinished;
    private final Object lockObject = new Object();
    private Parameters parameters;
    private Throwable transcodingError;

    public Transcode(Parameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public Uri execute(Context context, Uri resource) throws PreprocessException {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            throw new PreprocessException("Sdk version must be >= 18 to use LiTr");
        }

        String targetFilePath = context.getFilesDir() + File.separator + UUID.randomUUID().toString();
        File outputVideoFile = new File(targetFilePath);
        try {
            outputVideoFile.createNewFile();
        } catch (IOException e) {
            throw new PreprocessException("Cannot create output video file.");
        }

        MediaFormat targetVideoFormat = createTargetVideoFormat(parameters.getWidth(), parameters.getHeight(), parameters.getTargetVideoBitrateKbps(), parameters.getKeyFramesInterval(), parameters.getFrameRate());
        MediaFormat targetAudioFormat = createTargetAudioFormat(context, resource, parameters.getTargetAudioBitrateKbps());

        TransformationListener transformationListener = new VideoTransformationListener();
        MediaTransformer mediaTransformer = new MediaTransformer(context.getApplicationContext());
        mediaTransformer.transform(parameters.getRequestId(),
                resource,
                targetFilePath,
                targetVideoFormat,
                targetAudioFormat,
                transformationListener,
                MediaTransformer.GRANULARITY_DEFAULT,
                null);

        synchronized (lockObject) {
            try {
                if (!isTransformationFinished) {
                    lockObject.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (transcodingError != null) {
            String errorMessage = transcodingError.getMessage();
            transcodingError = null;
            throw new PreprocessException(errorMessage);
        }

        return Uri.parse(targetFilePath);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private MediaFormat createTargetVideoFormat(int width, int height, int bitrate, int keyFrameInterval, int frameRate) {
        MediaFormat targetVideoFormat = new MediaFormat();
        targetVideoFormat.setString(MediaFormat.KEY_MIME, "video/avc");
        targetVideoFormat.setInteger(MediaFormat.KEY_WIDTH, width);
        targetVideoFormat.setInteger(MediaFormat.KEY_HEIGHT, height);
        targetVideoFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
        targetVideoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, keyFrameInterval);
        targetVideoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
        targetVideoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);

        return targetVideoFormat;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private MediaFormat createTargetAudioFormat(Context context, Uri sourceVideoUri, int targetBitrateKbps) {
        MediaFormat audioMediaFormat = null;
        try {
            MediaExtractor mediaExtractor = new MediaExtractor();
            mediaExtractor.setDataSource(context, sourceVideoUri, null);
            int trackCount = mediaExtractor.getTrackCount();
            for (int track = 0; track < trackCount; track++) {
                MediaFormat mediaFormat = mediaExtractor.getTrackFormat(track);
                String mimeType = null;
                if (mediaFormat.containsKey(MediaFormat.KEY_MIME)) {
                    mimeType = mediaFormat.getString(MediaFormat.KEY_MIME);
                }
                if (mimeType != null && mimeType.startsWith("audio")) {
                    audioMediaFormat = new MediaFormat();
                    audioMediaFormat.setString(MediaFormat.KEY_MIME, mediaFormat.getString(MediaFormat.KEY_MIME));
                    audioMediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT));
                    audioMediaFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE));
                    audioMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, targetBitrateKbps * 1000);
                }
            }
        } catch (IOException ex) {
            Log.e(TAG, "Failed to extract audio track metadata: " + ex);
        }

        return audioMediaFormat;
    }

    private class VideoTransformationListener implements TransformationListener {
        @Override
        public void onStarted(@NonNull String id) {

        }

        @Override
        public void onProgress(@NonNull String id, float progress) {

        }

        @Override
        public void onCompleted(@NonNull String id, @Nullable List<TrackTransformationInfo> trackTransformationInfos) {
            synchronized (lockObject) {
                isTransformationFinished = true;
                lockObject.notify();
            }
        }

        @Override
        public void onCancelled(@NonNull String id, @Nullable List<TrackTransformationInfo> trackTransformationInfos) {
            synchronized (lockObject) {
                isTransformationFinished = true;
                lockObject.notify();
            }
        }

        @Override
        public void onError(@NonNull String id, @Nullable Throwable cause, @Nullable List<TrackTransformationInfo> trackTransformationInfos) {
            synchronized (lockObject) {
                isTransformationFinished = true;
                transcodingError = cause;
                lockObject.notify();
            }
        }
    }
}
