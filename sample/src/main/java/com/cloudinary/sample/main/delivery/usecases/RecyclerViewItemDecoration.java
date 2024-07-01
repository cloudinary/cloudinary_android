package com.cloudinary.sample.main.delivery.usecases;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewItemDecoration extends RecyclerView.ItemDecoration {
    private final int spacing;

    public RecyclerViewItemDecoration(Context context, int spacing) {
        this.spacing = spacing;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        // Apply spacing to the bottom of all items except the last one
        int itemPosition = parent.getChildAdapterPosition(view);
        if (itemPosition != parent.getAdapter().getItemCount() - 1) {
            outRect.right = spacing;
        }
    }
}
