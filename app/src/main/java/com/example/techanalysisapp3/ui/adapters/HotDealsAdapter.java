package com.example.techanalysisapp3.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.techanalysisapp3.R;
import com.example.techanalysisapp3.model.HotDeal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class HotDealsAdapter extends RecyclerView.Adapter<HotDealsAdapter.ViewHolder> {
    private List<HotDeal> hotDeals = new ArrayList<>();
    private OnItemClickListener listener;
    private List<HotDeal> originalDeals;
    private List<Integer> recentPositions = new ArrayList<>();
    private static final int MIN_DISTANCE = 3;

    public interface OnItemClickListener {
        void onItemClick(HotDeal deal);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setHotDeals(List<HotDeal> hotDeals) {
        this.originalDeals = new ArrayList<>(hotDeals);
        this.hotDeals = generateShuffledListWithDistance();
        notifyDataSetChanged();
    }

    private List<HotDeal> generateShuffledListWithDistance() {
        if (originalDeals.size() < 5) {
            return new ArrayList<>(originalDeals); // No distance rule for small lists
        }

        List<HotDeal> result = new ArrayList<>();
        List<HotDeal> temp = new ArrayList<>(originalDeals);
        Collections.shuffle(temp);

        while (!temp.isEmpty()) {
            HotDeal candidate = temp.remove(0);

            // Find position that satisfies distance rule
            int position = findSuitablePosition(result, candidate);
            result.add(position, candidate);
        }
        return result;
    }

    private int findSuitablePosition(List<HotDeal> currentList, HotDeal candidate) {
        if (currentList.isEmpty()) return 0;

        int candidateIndex = originalDeals.indexOf(candidate);
        if (isPositionValid(currentList.size(), candidateIndex)) {
            return currentList.size();
        }

        for (int i = currentList.size() - 1; i >= 0; i--) {
            if (isPositionValid(i, candidateIndex)) {
                return i + 1;
            }
        }

        return currentList.size();
    }

    private boolean isPositionValid(int position, int candidateIndex) {
        int start = Math.max(0, position - MIN_DISTANCE);
        for (int i = start; i < position; i++) {
            if (i < hotDeals.size()) {
                int existingIndex = originalDeals.indexOf(hotDeals.get(i));
                if (existingIndex == candidateIndex) {
                    return false;
                }
            }
        }
        return true;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hot_deal_small, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int actualPosition = position % getRealItemCount();
        HotDeal deal = hotDeals.get(actualPosition);
        holder.bind(deal);

        Context context = holder.itemView.getContext().getApplicationContext();

        if (!deal.getImageUrls().isEmpty()) {
            Glide.with(context)
                    .load(deal.getImageUrls().get(0))
                    .placeholder(R.drawable.ic_laptop_placeholder)
                    .error(R.drawable.ic_laptop_placeholder)
                    .into(holder.ivImage);
        }
    }

    @Override
    public int getItemCount() {
        return getRealItemCount() > 0 ? Integer.MAX_VALUE : 0;
    }

    public int getRealItemCount() {
        return hotDeals != null ? hotDeals.size() : 0;
    }

    public List<HotDeal> getHotDeals() {
        return hotDeals;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivImage;
        private final TextView tvTitle;
        private final TextView tvRating;

        ViewHolder(View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivImage);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvRating = itemView.findViewById(R.id.tvRating);
        }

        void bind(final HotDeal deal) {
            if (deal.getImageUrls() != null && !deal.getImageUrls().isEmpty()) {
                Glide.with(itemView)
                        .load(deal.getImageUrls().get(0))
                        .centerCrop()
                        .into(ivImage);
            }

            tvTitle.setText(deal.getOlxTitle());
            tvRating.setText(String.format(Locale.getDefault(), "%.1f★", deal.getRating()));

            // ** MODIFICATION FOR CLICK SECURITY **
            // Pull position with getAdapterPosition() and calculate real item again
            // to ensure that listener is getting right data, even during view recycle process
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int currentPosition = getAdapterPosition();
                    if (currentPosition != RecyclerView.NO_POSITION) {
                        int actualPosition = currentPosition % getRealItemCount();
                        listener.onItemClick(hotDeals.get(actualPosition));
                    }
                }
            });
        }
    }
}