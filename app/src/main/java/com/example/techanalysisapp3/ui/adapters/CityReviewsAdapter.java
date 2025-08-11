package com.example.techanalysisapp3.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.techanalysisapp3.model.Review;

import java.util.List;

public class CityReviewsAdapter
        extends RecyclerView.Adapter<CityReviewsAdapter.VH> {

    public interface OnClick { void onClick(Review r); }

    private final List<Review> items;
    private final OnClick listener;

    public CityReviewsAdapter(List<Review> items, OnClick listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Review r = items.get(position);
        holder.tv.setText(r.getOlxTitle());
        holder.itemView.setOnClickListener(x -> listener.onClick(r));
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tv;
        VH(View v) {
            super(v);
            tv = v.findViewById(android.R.id.text1);
        }
    }
}
