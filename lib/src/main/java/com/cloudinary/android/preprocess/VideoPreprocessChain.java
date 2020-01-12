package com.cloudinary.android.preprocess;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.util.UUID;

/**
 * A preprocess chain to run on videos before uploading. Pass an instance of a populated chain to {@link com.cloudinary.android.UploadRequest#preprocess(PreprocessChain)}.
 * The processing steps will run by the order in which they were added to the chain. This chain uses the default Video encoder/decoder, however custom implementations
 * can be supplied using {@link PreprocessChain#loadWith(ResourceDecoder)} and {@link PreprocessChain#saveWith(ResourceEncoder)}.
 */
public class VideoPreprocessChain extends PreprocessChain<Uri> {

    private static String targetfilePath;

    /**
     * Convenience method for building an efficient video transcoding chain using {@link VideoEncoder} and {@link Transcode}.
     * Use this in {@link com.cloudinary.android.UploadRequest#preprocess(PreprocessChain)}.
     *
     * @param context Android context.
     * @param parameters Transcoding parameters.
     * @return The prepared chain to pass on to {@link com.cloudinary.android.UploadRequest#preprocess(PreprocessChain)}
     */
    public static VideoPreprocessChain videoTranscodingChain(Context context, Parameters parameters) {
        targetfilePath = context.getFilesDir() + File.separator + UUID.randomUUID().toString();
        parameters.targetFilePath = targetfilePath;

        return (VideoPreprocessChain) new VideoPreprocessChain()
                .saveWith(new VideoEncoder(targetfilePath))
                .addStep(new Transcode(parameters));
    }

    @Override
    protected ResourceEncoder<Uri> getDefaultEncoder() {
        return new VideoEncoder(targetfilePath);
    }

    @Override
    protected ResourceDecoder<Uri> getDefaultDecoder() {
        return new VideoDecoder();
    }
}
