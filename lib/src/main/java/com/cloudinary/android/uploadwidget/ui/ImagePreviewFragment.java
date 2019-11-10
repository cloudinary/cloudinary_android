package com.cloudinary.android.uploadwidget.ui;


import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cloudinary.android.R;

/**
 * TODO: Document this
 */
public class ImagePreviewFragment extends Fragment {

    private static final String IMAGE_URI_ARG = "image_uri_arg";
    private Uri imageUri;
    private ImageView imageView;

    public ImagePreviewFragment() { }

    /**
     * Instantiates a new {@link ImagePreviewFragment} with an image uri argument.
     * @param imageUri Uri of the image to be displayed
     */
    public static ImagePreviewFragment newInstance(Uri imageUri) {
        if (imageUri == null) {
            throw new IllegalArgumentException("Image uri must be provided");
        }

        ImagePreviewFragment fragment = new ImagePreviewFragment();
        Bundle args = new Bundle();
        args.putString(IMAGE_URI_ARG, imageUri.toString());
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            imageUri = Uri.parse(arguments.getString(IMAGE_URI_ARG));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_preview, container, false);
        imageView = view.findViewById(R.id.imageUriImageView);
        imageView.setImageURI(imageUri);

        return view;
    }
}
