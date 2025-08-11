package com.example.techanalysisapp3.ui.activities;

import static com.example.techanalysisapp3.util.AppConstants.OLX_API_TOKEN;
import static com.example.techanalysisapp3.util.AppConstants.OPENROUTER_API_KEY;
import static com.example.techanalysisapp3.util.AppConstants.RAWG_API_KEY;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.example.techanalysisapp3.R;
import com.example.techanalysisapp3.model.FunnyMessages;
import com.example.techanalysisapp3.model.HotDeal;
import com.example.techanalysisapp3.model.Listing;
import com.example.techanalysisapp3.network.OlxApiService;
import com.example.techanalysisapp3.ui.fragments.ConfirmAnalysisFragment;
import com.example.techanalysisapp3.ui.fragments.ConfirmListingFragment;
import com.example.techanalysisapp3.ui.fragments.GameSelectionFragment;
import com.example.techanalysisapp3.ui.fragments.PrimaryPurposeFragment;
import com.example.techanalysisapp3.ui.fragments.SecondaryFocusFragment;
import com.example.techanalysisapp3.ui.fragments.VisualAnalysisFragment;
import com.example.techanalysisapp3.util.BadgeUtils;
import com.example.techanalysisapp3.util.PromptBuilder;
import com.example.techanalysisapp3.util.UrlUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ShareProcessActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private ShareProcessAdapter adapter;
    private Button btnNext, btnBack;
    private TextView tvStepTitle;
    private String olxUrl;
    private Listing listing;

    private String selectedPurpose = "🧰 Svakodnevna upotreba";
    private String selectedSecondary = "⚖️ Uravnoteženo";
    private int selectedGameId = -1;
    private String selectedGameName = "";
    public boolean visualAnalysisEnabled = false;

    private Dialog progressDialog;
    private TextView progressMessage;
    private Handler msgHandler;
    private int msgIndex = 0;

    public int freeUsed = 0;

    public void setSelectedPurpose(String purpose) {
        this.selectedPurpose = purpose;
    }

    public void setSelectedSecondary(String secondary) {
        this.selectedSecondary = secondary;
    }

    public void setSelectedGameId(int gameId) {
        this.selectedGameId = gameId;
    }

    public void setSelectedGameName(String gameName) {
        this.selectedGameName = gameName;
    }

    public void setVisualAnalysisEnabled(boolean enabled) {
        this.visualAnalysisEnabled = enabled;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_process);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            Long f = doc.getLong("freeTierUsed");
                            freeUsed = (f != null) ? f.intValue() : 0;
                        }
                    });
        }

        olxUrl = getIntent().getStringExtra("olx_url");

        viewPager = findViewById(R.id.viewPager);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);
        tvStepTitle = findViewById(R.id.tvStepTitle);

        viewPager.setUserInputEnabled(false);
        viewPager.setPageTransformer(new MarginPageTransformer(32));

        setupProgressDialog();
        fetchListingData();
    }

    private void fetchListingData() {
        showProgress();
        String cleanUrl = UrlUtils.extractAndFormatUrl(olxUrl);
        if (cleanUrl == null) {
            showErrorAndFinish("Neispravan format URL-a. Molimo unesite ispravan OLX.ba URL za laptop.");
            return;
        }

        olxUrl = cleanUrl;
        String id = olxUrl.substring(olxUrl.lastIndexOf('/') + 1);

        Log.d("ShareProcess", "Preuzimanje oglasa za ID: " + id);

        OlxApiService.getInstance().getListing("Bearer " + OLX_API_TOKEN, id)
                .enqueue(new retrofit2.Callback<Listing>() {
                    @Override
                    public void onResponse(retrofit2.Call<Listing> call, retrofit2.Response<Listing> response) {
                        hideProgress();
                        if (response.isSuccessful() && response.body() != null) {
                            listing = response.body();

                            if (listing.category_id != 39) {
                                showErrorAndFinish("Molimo unesite ispravan URL za laptop sa OLX.ba!");
                                return;
                            }

                            setupViewPager();
                        } else {
                            String errorMsg = "Neispravan oglas";
                            if (response.errorBody() != null) {
                                try {
                                    errorMsg = response.errorBody().string();
                                } catch (IOException e) {
                                    Log.e("ShareProcess", "Greška pri čitanju tijela greške", e);
                                }
                            }
                            showErrorAndFinish("Greška API-ja: " + response.code() + " - " + errorMsg);
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<Listing> call, Throwable t) {
                        hideProgress();
                        Log.e("ShareProcess", "Mrežna greška", t);
                        showErrorAndFinish("Greška pri povezivanju: " + t.getMessage());
                    }
                });
    }

    private void setupViewPager() {
        adapter = new ShareProcessAdapter(this, listing);
        viewPager.setAdapter(adapter);

        btnNext.setOnClickListener(v -> {
            int currentItem = viewPager.getCurrentItem();
            if (currentItem < adapter.getItemCount() - 1) {
                viewPager.setCurrentItem(currentItem + 1, true);
            } else {
                startAnalysis();
            }
        });

        btnBack.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() > 0) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
            } else {
                finish();
            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateUI(position);
            }
        });

        updateUI(0);
    }

    private void updateUI(int position) {
        tvStepTitle.setText(adapter.getPageTitle(position));
        btnBack.setVisibility(position > 0 ? View.VISIBLE : View.GONE);
        btnNext.setText(position == adapter.getItemCount() - 1 ? "Pokreni Analizu" : "Nastavi");
    }

    private void startAnalysis() {
        showProgress();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            showErrorAndFinish("User not authenticated");
            return;
        }

        final String uid = firebaseUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(userDoc -> {
                    if (!userDoc.exists()) {
                        showErrorAndFinish("User data not found");
                        return;
                    }

                    final String gender = userDoc.getString("gender");
                    final String firstName = userDoc.getString("firstName");
                    final String favoriteBrand = userDoc.getString("favoriteBrand");
                    final Long creditsLong = userDoc.getLong("credits");
                    final Long freeTierUsedLong = userDoc.getLong("freeTierUsed");
                    final Long lastFreeDateLong = userDoc.getLong("lastFreeTierDate");

                    final long credits = creditsLong != null ? creditsLong : 0;
                    final int freeUsed = freeTierUsedLong != null ? freeTierUsedLong.intValue() : 0;
                    final long lastFreeDate = lastFreeDateLong != null ? lastFreeDateLong : 0;

                    final int requiredCredits = visualAnalysisEnabled ? 2 : 1;
                    final boolean isNewDay = isNewDay(lastFreeDate);
                    final boolean isFree = isNewDay || freeUsed < 3;

                    if (!isFree && credits < requiredCredits) {
                        hideProgress();
                        showNotEnoughCreditsError();
                        return;
                    }

                    // Handle visual analysis if enabled
                    if (visualAnalysisEnabled) {
                        List<String> imageUrls = listing.images != null ?
                                new ArrayList<>(listing.images) :
                                new ArrayList<>();

                        analyzeImages(imageUrls, new VisionAnalysisCallback() {
                            @Override
                            public void onSuccess(String analysisResult) {
                                // Continue with analysis after getting visual results
                                proceedWithAnalysis(
                                        gender, firstName, favoriteBrand,
                                        freeUsed, credits, requiredCredits, isFree,
                                        analysisResult
                                );
                            }

                            @Override
                            public void onFailure(String error) {
                                Log.e("VisualAnalysis", "Visual analysis failed: " + error);
                                // Proceed without visual analysis
                                proceedWithAnalysis(
                                        gender, firstName, favoriteBrand,
                                        freeUsed, credits, requiredCredits, isFree,
                                        ""
                                );
                            }
                        });
                    } else {
                        // Proceed without visual analysis
                        proceedWithAnalysis(
                                gender, firstName, favoriteBrand,
                                freeUsed, credits, requiredCredits, isFree,
                                ""
                        );
                    }
                })
                .addOnFailureListener(e -> {
                    hideProgress();
                    showErrorAndFinish("Error fetching user data: " + e.getMessage());
                });
    }

    // Helper method to handle analysis after visual processing
    private void proceedWithAnalysis(
            String gender, String firstName, String favoriteBrand,
            int freeUsed, long credits, int requiredCredits, boolean isFree,
            String visualAnalysisResult
    ) {
        if (selectedGameId != -1) {
            fetchGameDetails(selectedGameId, (minReq, recReq) -> {
                buildAndSendPrompt(
                        gender, firstName, favoriteBrand,
                        freeUsed, credits, requiredCredits, isFree,
                        minReq, recReq,
                        visualAnalysisResult
                );
            });
        } else {
            buildAndSendPrompt(
                    gender, firstName, favoriteBrand,
                    freeUsed, credits, requiredCredits, isFree,
                    "", "",
                    visualAnalysisResult
            );
        }
    }

    private float parseRatingFromAnalysis(String analysis) {
        try {
            Pattern headerPattern = Pattern.compile(
                    "🎯 Ukupna ocjena\\s*(\\(1-10\\))?:\\s*",
                    Pattern.CASE_INSENSITIVE
            );

            String[] parts = headerPattern.split(analysis, 2);
            if(parts.length < 2) return -1;

            String ratingSection = parts[1];

            Pattern ratingPattern = Pattern.compile(
                    "(\\d+\\.?\\d*)\\s*/\\s*10\\b",
                    Pattern.DOTALL
            );

            Matcher matcher = ratingPattern.matcher(ratingSection);
            if(matcher.find()) {
                return Float.parseFloat(matcher.group(1));
            }

            return -1;
        } catch (Exception e) {
            Log.e("RATING_PARSE", "Greška pri parsiranju ocjene: " + analysis, e);
            return -1;
        }
    }

    private void updateHotDeals(String aiAnalysis, Listing listing, String url) {
        try {
            final float rating = Math.max(parseRatingFromAnalysis(aiAnalysis), 0f);

            Calendar calStart = Calendar.getInstance();
            calStart.setFirstDayOfWeek(Calendar.MONDAY);
            calStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            calStart.set(Calendar.HOUR_OF_DAY, 0);
            calStart.set(Calendar.MINUTE, 0);
            calStart.set(Calendar.SECOND, 0);
            calStart.set(Calendar.MILLISECOND, 0);
            final Date weekStart = calStart.getTime();

            Calendar calEnd = (Calendar) calStart.clone();
            calEnd.add(Calendar.DATE, 6);
            calEnd.set(Calendar.HOUR_OF_DAY, 23);
            calEnd.set(Calendar.MINUTE, 59);
            calEnd.set(Calendar.SECOND, 59);
            calEnd.set(Calendar.MILLISECOND, 999);
            final Date weekEnd = calEnd.getTime();

            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            final String weekId = sdf.format(weekStart) + " - " + sdf.format(weekEnd);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            final DocumentReference weekDocRef = db
                    .collection("weekly_hot_deals")
                    .document(weekId);
            final CollectionReference dealsRef = weekDocRef.collection("deals");

            Map<String, Object> weekMeta = new HashMap<>();
            weekMeta.put("startDate", weekStart);
            weekMeta.put("endDate", weekEnd);
            weekDocRef.set(weekMeta, SetOptions.merge());

            FirebaseUser authUser = FirebaseAuth.getInstance().getCurrentUser();
            if (authUser == null) {
                Log.e("HotDeals", "Nema prijavljenog korisnika");
                return;
            }
            final String uid = authUser.getUid();

            db.collection("users").document(uid)
                    .get()
                    .addOnSuccessListener(userDoc -> {
                        String username = userDoc.getString("username");
                        if (username == null || username.isEmpty()) {
                            username = authUser.getDisplayName() != null
                                    ? authUser.getDisplayName()
                                    : "Nepoznati korisnik";
                        }

                        final HotDeal newDeal = new HotDeal();
                        newDeal.setOlxTitle(listing.title != null ? listing.title : "Nepoznat naslov");
                        newDeal.setOlxUrl(url);
                        newDeal.setUsername(username);
                        newDeal.setImageUrls(listing.images != null ? listing.images : new ArrayList<>());
                        newDeal.setAnalysisText(aiAnalysis);
                        newDeal.setRating(rating);
                        newDeal.setTimestamp(new Date());

                        if (listing.user != null && listing.user.location != null) {
                            newDeal.setCityName(listing.user.location.name);
                            newDeal.setLatitude(String.valueOf(listing.user.location.coordinates.latitude));
                            newDeal.setLongitude(String.valueOf(listing.user.location.coordinates.longitude));
                        }

                        dealsRef.orderBy("rating", Query.Direction.DESCENDING)
                                .limit(5)
                                .get()
                                .addOnSuccessListener(snapshot -> {
                                    List<DocumentSnapshot> docs = snapshot.getDocuments();
                                    if (docs.size() < 5) {
                                        dealsRef.add(newDeal)
                                                .addOnSuccessListener(r -> Log.d("HotDeals", "Novi deal dodan"))
                                                .addOnFailureListener(ex -> Log.e("HotDeals", "Greška pri dodavanju", ex));
                                    } else {
                                        HotDeal lowest = null;
                                        DocumentReference lowestRef = null;
                                        for (DocumentSnapshot doc : docs) {
                                            HotDeal hd = doc.toObject(HotDeal.class);
                                            if (lowest == null || hd.getRating() < lowest.getRating()) {
                                                lowest = hd;
                                                lowestRef = doc.getReference();
                                            }
                                        }
                                        if (lowest != null && newDeal.getRating() > lowest.getRating()) {
                                            lowestRef.delete()
                                                    .addOnSuccessListener(aVoid ->
                                                            dealsRef.add(newDeal)
                                                                    .addOnSuccessListener(r -> Log.d("HotDeals", "Deal zamijenjen"))
                                                                    .addOnFailureListener(ex2 -> Log.e("HotDeals", "Greška pri dodavanju zamjene", ex2))
                                                    )
                                                    .addOnFailureListener(ex3 -> Log.e("HotDeals", "Greška pri brisanju slabijeg", ex3));
                                        } else {
                                            Log.d("HotDeals", "Novi deal nije dovoljno dobar");
                                        }
                                    }
                                })
                                .addOnFailureListener(ex4 -> Log.e("HotDeals", "Greška pri dohvatu postojećih dealova", ex4));

                    })
                    .addOnFailureListener(e -> Log.e("HotDeals", "Greška pri dohvatu korisničkog dokumenta", e));

        } catch (Exception e) {
            Log.e("HotDeals", "Greška u metodi updateHotDeals", e);
        }
    }

    private void updateUserAnalysisCount() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) return;
        final String uid = firebaseUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 1) Compute current week start/end
        Calendar calStart = Calendar.getInstance();
        calStart.setFirstDayOfWeek(Calendar.MONDAY);
        calStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calStart.set(Calendar.HOUR_OF_DAY, 0);
        calStart.set(Calendar.MINUTE, 0);
        calStart.set(Calendar.SECOND, 0);
        calStart.set(Calendar.MILLISECOND, 0);
        final Date weekStart = calStart.getTime();

        Calendar calEnd = (Calendar) calStart.clone();
        calEnd.add(Calendar.DATE, 6);
        calEnd.set(Calendar.HOUR_OF_DAY, 23);
        calEnd.set(Calendar.MINUTE, 59);
        calEnd.set(Calendar.SECOND, 59);
        calEnd.set(Calendar.MILLISECOND, 999);
        final Date weekEnd = calEnd.getTime();

        // 2) Format week ID
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        final String weekId = sdf.format(weekStart) + " - " + sdf.format(weekEnd);

        // 3) Ensure week metadata exists
        DocumentReference weekDocRef = db.collection("weekly_user_stats")
                .document(weekId);
        Map<String, Object> weekMeta = new HashMap<>();
        weekMeta.put("startDate", weekStart);
        weekMeta.put("endDate", weekEnd);
        weekDocRef.set(weekMeta, SetOptions.merge());

        // 4) Fetch username
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(userDoc -> {
                    Long totalAnalysis = userDoc.getLong("totalAnalysisCount");
                    int total = totalAnalysis != null ? totalAnalysis.intValue() : 0;
                    String fetched = userDoc.getString("username");
                    final String username = (fetched != null && !fetched.isEmpty())
                            ? fetched
                            : (firebaseUser.getDisplayName() != null
                            ? firebaseUser.getDisplayName()
                            : "Anoniman");

                    // 5) Increment weekly count
                    DocumentReference userWeekRef = weekDocRef
                            .collection("top_users")
                            .document(uid);
                    userWeekRef.get()
                            .addOnSuccessListener(snap -> {
                                long newWeekly = 1;
                                if (snap.exists()) {
                                    Long old = snap.getLong("count");
                                    newWeekly = (old != null ? old : 0) + 1;
                                }
                                Map<String, Object> weekData = new HashMap<>();
                                weekData.put("username", username);
                                weekData.put("count", newWeekly);
                                userWeekRef.set(weekData, SetOptions.merge())
                                        .addOnSuccessListener(v -> {
                                            db.collection("users").document(uid)
                                                    .update("totalAnalysisCount", FieldValue.increment(1))
                                                    .addOnSuccessListener(aVoid -> {
                                                    });
                                        });
                            });
                })
                .addOnFailureListener(e ->
                        Log.e("UserStats", "Failed to fetch user data", e)
                );
    }

    private void buildAndSendPrompt(
            String gender, String firstName, String favoriteBrand,
            int freeUsed, long credits, int requiredCredits, boolean isFree,
            String minReq, String recReq,
            String visualAnalysisResult  // Add this parameter
    ) {
        String descriptionText = "";
        if (listing.additional != null &&
                listing.additional.description != null) {
            descriptionText = Jsoup.parse(listing.additional.description).text();
        }

        String promptContent = PromptBuilder.buildPrompt(
                listing,
                descriptionText,
                selectedPurpose,
                selectedSecondary,
                visualAnalysisResult,  // Pass visual analysis result here
                visualAnalysisEnabled,  // Pass the enabled flag
                selectedGameId,
                selectedGameName,
                minReq,
                recReq,
                gender,
                firstName,
                favoriteBrand
        );

        callOpenRouterAnalysis(
                promptContent,
                requiredCredits,
                isFree,
                freeUsed,
                credits
        );
    }

    private void callOpenRouterAnalysis(
            String promptContent,
            int requiredCredits,
            boolean isFree,
            int freeUsed,
            long credits
    ) {
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        try {
            JSONObject json = new JSONObject();
            json.put("model", "tngtech/deepseek-r1t2-chimera:free");

            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", promptContent);
            messages.put(message);

            json.put("messages", messages);

            RequestBody body = RequestBody.create(JSON, json.toString());
            Request request = new Request.Builder()
                    .url("https://openrouter.ai/api/v1/chat/completions")
                    .addHeader("Authorization", "Bearer " + OPENROUTER_API_KEY)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        hideProgress();
                        showErrorAndFinish("Network error: " + e.getMessage());
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String respBody = response.body() != null ? response.body().string() : "";
                    runOnUiThread(() -> {
                        hideProgress();

                        if (!response.isSuccessful()) {
                            showErrorAndFinish("API error: " + response.code() + " - " + respBody);
                            return;
                        }

                        try {
                            JSONObject root = new JSONObject(respBody);
                            JSONArray choices = root.getJSONArray("choices");
                            JSONObject firstChoice = choices.getJSONObject(0);
                            JSONObject message = firstChoice.getJSONObject("message");
                            String aiContent = message.getString("content");

                            float rating = parseRatingFromAnalysis(aiContent);

                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (user != null) {
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                db.collection("users").document(user.getUid()).get()
                                        .addOnSuccessListener(userDoc -> {
                                            if (userDoc.exists()) {
                                                List<String> badges = (List<String>) userDoc.get("badges");
                                                boolean hasHighRatingBadge = badges != null &&
                                                        badges.contains(BadgeUtils.BADGE_HIGH_RATING);

                                                if (!hasHighRatingBadge && rating >= 0) {
                                                    addRatingToBadgeArraylist(user.getUid(), rating);
                                                }
                                            }
                                        });
                            }

                            updateUserCredits(requiredCredits, isFree, freeUsed);

                            updateUserAnalysisCount();
                            updateHotDeals(aiContent, listing, olxUrl);

                            startAnalysisSwipeActivity(aiContent);

                        } catch (JSONException e) {
                            showErrorAndFinish("JSON parsing error: " + e.getMessage());
                        }
                    });
                }
            });

        } catch (JSONException e) {
            hideProgress();
            showErrorAndFinish("JSON error: " + e.getMessage());
        }
    }

    private void addRatingToBadgeArraylist(String uid, float rating) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(uid)
                .update("badgeArraylist", FieldValue.arrayUnion(rating))
                .addOnFailureListener(e ->
                        Log.e("BadgeUpdate", "Error adding rating to badge list", e)
                );
    }

    private void updateUserCredits(int requiredCredits, boolean isFree, int freeUsed) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> updates = new HashMap<>();

        if (isFree) {
            updates.put("freeTierUsed", freeUsed + 1);
        } else {
            updates.put("credits", FieldValue.increment(-requiredCredits));
        }

        db.collection("users").document(user.getUid())
                .update(updates)
                .addOnFailureListener(e ->
                        Log.e("Credits", "Failed to update credits", e)
                );
    }

    private void startAnalysisSwipeActivity(String aiContent) {
        Intent intent = new Intent(this, AnalysisSwipeActivity.class);
        intent.putExtra("ai_analysis", aiContent);
        intent.putExtra("olx_url", olxUrl);
        intent.putExtra("olx_title", listing.title);

        float rating = parseRatingFromAnalysis(aiContent);
        intent.putExtra("rating", rating);

        if (listing.images != null && !listing.images.isEmpty()) {
            intent.putStringArrayListExtra("image_list", new ArrayList<>(listing.images));
        }

        if (listing.user != null &&
                listing.user.location != null &&
                listing.user.location.coordinates != null) {
            intent.putExtra("city_name", listing.user.location.name);
            intent.putExtra("latitude", listing.user.location.coordinates.latitude);
            intent.putExtra("longitude", listing.user.location.coordinates.longitude);
        }

        startActivity(intent);
        finish();
    }

    private boolean isNewDay(long lastTs) {
        if (lastTs == 0) return true;

        Calendar lastCal = Calendar.getInstance();
        lastCal.setTimeInMillis(lastTs);

        Calendar now = Calendar.getInstance();

        return lastCal.get(Calendar.YEAR) != now.get(Calendar.YEAR) ||
                lastCal.get(Calendar.DAY_OF_YEAR) != now.get(Calendar.DAY_OF_YEAR);
    }

    private void fetchGameDetails(int gameId, GameDetailsCallback callback) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://api.rawg.io/api/games/" + gameId + "?key=" + RAWG_API_KEY;

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    callback.onDetailsFetched(
                            "Greška pri povezivanju sa serverom",
                            "Greška pri povezivanju sa serverom"
                    );
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String minReq = "Minimalne specifikacije nisu dostupne";
                String recReq = "Preporučene specifikacije nisu dostupne";

                if (response.isSuccessful()) {
                    try {
                        String json = response.body().string();
                        JSONObject gameDetails = new JSONObject(json);
                        JSONArray platforms = gameDetails.getJSONArray("platforms");

                        for (int i = 0; i < platforms.length(); i++) {
                            JSONObject platform = platforms.getJSONObject(i);
                            JSONObject platformObj = platform.getJSONObject("platform");
                            String platformName = platformObj.getString("name");

                            if ("PC".equalsIgnoreCase(platformName)) {
                                JSONObject requirements = platform.optJSONObject("requirements");
                                if (requirements != null) {
                                    minReq = requirements.optString("minimum", minReq)
                                            .replaceAll("<br\\s*/?>", "\n")
                                            .replaceAll("<[^>]+>", "");

                                    recReq = requirements.optString("recommended", recReq)
                                            .replaceAll("<br\\s*/?>", "\n")
                                            .replaceAll("<[^>]+>", "");
                                }
                                break;
                            }
                        }
                    } catch (Exception e) {
                        Log.e("GameDetails", "Parsing error", e);
                    }
                }

                final String finalMinReq = minReq;
                final String finalRecReq = recReq;
                runOnUiThread(() -> callback.onDetailsFetched(finalMinReq, finalRecReq));
            }
        });
    }

    interface GameDetailsCallback {
        void onDetailsFetched(String minReq, String recReq);
    }

    interface VisionAnalysisCallback {
        void onSuccess(String analysis);
        void onFailure(String error);
    }

    private void analyzeImages(List<String> imageUrls, VisionAnalysisCallback callback) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            callback.onSuccess("");
            return;
        }

        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        try {
            String textPrompt =
                    "Act as senior laptop inspection engineer. Follow this workflow:\n\n"
                            + "1. COMPONENT SCAN:\n"
                            + "- Identify all visible components with model numbers\n"
                            + "- Note any missing/extra parts compared to standard config\n\n"
                            + "2. CONDITION GRADING (A=Excellent, F=Failed):\n"
                            + "A) Display: Backlight uniformity | Pixel defects | Coating wear\n"
                            + "B) Keyboard: Keycap wear level (1-5) | Missing keys | LED function\n"
                            + "C) Ports: Physical damage | Connector tightness | Corrosion\n"
                            + "D) Chassis: Structural integrity | Panel gaps | Paint condition\n"
                            + "E) Hinges: Smoothness (1-5 scale) | Screen wobble\n\n"
                            + "3. AUTHENTICITY CHECK:\n"
                            + "- Verify OEM markings/stickers\n"
                            + "- Check for inconsistent wear patterns\n"
                            + "- Identify aftermarket modifications\n\n"
                            + "4. RISK ASSESSMENT:\n"
                            + "- Immediate safety hazards\n"
                            + "- Potential future failures\n\n"
                            + "RESPONSE FORMAT (STRICTLY FOLLOW):\n"
                            + "1. [Component Type] | Condition: [Grade] | Findings: [Details]\n"
                            + "2. Authenticity: [OEM/Aftermarket]\n"
                            + "3. Critical Issues: [List]\n"
                            + "4. Recommended Actions\n"
                            + "NOTE: Specify when image quality limits assessment\n\n"
                            + "ANALYSIS IMAGES:";

            StringBuilder modelPrompt = new StringBuilder(textPrompt);

            JSONArray contentArray = new JSONArray();
            contentArray.put(new JSONObject()
                    .put("type", "text")
                    .put("text", textPrompt));

            int maxImages = Math.min(imageUrls.size(), 5);
            for (int i = 0; i < maxImages; i++) {
                String url = imageUrls.get(i);

                modelPrompt.append("\nSlika ").append(i + 1).append(": ").append(url);

                contentArray.put(new JSONObject()
                        .put("type", "image_url")
                        .put("image_url", new JSONObject().put("url", url)));
            }

            Log.d("ModelPrompt", modelPrompt.toString());

            JSONArray messages = new JSONArray();
            messages.put(new JSONObject()
                    .put("role", "user")
                    .put("content", contentArray));

            JSONObject json = new JSONObject();
            json.put("model", "qwen/qwen2.5-vl-72b-instruct:free");
            json.put("messages", messages);

            RequestBody body = RequestBody.create(JSON, json.toString());
            Request request = new Request.Builder()
                    .url("https://openrouter.ai/api/v1/chat/completions")
                    .addHeader("Authorization", "Bearer " + OPENROUTER_API_KEY)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure("Vision analysis failed: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        if (!response.isSuccessful()) {
                            callback.onFailure("HTTP error: " + response.code());
                            return;
                        }

                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        String analysis = jsonResponse.getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content");
                        callback.onSuccess(analysis);
                    } catch (Exception e) {
                        callback.onFailure("Analysis parsing error: " + e.getMessage());
                    }
                }
            });

        } catch (JSONException e) {
            callback.onFailure("JSON error: " + e.getMessage());
        }
    }

    private void showNotEnoughCreditsError() {
        new AlertDialog.Builder(this)
                .setTitle("Nedovoljno kredita")
                .setMessage("Nemate dovoljno kredita za ovu analizu.")
                .setPositiveButton("U redu", null)
                .show();
    }

    private void showErrorAndFinish(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Ups!")
                .setMessage("Nije moguće analizirati ovu objavu:\n\n" + message +
                        "\n\nMolimo provjerite URL i pokušajte ponovo.")
                .setPositiveButton("U redu", (d, w) -> finish())
                .show();
    }

    public void goToNext() {
        int currentItem = viewPager.getCurrentItem();
        if (currentItem < adapter.getItemCount() - 1) {
            viewPager.setCurrentItem(currentItem + 1, true);
        }
    }

    public static class ShareProcessAdapter extends FragmentStateAdapter {
        private final Listing listing;
        private final String[] titles = {
                "Potvrdi oglas",
                "Primarna namjena",
                "Sekundarni fokus",
                "Odabir igre",
                "Vizualna analiza",
                "Pokreni analizu"
        };

        public ShareProcessAdapter(FragmentActivity fa, Listing listing) {
            super(fa);
            this.listing = listing;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0: return ConfirmListingFragment.newInstance(listing);
                case 1: return new PrimaryPurposeFragment();
                case 2: return new SecondaryFocusFragment();
                case 3: return new GameSelectionFragment();
                case 4: return new VisualAnalysisFragment();
                case 5: return new ConfirmAnalysisFragment();
                default: return new Fragment();
            }
        }

        @Override
        public int getItemCount() {
            return titles.length;
        }

        public String getPageTitle(int position) {
            return titles[position];
        }
    }

    private void setupProgressDialog() {
        progressDialog = new Dialog(this);
        progressDialog.setContentView(R.layout.dialog_progress);
        progressDialog.setCancelable(false);

        Window window = progressDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.gravity = Gravity.CENTER;
            window.setAttributes(params);
        }
        progressMessage = progressDialog.findViewById(R.id.progressMessage);
    }

    private void showProgress() {
        if (progressDialog == null) {
            setupProgressDialog();
        }

        if (!progressDialog.isShowing()) {
            String[] funnyMessages = FunnyMessages.funnyMessages;
            List<String> messagesList = Arrays.asList(funnyMessages);
            Collections.shuffle(messagesList);
            final String[] messages = messagesList.toArray(new String[0]);

            progressMessage.setText(messages[0]);
            progressDialog.show();

            msgHandler = new Handler();
            msgIndex = 0;

            msgHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    msgIndex = (msgIndex + 1) % messages.length;
                    progressMessage.animate().alpha(0).setDuration(500).withEndAction(() -> {
                        progressMessage.setText(messages[msgIndex]);
                        progressMessage.animate().alpha(1).setDuration(500);
                    });
                    msgHandler.postDelayed(this, 4500);
                }
            }, 4500);
        }
    }

    private void hideProgress() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            if (msgHandler != null) {
                msgHandler.removeCallbacksAndMessages(null);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (progressDialog != null && progressDialog.isShowing()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        hideProgress();
        super.onDestroy();
    }
}