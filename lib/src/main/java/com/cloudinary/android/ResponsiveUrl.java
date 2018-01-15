package com.cloudinary.android;

import android.os.Build;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.cloudinary.Cloudinary;
import com.cloudinary.Url;
import com.cloudinary.utils.StringUtils;

import java.util.EnumSet;

/**
 * This class is used to generate view-size aware cloudinary Urls. It takes any {@link View} and a {@link Url}
 * as input and returns a modifed {@link Url} with the correct width/height transformation inside.
 * Crop mode and gravity are configurable as well.
 * Important: When using this class, it's preferable to not include any cropping/scaling transformation in the base
 * {@link Url} used.
 */
public class ResponsiveUrl {
    // single dimension defaults - /w_***,g_center,c_scale/
    private static final String DEFAULT_SINGLE_DIMENSION_CROP_MODE = "scale";
    private static final String DEFAULT_SINGLE_DIMENSION_GRAVITY = "center";

    // defaults when using both dimensions - /w_***,h_***,g_auto,c_fill/
    private static final String DEFAULT_CROP_MODE = "fill";
    private static final String DEFAULT_GRAVITY = "auto";

    private static final int DEFAULT_MIN_DIMENSION = 100;
    private static final int DEFAULT_MAX_DIMENSION = 3000;

    private static final EnumSet<Dimension> DEFAULT_DIMENSIONS = EnumSet.of(Dimension.width, Dimension.height);
    private static final int DEFAULT_STEP_SIZE = 100;

    // holds url mapped to class instance hashes to make sure we're synched
    private static final SparseArray<Url> viewsInProgress = new SparseArray<>();
    private final Cloudinary cloudinary;

    // fields
    private int stepSize = DEFAULT_STEP_SIZE;
    private EnumSet<Dimension> dimensions = DEFAULT_DIMENSIONS;
    private int maxDimension = DEFAULT_MAX_DIMENSION;
    private int minDimension = DEFAULT_MIN_DIMENSION;

    // These two fields are not assigned default values, because their defaults depend on dimensions
    // that may be set later - they're assigned default values when generating the result.
    private String cropMode;
    private String gravity;

    /**
     * Create a new responsive url generator instance.
     *
     * @param cloudinary The cloudinary instance used.
     * @param dimension  The dimensions choice to include in the transformation
     * @param cropMode   The crop mode to use in the transformation (crop, scale, fit, etc.).
     * @param gravity    The gravity to use in the transformation (north, west, auto, thumb, etc.).
     */
    ResponsiveUrl(Cloudinary cloudinary, Dimension dimension, String cropMode, String gravity) {
        this.cloudinary = cloudinary;
        this.gravity(gravity).cropMode(cropMode).dimension(dimension);
    }

    private ResponsiveUrl cropMode(String cropMode) {
        this.cropMode = cropMode;
        return this;
    }

    private ResponsiveUrl dimension(Dimension dimension) {
        this.dimensions = buildSet(dimension);
        return this;
    }

    private ResponsiveUrl gravity(String gravity) {
        this.gravity = gravity;
        return this;
    }

    public ResponsiveUrl stepSize(int stepSize) {
        this.stepSize = stepSize;
        return this;
    }

    /**
     * Limit the maximum allowed dimension. If the actual view dimensions are larger than
     * the value chosen here, this max value will be used instead. This is useful to limit the
     * total amount of transformations generated.
     *
     * @param maxDimension The highest allowed dimension.
     * @return itself for chaining.
     */
    public ResponsiveUrl maxDimension(int maxDimension) {
        this.maxDimension = maxDimension;
        return this;
    }

    /**
     * Limit the minimum allowed dimension. If the actual view dimensions are smaller than
     * the value chosen here, this min value will be used instead. This is useful to limit the
     * total amount of transformations generated.
     *
     * @param minDimension The smallest allowed dimension.
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
     * @param baseUrl  A base url to modify with the view's dimensions. This url can contain
     *                 any configurations and transformations - The scaling/cropping transformation
     *                 is chained to the last part.
     * @param view     The view to adapt the resource dimensions to.
     * @param callback Callback to called when the modified Url is ready.
     */
    public void generate(final Url baseUrl, final View view, final Callback callback) {
        assertViewValidForResponsive(view);
        verifyAllParams();
        final int key = view.hashCode();

        final int width = getWidth(view);
        final int height = getHeight(view);

        if (conditionsAreMet(dimensions, width, height)) {
            // The required dimension are already known, build url:
            buildUrl(view, baseUrl, dimensions, width, height, callback);
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
                        buildUrl(view, baseUrl, dimensions, view.getWidth(), view.getHeight(), callback);
                        viewsInProgress.remove(key);
                    }

                    return true;
                }
            });
        }
    }

    private void verifyAllParams() {
        if (dimensions == null || dimensions.isEmpty()) {
            dimensions = DEFAULT_DIMENSIONS;
        }

        if (StringUtils.isBlank(cropMode)) {
            cropMode = dimensions.size() == 1 ? DEFAULT_SINGLE_DIMENSION_CROP_MODE : DEFAULT_CROP_MODE;
        }

        if (StringUtils.isBlank(gravity)) {
            gravity = dimensions.size() == 1 ? DEFAULT_SINGLE_DIMENSION_GRAVITY : DEFAULT_GRAVITY;
        }
    }

    private EnumSet<Dimension> buildSet(Dimension dimension) {
        if (dimension == Dimension.all) {
            return EnumSet.allOf(Dimension.class);
        }

        return EnumSet.of(dimension);
    }

    private void assertViewValidForResponsive(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN &&
                view instanceof ImageView &&
                ((ImageView) view).getAdjustViewBounds()) {
            // We can't get the size of a dynamically-sized container
            throw new IllegalArgumentException("Cannot use responsive images with AdjustViewBounds");
        }
    }

    private boolean conditionsAreMet(EnumSet<Dimension> dimensions, int width, int height) {
        boolean widthOk = !dimensions.contains(Dimension.width) || width > 0;
        boolean heightOk = !dimensions.contains(Dimension.height) || height > 0;

        return widthOk && heightOk;
    }

    private int getHeight(View view) {
        if (view.getHeight() > 0) {
            return view.getHeight();
        }

        if (view.getLayoutParams() != null) {
            return view.getLayoutParams().height;
        }

        return 0;
    }

    private int getWidth(View view) {
        if (view.getWidth() > 0) {
            return view.getWidth();
        }

        if (view.getLayoutParams() != null) {
            return view.getLayoutParams().width;
        }

        return 0;
    }

    private void buildUrl(View view, Url baseUrl, EnumSet<Dimension> dimensions, int width, int height, Callback callback) {
        // add a new transformation on top of anything already there
        Url url = baseUrl.clone();

        url.transformation().chain();

        if (dimensions.contains(Dimension.height)) {
            int contentHeight = height - view.getPaddingTop() - view.getPaddingBottom();
            url.transformation().height(trimAndRoundUp(contentHeight, stepSize));
        }

        if (dimensions.contains(Dimension.width)) {
            int contentWidth = width - view.getPaddingLeft() - view.getPaddingRight();
            url.transformation().width(trimAndRoundUp(contentWidth, stepSize));
        }

        url.transformation().crop(cropMode).gravity(gravity);

        callback.onUrlReady(url);
    }

    /**
     * Returns the smallest round number (in terms of step size) that is bigger than the requested dimension,
     * trimmed to max bounds.
     *
     * @param dimension The Requested size
     * @param stepSize  The step size to round (up) to.
     * @return The rounded value
     */
    private int trimAndRoundUp(int dimension, int stepSize) {
        int value = ((dimension - 1) / stepSize + 1) * stepSize;
        return Math.max(minDimension, Math.min(value, maxDimension));
    }

    /**
     * Dimensions to be modified in the url.
     */
    public enum Dimension {
        width,
        height,
        all
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
