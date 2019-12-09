package com.cloudinary.android.preprocess;

import android.graphics.Bitmap;
import com.cloudinary.android.uploadwidget.UploadWidget;

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
     * Convenience method for building an upload widget preprocessing chain.
     * Use this in {@link com.cloudinary.android.UploadRequest#preprocess(PreprocessChain)}.
     * @param result The result from the upload widget.
     * @return The prepared chain to pass on to {@link com.cloudinary.android.UploadRequest#preprocess(PreprocessChain)}
     */
    public static ImagePreprocessChain uploadWidgetChain(UploadWidget.Result result) {
        return (ImagePreprocessChain) new ImagePreprocessChain()
                .addStep(new Rotate(result.getRotationAngle()))
                .addStep(new Crop(result.getCropPoints().getPoint1(), result.getCropPoints().getPoint2()));
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
