package com.cloudinary.sample.main.upload.single_upload;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class UploadCellItemDecoration extends RecyclerView.ItemDecoration {
    private final int margin;

    public UploadCellItemDecoration(Context context, int marginResId) {
        this.margin = context.getResources().getDimensionPixelSize(marginResId);
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        int itemCount = state.getItemCount();

        // Apply margin to bottom of each item
        if (position < itemCount - 1) {
            outRect.bottom = margin;
        }

        // Apply margin to the right of each item except the last item in each row
        if ((position + 1) % 3 != 0) {
            outRect.right = margin;
        }
    }
}
