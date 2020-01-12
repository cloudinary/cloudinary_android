package com.cloudinary.android.preprocess;

import android.content.Context;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.linkedin.android.litr.MediaTransformer;
import com.linkedin.android.litr.TransformationListener;
import com.linkedin.android.litr.analytics.TrackTransformationInfo;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Preprocess implementation for video transcoding.
 */
public class Transcode implements Preprocess<Uri>, TransformationListener {

    private static final String TAG = Transcode.class.getSimpleName();
    private boolean isTransformationFinished;
    private Parameters parameters;
    private Throwable transcodingError;

    public Transcode(Parameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public Uri execute(Context context, Uri resource) throws PreprocessException {
        File outputVideoFile = new File(parameters.targetFilePath);
        try {
            outputVideoFile.createNewFile();
        } catch (IOException e) {
            throw new PreprocessException("Cannot create output video file.");
        }

        MediaFormat targetVideoFormat = createTargetVideoFormat(parameters.width, parameters.height, parameters.targetVideoBitrateKbps, parameters.keyFramesInterval, parameters.frameRate);
        MediaFormat targetAudioFormat = createTargetAudioFormat(context, resource, parameters.targetAudioBitrateKbps);

        MediaTransformer mediaTransformer = new MediaTransformer(context.getApplicationContext());
        mediaTransformer.transform(parameters.requestId,
                resource,
                parameters.targetFilePath,
                targetVideoFormat,
                targetAudioFormat,
                this,
                MediaTransformer.GRANULARITY_DEFAULT,
                null);

        synchronized (this) {
            try {
                if (!isTransformationFinished) {
                    wait();
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

        return Uri.parse(parameters.targetFilePath);
    }

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

    @Override
    public void onStarted(@NonNull String id) {

    }

    @Override
    public void onProgress(@NonNull String id, float progress) {

    }

    @Override
    public synchronized void onCompleted(@NonNull String id, @Nullable List<TrackTransformationInfo> trackTransformationInfos) {
        isTransformationFinished = true;
        notify();
    }

    @Override
    public synchronized void onCancelled(@NonNull String id, @Nullable List<TrackTransformationInfo> trackTransformationInfos) {
        isTransformationFinished = true;
        notify();
    }

    @Override
    public synchronized void onError(@NonNull String id, @Nullable Throwable cause, @Nullable List<TrackTransformationInfo> trackTransformationInfos) {
        isTransformationFinished = true;
        transcodingError = cause;
        notify();
    }
}
