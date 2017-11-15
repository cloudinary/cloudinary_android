package com.cloudinary.android.preprocess;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * A preprocess chain to run on images before uploading. Pass an instance of a populated chain to {@link com.cloudinary.android.UploadRequest#preprocess(PreprocessChain)}.
 * The processing steps will run by the order in which they were added to the chain. This chain uses the default Bitmap encoder/decoder, however custom implementations
 * can be supplied using {@link PreprocessChain#loadWith(ResourceDecoder)} and {@link PreprocessChain#saveWith(ResourceEncoder)}.
 */
public class ImagePreprocessChain extends PreprocessChain<Bitmap> {
    /**
     * Creates a chain instance for reducing image dimensions. If the image is already smaller it will be returned unchanged.
     * The scaling retains aspect ratio while making sure the height and width are within the requested maximum bounds.
     * The chain also handled efficient decoding of the bitmap (see {@link DefaultBitmapDecoder#calculateInSampleSize(BitmapFactory.Options, int, int)}).
     *
     * @param maxWidth  The maximum width allowed. If the width of the image is greater, the image will be resized accordingly.
     * @param maxHeight The maximum height allowed. If the height of the image is greater, the image will be resized accordingly.
     * @return The prepared chain to pass on to {@link com.cloudinary.android.UploadRequest#preprocess(PreprocessChain)}
     */
    public static ImagePreprocessChain reduceDimensionsChain(int maxWidth, int maxHeight) {
        return (ImagePreprocessChain) new ImagePreprocessChain()
                .loadWith(new DefaultBitmapDecoder(maxWidth, maxHeight))
                .addStep(new ScaleDownIfLargerThan(maxWidth, maxHeight));
    }

    @Override
    protected ResourceEncoder<Bitmap> getDefaultEncoder() {
        return new DefaultBitmapEncoder();
    }

    @Override
    protected ResourceDecoder<Bitmap> getDefaultDecoder() {
        return new DefaultBitmapDecoder();
    }
}
