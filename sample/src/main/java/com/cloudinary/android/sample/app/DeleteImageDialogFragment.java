package com.cloudinary.android.sample.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class DeleteImageDialogFragment extends DialogFragment {

    public static DeleteImageDialogFragment newInstance(String imageId) {
        DeleteImageDialogFragment fragment = new DeleteImageDialogFragment();
        Bundle args = new Bundle();
        args.putString("id", imageId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity()).setTitle("Delete resource")
                .setMessage("Are you sure you want to delete this resource?")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (getActivity() instanceof ImageDeleteRequested) {
                            ((ImageDeleteRequested) getActivity()).deleteResource(getArguments().getString("id"));
                        }
                    }
                }).create();
    }

    public interface ImageDeleteRequested {
        void deleteResource(String id);
    }
}
