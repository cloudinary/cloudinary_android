package com.cloudinary.android.sample.app;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.cloudinary.android.sample.R;
import com.cloudinary.android.sample.model.Resource;

public class DeleteImageDialogFragment extends AppCompatDialogFragment {

    public static DeleteImageDialogFragment newInstance(Resource resource, boolean recent) {
        DeleteImageDialogFragment fragment = new DeleteImageDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("resource", resource);
        args.putBoolean("recent", recent);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Resource resource = (Resource) getArguments().getSerializable("resource");
        final boolean recent = getArguments().getBoolean("recent");
        final String message = recent ? getString(R.string.delete_resource_everywhere_message, resource.getResourceType()) :
                getString(R.string.delete_resource_locally_message, resource.getResourceType());

        return new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.delete_single_resource_title, resource.getResourceType()))
                .setMessage(message)
                .setNegativeButton(R.string.delete_single_resource_no, null)
                .setPositiveButton(R.string.delete_single_resource_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (recent) {
                            ((DeleteRequestsCallback) getActivity()).onDeleteResourceEverywhere(resource);
                        } else {
                            ((DeleteRequestsCallback) getActivity()).onDeleteResourceLocally(resource);
                        }
                    }
                }).create();
    }
}
