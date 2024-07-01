package com.cloudinary.sample.main.delivery.transform;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cloudinary.sample.R;
import com.cloudinary.sample.databinding.DeliveryTransformCellBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
public class DeliveryTransformAdapter extends RecyclerView.Adapter<DeliveryTransformAdapter.DeliveryTransformCellViewHolder> {

    private List<String> dataList;

    private DeliveryTransformCellBinding binding;

    OnTransformationItemSelectedListener onTransformationItemSelectedListener;

    public DeliveryTransformAdapter(Context context, OnTransformationItemSelectedListener listener) {
        dataList = new ArrayList<>(Collections.nCopies(4, ""));
        onTransformationItemSelectedListener = listener;
    }

    @NonNull
    @Override
    public DeliveryTransformCellViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.binding = DeliveryTransformCellBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
//        View view = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.custom_cell_layout, parent, false);
        return new DeliveryTransformCellViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DeliveryTransformCellViewHolder holder, int position) {
        holder.setImageView(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemSelected(holder.getBindingAdapterPosition());
            }
        });
    }

    private void itemSelected(int position) {
        onTransformationItemSelectedListener.onTransformationItemSelected(position);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class DeliveryTransformCellViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        public DeliveryTransformCellViewHolder(@NonNull DeliveryTransformCellBinding binding) {
            super(binding.getRoot());
            imageView = binding.deliveryTransformationCellImageview;
        }

        public void setImageView(int position) {
            switch (position) {
                case 0:
                    imageView.setImageResource(R.drawable.smart_cropping);
                    break;
                case 1:
                    imageView.setImageResource(R.drawable.localization_branding);
                    break;
                case 2:
                    imageView.setImageResource(R.drawable.background_normalizing);
                    break;
                case 3:
                    imageView.setImageResource(R.drawable.color_alternation);
                    break;
            }
        }
    }
}