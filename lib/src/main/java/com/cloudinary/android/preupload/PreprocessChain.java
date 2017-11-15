package com.cloudinary.android.preupload;

import android.content.Context;

import com.cloudinary.android.payload.CouldNotDecodePayloadException;
import com.cloudinary.android.payload.ErrorCreatingNewBitmapException;
import com.cloudinary.android.payload.Payload;
import com.cloudinary.android.payload.PayloadNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * A preprocess chain to run on resource before uploading. Pass an instance of populated chain to {@link com.cloudinary.android.UploadRequest#preprocess(PreprocessChain)}.
 * The processing steps will run by the order in which they were added to the chain
 *
 * @param <T> The type of the resource to execute the processing on (e.g. {@link android.graphics.Bitmap})
 */
public final class PreprocessChain<T> {
    private List<Preprocess<T>> list = new ArrayList<>();

    /**
     * Create a new chain
     *
     * @param first A preprocess step to run
     */
    public PreprocessChain(Preprocess<T> first) {
        list.add(first);
    }

    /**
     * Add a preprocessing step to the chain
     *
     * @param step Preprocess step
     * @return itself for chaining.
     */
    public PreprocessChain<T> withStep(Preprocess<T> step) {
        list.add(step);
        return this;
    }

    /**
     * Execute the processing chain. this is for INTERNAL use by the upload request itself. Do not call this directly.
     *
     * @param context Android context
     * @param payload Payload to run the chain on
     * @return A filepath of the end result of the chain
     * @throws PayloadNotFoundException
     * @throws CouldNotDecodePayloadException
     * @throws ErrorCreatingNewBitmapException
     */
    public String execute(Context context, Payload payload) throws PayloadNotFoundException, CouldNotDecodePayloadException, ErrorCreatingNewBitmapException {
        T resource = list.get(0).prepare(context, payload);

        for (Preprocess<T> preprocess : list) {
            resource = preprocess.execute(context, resource);
        }

        return list.get(list.size() - 1).finalize(context, resource);
    }
}
