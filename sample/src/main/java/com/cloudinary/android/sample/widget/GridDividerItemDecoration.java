package com.cloudinary.android.sample.widget;

import android.graphics.Rect;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

public class GridDividerItemDecoration extends RecyclerView.ItemDecoration {
    private final int span;
    private final int dividerWidth;

    public GridDividerItemDecoration(int span, int dividerWidth) {
        this.span = span;
        this.dividerWidth = dividerWidth;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int itemPosition = parent.getChildLayoutPosition(view);
        int col = itemPosition % span;
        outRect.top = itemPosition < span ? 0 : dividerWidth;
        outRect.bottom = 0;
        outRect.right = col < span - 1 ? dividerWidth / 2 : 0;
        outRect.left = col > 0 ? dividerWidth / 2 : 0;
    }
}
