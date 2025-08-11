package com.example.techanalysisapp3.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.techanalysisapp3.R;

import java.util.ArrayList;
import java.util.List;

public class ComparisonSwipeAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_LEFT_IMAGE  = 0;
    private static final int TYPE_RIGHT_IMAGE = 1;
    private static final int TYPE_TEXT        = 2;

    private final ArrayList<String> leftImgs;
    private final ArrayList<String> rightImgs;
    private final List<String> sections;

    public ComparisonSwipeAdapter(ArrayList<String> l, ArrayList<String> r, List<String> sections) {
        this.leftImgs = l;
        this.rightImgs = r;
        this.sections = sections;
    }

    @Override public int getItemCount() {
        return 2 + sections.size();
    }

    @Override public int getItemViewType(int pos) {
        if (pos == 0) return TYPE_LEFT_IMAGE;
        if (pos == 1) return TYPE_RIGHT_IMAGE;
        return TYPE_TEXT;
    }

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_LEFT_IMAGE || viewType == TYPE_RIGHT_IMAGE) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_analysis_image_card, parent, false);
            return new ImageVH(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_analysis_card, parent, false);
            return new TextVH(v);
        }
    }

    @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int pos) {
        if (holder instanceof ImageVH) {
            String url = (pos == 0) ? leftImgs.get(0) : rightImgs.get(0);
            Glide.with(holder.itemView).load(url).into(((ImageVH) holder).imageViewFull);
        } else {
            TextVH tvh = (TextVH) holder;
            int sectionIndex = pos - 2;
            String section = sections.get(sectionIndex);

            int colonIndex = section.indexOf(':');
            if (colonIndex != -1) {
                String title = section.substring(0, colonIndex + 1).trim();
                String content = section.substring(colonIndex + 1).trim();
                tvh.tvCardTitle.setText(title);
                tvh.tvCardContent.setText(content);
            } else {
                tvh.tvCardTitle.setText("⚔️ Rezime poređenja");
                tvh.tvCardContent.setText(section);
            }
            tvh.ivArrow.setVisibility(View.GONE);
        }
    }

    static class ImageVH extends RecyclerView.ViewHolder {
        ImageView imageViewFull;
        public ImageVH(@NonNull View v) {
            super(v);
            imageViewFull = v.findViewById(R.id.imageViewFull);
        }
    }

    static class TextVH extends RecyclerView.ViewHolder {
        TextView tvCardTitle, tvCardContent;
        ImageView ivArrow;
        public TextVH(@NonNull View v) {
            super(v);
            tvCardTitle   = v.findViewById(R.id.tvCardTitle);
            tvCardContent = v.findViewById(R.id.tvCardContent);
            ivArrow       = v.findViewById(R.id.ivArrow);
        }
    }
}
