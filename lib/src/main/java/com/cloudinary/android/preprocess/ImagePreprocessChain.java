package com.cloudinary.android.preprocess;

import android.graphics.Bitmap;
import android.graphics.Point;

/**
 * A preprocess chain to run on images before uploading. Pass an instance of a populated chain to {@link com.cloudinary.android.UploadRequest#preprocess(PreprocessChain)}.
 * The processing steps will run by the order in which they were added to the chain. This chain uses the default Bitmap encoder/decoder, however custom implementations
 * can be supplied using {@link PreprocessChain#loadWith(ResourceDecoder)} and {@link PreprocessChain#saveWith(ResourceEncoder)}.
 */
public class ImagePreprocessChain extends PreprocessChain<Bitmap> {
    /**
     * Convenience method for building an efficient dimension limiting chain using {@link BitmapDecoder} and {@link Limit}.
     * Use this in {@link com.cloudinary.android.UploadRequest#preprocess(PreprocessChain)}.
     * The scaling retains the original aspect ratio while guaranteeing the height and width are within the requested
     * maximum bounds. Note: If the image is already smaller it will be returned unchanged.
     *
     * @param maxWidth  The maximum width allowed. If the width of the image is greater, the image will be resized accordingly.
     * @param maxHeight The maximum height allowed. If the height of the image is greater, the image will be resized accordingly.
     * @return The prepared chain to pass on to {@link com.cloudinary.android.UploadRequest#preprocess(PreprocessChain)}
     */
    public static ImagePreprocessChain limitDimensionsChain(int maxWidth, int maxHeight) {
        return (ImagePreprocessChain) new ImagePreprocessChain()
                .loadWith(new BitmapDecoder(maxWidth, maxHeight))
                .addStep(new Limit(maxWidth, maxHeight));
    }

    /**
     * Convenience method for building a cropping chain using {@link Crop}.
     * Use this in {@link com.cloudinary.android.UploadRequest#preprocess(PreprocessChain)}.
     * The cropping points must form a within the image bounds.
     * Note: If the points form the same diagonal size as the original image, it will be returned unchanged
     * @param p1 First point that form the diagonal.
     * @param p2 Second point that form the diagonal.
     * @return The prepared chain to pass on to {@link com.cloudinary.android.UploadRequest#preprocess(PreprocessChain)}
     */
    public static ImagePreprocessChain cropChain(Point p1, Point p2) {
        return (ImagePreprocessChain) new ImagePreprocessChain()
                .addStep(new Crop(p1, p2));
    }

    @Override
    protected ResourceEncoder<Bitmap> getDefaultEncoder() {
        return new BitmapEncoder();
    }

    @Override
    protected ResourceDecoder<Bitmap> getDefaultDecoder() {
        return new BitmapDecoder();
    }
}
