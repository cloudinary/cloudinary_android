package com.cloudinary.android.preprocess;

import android.net.Uri;

/**
 * A preprocess chain to run on videos before uploading. Pass an instance of a populated chain to {@link com.cloudinary.android.UploadRequest#preprocess(PreprocessChain)}.
 * The processing steps will run by the order in which they were added to the chain. This chain uses the default Video encoder/decoder, however custom implementations
 * can be supplied using {@link PreprocessChain#loadWith(ResourceDecoder)} and {@link PreprocessChain#saveWith(ResourceEncoder)}.
 */
public class VideoPreprocessChain extends PreprocessChain<Uri> {

    /**
     * Convenience method for building an efficient video transcoding chain using {@link Transcode}.
     * Use this in {@link com.cloudinary.android.UploadRequest#preprocess(PreprocessChain)}.
     *
     * @param parameters Transcoding parameters.
     * @return The prepared chain to pass on to {@link com.cloudinary.android.UploadRequest#preprocess(PreprocessChain)}
     */
    public static VideoPreprocessChain videoTranscodingChain(Parameters parameters) {
        return (VideoPreprocessChain) new VideoPreprocessChain()
                .addStep(new Transcode(parameters));
    }

    @Override
    protected ResourceEncoder<Uri> getDefaultEncoder() {
        return new VideoEncoder();
    }

    @Override
    protected ResourceDecoder<Uri> getDefaultDecoder() {
        return new VideoDecoder();
    }
}
