package com.example.techanalysisapp3.ui.adapters;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.techanalysisapp3.R;
import com.example.techanalysisapp3.model.CardData;

import java.util.List;

public class AnalysisPagerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<CardData> cardList;
    private final int imageCount;
    private final ViewPager2 viewPager;

    private static final int TYPE_IMAGE = 1, TYPE_TEXT = 0;

    public AnalysisPagerAdapter(List<CardData> cardList, int imageCount, ViewPager2 viewPager) {
        this.cardList = cardList;
        this.imageCount = imageCount;
        this.viewPager = viewPager;
    }

    @Override public int getItemViewType(int pos) {
        return cardList.get(pos).isImageCard() ? TYPE_IMAGE : TYPE_TEXT;
    }

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_IMAGE) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_analysis_image_card, parent, false);
            return new ImageViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_analysis_card, parent, false);
            return new TextViewHolder(v);
        }
    }

    @Override public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder holder, int pos) {
        CardData data = cardList.get(pos);

        if (holder instanceof ImageViewHolder) {
            ImageViewHolder ivh = (ImageViewHolder) holder;
            Glide.with(ivh.itemView.getContext())
                    .load(data.getImageUrl())
                    .into(ivh.imageView);

            ivh.itemView.setOnClickListener(v -> {
                if (pos + imageCount < getItemCount())
                    viewPager.setCurrentItem(pos + imageCount, true);
            });
        } else {
            TextViewHolder tvh = (TextViewHolder) holder;
            boolean isLast = pos == getItemCount() - 1;
            tvh.ivArrow.setVisibility(isLast ? View.GONE : View.VISIBLE);

            String title = data.getTitle(), content = data.getContent();
            if ("Ocjena".equalsIgnoreCase(title)) {
                String[] parts = content.split(",", 2);
                tvh.title.setText(parts[0].trim());
                tvh.title.setTextSize(32);
                tvh.title.setTypeface(null, Typeface.BOLD);
                tvh.content.setText(parts.length>1?parts[1].trim():"");
                tvh.tvRating.setVisibility(View.VISIBLE);
                tvh.tvRatingExplanation.setVisibility(View.VISIBLE);
            } else {
                tvh.title.setText(title);
                tvh.title.setTextSize(18);
                tvh.title.setTypeface(null, Typeface.NORMAL);
                tvh.content.setText(content);
                tvh.tvRating.setVisibility(View.GONE);
                tvh.tvRatingExplanation.setVisibility(View.GONE);
            }
            // Ako je ova text kartica “vezana” za sliku
            if (pos >= imageCount && pos < imageCount*2) {
                tvh.itemView.setOnClickListener(v -> {
                    viewPager.setCurrentItem(pos - imageCount, true);
                });
            }
        }
    }

    @Override public int getItemCount() { return cardList.size(); }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageViewHolder(@NonNull View iv) {
            super(iv);
            imageView = iv.findViewById(R.id.imageViewFull);
        }
    }
    static class TextViewHolder extends RecyclerView.ViewHolder {
        TextView title, content, tvRating, tvRatingExplanation;
        ImageView ivArrow;
        TextViewHolder(@NonNull View v) {
            super(v);
            title               = v.findViewById(R.id.tvCardTitle);
            content             = v.findViewById(R.id.tvCardContent);
            ivArrow             = v.findViewById(R.id.ivArrow);
            tvRating            = v.findViewById(R.id.tvRating);
            tvRatingExplanation = v.findViewById(R.id.tvRatingExplanation);
        }
    }
}