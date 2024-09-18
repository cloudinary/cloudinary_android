package com.cloudinary.sample.main.delivery.optimization;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.cloudinary.Transformation;
import com.cloudinary.android.MediaManager;
import com.cloudinary.sample.R;
import com.cloudinary.sample.databinding.OptimizationFragmentBinding;

import java.io.ByteArrayOutputStream;

public class OptimizationFragment extends Fragment {

    OptimizationFragmentBinding binding;

    OptimizationType type = OptimizationType.Optimization;

    String publicId = "Demo%20app%20content/optimization_original";

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = OptimizationFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getImages();
    }

    private void getImages() {
        setOriginalImageView();
        setOptimizedImageView();
    }

    private void setOptimizedImageView() {
        ImageView optimizedImageView = binding.optimizationOptimizedImageview;

        String url = null;
        if (type == OptimizationType.Optimization) {
//            url = MediaManager.get().url().transformation(new Transformation().crop("scale").width(800).fetchFormat("avif").quality("auto").dpr("auto")).generate(publicId);
            url = "https://res.cloudinary.com/mobiledemoapp/image/upload/c_scale,dpr_auto,f_avif,q_auto,w_800/v1/Demo%20app%20content/optimization_original?_a=DAFAMiAiAiA0";
        }
        if(type == OptimizationType.FetchUplaod) {
            url = MediaManager.get().url().type("fetch").transformation(new Transformation().crop("scale").width(800).fetchFormat("avif").quality("auto").dpr("auto")).generate(publicId);
        }

        Glide.with(optimizedImageView)
                .asBitmap()
                .load(url)
                .placeholder(R.drawable.placeholder)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap bitmap, Transition<? super Bitmap> transition) {
                        int width = bitmap.getWidth();
                        int height = bitmap.getHeight();

                        // Set dimensions text
                        binding.optimizationOptimizedDimensionsTextview.setText(width + "x" + height);

                        // Set format text (assuming AVIF format)
                        binding.optimizationOptimizedFormatTextview.setText("AVIF");

                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream); // Adjust compression quality as needed

                        byte[] byteArray = outputStream.toByteArray();

// Calculate size in KB
                        long fileSizeInBytes = byteArray.length;
                        long fileSizeInKB = fileSizeInBytes / 1024;

// Set size text
                        binding.optimizationOptimizedSizeTextview.setText(fileSizeInKB + " KB");
                        optimizedImageView.setImageBitmap(bitmap);
                    }
                });
    }

    private void setOriginalImageView() {
        ImageView originalImageView = binding.optimizationOriginalImageview;

        String url = null;
        if (type == OptimizationType.Optimization) {
//            url = MediaManager.get().url().transformation(new Transformation().crop("scale").width(1000).fetchFormat("avif").quality("auto").dpr("auto")).generate(publicId);
            url = "https://res.cloudinary.com/mobiledemoapp/image/upload/c_scale,dpr_auto,f_avif,q_auto,w_1000/v1/Demo%20app%20content/optimization_original?_a=DAFAMiAiAiA0";
        }
        if(type == OptimizationType.FetchUplaod) {
            url = MediaManager.get().url().type("fetch").transformation(new Transformation().crop("scale").width(1000).fetchFormat("avif").quality("auto").dpr("auto")).generate(publicId);
        }

        Glide.with(originalImageView)
                .asBitmap()
                .load(url)
                .placeholder(R.drawable.placeholder)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap bitmap, Transition<? super Bitmap> transition) {
                        int width = bitmap.getWidth();
                        int height = bitmap.getHeight();

                        // Set dimensions text
                        binding.optimizationOriginalDimensionsTextview.setText(width + "x" + height);

                        // Set format text (assuming AVIF format)
                        binding.optimizationOriginalFormatTextview.setText("JPG");

                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream); // Adjust compression quality as needed

                        byte[] byteArray = outputStream.toByteArray();

// Calculate size in KB
                        long fileSizeInBytes = byteArray.length;
                        long fileSizeInKB = fileSizeInBytes / 1024;

// Set size text
                        binding.optimizationOriginalSizeTextview.setText(fileSizeInKB + " KB");
                        originalImageView.setImageBitmap(bitmap);
                    }
                });
    }

    private String getImageFormat(String imageUrl) {
        // Extract format from image URL
        int index = imageUrl.lastIndexOf('.');
        if (index > 0) {
            return imageUrl.substring(index + 1).toUpperCase();
        }
        return "";
    }

    public Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public void setType(OptimizationType optimizationType) {
        this.type = optimizationType;
    }
}
