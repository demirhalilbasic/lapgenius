package com.example.techanalysisapp3.ui.fragments;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.techanalysisapp3.ui.adapters.LeaderboardAdapter;
import com.example.techanalysisapp3.R;
import com.example.techanalysisapp3.model.HotDeal;
import com.example.techanalysisapp3.model.UserStats;
import com.example.techanalysisapp3.util.BadgeUtils;
import com.example.techanalysisapp3.util.BrandUtils;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.airbnb.lottie.LottieAnimationView;
import android.graphics.ColorFilter;
import android.graphics.PorterDuffColorFilter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class LeaderboardFragment extends Fragment {
    private Spinner spinnerSections;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private LeaderboardAdapter adapter;
    private int currentSection = 0;
    private Dialog refreshDialog;
    private ImageView ivProgress, ivStatus;
    private TextView tvMessage;
    private ObjectAnimator rotationAnimator;
    private Handler dismissHandler = new Handler();
    private Runnable dismissRunnable;

    private LottieAnimationView fireAnimation;
    private Random random = new Random();
    private Handler fireHandler = new Handler();
    private Runnable fireRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leaderboard, container, false);

        spinnerSections = view.findViewById(R.id.spinnerSections);
        recyclerView = view.findViewById(R.id.leaderboardRecycler);
        progressBar = view.findViewById(R.id.progressBar);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.leaderboard_sections,
                android.R.layout.simple_spinner_item
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSections.setAdapter(spinnerAdapter);

        spinnerSections.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                currentSection = pos;
                loadDataForSection(pos, false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        fireAnimation = view.findViewById(R.id.fireAnimation);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btnRefresh).setOnClickListener(v -> {
            loadDataForSection(currentSection, true);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            refreshData();
        }
    }

    private void refreshData() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        loadDataForSection(currentSection, false);
    }

    public void loadDataForSection(int section, boolean showDialog) {
        if (showDialog) showRefreshStatus(true, "Osvježavam podatke…");
        switch (section) {
            case 0:
                loadWeeklyHotDeals();
                break;

            case 1:
                showWeekSelectionDialog();
                break;

            case 2: // 🚀 Rastu ko kvasac
                loadWeeklyUsers();
                break;

            case 3: // 🏆 Šefovi svih vremena
                loadAllTimeUsers();
                break;
        }
    }

    private void showWeekSelectionDialog() {
        final Date today = new Date();
        FirebaseFirestore.getInstance()
                .collection("weekly_hot_deals")
                .orderBy("startDate", Query.Direction.DESCENDING)
                .limit(4)
                .get()
                .addOnSuccessListener(query -> {
                    List<DocumentSnapshot> allWeeks = query.getDocuments();
                    List<DocumentSnapshot> pastWeeks = new ArrayList<>();
                    for (DocumentSnapshot week : allWeeks) {
                        Date start = week.getDate("startDate");
                        Date end   = week.getDate("endDate");
                        if (start == null || end == null) continue;
                        if (today.before(start) || today.after(end)) {
                            pastWeeks.add(week);
                        }
                    }
                    int count = Math.min(pastWeeks.size(), 3);
                    String[] labels = new String[count];
                    for (int i = 0; i < count; i++) {
                        switch (i) {
                            case 0: labels[i] = "🔙 prošla sedmica"; break;
                            case 1: labels[i] = "⏪ pretprošla sedmica"; break;
                            default: labels[i] = "😜 ona tamo sedmica"; break;
                        }
                    }

                    if (pastWeeks.isEmpty()) {
                        spinnerSections.setSelection(0);
                        loadDataForSection(0, true);
                        return;
                    }

                    AlertDialog dialog = new AlertDialog.Builder(requireContext())
                            .setTitle("Odaberi prošlu sedmicu")
                            .setItems(labels, (dlg, which) -> {
                                loadSelectedWeek(pastWeeks.get(which));
                            })
                            .create();
                    dialog.setCanceledOnTouchOutside(true);
                    dialog.setOnCancelListener(dlg -> {
                        spinnerSections.setSelection(0);
                        loadDataForSection(0, true);
                    });
                    dialog.show();
                })
                .addOnFailureListener(e -> {
                    spinnerSections.setSelection(0);
                    loadDataForSection(0, true);
                });
    }

    private void loadSelectedWeek(DocumentSnapshot week) {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        week.getReference().collection("deals")
                .orderBy("rating", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(query -> {
                    List<HotDeal> deals = query.toObjects(HotDeal.class);
                    // FIXED: Pass null as click listener since there are no users
                    adapter = new LeaderboardAdapter(deals, new ArrayList<>(), null);
                    recyclerView.setAdapter(adapter);
                    handleDataLoaded();
                })
                .addOnFailureListener(e -> {
                    handleDataError();
                });
    }

    private void loadWeeklyHotDeals() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date weekStart = cal.getTime();

        Calendar calEnd = (Calendar) cal.clone();
        calEnd.add(Calendar.DATE, 6);
        calEnd.set(Calendar.HOUR_OF_DAY, 23);
        calEnd.set(Calendar.MINUTE, 59);
        calEnd.set(Calendar.SECOND, 59);
        calEnd.set(Calendar.MILLISECOND, 999);
        Date weekEnd = calEnd.getTime();

        FirebaseFirestore.getInstance().collection("weekly_hot_deals")
                .whereLessThanOrEqualTo("startDate", new Date())
                .whereGreaterThanOrEqualTo("endDate", new Date())
                .limit(1)
                .get()
                .addOnSuccessListener(weekQuery -> {
                    if (weekQuery.isEmpty()) {
                        handleDataLoaded();
                        // FIXED: Pass null as click listener since there are no users
                        adapter = new LeaderboardAdapter(new ArrayList<>(), new ArrayList<>(), null);
                        recyclerView.setAdapter(adapter);
                        return;
                    }

                    DocumentSnapshot currentWeek = weekQuery.getDocuments().get(0);
                    currentWeek.getReference().collection("deals")
                            .orderBy("rating", Query.Direction.DESCENDING)
                            .limit(5)
                            .get()
                            .addOnSuccessListener(dealsQuery -> {
                                List<HotDeal> deals = dealsQuery.toObjects(HotDeal.class);
                                adapter = new LeaderboardAdapter(deals, new ArrayList<>(), null);
                                recyclerView.setAdapter(adapter);
                                handleDataLoaded();

                                showFireAnimation();
                            })
                            .addOnFailureListener(e -> handleDataError());
                })
                .addOnFailureListener(e -> {
                    handleDataError();
                    Log.e("Firestore", "Greška pri učitavanju sedmice", e);
                });
    }

    private void showFireAnimation() {
        if (fireAnimation == null) return;

        int duration = 4000 + random.nextInt(4000);

        fireAnimation.cancelAnimation();
        fireAnimation.setVisibility(View.VISIBLE);
        fireAnimation.setProgress(0f);
        fireAnimation.setScaleX(1.25f);
        fireAnimation.setScaleY(1.25f);
        fireAnimation.setTranslationX(0f);
        fireAnimation.setTranslationY(0f);
        fireAnimation.setAlpha(1f);

        fireAnimation.setColorFilter(createFireColorFilter());

        fireAnimation.bringToFront();

        fireAnimation.playAnimation();

        ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(fireAnimation, "alpha", 1f, 0f);
        alphaAnim.setDuration(duration);
        alphaAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        alphaAnim.start();

        fireRunnable = () -> {
            alphaAnim.cancel();
            fireAnimation.cancelAnimation();
            fireAnimation.setVisibility(View.GONE);
            fireAnimation.setAlpha(1f);
            fireAnimation.clearColorFilter();
        };
        fireHandler.postDelayed(fireRunnable, duration);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private ColorFilter createFireColorFilter() {
        return new PorterDuffColorFilter(
                ContextCompat.getColor(requireContext(), R.color.fire_orange),
                PorterDuff.Mode.MULTIPLY
        );
    }

    private void loadWeeklyUsers() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date weekStart = cal.getTime();

        Calendar calEnd = (Calendar) cal.clone();
        calEnd.add(Calendar.DATE, 6);
        calEnd.set(Calendar.HOUR_OF_DAY, 23);
        calEnd.set(Calendar.MINUTE, 59);
        calEnd.set(Calendar.SECOND, 59);
        calEnd.set(Calendar.MILLISECOND, 999);
        Date weekEnd = calEnd.getTime();

        String weekId = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                .format(weekStart) + " - " +
                new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                        .format(weekEnd);

        FirebaseFirestore.getInstance()
                .collection("weekly_user_stats")
                .document(weekId)
                .collection("top_users")
                .orderBy("count", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener(q -> {
                    List<UserStats> users = new ArrayList<>();
                    for (DocumentSnapshot doc : q.getDocuments()) {
                        UserStats u = doc.toObject(UserStats.class);
                        u.setUid(doc.getId());
                        u.setWeeklyAnalysisCount(doc.getLong("count").intValue());
                        users.add(u);
                    }
                    // FIXED: Pass click listener for user items
                    adapter = new LeaderboardAdapter(new ArrayList<>(), users, this::showUserDetails);
                    recyclerView.setAdapter(adapter);
                    handleDataLoaded();
                })
                .addOnFailureListener(e -> handleDataError());
    }

    private void loadAllTimeUsers() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        FirebaseFirestore.getInstance()
                .collection("users")
                .orderBy("totalAnalysisCount", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(query -> {
                    List<UserStats> leaders = new ArrayList<>();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        UserStats u = doc.toObject(UserStats.class);
                        u.setUid(doc.getId());
                        Long tot = doc.getLong("totalAnalysisCount");
                        u.setTotalAnalysisCount(tot != null ? tot.intValue() : 0);
                        leaders.add(u);
                    }
                    // FIXED: Pass click listener for user items
                    adapter = new LeaderboardAdapter(new ArrayList<>(), leaders, this::showUserDetails);
                    recyclerView.setAdapter(adapter);
                    handleDataLoaded();
                })
                .addOnFailureListener(e -> handleDataError());
    }

    private void showUserDetails(UserStats user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.CustomDialog);
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_user_details, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setWindowAnimations(R.style.DialogAnimation);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialog.show();

        ImageView ivProfile = view.findViewById(R.id.ivProfile);
        TextView tvUsername = view.findViewById(R.id.tvUsername);
        TextView tvJoinDate = view.findViewById(R.id.tvJoinDate);
        LinearLayout badgesContainer = view.findViewById(R.id.badgesContainer);
        badgesContainer.removeAllViews();
        ImageView ivGender = view.findViewById(R.id.ivGender);
        ImageView ivBrand = view.findViewById(R.id.ivBrand);
        TextView tvAnalysisCount = view.findViewById(R.id.tvAnalysisCount);

        FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String pfp = document.getString("pfp");
                        if (pfp != null) {
                            int resId = getResources().getIdentifier(pfp, "drawable", requireContext().getPackageName());
                            if (resId != 0) {
                                ivProfile.setImageResource(resId);
                            }
                        }

                        Long createdAt = document.getLong("createdAt");
                        if (createdAt != null) {
                            Calendar cal = Calendar.getInstance();
                            cal.setTimeInMillis(createdAt);

                            String[] months = {"januar", "februar", "mart", "april", "maj",
                                    "juni", "juli", "avgust", "septembar",
                                    "oktobar", "novembar", "decembar"};

                            int monthIndex = cal.get(Calendar.MONTH);
                            int year = cal.get(Calendar.YEAR);

                            String gender = document.getString("gender");
                            String joinText;

                            if ("female".equals(gender)) {
                                joinText = "Pridružila se " + months[monthIndex] + ", " + year;
                            } else {
                                joinText = "Pridružio se " + months[monthIndex] + ", " + year;
                            }

                            tvJoinDate.setText(joinText);
                        }

                        List<String> badges = (List<String>) document.get("badges");
                        if (badges == null || badges.isEmpty()) {
                            TextView noBadges = new TextView(requireContext());
                            noBadges.setText("Nema badgeva");
                            noBadges.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));
                            badgesContainer.addView(noBadges);
                        } else {
                            for (String badge : badges) {
                                ImageView badgeView = new ImageView(requireContext());
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                        dpToPx(48),
                                        dpToPx(48)
                                );
                                params.setMargins(0, 0, dpToPx(8), 0);
                                badgeView.setLayoutParams(params);

                                switch (badge) {
                                    case BadgeUtils.BADGE_384_CREDITS:
                                        badgeView.setImageResource(R.drawable.badge_384_small);
                                        badgeView.setContentDescription("384 kredita badge");
                                        break;
                                    case BadgeUtils.BADGE_100_ANALYSIS:
                                        badgeView.setImageResource(R.drawable.badge_100_small);
                                        badgeView.setContentDescription("100 analiza badge");
                                        break;
                                    case BadgeUtils.BADGE_HIGH_RATING:
                                        badgeView.setImageResource(R.drawable.badge_rating_small);
                                        badgeView.setContentDescription("Visoka ocjena badge");
                                        break;
                                }

                                badgeView.setOnClickListener(v ->
                                        BadgeUtils.showBadgeInfoDialog(badge, requireContext())
                                );
                                badgesContainer.addView(badgeView);
                            }
                        }

                        String gender = document.getString("gender");
                        if ("male".equals(gender)) {
                            ivGender.setImageResource(R.drawable.ic_male);
                        } else if ("female".equals(gender)) {
                            ivGender.setImageResource(R.drawable.ic_female);
                        }

                        String brand = document.getString("favoriteBrand");
                        if (brand != null) {
                            int brandResId = BrandUtils.getBrandLogoResource(brand);
                            if (brandResId != 0) {
                                ivBrand.setImageResource(brandResId);
                            }
                        }

                        // ALWAYS show total analysis count
                        Long totalAnalysis = document.getLong("totalAnalysisCount");
                        if (totalAnalysis != null) {
                            tvAnalysisCount.setText(String.format(Locale.getDefault(),
                                    "%d analiza ukupno", totalAnalysis));
                        }
                    }

                    tvUsername.setText(user.getUsername());
                });
    }

    private void handleDataLoaded() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void handleDataError() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void showRefreshStatus(boolean success, String message) {
        if (refreshDialog == null) {
            refreshDialog = new Dialog(requireContext());
            refreshDialog.setContentView(R.layout.dialog_refresh);
            ivProgress = refreshDialog.findViewById(R.id.ivProgress);
            ivStatus = refreshDialog.findViewById(R.id.ivStatus);
            tvMessage = refreshDialog.findViewById(R.id.tvMessage);

            rotationAnimator = ObjectAnimator.ofFloat(ivProgress, "rotation", 0f, 360f);
            rotationAnimator.setDuration(1000);
            rotationAnimator.setRepeatCount(ValueAnimator.INFINITE);
            rotationAnimator.setInterpolator(new LinearInterpolator());
        }

        ivProgress.setVisibility(View.VISIBLE);
        ivStatus.setVisibility(View.GONE);
        rotationAnimator.start();

        ivProgress.setColorFilter(ContextCompat.getColor(requireContext(), R.color.secondary));
        tvMessage.setTextColor(ContextCompat.getColor(requireContext(), R.color.secondary));
        tvMessage.setText(message);

        refreshDialog.show();

        dismissRunnable = () -> {
            if (refreshDialog.isShowing()) {
                rotationAnimator.cancel();
                refreshDialog.dismiss();
            }
        };
        dismissHandler.postDelayed(dismissRunnable, 2000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (dismissHandler != null) {
            dismissHandler.removeCallbacksAndMessages(null);
        }
        if (rotationAnimator != null) {
            rotationAnimator.cancel();
        }
        recyclerView.setAdapter(null);

        if (fireHandler != null && fireRunnable != null) {
            fireHandler.removeCallbacks(fireRunnable);
        }

        if (fireAnimation != null) {
            fireAnimation.cancelAnimation();
        }
    }
}