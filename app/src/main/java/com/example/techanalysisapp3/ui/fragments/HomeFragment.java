package com.example.techanalysisapp3.ui.fragments;

import static android.content.ContentValues.TAG;
import static com.example.techanalysisapp3.util.AppConstants.RAWG_API_KEY;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.techanalysisapp3.ui.activities.ShareProcessActivity;
import com.example.techanalysisapp3.ui.adapters.HotDealsAdapter;
import com.example.techanalysisapp3.ui.components.GameSearchDialog;
import com.example.techanalysisapp3.R;
import com.example.techanalysisapp3.util.BadgeUtils;
import com.example.techanalysisapp3.util.UrlUtils;
import com.example.techanalysisapp3.viewmodel.SharedViewModel;
import com.example.techanalysisapp3.model.HotDeal;
import com.example.techanalysisapp3.ui.activities.AnalysisSwipeActivity;
import com.example.techanalysisapp3.ui.activities.LoginActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HomeFragment extends Fragment implements GameSearchDialog.OnGameSelectedListener {
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseUser user;
    private String uid, firstName;
    private long credits = 0;
    private int freeUsed = 0;
    private long lastFreeDate = 0;
    private TextView tvWelcome, tvCreditsStatus;
    private String lastProcessedUrl = "";
    private String gender = "";

    // game selection
    private int selectedGameId = -1;
    private String selectedGameName = "", selectedMinReq = "", selectedRecReq = "";

    // progress dialog
    private Dialog progressDialog;
    private TextView progressMessage;
    // messages on repeat
    private Handler msgHandler;

    private SharedViewModel sharedViewModel;

    private FirebaseAuth.AuthStateListener authStateListener;

    private MaterialButton btnStartAnalysis;
    private RecyclerView rvHotDeals;
    private HotDealsAdapter hotDealsAdapter;

    private RecyclerView rvFavoriteBrand;
    private HotDealsAdapter favoriteBrandAdapter;
    private String favoriteBrand;
    private TextView tvFavoriteBrandLabel;
    private ValueAnimator favoriteBrandAnimator;

    private ValueAnimator scrollAnimator;
    private int scrollDirection = 1; // 1 za desno, -1 za lijevo

    private Handler creditUpdateHandler;
    private Runnable creditUpdateRunnable;
    private static final long CREDIT_UPDATE_INTERVAL = 5000; // 5 seconds

    private AnimatorSet logoPulseAnimator;
    private AnimatorSet welcomeCardPressAnimator;

    private boolean isLongPress = false;
    private Handler longPressHandler = new Handler();
    private static final int LONG_PRESS_DURATION = 500; // 0.5 seconds

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // init Firebase
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();
        if (user != null) uid = user.getUid();

        // find views
        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvCreditsStatus = view.findViewById(R.id.tvCreditsStatus);
        btnStartAnalysis = view.findViewById(R.id.btnStartAnalysis);
        rvHotDeals = view.findViewById(R.id.rvHotDeals);

        // Setup hot deals recycler view
        rvHotDeals.setLayoutManager(new LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
        ));
        hotDealsAdapter = new HotDealsAdapter();
        rvHotDeals.setAdapter(hotDealsAdapter);

        rvFavoriteBrand = view.findViewById(R.id.rvFavoriteBrand);
        tvFavoriteBrandLabel = view.findViewById(R.id.tvFavoriteBrandLabel);

        rvFavoriteBrand.setLayoutManager(new LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
        ));
        favoriteBrandAdapter = new HotDealsAdapter();
        rvFavoriteBrand.setAdapter(favoriteBrandAdapter);

        // Load user data
        loadUserData();

        // Load hot deals
        loadWeeklyHotDeals();

        btnStartAnalysis.setOnClickListener(v -> showUrlInputDialog());
        return view;
    }

    private void showUrlInputDialog() {
        if (freeUsed >= 3 && credits <= 0) {
            Toast.makeText(
                    requireContext(),
                    "Nemate dovoljno kredita na nalogu.\nMolimo sačekajte do sutra za besplatne analize ili dokupite više kredita.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_url_input, null);

        EditText etUrl = dialogView.findViewById(R.id.etUrl);
        MaterialButton btnAnalyze = dialogView.findViewById(R.id.btnAnalyze);
        MaterialButton btnBrowseOlx = dialogView.findViewById(R.id.btnBrowseOlx);
        TextView tvUrlStatus = dialogView.findViewById(R.id.tvUrlStatus);

        AlertDialog dialog = builder.setView(dialogView).create();

        etUrl.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString().trim();
                boolean isValid = UrlUtils.extractAndFormatUrl(input) != null;

                btnAnalyze.setEnabled(isValid);
                tvUrlStatus.setText(isValid ? "URL je validan" : "Unesite validan OLX URL");
                tvUrlStatus.setTextColor(isValid ?
                        ContextCompat.getColor(requireContext(), R.color.success) :
                        ContextCompat.getColor(requireContext(), R.color.error));
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        btnAnalyze.setOnClickListener(v -> {
            String input = etUrl.getText().toString().trim();
            String url = UrlUtils.extractAndFormatUrl(input);

            if (url != null) {
                Intent intent = new Intent(requireActivity(), ShareProcessActivity.class);
                intent.putExtra("olx_url", url);
                startActivity(intent);
                dialog.dismiss();
            }
        });

        btnBrowseOlx.setOnClickListener(v -> {
            Intent browserIntent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://olx.ba/pretraga?attr=&attr_encoded=1&category_id=39")
            );
            startActivity(browserIntent);
        });

        dialog.show();
    }

    private void loadWeeklyHotDeals() {
        // Get current and previous 3 weeks
        List<String> weekIds = getLastFourWeekIds();
        List<HotDeal> allDeals = new ArrayList<>();
        final int totalWeeks = weekIds.size();
        AtomicInteger weeksFetched = new AtomicInteger(0);

        for (String weekId : weekIds) {
            firestore.collection("weekly_hot_deals")
                    .document(weekId)
                    .collection("deals")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            HotDeal deal = doc.toObject(HotDeal.class);
                            if (deal != null) allDeals.add(deal);
                        }

                        if (weeksFetched.incrementAndGet() == totalWeeks) {
                            Collections.shuffle(allDeals); // Shuffle all deals
                            hotDealsAdapter.setHotDeals(allDeals);
                            startSmoothAutoScroll();
                        }
                    })
                    .addOnFailureListener(e -> {
                        weeksFetched.incrementAndGet();
                        Log.e(TAG, "Error loading week: " + weekId, e);
                    });
        }
    }

    private List<String> getLastFourWeekIds() {
        List<String> weekIds = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

        // Get current and previous 3 weeks
        for (int i = 0; i > -4; i--) {
            Calendar weekStart = (Calendar) cal.clone();
            weekStart.add(Calendar.WEEK_OF_YEAR, i);
            weekStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            weekStart.set(Calendar.HOUR_OF_DAY, 0);
            weekStart.clear(Calendar.MINUTE);
            weekStart.clear(Calendar.SECOND);
            weekStart.clear(Calendar.MILLISECOND);

            Calendar weekEnd = (Calendar) weekStart.clone();
            weekEnd.add(Calendar.DATE, 6);
            weekEnd.set(Calendar.HOUR_OF_DAY, 23);
            weekEnd.set(Calendar.MINUTE, 59);
            weekEnd.set(Calendar.SECOND, 59);
            weekEnd.set(Calendar.MILLISECOND, 999);

            weekIds.add(sdf.format(weekStart.getTime()) + " - " + sdf.format(weekEnd.getTime()));
        }
        return weekIds;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void hideProgress() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
            if (msgHandler != null) msgHandler.removeCallbacksAndMessages(null);
            progressMessage.animate().cancel();
        }
    }

    private void loadUserData() {
        if (user == null) {
            Context context = requireContext();
            startActivity(new Intent(context, LoginActivity.class));
            if (getActivity() != null) {
                getActivity().finish();
            }
            return;
        }

        firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        firstName = doc.getString("firstName");
                        gender = doc.getString("gender");
                        Long c = doc.getLong("credits");
                        Long f = doc.getLong("freeTierUsed");
                        Long d = doc.getLong("lastFreeTierDate");
                        favoriteBrand = doc.getString("favoriteBrand");
                        if (favoriteBrand != null && !favoriteBrand.isEmpty()) {
                            loadFavoriteBrandDeals();
                        }

                        credits = (c != null) ? c : 0L;
                        freeUsed = (f != null) ? f.intValue() : 0;
                        lastFreeDate = (d != null) ? d : 0L;

                        updateUserUI();

                        List<Double> badgeArraylist = (List<Double>) doc.get("badgeArraylist");
                        List<String> badges = (List<String>) doc.get("badges");

                        if (badgeArraylist != null && !badgeArraylist.isEmpty()) {
                            boolean foundHighRating = false;

                            for (Double rating : badgeArraylist) {
                                if (rating >= 9.8) {
                                    if (badges == null || !badges.contains(BadgeUtils.BADGE_HIGH_RATING)) {
                                        BadgeUtils.unlockBadge(uid, BadgeUtils.BADGE_HIGH_RATING, requireContext());
                                    }

                                    Map<String, Object> updates = new HashMap<>();
                                    updates.put("badgeArraylist", FieldValue.delete());

                                    firestore.collection("users").document(uid)
                                            .update(updates)
                                            .addOnSuccessListener(aVoid -> Log.d(TAG, "badgeArraylist potpuno obrisan"))
                                            .addOnFailureListener(e -> Log.e(TAG, "Greška pri brisanju", e));

                                    foundHighRating = true;
                                    break;
                                }
                            }

                            if (!foundHighRating) {
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("badgeArraylist", FieldValue.delete());

                                firestore.collection("users").document(uid)
                                        .update(updates)
                                        .addOnSuccessListener(aVoid ->
                                                Log.d(TAG, "badgeArraylist obrisan jer nema ocjena >= 9.8")
                                        )
                                        .addOnFailureListener(e ->
                                                Log.e(TAG, "Greška pri brisanju badgeArraylist", e)
                                        );
                            }
                        }

                        Long totalAnalysisCount = doc.getLong("totalAnalysisCount");
                        if (totalAnalysisCount != null && totalAnalysisCount >= 100) {
                            if (badges == null || !badges.contains(BadgeUtils.BADGE_100_ANALYSIS)) {
                                BadgeUtils.unlockBadge(uid, BadgeUtils.BADGE_100_ANALYSIS, requireContext());
                            }
                        }

                        if (getActivity() != null && getActivity().getIntent() != null) {
                            boolean isNewUser = getActivity().getIntent().getBooleanExtra("newUser", false);
                            if (isNewUser) {
                                showWelcomeDialog();
                                getActivity().getIntent().removeExtra("newUser");
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "loadUserData", e));
    }

    private void loadFavoriteBrandDeals() {
        String keyword = getSearchKeywordForBrand(favoriteBrand).toLowerCase();
        Map<String, String> brandMap = getBrandTitleMap();

        String title = brandMap.containsKey(favoriteBrand) ?
                brandMap.get(favoriteBrand) :
                "🔥 Vaš omiljeni brend";
        tvFavoriteBrandLabel.setText(title);

        List<String> mainBrands = new ArrayList<>();
        mainBrands.add(keyword);

        if (favoriteBrand.contains("_")) {
            String mainBrand = favoriteBrand.split("_")[0];
            if (!mainBrand.equals(keyword)) {
                mainBrands.add(mainBrand);
            }
        }

        List<String> weekIds = getLastFourWeekIds();
        List<HotDeal> brandDeals = new ArrayList<>();
        AtomicInteger weeksFetched = new AtomicInteger(0);
        final int totalWeeks = weekIds.size();

        for (String weekId : weekIds) {
            firestore.collection("weekly_hot_deals")
                    .document(weekId)
                    .collection("deals")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!isAdded()) return;
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            HotDeal deal = doc.toObject(HotDeal.class);
                            if (deal != null) {
                                String titleLower = deal.getOlxTitle().toLowerCase();

                                for (String brandKeyword : mainBrands) {
                                    if (titleLower.contains(brandKeyword)) {
                                        brandDeals.add(deal);
                                        break;
                                    }
                                }
                            }
                        }

                        if (weeksFetched.incrementAndGet() == totalWeeks) {
                            Collections.shuffle(brandDeals);
                            favoriteBrandAdapter.setHotDeals(brandDeals);

                            if (!brandDeals.isEmpty()) {
                                tvFavoriteBrandLabel.setVisibility(View.VISIBLE);
                                rvFavoriteBrand.setVisibility(View.VISIBLE);
                                startFavoriteBrandAutoScroll();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (!isAdded()) return;
                        weeksFetched.incrementAndGet();
                        Log.e(TAG, "Error loading brand deals for week: " + weekId, e);
                    });
        }
    }

    private void startFavoriteBrandAutoScroll() {
        favoriteBrandAnimator = ValueAnimator.ofInt(0, 1);
        favoriteBrandAnimator.setRepeatCount(ValueAnimator.INFINITE);
        favoriteBrandAnimator.setInterpolator(new LinearInterpolator());
        favoriteBrandAnimator.setDuration(10000);

        favoriteBrandAnimator.addUpdateListener(animation -> {
            if (isAdded() && rvFavoriteBrand != null) {
                rvFavoriteBrand.scrollBy(2 * scrollDirection, 0);
            }
        });

        if (isAdded()) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) rvFavoriteBrand.getLayoutManager();
            int initialPosition = Integer.MAX_VALUE / 2;
            int startPosition = initialPosition - (initialPosition % favoriteBrandAdapter.getRealItemCount());
            layoutManager.scrollToPosition(startPosition);
            favoriteBrandAnimator.start();
        }
    }

    private void updateUserUI() {
        if (!isAdded()) return;
        String welcomePrefix = "female".equals(gender) ? "Dobrodošla" : "Dobrodošao";
        tvWelcome.setText(welcomePrefix + ", " + firstName);
        updateCreditDisplay();
    }

    private void startPeriodicCreditUpdates() {
        creditUpdateHandler = new Handler(Looper.getMainLooper());
        creditUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                refreshCreditData();
                creditUpdateHandler.postDelayed(this, CREDIT_UPDATE_INTERVAL);
            }
        };
        creditUpdateHandler.postDelayed(creditUpdateRunnable, CREDIT_UPDATE_INTERVAL);
    }

    private void refreshCreditData() {
        if (user == null || !isAdded()) return;

        firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Long c = doc.getLong("credits");
                        Long f = doc.getLong("freeTierUsed");

                        credits = (c != null) ? c : 0L;
                        freeUsed = (f != null) ? f.intValue() : 0;

                        updateCreditDisplay();
                    }
                });
    }

    private void updateCreditDisplay() {
        if (!isAdded()) return;

        String creditText;
        if (freeUsed < 3) {
            creditText = String.format(
                    "Besplatne dnevne analize: %d/3\nKrediti: %d\nSljedeća analiza u potpunosti BESPLATNA",
                    3 - freeUsed, credits
            );
        } else {
            creditText = String.format(
                    "Besplatne dnevne analize: 0/3\nKrediti: %d\nSljedeća analiza se naplaćuje kreditima",
                    credits
            );
        }
        tvCreditsStatus.setText(creditText);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle out) {
        super.onSaveInstanceState(out);
        out.putString("lastProcessedUrl", lastProcessedUrl);
    }

    @Override
    public void onGameSelected(int id, String name, String img) {
        selectedGameId   = id;
        selectedGameName = name;
        fetchGameDetails(id);
    }

    private void fetchGameDetails(int id){
        OkHttpClient c=new OkHttpClient();
        Request r=new Request.Builder()
                .url("https://api.rawg.io/api/games/"+id+"?key="+RAWG_API_KEY)
                .build();
        c.newCall(r).enqueue(new Callback(){
            @Override public void onFailure(Call c,IOException e){
                hideProgress();
                Log.e(TAG,e.toString());
            }
            @Override public void onResponse(Call c,Response r) throws IOException{
                hideProgress();
                try{
                    JSONObject root=new JSONObject(r.body().string());
                    JSONArray plt=root.getJSONArray("platforms");
                    for(int i=0;i<plt.length();i++){
                        JSONObject req=plt.getJSONObject(i)
                                .getJSONObject("requirements_en");
                        if(req.has("minimum")) selectedMinReq=req.getString("minimum");
                        if(req.has("recommended")) selectedRecReq=req.getString("recommended");
                        break;
                    }
                }catch(Exception e){Log.e(TAG,e.toString());}
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvHotDeals.setNestedScrollingEnabled(false);
        rvHotDeals.setHasFixedSize(true);

        hotDealsAdapter.setOnItemClickListener(deal -> {
            Intent intent = new Intent(requireActivity(), AnalysisSwipeActivity.class);
            intent.putExtra("ai_analysis", deal.getAnalysisText());
            intent.putStringArrayListExtra("image_list", new ArrayList<>(deal.getImageUrls()));
            intent.putExtra("olx_url", deal.getOlxUrl());
            intent.putExtra("olx_title", deal.getOlxTitle());
            intent.putExtra("city_name", deal.getCityName());
            intent.putExtra("latitude", deal.getLatitude());
            intent.putExtra("longitude", deal.getLongitude());
            startActivity(intent);
        });

        favoriteBrandAdapter.setOnItemClickListener(deal -> {
            Intent intent = new Intent(requireActivity(), AnalysisSwipeActivity.class);
            intent.putExtra("ai_analysis", deal.getAnalysisText());
            intent.putStringArrayListExtra("image_list", new ArrayList<>(deal.getImageUrls()));
            intent.putExtra("olx_url", deal.getOlxUrl());
            intent.putExtra("olx_title", deal.getOlxTitle());
            intent.putExtra("city_name", deal.getCityName());
            intent.putExtra("latitude", deal.getLatitude());
            intent.putExtra("longitude", deal.getLongitude());
            startActivity(intent);
        });

        startSmoothAutoScroll();

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        setupBackButtonHandling();

        ImageView ivLogo = view.findViewById(R.id.ivLogo);
        CardView cardWelcome = view.findViewById(R.id.cardWelcome);
        ViewGroup mainContainer = view.findViewById(R.id.mainContainer);

        startLogoPulseAnimation(ivLogo);

        cardWelcome.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isLongPress = false;
                    longPressHandler.postDelayed(() -> {
                        isLongPress = true;
                        playWelcomeCardPressAnimation(cardWelcome, true);
                        triggerCoinRain(cardWelcome);
                    }, LONG_PRESS_DURATION);

                    playWelcomeCardPressAnimation(cardWelcome, false);
                    return true;

                case MotionEvent.ACTION_UP:
                    longPressHandler.removeCallbacksAndMessages(null);

                    if (isLongPress) {
                        playWelcomeCardReleaseAnimation(cardWelcome);
                    } else {
                        playWelcomeCardReleaseAnimation(cardWelcome);
                        addFloatingCoins(cardWelcome, 5);
                    }
                    return true;

                case MotionEvent.ACTION_CANCEL:
                    longPressHandler.removeCallbacksAndMessages(null);
                    playWelcomeCardReleaseAnimation(cardWelcome);
                    return true;
            }
            return false;
        });

        playStaggeredEntryAnimations(mainContainer);

        sharedViewModel.getSharedUrl().observe(getViewLifecycleOwner(), url -> {
            if (url != null && !url.isEmpty()) {
                sharedViewModel.clearSharedUrl();
            }
        });

        view.requestFocus();

        ivLogo.setOnClickListener(v -> {
            if (freeUsed >= 3 && credits <= 0) {
                Toast.makeText(
                        requireContext(),
                        "Nemate dovoljno kredita na nalogu.\nMolimo sačekajte do sutra za besplatne analize ili dokupite više kredita.",
                        Toast.LENGTH_LONG
                ).show();
                return;
            }

            ObjectAnimator scaleDown = ObjectAnimator.ofPropertyValuesHolder(
                    ivLogo,
                    PropertyValuesHolder.ofFloat("scaleX", 0.9f),
                    PropertyValuesHolder.ofFloat("scaleY", 0.9f)
            );
            scaleDown.setDuration(100);

            ObjectAnimator scaleUp = ObjectAnimator.ofPropertyValuesHolder(
                    ivLogo,
                    PropertyValuesHolder.ofFloat("scaleX", 1f),
                    PropertyValuesHolder.ofFloat("scaleY", 1f)
            );
            scaleUp.setDuration(200);

            AnimatorSet clickAnim = new AnimatorSet();
            clickAnim.playSequentially(scaleDown, scaleUp);
            clickAnim.start();

            showUrlInputDialog();
        });

        sharedViewModel.getSharedUrl().observe(getViewLifecycleOwner(), url -> {
        });

        sharedViewModel.getShowInstructions().observe(getViewLifecycleOwner(), show -> {
            if (show) {
                Toast.makeText(requireContext(),
                        "URL uspješno unijet!", Toast.LENGTH_SHORT).show();

                new Handler(Looper.getMainLooper()).postDelayed(() ->
                        showInstructionSnackbar(), 500);

                sharedViewModel.clearData();
            }
        });

        sharedViewModel.getSharedUrl().observe(getViewLifecycleOwner(), url -> {
            if (url != null && !url.isEmpty()) {
                sharedViewModel.clearSharedUrl();
            }
        });

        FirebaseAuth.getInstance().addAuthStateListener(firebaseAuth -> {
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if (currentUser != null && currentUser.isEmailVerified()) {
                syncEmailWithFirestore(currentUser);
            }
        });

        authStateListener = firebaseAuth -> {
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if (currentUser != null && currentUser.isEmailVerified()) {
                syncEmailWithFirestore(currentUser);
            }
        };

        FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
    }

    private void playWelcomeCardPressAnimation(View cardWelcome, boolean isLongPress) {
        if (welcomeCardPressAnimator != null && welcomeCardPressAnimator.isRunning()) {
            welcomeCardPressAnimator.cancel();
        }

        float scaleFactor = isLongPress ? 0.92f : 0.98f;
        ObjectAnimator pressDown = ObjectAnimator.ofPropertyValuesHolder(
                cardWelcome,
                PropertyValuesHolder.ofFloat("scaleX", scaleFactor),
                PropertyValuesHolder.ofFloat("scaleY", scaleFactor)
        );
        pressDown.setDuration(isLongPress ? 300 : 150);
        pressDown.start();
    }

    private void playWelcomeCardReleaseAnimation(View cardWelcome) {
        ObjectAnimator releaseUp = ObjectAnimator.ofPropertyValuesHolder(
                cardWelcome,
                PropertyValuesHolder.ofFloat("scaleX", 1f),
                PropertyValuesHolder.ofFloat("scaleY", 1f)
        );
        releaseUp.setDuration(400);
        releaseUp.setInterpolator(new OvershootInterpolator(1.5f));
        releaseUp.start();
    }

    private void addFloatingCoins(View anchorView, int coinCount) {
        if (getView() == null) return;

        FrameLayout rootContainer = requireView().findViewById(R.id.rootContainer);
        int[] location = new int[2];
        anchorView.getLocationOnScreen(location);

        for (int i = 0; i < coinCount; i++) {
            ImageView coin = new ImageView(requireContext());
            coin.setImageResource(R.drawable.ic_coin);

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    dpToPx(30),
                    dpToPx(30)
            );

            // MODIFIED: Spawn coins at random positions within the card
            int randomX = new Random().nextInt(anchorView.getWidth());
            int randomY = new Random().nextInt(anchorView.getHeight());
            params.leftMargin = location[0] + randomX;
            params.topMargin = location[1] + randomY;

            rootContainer.addView(coin, params);

            // Random end position with physics
            int endY = params.topMargin - dpToPx(100 + new Random().nextInt(200));
            int endX = params.leftMargin +
                    (new Random().nextBoolean() ? 1 : -1) *
                            dpToPx(50 + new Random().nextInt(100));

            // Rotation animation
            ObjectAnimator rotateAnim = ObjectAnimator.ofFloat(coin, "rotation", 0f, 360f * (new Random().nextInt(3) + 1));
            rotateAnim.setDuration(1200);
            rotateAnim.setInterpolator(new LinearInterpolator());

            // Movement animation
            ObjectAnimator yAnim = ObjectAnimator.ofFloat(coin, "translationY", 0, endY - params.topMargin);
            ObjectAnimator xAnim = ObjectAnimator.ofFloat(coin, "translationX", 0, endX - params.leftMargin);

            // Fade out
            ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(coin, "alpha", 1f, 0f);

            // Scale animation for "pop" effect
            ObjectAnimator scaleAnim = ObjectAnimator.ofPropertyValuesHolder(
                    coin,
                    PropertyValuesHolder.ofFloat("scaleX", 0f, 1.5f, 1f),
                    PropertyValuesHolder.ofFloat("scaleY", 0f, 1.5f, 1f)
            );
            scaleAnim.setDuration(300);

            AnimatorSet set = new AnimatorSet();
            set.playTogether(yAnim, xAnim, alphaAnim, rotateAnim);
            set.play(scaleAnim).before(yAnim); // Scale first then move
            set.setDuration(1200);
            set.setStartDelay(i * 50);
            set.setInterpolator(new AccelerateDecelerateInterpolator());
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    rootContainer.removeView(coin);
                }
            });
            set.start();
        }
    }

    // Coin rain effect for long press
    private void triggerCoinRain(View anchorView) {
        if (getView() == null) return;

        FrameLayout rootContainer = requireView().findViewById(R.id.rootContainer);
        int[] location = new int[2];
        anchorView.getLocationOnScreen(location);
        int centerX = location[0] + anchorView.getWidth() / 2;
        int centerY = location[1] + anchorView.getHeight() / 2;

        // Create 30 coins for the rain effect
        for (int i = 0; i < 30; i++) {
            ImageView coin = new ImageView(requireContext());
            coin.setImageResource(R.drawable.ic_coin);

            // Random starting position above the card
            int startX = centerX - dpToPx(100) + new Random().nextInt(dpToPx(200));
            int startY = centerY - dpToPx(150) - new Random().nextInt(dpToPx(100));

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    dpToPx(24 + new Random().nextInt(12)), // Random size
                    dpToPx(24 + new Random().nextInt(12))
            );
            params.leftMargin = startX;
            params.topMargin = startY;

            rootContainer.addView(coin, params);

            // Random end position below the card
            int endY = centerY + dpToPx(200) + new Random().nextInt(dpToPx(100));
            int endX = startX + (new Random().nextBoolean() ? 1 : -1) *
                    new Random().nextInt(dpToPx(100));

            // Random rotation
            int rotations = 1 + new Random().nextInt(3);
            ObjectAnimator rotateAnim = ObjectAnimator.ofFloat(coin, "rotation", 0f, 360f * rotations);
            rotateAnim.setDuration(1800);
            rotateAnim.setInterpolator(new LinearInterpolator());

            // Falling animation
            ObjectAnimator yAnim = ObjectAnimator.ofFloat(coin, "translationY", 0, endY - startY);
            ObjectAnimator xAnim = ObjectAnimator.ofFloat(coin, "translationX", 0, endX - startX);

            // Fade out at the end
            ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(coin, "alpha", 1f, 0f);
            alphaAnim.setStartDelay(1200);
            alphaAnim.setDuration(600);

            // Scale effect when landing
            ObjectAnimator scaleAnim = ObjectAnimator.ofPropertyValuesHolder(
                    coin,
                    PropertyValuesHolder.ofFloat("scaleX", 1f, 1.2f, 0.8f, 1f),
                    PropertyValuesHolder.ofFloat("scaleY", 1f, 1.2f, 0.8f, 1f)
            );
            scaleAnim.setStartDelay(1000);
            scaleAnim.setDuration(800);

            AnimatorSet set = new AnimatorSet();
            set.playTogether(yAnim, xAnim, rotateAnim);
            set.play(alphaAnim).after(1200);
            set.play(scaleAnim).after(1000);
            set.setDuration(1800);
            set.setStartDelay(i * 30);
            set.setInterpolator(new AccelerateDecelerateInterpolator());
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    rootContainer.removeView(coin);
                }
            });
            set.start();
        }

        // Add vibration feedback for long press
        try {
            Vibrator vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(50);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Vibration error", e);
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void startLogoPulseAnimation(ImageView ivLogo) {
        if (ivLogo == null) return;

        if (logoPulseAnimator != null && logoPulseAnimator.isRunning()) {
            logoPulseAnimator.cancel();
        }

        ObjectAnimator scaleUp = ObjectAnimator.ofPropertyValuesHolder(
                ivLogo,
                PropertyValuesHolder.ofFloat("scaleX", 1.05f),
                PropertyValuesHolder.ofFloat("scaleY", 1.05f)
        );
        scaleUp.setDuration(1200);

        ObjectAnimator scaleDown = ObjectAnimator.ofPropertyValuesHolder(
                ivLogo,
                PropertyValuesHolder.ofFloat("scaleX", 1f),
                PropertyValuesHolder.ofFloat("scaleY", 1f)
        );
        scaleDown.setDuration(800);

        logoPulseAnimator = new AnimatorSet();
        logoPulseAnimator.playSequentially(scaleUp, scaleDown);
        logoPulseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        logoPulseAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                logoPulseAnimator.start(); // Loop animation
            }
        });
        logoPulseAnimator.start();
    }

    private void playStaggeredEntryAnimations(ViewGroup container) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            child.setAlpha(0);
            child.setTranslationY(dpToPx(20));
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            for (int i = 0; i < container.getChildCount(); i++) {
                View child = container.getChildAt(i);
                child.animate()
                        .alpha(1)
                        .translationY(0)
                        .setDuration(600)
                        .setStartDelay(100 * i)
                        .setInterpolator(new OvershootInterpolator(1.0f))
                        .start();
            }
        }, 300);
    }

    private void syncEmailWithFirestore(FirebaseUser currentUser) {
        firestore.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String firestoreEmail = documentSnapshot.getString("email");
                        String authEmail = currentUser.getEmail();
                        boolean firestoreVerified = Boolean.TRUE.equals(
                                documentSnapshot.getBoolean("emailVerified")
                        );

                        if (firestoreEmail != null && authEmail != null &&
                                (!firestoreEmail.equals(authEmail) || !firestoreVerified)) {

                            Map<String, Object> updates = new HashMap<>();
                            updates.put("email", authEmail);
                            updates.put("emailVerified", true);

                            firestore.collection("users").document(currentUser.getUid())
                                    .update(updates)
                                    .addOnSuccessListener(aVoid ->
                                            Log.d("EMAIL_SYNC", "Email uspješno sinkroniziran")
                                    )
                                    .addOnFailureListener(e ->
                                            Log.e("EMAIL_SYNC", "Greška pri sinkronizaciji", e)
                                    );
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("EMAIL_SYNC", "Greška pri dohvatu podataka", e));
    }

    private void showWelcomeDialog() {
        if (!isAdded() || getActivity() == null) return;
        Dialog welcomeDialog = new Dialog(requireContext());
        welcomeDialog.setContentView(R.layout.dialog_welcome);
        welcomeDialog.setCancelable(false);

        TextView tvMessage = welcomeDialog.findViewById(R.id.tvWelcomeMessage);
        Button btnOk = welcomeDialog.findViewById(R.id.btnOk);

        String welcomeText;
        if ("female".equals(gender)) {
            welcomeText = "Dobrodošla, " + firstName + "! Dobili ste 10 početnih kredita kao poklon!";
        } else {
            welcomeText = "Dobrodošao, " + firstName + "! Dobili ste 10 početnih kredita kao poklon!";
        }
        tvMessage.setText(welcomeText);

        btnOk.setOnClickListener(v -> {
            welcomeDialog.dismiss();
            showEmailVerificationPrompt();
        });

        Window window = welcomeDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.CENTER;
            window.setAttributes(params);
        }

        welcomeDialog.show();
    }

    private void showEmailVerificationPrompt() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || currentUser.isEmailVerified()) {
            return;
        }

        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_email_verification_prompt);
        dialog.setCancelable(false);

        TextView tvEmail = dialog.findViewById(R.id.tvEmail);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnSend = dialog.findViewById(R.id.btnSend);

        tvEmail.setText(currentUser.getEmail());

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSend.setOnClickListener(v -> {
            dialog.dismiss();
            sendVerificationEmail(currentUser);
        });

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            params.gravity = Gravity.CENTER;
            window.setAttributes(params);
        }

        dialog.show();
    }

    private void sendVerificationEmail(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(
                                requireContext(),
                                "Verifikacioni email je poslat! Provjerite svoj inbox.",
                                Toast.LENGTH_LONG
                        ).show();

                        firestore.collection("users").document(user.getUid())
                                .update("verificationRequested", true)
                                .addOnFailureListener(e -> Log.e(TAG, "Firestore update error", e));
                    } else {
                        Toast.makeText(
                                requireContext(),
                                "Greška: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void showInstructionSnackbar() {
        Snackbar.make(requireView(),
                        "Molimo odaberite namjenu korištenja laptopa i kliknite Analiziraj",
                        Snackbar.LENGTH_INDEFINITE)
                .setAction("OK", v -> {
                })
                .show();
    }

    private void setupBackButtonHandling() {
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        showLogoutConfirmationDialog();
                    }
                }
        );
    }

    private Map<String, String> getBrandTitleMap() {
        Map<String, String> brandMap = new HashMap<>();

        brandMap.put("acer", "🧩 Acer – Svestrani izbor za svakodnevnicu");
        brandMap.put("acer_predator", "🐉 Predator – Gdje snaga sretne stil");
        brandMap.put("acer_nitro", "💣 Nitro – Gaming bez kompromisa");

        brandMap.put("apple", "🍏 Apple – Čista elegancija i fluidnost");

        brandMap.put("asus", "🔧 ASUS – Tehnološka pouzdanost");
        brandMap.put("asus_rog", "🎮 ROG – Igra počinje ovdje");
        brandMap.put("asus_tuf_gaming", "🛡️ TUF – Građeni da traju");
        brandMap.put("asus_zenbook", "🧘 Zenbook – Tišina, stil i snaga");

        brandMap.put("dell", "🖥️ Dell – Stabilnost na svakom zadatku");
        brandMap.put("dell_xps", "✨ XPS – Gdje se dizajn susreće s moći");
        brandMap.put("dell_alienware", "👾 Alienware – Iz druge galaksije");

        brandMap.put("gigabyte", "⚡ Gigabyte – Performanse bez pauze");
        brandMap.put("gigabyte_aero", "🎬 Aero – Za kreativce u pokretu");
        brandMap.put("gigabyte_aorus", "🦅 AORUS – Gaming uz uzlet");

        brandMap.put("hp", "🏢 HP – Uvijek korak ispred u poslovanju");
        brandMap.put("hp_omen", "🕹️ OMEN – Igrajte kao profesionalac");
        brandMap.put("hp_victus", "🎯 Victus – Gaming za sve generacije");

        brandMap.put("lenovo", "🗂️ Lenovo – Radna stanica u pokretu");
        brandMap.put("lenovo_thinkpad", "🧠 ThinkPad – Klasa za sebe");
        brandMap.put("lenovo_yoga", "🌀 Yoga – Savitljivost bez granica");
        brandMap.put("lenovo_legion", "⚔️ Legion – Gaming bez milosti");

        brandMap.put("lg_gram", "🌬️ LG Gram – Lagan kao pero, snažan iznutra");

        brandMap.put("microsoft_surface", "🖋️ Surface – Pisanje, crtanje, stvaranje");

        brandMap.put("msi", "🎛️ MSI – Sve za performanse");
        brandMap.put("msi_gaming", "🧨 MSI Gaming – Maksimalna snaga za igrače");

        brandMap.put("razer", "🕷️ Razer – Preciznost u svakom pokretu");

        brandMap.put("samsung_galaxy_book", "🌌 Galaxy Book – Vizija budućnosti");

        brandMap.put("xmg", "🧱 XMG – Građen po tvojoj mjeri");

        return brandMap;
    }

    private String getSearchKeywordForBrand(String brand) {
        switch (brand) {
            case "acer_predator": return "predator";
            case "acer_nitro": return "nitro";
            case "asus_rog": return "rog";
            case "asus_tuf_gaming": return "tuf";
            case "asus_zenbook": return "zenbook";
            case "dell_xps": return "xps";
            case "dell_alienware": return "alienware";
            case "gigabyte_aero": return "aero";
            case "gigabyte_aorus": return "aorus";
            case "hp_omen": return "omen";
            case "hp_victus": return "victus";
            case "lenovo_thinkpad": return "thinkpad";
            case "lenovo_yoga": return "yoga";
            case "lenovo_legion": return "legion";
            case "msi_gaming": return "msi"; // Uključuje sve MSI
            default: return brand.split("_")[0]; // Glavni brend
        }
    }

    private void startSmoothAutoScroll() {
        if (hotDealsAdapter == null || hotDealsAdapter.getRealItemCount() == 0) {
            return;
        }

        if (scrollAnimator != null && scrollAnimator.isRunning()) {
            scrollAnimator.cancel();
        }

        scrollAnimator = ValueAnimator.ofInt(0, 1);
        scrollAnimator.setRepeatCount(ValueAnimator.INFINITE);
        scrollAnimator.setInterpolator(new LinearInterpolator());
        scrollAnimator.setDuration(10000);

        scrollAnimator.addUpdateListener(animation -> {
            rvHotDeals.scrollBy(scrollDirection * 2, 0);
        });

        LinearLayoutManager layoutManager = (LinearLayoutManager) rvHotDeals.getLayoutManager();
        int initialPosition = Integer.MAX_VALUE / 2;
        int startPosition = initialPosition - (initialPosition % hotDealsAdapter.getRealItemCount());

        layoutManager.scrollToPosition(startPosition);

        scrollAnimator.start();
    }

    private void showLogoutConfirmationDialog() {
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Potvrda odjave")
                .setMessage("Da li ste sigurni da želite da se odjavite?")
                .setPositiveButton("Odjavi se", (d, which) -> {
                    auth.signOut();
                    startActivity(new Intent(requireActivity(), LoginActivity.class));
                    requireActivity().finish();
                })
                .setNegativeButton("Odustani", (d, which) -> d.dismiss())
                .setCancelable(false)
                .create();

        dialog.show();

        Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        if(positive != null) positive.setTextColor(ContextCompat.getColor(requireContext(), R.color.secondary));
        if(negative != null) negative.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
    }

    @Override
    public void onResume() {
        super.onResume();
        startPeriodicCreditUpdates();

        if (scrollAnimator != null && scrollAnimator.isPaused()) {
            scrollAnimator.resume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopPeriodicCreditUpdates();

        if (scrollAnimator != null && scrollAnimator.isRunning()) {
            scrollAnimator.pause();
        }
    }

    private void stopPeriodicCreditUpdates() {
        if (creditUpdateHandler != null && creditUpdateRunnable != null) {
            creditUpdateHandler.removeCallbacks(creditUpdateRunnable);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (scrollAnimator != null) {
            scrollAnimator.cancel();
            scrollAnimator = null;
        }

        if (favoriteBrandAnimator != null) {
            favoriteBrandAnimator.cancel();
            favoriteBrandAnimator = null;
        }
        
        if (logoPulseAnimator != null && logoPulseAnimator.isRunning()) {
            logoPulseAnimator.cancel();
        }

        if (welcomeCardPressAnimator != null && welcomeCardPressAnimator.isRunning()) {
            welcomeCardPressAnimator.cancel();
        }

        stopPeriodicCreditUpdates();

        if (scrollAnimator != null) {
            scrollAnimator.cancel();
        }
        FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
    }
}