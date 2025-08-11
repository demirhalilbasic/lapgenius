package com.example.techanalysisapp3.ui.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.techanalysisapp3.R;
import com.example.techanalysisapp3.model.HotDeal;
import com.example.techanalysisapp3.model.UserStats;
import com.example.techanalysisapp3.ui.activities.AnalysisSwipeActivity;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HOT_DEAL = 0;
    private static final int TYPE_USER = 1;

    private final List<HotDeal> hotDeals;
    private final List<UserStats> users;
    private final OnUserClickListener userClickListener;

    // Interface for user click events
    public interface OnUserClickListener {
        void onUserClick(UserStats user);
    }

    public LeaderboardAdapter(List<HotDeal> hotDeals, List<UserStats> users, OnUserClickListener listener) {
        this.hotDeals = hotDeals != null ? hotDeals : new ArrayList<>();
        this.users = users != null ? users : new ArrayList<>();
        this.userClickListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if(position < hotDeals.size()) {
            return TYPE_HOT_DEAL;
        } else {
            return TYPE_USER;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == TYPE_HOT_DEAL) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_hot_deal, parent, false);
            return new HotDealViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user_stats, parent, false);
            return new UserViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof HotDealViewHolder && position < hotDeals.size()) {
            ((HotDealViewHolder) holder).bind(hotDeals.get(position));
        } else if(holder instanceof UserViewHolder) {
            int userPosition = position - hotDeals.size();
            if(userPosition < users.size()) {
                ((UserViewHolder) holder).bind(users.get(userPosition));
            }
        }
    }

    @Override
    public int getItemCount() {
        return hotDeals.size() + users.size();
    }

    static class HotDealViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle, tvRating, tvUsername;
        private final ImageView ivPreview;

        HotDealViewHolder(View view) {
            super(view);
            tvTitle = view.findViewById(R.id.tvTitle);
            tvRating = view.findViewById(R.id.tvRating);
            tvUsername = view.findViewById(R.id.tvUsername);
            ivPreview = view.findViewById(R.id.ivPreview);
        }

        void bind(HotDeal deal) {
            tvRating.setCompoundDrawablesWithIntrinsicBounds(
                    ContextCompat.getDrawable(itemView.getContext(), R.drawable.ic_favorites),
                    null, null, null
            );
            tvRating.setCompoundDrawablePadding(8);

            tvUsername.setCompoundDrawablesWithIntrinsicBounds(
                    ContextCompat.getDrawable(itemView.getContext(), R.drawable.ic_person),
                    null, null, null
            );
            tvUsername.setCompoundDrawablePadding(8);

            tvTitle.setText(deal.getOlxTitle());
            tvRating.setText(String.format("Ocjena: %.1f", deal.getRating()));
            tvUsername.setText(deal.getUsername() != null ? deal.getUsername() : "Anoniman korisnik");

            if(!deal.getImageUrls().isEmpty()) {
                Glide.with(itemView)
                        .load(deal.getImageUrls().get(0))
                        .override(300, 300)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .into(ivPreview);
            }

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), AnalysisSwipeActivity.class);
                intent.putExtra("ai_analysis", deal.getAnalysisText());
                intent.putStringArrayListExtra("image_list", new ArrayList<>(deal.getImageUrls()));
                intent.putExtra("olx_url", deal.getOlxUrl());
                intent.putExtra("olx_title", deal.getOlxTitle());
                intent.putExtra("city_name", deal.getCityName());
                intent.putExtra("latitude", deal.getLatitude());
                intent.putExtra("longitude", deal.getLongitude());
                itemView.getContext().startActivity(intent);
            });
        }
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName, tvCount;

        UserViewHolder(View view) {
            super(view);
            tvName = view.findViewById(R.id.tvUserName);
            tvCount = view.findViewById(R.id.tvAnalysisCount);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    int userIndex = position - hotDeals.size();
                    if (userIndex >= 0 && userIndex < users.size()) {
                        userClickListener.onUserClick(users.get(userIndex));
                    }
                }
            });
        }

        void bind(UserStats user) {
            tvName.setText(user.getUsername());

            int analysisCount = user.getTotalAnalysisCount() > 0
                    ? user.getTotalAnalysisCount()
                    : user.getWeeklyAnalysisCount();

            String countText = itemView.getContext().getString(
                    R.string.analyses_count, analysisCount);
            tvCount.setText(countText);
        }
    }

    public void updateHotDeals(List<HotDeal> newDeals) {
        this.hotDeals.clear();
        this.hotDeals.addAll(newDeals);
        notifyDataSetChanged();
    }

    public void updateUsers(List<UserStats> newUsers) {
        this.users.clear();
        this.users.addAll(newUsers);
        notifyDataSetChanged();
    }
}