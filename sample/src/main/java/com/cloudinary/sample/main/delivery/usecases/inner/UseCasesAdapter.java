package com.cloudinary.sample.main.delivery.usecases.inner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cloudinary.sample.R;
import com.cloudinary.sample.databinding.UsecasesCellBinding;
import com.cloudinary.sample.main.delivery.usecases.OnUseCaseItemSelectedListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UseCasesAdapter extends RecyclerView.Adapter<UseCasesAdapter.UseCasesCellViewHolder> {

    private List<String> dataList;
    private int selectedItemPosition = 0;

    private final OnUseCaseItemSelectedListener onUseCasesItemSelectedListener;

    public UseCasesAdapter(Context context, OnUseCaseItemSelectedListener listener) {
        dataList = new ArrayList<>(Collections.nCopies(4, ""));
        onUseCasesItemSelectedListener = listener;
    }

    @NonNull
    @Override
    public UseCasesAdapter.UseCasesCellViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        UsecasesCellBinding binding = UsecasesCellBinding.inflate(inflater, parent, false);
        return new UseCasesAdapter.UseCasesCellViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UseCasesAdapter.UseCasesCellViewHolder holder, int position) {
        holder.setTextView(position);

        holder.itemView.setSelected(position == selectedItemPosition);
        holder.borderView.setVisibility(position == selectedItemPosition ? View.VISIBLE : View.INVISIBLE);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemSelected(holder.getBindingAdapterPosition());
            }
        });
    }

    private void itemSelected(int position) {
        if (selectedItemPosition != position) {
            notifyItemChanged(selectedItemPosition);
            selectedItemPosition = position;
            notifyItemChanged(selectedItemPosition);
        }
        onUseCasesItemSelectedListener.onUseCaseItemSelected(position);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class UseCasesCellViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView textView;
        View borderView;

        public UseCasesCellViewHolder(@NonNull UsecasesCellBinding binding) {
            super(binding.getRoot());
            textView = binding.transformationCellTextview;
            borderView = binding.getRoot().findViewById(R.id.border_view);
        }

        public void setTextView(int position) {
            switch (position) {
                case 0:
                    textView.setText(R.string.normalize_asset_sizing);
                    break;
                case 1:
                    textView.setText(R.string.conditional_product_badging);
                    break;
                case 2:
                    textView.setText(R.string.adapt_to_screen_size);
                    break;
                case 3:
                    textView.setText(R.string.ai_generated_backgrounds);
                    break;
            }
        }
    }

    public void setSelectedItemPosition(int position) {
        selectedItemPosition = position;
    }

}
