package com.cloudinary.android;

import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.cloudinary.Cloudinary;
import com.cloudinary.Url;

/**
 * This class is used to generate view-size aware cloudinary Urls. It takes any {@link View} and
 * a {@link Url} as input and returns a modified {@link Url} with the width/height transformation
 * included, according to the chosen parameters.
 * Note: When using this class, it's preferable to not include any cropping/scaling/dpr
 * transformation in the base {@link Url} used. This can lead to unexpected results.
 */
public class ResponsiveUrl {
    // defaults
    private static final int DEFAULT_MIN_DIMENSION = 200;
    private static final int DEFAULT_MAX_DIMENSION = 1000;
    private static final int DEFAULT_STEP_SIZE = 200;

    // holds url mapped to class instance hashes to make sure we're synced
    private static final SparseArray<Url> viewsInProgress = new SparseArray<>();

    // fields
    private final Cloudinary cloudinary;
    private final String cropMode;
    private final String gravity;
    private final boolean autoWidth;
    private final boolean autoHeight;
    private int stepSize = DEFAULT_STEP_SIZE;
    private int maxDimension = DEFAULT_MAX_DIMENSION;
    private int minDimension = DEFAULT_MIN_DIMENSION;

    /**
     * Create a new responsive url generator instance.
     *
     * @param cloudinary The cloudinary instance to use.
     * @param autoWidth  Specifying true will adjust the image width to the view width
     * @param autoHeight Specifying true will adjust the image height to the view height
     * @param cropMode   Crop mode to use in the transformation. See <a href="https://cloudinary.com/documentation/image_transformation_reference#crop_parameter">here</a>).
     * @param gravity    Gravity to use in the transformation. See <a href="https://cloudinary.com/documentation/image_transformation_reference#gravity_parameter">here</a>).
     */
    ResponsiveUrl(@NonNull Cloudinary cloudinary, boolean autoWidth, boolean autoHeight, @Nullable String cropMode, @Nullable String gravity) {
        this.cloudinary = cloudinary;
        this.autoWidth = autoWidth;
        this.autoHeight = autoHeight;
        this.cropMode = cropMode;
        this.gravity = gravity;
    }

    /**
     * Step size i pixels. This is used to limit the number of generated transformations.
     * The actual width/height parameter in the constructed url will always be a multiplication of
     * step size and not the exact view width/height.
     * For example, when using `width` with `stepSize` 100 on a view with width between 301 and 400
     * will render as `w_400` in the url.
     *
     * @param stepSize The step size to use, in pixels.
     * @return Itself for chaining.
     */
    public ResponsiveUrl stepSize(int stepSize) {
        this.stepSize = stepSize;
        return this;
    }

    /**
     * Limit the minimum allowed dimension, in pixels. If the actual view width or height are
     * larger than the value chosen here, this value will be used instead. This is useful to
     * limit the total number of generated transformations.
     * @param maxDimension The highest allowed dimension, in pixels.
     * @return itself for chaining.
     */
    public ResponsiveUrl maxDimension(int maxDimension) {
        this.maxDimension = maxDimension;
        return this;
    }

    /**
     * Limit the minimum allowed dimension, in pixels. If the actual view width or height are
     * smaller than the value chosen here, this value will be used instead. This is useful to
     * limit the total number of generated transformations.
     * @param minDimension The smallest allowed dimension, in pixels.
     * @return itself for chaining.
     */
    public ResponsiveUrl minDimension(int minDimension) {
        this.minDimension = minDimension;
        return this;
    }

    /**
     * Generate the modified url.
     *
     * @param publicId The public id of the cloudinary resource
     * @param view     The view to adapt the resource dimensions to.
     * @param callback Callback to called when the modified Url is ready.
     */
    public void generate(String publicId, final View view, final Callback callback) {
        generate(cloudinary.url().publicId(publicId), view, callback);
    }

    /**
     * Generate the modified url.
     *
     * @param baseUrl  A url to be used as a base to the responsive transformation. This url can
     *                 contain any configurations and transformations. The generated responsive
     *                 transformation will be chained as the last transformation in the url.
     *                 Important: When generating using a base url, it's preferable to not include
     *                 any cropping/scaling in the original transformations.
     * @param view     The view to adapt the resource dimensions to.
     * @param callback Callback to called when the modified Url is ready.
     */
    public void generate(final Url baseUrl, final View view, final Callback callback) {
        assertViewValidForResponsive(view);
        final int key = view.hashCode();

        int width = view.getWidth();
        int height = view.getHeight();

        if (conditionsAreMet(width, height)) {
            // The required dimensions are already known, build url:
            callback.onUrlReady(buildUrl(view, baseUrl));
            viewsInProgress.remove(key);
        } else {
            // save the link between the requested url and the specific view, so that
            // if in the meantime the view is assigned a new item (e.g. recycling views in a list)
            // it won't override the correct data.
            viewsInProgress.put(key, baseUrl);
            view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    view.getViewTreeObserver().removeOnPreDrawListener(this);
                    if (baseUrl.equals(viewsInProgress.get(key))) {
                        callback.onUrlReady(buildUrl(view, baseUrl));
                        viewsInProgress.remove(key);
                    }

                    return true;
                }
            });
        }
    }

    /**
     * Verify that the given view is properly configured to handle dynamically sized images.
     *
     * @param view The view to inspect.
     */
    private void assertViewValidForResponsive(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN &&
                view instanceof ImageView &&
                ((ImageView) view).getAdjustViewBounds()) {
            // We can't determine the actual size of a dynamically-sized container, it's a circular
            // dependency.
            throw new IllegalArgumentException("Cannot use responsive Url with AdjustViewBounds");
        }
    }

    private boolean conditionsAreMet(int width, int height) {
        boolean widthOk = !autoWidth || width > 0;
        boolean heightOk = !autoHeight || height > 0;

        return widthOk && heightOk;
    }

    /**
     * Construct the final url with the dimensions included as the last transformation in the url.

     * @param view      The view to adapt the image size to.
     * @param baseUrl   The base cloudinary Url to chain the transformation to.
     * @return The url with the responsive transformation.
     */
    private Url buildUrl(View view, Url baseUrl) {
        int contentWidth = view.getWidth() - view.getPaddingLeft() - view.getPaddingRight();
        int contentHeight = view.getHeight() - view.getPaddingTop() - view.getPaddingBottom();

        return buildUrl(baseUrl, contentWidth, contentHeight);
    }

    /**
     * Construct the final url with the dimensions included as the last transformation in the url.
     *
     * @param baseUrl   The base cloudinary Url to chain the transformation to.
     * @param width     The width to adapt the image width to.
     * @param height    The height to adapt the image height to.
     * @return The url with the responsive transformation.
     */
    public Url buildUrl(Url baseUrl, int width, int height) {
        // add a new transformation on top of anything already there:
        Url url = baseUrl.clone();
        url.transformation().chain();

        if (autoHeight) {
            url.transformation().height(trimAndRoundUp(height));
        }

        if (autoWidth) {
            url.transformation().width(trimAndRoundUp(width));
        }

        url.transformation().crop(cropMode).gravity(gravity);

        return url;
    }

    /**
     * Returns the smallest round number (in terms of step size) that is bigger than the requested dimension,
     * trimmed to max bounds.
     *
     * @param dimension The Requested size
     * @return The rounded value
     */
    private int trimAndRoundUp(int dimension) {
        int value = ((dimension - 1) / stepSize + 1) * stepSize;
        return Math.max(minDimension, Math.min(value, maxDimension));
    }

    /**
     * Ready made presets for common responsive use cases
     */
    public enum Preset {
        /**
         * Adjusts both height and width of the image, retaining the aspect-ratio, to fill the
         * ImageView, using automatic gravity to determine which part of the image is visible.
         * Some cropping may occur. Similar to {@link ImageView.ScaleType#CENTER_CROP}
         */
        AUTO_FILL(true, true, "fill", "auto"),

        /**
         * Adjusts both height and width of the image, retaining the aspect-ratio, to completely fit
         * within the bounds of the ImageView. The whole image will be shown, however some blank
         * space may be visible (letterbox). Similar {@link ImageView.ScaleType#CENTER_INSIDE}
         */
        FIT(true, true, "fit", "center");

        private final boolean autoWidth;
        private final boolean autoHeight;
        private final String cropMode;
        private final String gravity;

        Preset(boolean autoWidth, boolean autoHeight, String cropMode, String gravity) {
            this.autoWidth = autoWidth;
            this.autoHeight = autoHeight;
            this.cropMode = cropMode;
            this.gravity = gravity;
        }

        /**
         * Build an instance of {@link ResponsiveUrl} pre-configured according to the preset.
         *
         * @param cloudinary Cloudinary instance to use.
         * @return The {@link ResponsiveUrl} instance
         */
        public ResponsiveUrl get(Cloudinary cloudinary) {
            return new ResponsiveUrl(cloudinary, autoWidth, autoHeight, cropMode, gravity);
        }
    }

    /**
     * Callback to send to {@link ResponsiveUrl#generate(Url, View, Callback)}
     */
    public interface Callback {
        /**
         * This will be called with the finished url, containing the scaling/cropping transformation
         * based on the actual view dimensions.
         *
         * @param url The finished url. Call {@link Url#generate()} to get the url string
         */
        void onUrlReady(Url url);
    }
}
