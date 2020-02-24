package com.cloudinary.android.preprocess;

import android.content.Context;

import com.cloudinary.android.UploadRequest;
import com.cloudinary.android.payload.Payload;
import com.cloudinary.android.payload.PayloadNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * A preprocess chain to run on resource before uploading. Pass an instance of a populated chain to {@link UploadRequest#preprocess(PreprocessChain)}.
 * The processing steps will run by the order in which they were added to the chain. Note: The best practice is to use a concrete subclass
 * rather than extend this class. A chain can be used for manipulating and/or validating resources before starting the upload.
 *
 * @param <T> The type of the resource to execute the processing on (e.g. {@link android.graphics.Bitmap})
 */
public abstract class PreprocessChain<T> {
    private ResourceDecoder<T> decoder;
    private ResourceEncoder<T> encoder;
    private List<Preprocess<T>> preprocessList = new ArrayList<>();


    protected abstract ResourceEncoder<T> getDefaultEncoder();

    protected abstract ResourceDecoder<T> getDefaultDecoder();

    /**
     * Set a decoder to decode the resource
     *
     * @param decoder The decoder to use
     * @return itself for chaining.
     */
    public PreprocessChain<T> loadWith(ResourceDecoder<T> decoder) {
        this.decoder = decoder;
        return this;
    }

    /**
     * Set an encoder to encode the resource
     *
     * @param encoder The encoder to use
     * @return itself for chaining.
     */
    public PreprocessChain<T> saveWith(ResourceEncoder<T> encoder) {
        this.encoder = encoder;
        return this;
    }

    /**
     * Add a preprocessing step to the chain
     *
     * @param step Preprocess step
     * @return itself for chaining.
     */
    public PreprocessChain<T> addStep(Preprocess<T> step) {
        preprocessList.add(step);
        return this;
    }

    /**
     * Execute the processing chain. this is for INTERNAL use by the upload request itself. Do not call this directly.
     *
     * @param context Android context
     * @param payload Payload to run the chain on
     * @return A filepath of the end result of the chain
     * @throws PayloadNotFoundException if the payload is not found
     * @throws PayloadDecodeException if the payload is found but cannot be decoded
     * @throws ResourceCreationException if the processing is done but the result cannot be saved.
     */
    public String execute(Context context, Payload payload) throws PayloadNotFoundException, PreprocessException {
        ensureDecoderAndEncoder();
        T resource = decoder.decode(context, payload);

        for (Preprocess<T> preprocess : preprocessList) {
            resource = preprocess.execute(context, resource);
        }

        return encoder.encode(context, resource);
    }

    private void ensureDecoderAndEncoder() {
        if (encoder == null) {
            encoder = getDefaultEncoder();
        }

        if (decoder == null) {
            decoder = getDefaultDecoder();
        }
    }

    /**
     * Checks if this chain is an empty chain (NOP chain).
     *
     * @return True if it's empty.
     */
    public boolean isEmpty() {
        return encoder == null && decoder == null && preprocessList.isEmpty();
    }
}
