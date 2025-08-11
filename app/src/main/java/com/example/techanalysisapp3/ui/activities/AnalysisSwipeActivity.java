package com.example.techanalysisapp3.ui.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.techanalysisapp3.ui.adapters.AnalysisPagerAdapter;
import com.example.techanalysisapp3.ui.components.FlipPageTransformer;
import com.example.techanalysisapp3.R;
import com.example.techanalysisapp3.model.CardData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalysisSwipeActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private AnalysisPagerAdapter adapter;
    private List<CardData> cardDataList;
    private Button openOlxButton, saveReviewButton, exitReviewButton;
    private boolean isFromFavorites = false;
    private String documentId;

    // Firebase components
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;
    private List<String> imageUrls;
    private boolean isSaved = false;
    private String cityName, latitude, longitude;
    private static final int REQUEST_PURCHASE_SLOTS = 1;
    private static final String KEY_REVIEW_DATA = "review_data";
    private final int[] slotPackages = {10, 25, 50, 100};
    private final int[] slotPrices = {20, 45, 80, 150};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        viewPager         = findViewById(R.id.viewPager);
        openOlxButton     = findViewById(R.id.btnOpenOlx);
        saveReviewButton  = findViewById(R.id.btnSaveReview);
        exitReviewButton  = findViewById(R.id.btnExitReview);

        openOlxButton.setBackgroundTintList(
                ColorStateList.valueOf(
                        ContextCompat.getColor(this, R.color.lilac)
                )
        );
        openOlxButton.setBackgroundTintMode(PorterDuff.Mode.SRC_IN);

        exitReviewButton.setBackgroundTintList(
                ColorStateList.valueOf(
                        ContextCompat.getColor(this, R.color.secondary)
                )
        );
        exitReviewButton.setBackgroundTintMode(PorterDuff.Mode.SRC_IN);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sačuvavam recenziju...");

        Intent intent = getIntent();
        isFromFavorites = intent.getBooleanExtra("from_favorites", false);
        documentId = intent.getStringExtra("document_id");
        imageUrls = intent.getStringArrayListExtra("image_list");
        String aiText = intent.getStringExtra("ai_analysis");
        String olxUrl  = intent.getStringExtra("olx_url");
        String olxTitle= intent.getStringExtra("olx_title");
        cityName = intent.getStringExtra("city_name");
        latitude = intent.getStringExtra("latitude");
        longitude = intent.getStringExtra("longitude");

        if (isFromFavorites) {
            saveReviewButton.setText("🗑 Obriši recenziju");
            saveReviewButton.setBackgroundTintList(
                    ColorStateList.valueOf(Color.RED)
            );
            saveReviewButton.setOnClickListener(v -> showDeleteConfirmation());
        } else {
            saveReviewButton.setText("💾 Sačuvaj recenziju");
            saveReviewButton.setBackgroundTintList(
                    ColorStateList.valueOf(
                            ContextCompat.getColor(this, R.color.primary)
                    )
            );
            saveReviewButton.setOnClickListener(v -> saveReviewToFirebase());
        }

        initializeCardData(imageUrls, aiText, olxUrl);
        setupViewPager();

        setupButtonListeners(olxUrl);
    }

    private void initializeCardData(List<String> images, String aiText, String olxUrl) {
        cardDataList = new ArrayList<>();

        // Images (to 10) - original logic
        if (images != null) {
            int maxImages = Math.min(images.size(), 10);
            for (int i = 0; i < maxImages; i++) {
                cardDataList.add(new CardData(images.get(i)));
            }
        }

        if (aiText != null && !aiText.isEmpty()) {
            String[] sections = aiText.split("(?=💪|👀|💾|💻|🔋|🔌|🛡️|⚖️|💡|🎯)");
            for (String sec : sections) {
                sec = sec.trim();
                int colon = sec.indexOf(':');
                if (colon > 0) {
                    String title = sec.substring(0, colon + 1).trim();
                    String content = sec.substring(colon + 1).trim();
                    cardDataList.add(new CardData(title, content));
                }
            }
        }

        cardDataList.add(new CardData("🔗 Preporuka", "Pogledaj artikal na OLX:\n\n" + olxUrl));
    }

    private void setupViewPager() {
        adapter = new AnalysisPagerAdapter(cardDataList,
                imageUrls.size(), viewPager);
        viewPager.setAdapter(adapter);
        viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        viewPager.setPageTransformer(new FlipPageTransformer());

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                boolean isLast = position == cardDataList.size() - 1;
                openOlxButton.setVisibility(isLast ? View.VISIBLE : View.GONE);
                saveReviewButton.setVisibility(isLast ? View.VISIBLE : View.GONE);
                exitReviewButton.setVisibility(isLast ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void setupButtonListeners(String olxUrl) {
        openOlxButton.setOnClickListener(v -> {
            if (olxUrl == null || olxUrl.isEmpty()) {
                Toast.makeText(this, "URL nije dostupan", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(olxUrl));
            startActivity(browserIntent);
        });

        saveReviewButton.setOnClickListener(v -> {
            if(isSaved) {
                Toast.makeText(this, "Recenzija je već sačuvana!", Toast.LENGTH_SHORT).show();
                return;
            }

            if(isFromFavorites) {
                showDeleteConfirmation();
            } else {
                if(FirebaseAuth.getInstance().getCurrentUser() == null) {
                    Toast.makeText(this, "Morate biti prijavljeni!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, LoginActivity.class));
                } else {
                    saveReviewToFirebase();
                }
            }
        });

        exitReviewButton.setOnClickListener(v -> finish());
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Brisanje recenzije")
                .setMessage("Da li želite trajno obrisati ovu recenziju?")
                .setPositiveButton("Obriši", (dialog, which) -> deleteReview())
                .setNegativeButton("Odustani", null)
                .show();
    }

    private void deleteReview() {
        String docId = getIntent().getStringExtra("document_id");
        if(docId == null) {
            Toast.makeText(this, "Greška: Nedostaje ID recenzije", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .collection("favorites")
                .document(docId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Recenzija obrisana", Toast.LENGTH_SHORT).show();

                    // Refresh after deletion
                    Intent refresh = new Intent(this, MapActivity.class);
                    refresh.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(refresh);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Greška: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void saveReviewToFirebase() {
        if (isSaved) {
            Toast.makeText(this, "Recenzija je već sačuvana!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Bundle saved state in case activity resets itself
        Bundle savedState = new Bundle();
        savedState.putStringArrayList("image_list", new ArrayList<>(imageUrls));
        savedState.putString("ai_analysis", getIntent().getStringExtra("ai_analysis"));
        savedState.putString("olx_url", getIntent().getStringExtra("olx_url"));
        savedState.putString("olx_title", getIntent().getStringExtra("olx_title"));
        savedState.putString("city_name", cityName);
        savedState.putString("latitude", latitude);
        savedState.putString("longitude", longitude);

        getIntent().putExtra(KEY_REVIEW_DATA, savedState);

        progressDialog.show();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            progressDialog.dismiss();
            Toast.makeText(this, "Niste prijavljeni!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        db.collection("users").document(currentUser.getUid())
                .collection("favorites")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    long favoritesCount = querySnapshot.size();

                    db.collection("users").document(currentUser.getUid())
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                long purchasedSlots = documentSnapshot.getLong("purchasedSlots") != null ?
                                        documentSnapshot.getLong("purchasedSlots") : 0;
                                long maxSlots = 10 + purchasedSlots;

                                if(favoritesCount >= maxSlots) {
                                    progressDialog.dismiss();
                                    showSlotLimitDialog(favoritesCount, maxSlots); // Ovo je promijenjeno
                                    return;
                                }

                                saveReviewDocument();
                            });
                });
    }

    // Continue of saving after buying slots
    private void continueSavingAfterSlotPurchase() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("users").document(currentUser.getUid())
                .collection("favorites")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    long favoritesCount = querySnapshot.size();

                    db.collection("users").document(currentUser.getUid())
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                long purchasedSlots = documentSnapshot.getLong("purchasedSlots") != null ?
                                        documentSnapshot.getLong("purchasedSlots") : 0;
                                long maxSlots = 10 + purchasedSlots;

                                if(favoritesCount < maxSlots) {
                                    saveReviewDocument();
                                } else {
                                    showSlotLimitDialog(favoritesCount, maxSlots);
                                }
                            });
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PURCHASE_SLOTS && resultCode == RESULT_OK) {
            // Try to save after user comes back
            continueSavingAfterSlotPurchase();
        }
    }

    private void showSlotLimitDialog(long favoritesCount, long maxSlots) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    long purchasedSlots = documentSnapshot.getLong("purchasedSlots") != null ?
                            documentSnapshot.getLong("purchasedSlots") : 0;
                    long credits = documentSnapshot.getLong("credits") != null ?
                            documentSnapshot.getLong("credits") : 0;

                    ArrayList<String> options = new ArrayList<>();
                    ArrayList<Integer> availablePackages = new ArrayList<>();

                    for(int i = 0; i < slotPackages.length; i++) {
                        if(slotPackages[i] > purchasedSlots && credits >= slotPrices[i]) {
                            options.add(String.format("+%d slotova (%d kredita)",
                                    slotPackages[i], slotPrices[i]));
                            availablePackages.add(i);
                        }
                    }

                    if(options.isEmpty()) {
                        Toast.makeText(this,
                                "Nema dostupnih paketa ili nedovoljno kredita",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    new AlertDialog.Builder(this)
                            .setTitle("Odaberite paket slotova")
                            .setItems(options.toArray(new String[0]), (dialog, which) -> {
                                int packageIndex = availablePackages.get(which);
                                purchaseSlots(packageIndex, currentUser.getUid());
                            })
                            .setNegativeButton("Odustani", null)
                            .show();
                });
    }

    private void purchaseSlots(int packageIndex, String userId) {
        long price = slotPrices[packageIndex];
        long slots = slotPackages[packageIndex];

        ProgressDialog purchaseDialog = new ProgressDialog(this);
        purchaseDialog.setMessage("Obavljam kupovinu...");
        purchaseDialog.show();

        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(db.collection("users").document(userId));

            Long currentCredits = snapshot.getLong("credits");
            Long currentPurchased = snapshot.getLong("purchasedSlots");

            long credits = currentCredits != null ? currentCredits : 0;
            long purchased = currentPurchased != null ? currentPurchased : 0;

            if(credits < price) {
                throw new RuntimeException("Nedovoljno kredita");
            }
            if(purchased >= slots) {
                throw new RuntimeException("Već imate veći paket");
            }

            transaction.update(snapshot.getReference(),
                    "credits", credits - price);
            transaction.update(snapshot.getReference(),
                    "purchasedSlots", slots);

            return null;
        }).addOnSuccessListener(aVoid -> {
            purchaseDialog.dismiss();
            Toast.makeText(this, "Uspešno kupljeno " + slots + " slotova!", Toast.LENGTH_SHORT).show();
            saveReviewDocument(); // Automatski nastavi sa čuvanjem
        }).addOnFailureListener(e -> {
            purchaseDialog.dismiss();
            Toast.makeText(this, "Greška: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void saveReviewDocument() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            progressDialog.dismiss();
            Toast.makeText(this, "Niste prijavljeni!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        String analysisText = getIntent().getStringExtra("ai_analysis");
        String olxUrl = getIntent().getStringExtra("olx_url");
        String olxTitle = getIntent().getStringExtra("olx_title");

        if(analysisText == null || olxUrl == null || olxTitle == null) {
            progressDialog.dismiss();
            Toast.makeText(this, "Nedostaju podaci za čuvanje!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> review = new HashMap<>();
        review.put("userId", currentUser.getUid());
        review.put("olxTitle", olxTitle);
        review.put("analysisText", analysisText);
        review.put("imageUrls", imageUrls != null ? imageUrls : new ArrayList<>());
        review.put("olxUrl", olxUrl);
        review.put("timestamp", new Date());
        review.put("cityName", cityName != null ? cityName : "");
        review.put("latitude", latitude != null ? latitude : "");
        review.put("longitude", longitude != null ? longitude : "");

        // Add location data
        if (cityName != null) review.put("cityName", cityName);
        if (latitude != null) review.put("latitude", latitude);
        if (longitude != null) review.put("longitude", longitude);

        db.collection("users")
                .document(currentUser.getUid())
                .collection("favorites")
                .add(review)
                .addOnSuccessListener(documentReference -> {
                    progressDialog.dismiss();
                    isSaved = true;
                    saveReviewButton.setText("✅ Sačuvano");
                    saveReviewButton.setEnabled(false);
                    saveReviewButton.setBackgroundTintList(
                            ColorStateList.valueOf(Color.GRAY)
                    );
                    Toast.makeText(this, "✅ Sačuvano u favorite!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    saveReviewButton.setEnabled(true);
                    Log.e("FAVORITE_ERROR", "Greška pri čuvanju: ", e);
                    Toast.makeText(this, "❌ Greška: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                });
    }
}