package com.example.techanalysisapp3.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.techanalysisapp3.R;
import com.example.techanalysisapp3.model.HistoryItem;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<HistoryItem> historyItems;

    public HistoryAdapter(List<HistoryItem> historyItems) {
        this.historyItems = historyItems;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryItem item = historyItems.get(position);
        holder.dateText.setText(item.getDate());
        holder.imageView.setImageResource(item.getImageResId());
    }

    @Override
    public int getItemCount() {
        return historyItems.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView dateText;
        ImageView imageView;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.tvHistoryDate);
            imageView = itemView.findViewById(R.id.ivHistoryImage);
        }
    }
}
