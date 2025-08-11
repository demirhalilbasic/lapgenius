package com.example.techanalysisapp3.ui.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.techanalysisapp3.R;
import com.example.techanalysisapp3.model.Review;
import com.example.techanalysisapp3.ui.activities.AnalysisSwipeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ViewHolder> {
    private final List<Review> reviews;

    public FavoritesAdapter(List<Review> reviews) {
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Review review = reviews.get(position);

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        String dateString = sdf.format(review.getTimestamp());
        holder.tvDate.setText("Sačuvano: " + dateString);

        String locationText = "🌍 Nepoznata lokacija";
        if (review.getCityName() != null && !review.getCityName().isEmpty()) {
            locationText = "📍 " + review.getCityName();
        }
        else if (review.getLatitude() != null && review.getLongitude() != null) {
            try {
                double lat = Double.parseDouble(review.getLatitude());
                double lng = Double.parseDouble(review.getLongitude());
                locationText = String.format(Locale.getDefault(),
                        "📍 %.4f, %.4f", lat, lng);
            } catch (NumberFormatException e) {
                Log.e("LocationError", "Neispravne koordinate", e);
            }
        }
        holder.tvLocation.setText(locationText);

        if(review.getImageUrls() != null && !review.getImageUrls().isEmpty()) {
            Glide.with(holder.itemView)
                    .load(review.getImageUrls().get(0))
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Dodajte ovu liniju
                    .into(holder.ivPreview);
        }

        holder.tvTitle.setText(review.getOlxTitle() != null ?
                review.getOlxTitle() : "Nepoznat artikal");

        String analysisText = review.getAnalysisText() != null ?
                review.getAnalysisText() : "";
        holder.tvSnippet.setText(analysisText.length() > 100 ?
                analysisText.substring(0, 100) + "..." : analysisText);

        holder.btnOpen.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), AnalysisSwipeActivity.class);
            intent.putExtra("ai_analysis", review.getAnalysisText());
            intent.putStringArrayListExtra("image_list", new ArrayList<>(review.getImageUrls()));
            intent.putExtra("olx_url", review.getOlxUrl());
            intent.putExtra("olx_title", review.getOlxTitle());
            intent.putExtra("from_favorites", true);
            intent.putExtra("document_id", review.getDocumentId()); // Dodati ovo
            v.getContext().startActivity(intent);
        });

        holder.btnDelete.setOnClickListener(v -> showDeleteDialog(holder.itemView.getContext(), review, position));

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
        if(position == getItemCount() - 1) {
            params.bottomMargin = 32; // U dp
        } else {
            params.bottomMargin = 0;
        }
        holder.itemView.setLayoutParams(params);
    }

    private void showDeleteDialog(Context context, Review review, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Brisanje recenzije")
                .setMessage("Da li ste sigurni da želite obrisati ovu recenziju iz favorita?")
                .setPositiveButton("Obriši", (dialog, which) -> deleteReview(review, position))
                .setNegativeButton("Odustani", null)
                .show();
    }

    private void deleteReview(Review review, int position) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null || review.getDocumentId() == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .collection("favorites")
                .document(review.getDocumentId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    int actualPosition = reviews.indexOf(review);
                    if(actualPosition != -1) {
                        reviews.remove(actualPosition);
                        notifyItemRemoved(actualPosition);
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("DELETE", "Greška pri brisanju", e));
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivPreview;
        final TextView tvTitle;
        final TextView tvSnippet;
        final Button btnOpen;
        TextView tvDate;
        Button btnDelete;
        TextView tvLocation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPreview = itemView.findViewById(R.id.ivPreview);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSnippet = itemView.findViewById(R.id.tvSnippet);
            btnOpen = itemView.findViewById(R.id.btnOpen);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}