package com.example.techanalysisapp3.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.techanalysisapp3.R;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BadgeUtils {
    public static final String BADGE_384_CREDITS = "384_credits";
    public static final String BADGE_100_ANALYSIS = "100_analysis";
    public static final String BADGE_HIGH_RATING = "high_rating";

    public static void unlockBadge(String userId, String badgeKey, Context context) {
        if (context == null) {
            Log.e("BadgeUtils", "Context is null");
            return;
        }

        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            if (activity.isFinishing() || activity.isDestroyed()) {
                Log.w("BadgeUtils", "Activity is finishing or destroyed");
                return;
            }
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    List<String> badges = (List<String>) document.get("badges");

                    if (badges == null || !badges.contains(badgeKey)) {
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("badges", FieldValue.arrayUnion(badgeKey));

                        db.collection("users").document(userId)
                                .update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    if (context instanceof Activity) {
                                        Activity activity = (Activity) context;
                                        if (activity.isFinishing() || activity.isDestroyed()) {
                                            Log.w("BadgeUtils", "Activity state changed, not showing dialog");
                                            return;
                                        }
                                    }
                                    showBadgeUnlockedDialog(badgeKey, context);
                                })
                                .addOnFailureListener(e ->
                                        Log.e("BadgeUtils", "Error adding badge", e)
                                );
                    } else {
                        Log.d("BadgeUtils", "Badge already unlocked: " + badgeKey);
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("BadgeUtils", "Error fetching user badges", e)
                );
    }

    private static void showBadgeUnlockedDialog(String badgeKey, Context context) {
        if (!(context instanceof Activity)) {
            Log.e("BadgeUtils", "Context is not an activity");
            return;
        }

        Activity activity = (Activity) context;
        if (activity.isFinishing() || activity.isDestroyed()) {
            Log.w("BadgeUtils", "Activity is finishing or destroyed");
            return;
        }

        int titleRes = 0;
        int descRes = 0;
        int iconRes = 0;

        switch (badgeKey) {
            case BADGE_384_CREDITS:
                titleRes = R.string.badge_384_title;
                descRes = R.string.badge_384_desc;
                iconRes = R.drawable.badge_384;
                break;
            case BADGE_100_ANALYSIS:
                titleRes = R.string.badge_100_title;
                descRes = R.string.badge_100_desc;
                iconRes = R.drawable.badge_100;
                break;
            case BADGE_HIGH_RATING:
                titleRes = R.string.badge_rating_title;
                descRes = R.string.badge_rating_desc;
                iconRes = R.drawable.badge_rating;
                break;
        }

        if (titleRes == 0) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_badge_unlocked, null);

        ImageView ivBadge = view.findViewById(R.id.ivBadge);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvDescription = view.findViewById(R.id.tvDescription);

        ivBadge.setImageResource(iconRes);
        tvTitle.setText(titleRes);
        tvDescription.setText(descRes);

        builder.setView(view)
                .setPositiveButton("U redu", null)
                .create()
                .show();
    }

    public static void showBadgeInfoDialog(String badgeKey, Context context) {
        int titleRes = 0;
        int descRes = 0;
        int iconRes = 0;

        switch (badgeKey) {
            case BADGE_384_CREDITS:
                titleRes = R.string.badge_384_title;
                descRes = R.string.badge_384_desc;
                iconRes = R.drawable.badge_384_small;
                break;
            case BADGE_100_ANALYSIS:
                titleRes = R.string.badge_100_title;
                descRes = R.string.badge_100_desc;
                iconRes = R.drawable.badge_100_small;
                break;
            case BADGE_HIGH_RATING:
                titleRes = R.string.badge_rating_title;
                descRes = R.string.badge_rating_desc;
                iconRes = R.drawable.badge_rating_small;
                break;
        }

        if (titleRes == 0) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_badge_unlocked, null);

        ImageView ivBadge = view.findViewById(R.id.ivBadge);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvDescription = view.findViewById(R.id.tvDescription);

        ivBadge.setImageResource(iconRes);
        tvTitle.setText(titleRes);
        tvDescription.setText(descRes);

        builder.setView(view)
                .setPositiveButton("Zatvori", null)
                .create()
                .show();
    }
}