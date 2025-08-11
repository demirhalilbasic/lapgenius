package com.example.techanalysisapp3.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.techanalysisapp3.R;
import com.example.techanalysisapp3.model.Review;

import java.util.List;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ViewHolder> {
    private List<Review> reviews;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView previewImage;
        TextView previewText;
        TextView olxLink;

        public ViewHolder(View itemView) {
            super(itemView);
            previewImage = itemView.findViewById(R.id.ivPreview);
            previewText = itemView.findViewById(R.id.tvSnippet);
            olxLink = itemView.findViewById(R.id.btnOpen);
        }
    }

    public ReviewsAdapter(List<Review> reviews) {
        this.reviews = reviews;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Review review = reviews.get(position);
        Glide.with(holder.itemView)
                .load(review.getImageUrls().get(0))
                .into(holder.previewImage);

        holder.previewText.setText(review.getAnalysisText().substring(0, 100) + "...");
        holder.olxLink.setText(review.getOlxUrl());
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }
}