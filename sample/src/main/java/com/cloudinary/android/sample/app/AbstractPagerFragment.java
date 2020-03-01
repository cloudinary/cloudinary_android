package com.cloudinary.android.sample.app;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.cloudinary.android.sample.R;
import com.cloudinary.android.sample.model.Resource;
import com.cloudinary.android.sample.widget.GridDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPagerFragment extends Fragment {

    private RecyclerView recyclerView;
    private View emptyView;
    private int dividerSize;
    private RecyclerView.AdapterDataObserver observer;

    protected abstract ResourcesAdapter getAdapter(int thumbSize);
    protected abstract int getSpan();
    protected abstract List<Resource> getData();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_pager_page, container, false);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.mainGallery);
        recyclerView.setHasFixedSize(true);
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        RecyclerView.LayoutManager layoutManager = getLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        emptyView = rootView.findViewById(R.id.emptyListView);
        dividerSize = getResources().getDimensionPixelSize(R.dimen.grid_divider_width);
        addItemDecoration(recyclerView);
        observer = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                emptyView.setVisibility(recyclerView.getAdapter().getItemCount() == 0 ? View.VISIBLE : View.INVISIBLE);
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                emptyView.setVisibility(recyclerView.getAdapter().getItemCount() == 0 ? View.VISIBLE : View.INVISIBLE);

            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                emptyView.setVisibility(recyclerView.getAdapter().getItemCount() == 0 ? View.VISIBLE : View.INVISIBLE);

            }
        };

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (recyclerView.getWidth() > 0) {
            initThumbSizeAndLoadData();
        } else {
            recyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    recyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                    initThumbSizeAndLoadData();
                    return true;
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        recyclerView.getAdapter().unregisterAdapterDataObserver(observer);
    }

    @NonNull
    protected RecyclerView.LayoutManager getLayoutManager(Context context) {
        return new GridLayoutManager(context, getSpan());
    }

    protected void addItemDecoration(RecyclerView recyclerView) {
        recyclerView.addItemDecoration(new GridDividerItemDecoration(getSpan(), dividerSize));
    }

    private void initThumbSizeAndLoadData() {
        int thumbSize = recyclerView.getWidth() / getSpan() - dividerSize / 2;
        final ResourcesAdapter adapter = getAdapter(thumbSize);

        adapter.registerAdapterDataObserver(observer);
        recyclerView.setAdapter(adapter);

        // fetch data after we know the size so we can request the exact size from Cloudinary
        adapter.replaceImages(getData());
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public void clearData() {
        ((ResourcesAdapter) recyclerView.getAdapter()).replaceImages(new ArrayList<Resource>());
    }
}
