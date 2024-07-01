package com.cloudinary.sample.main.delivery.usecases;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cloudinary.sample.R;
import com.cloudinary.sample.databinding.DeliveryUsecaseCellBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeliveryUsecasesAdapter extends RecyclerView.Adapter<DeliveryUsecasesAdapter.DeliveryUseCasesCellViewHolder> {

        private List<String> dataList;

        private DeliveryUsecaseCellBinding binding;

    OnUseCaseItemSelectedListener onTransformationItemSelectedListener;

        public DeliveryUsecasesAdapter(Context context, OnUseCaseItemSelectedListener listener) {
            dataList = new ArrayList<>(Collections.nCopies(4, ""));
            onTransformationItemSelectedListener = listener;
        }

        @NonNull
        @Override
        public DeliveryUseCasesCellViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            this.binding = DeliveryUsecaseCellBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new DeliveryUseCasesCellViewHolder(binding);
        }

    @Override
    public void onBindViewHolder(@NonNull DeliveryUseCasesCellViewHolder holder, int position) {
        holder.setTextView(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemSelected(holder.getBindingAdapterPosition());
            }
        });
    }

    private void itemSelected(int position) {
        onTransformationItemSelectedListener.onUseCaseItemSelected(position);
    }

        @Override
        public int getItemCount() {
            return dataList.size();
        }

        public static class DeliveryUseCasesCellViewHolder extends RecyclerView.ViewHolder {

            TextView textView;
            public DeliveryUseCasesCellViewHolder(@NonNull DeliveryUsecaseCellBinding binding) {
                super(binding.getRoot());
                textView = binding.deliveryUsecasesCellTextview;
            }

            public void setTextView(int position) {
                switch (position) {
                    case 0:
                        textView.setText(R.string.auto_cropping);
                        break;
                    case 1:
                        textView.setText(R.string.overlay);
                        break;
                    case 2:
                        textView.setText(R.string.background_normalization);
                        break;
                    case 3:
                        textView.setText(R.string.generative_recolor);
                        break;
                }
            }
        }
    }

