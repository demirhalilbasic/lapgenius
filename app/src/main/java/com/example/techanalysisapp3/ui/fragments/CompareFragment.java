package com.example.techanalysisapp3.ui.fragments;

import static com.example.techanalysisapp3.model.FunnyMessages.funnyMessages;
import static com.example.techanalysisapp3.util.AppConstants.OPENROUTER_API_KEY;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.techanalysisapp3.R;
import com.example.techanalysisapp3.model.FunnyMessages;
import com.example.techanalysisapp3.model.Review;
import com.example.techanalysisapp3.ui.activities.ComparisonSwipeActivity;
import com.example.techanalysisapp3.ui.activities.LoginActivity;
import com.example.techanalysisapp3.ui.activities.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CompareFragment extends Fragment {
    private CardView cardLeft, cardRight;
    private ImageView ivLeft, ivRight;
    private TextView tvLeftTitle, tvRightTitle, tvCost;
    private Button btnReset, btnCompare;

    private Review leftReview, rightReview;
    private List<Review> allFavorites = new ArrayList<>();
    private Dialog progressDialog;
    private TextView progressMessage;
    private Handler msgHandler;
    private long credits = 0;
    private int freeUsed = 0;
    private long lastFreeDate = 0;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private ListenerRegistration favoritesListener;
    private Handler refreshHandler;
    private Runnable refreshRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_compare, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        loadUserData();
        cardLeft     = v.findViewById(R.id.cardLeft);
        cardRight    = v.findViewById(R.id.cardRight);
        ivLeft       = v.findViewById(R.id.ivLeftPreview);
        ivRight      = v.findViewById(R.id.ivRightPreview);
        tvLeftTitle  = v.findViewById(R.id.tvLeftTitle);
        tvRightTitle = v.findViewById(R.id.tvRightTitle);
        tvCost       = v.findViewById(R.id.tvCompareCost);
        btnReset     = v.findViewById(R.id.btnResetCompare);
        btnCompare   = v.findViewById(R.id.btnStartCompare);

        loadAllFavorites();

        cardLeft.setOnClickListener(x -> pickLaptop(true));
        cardRight.setOnClickListener(x -> pickLaptop(false));
        btnReset.setOnClickListener(x -> resetSelection());
        btnCompare.setOnClickListener(x -> startComparison());
    }

    private void loadAllFavorites() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        favoritesListener = FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .collection("favorites")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Compare", "Greška pri učitavanju", error);
                        return;
                    }

                    allFavorites.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Review r = doc.toObject(Review.class);
                            r.setDocumentId(doc.getId());
                            allFavorites.add(r);
                        }
                    }

                    updateSelectionState();
                });
    }

    private void updateSelectionState() {
        // API 23 compatibility
        if (leftReview != null) {
            boolean leftExists = false;
            for (Review r : allFavorites) {
                if (r.getDocumentId().equals(leftReview.getDocumentId())) {
                    leftExists = true;
                    break;
                }
            }
            if (!leftExists) resetSelection();
        }

        if (rightReview != null) {
            boolean rightExists = false;
            for (Review r : allFavorites) {
                if (r.getDocumentId().equals(rightReview.getDocumentId())) {
                    rightExists = true;
                    break;
                }
            }
            if (!rightExists) resetSelection();
        }
    }

    private void showProgress() {
        progressDialog = new Dialog(requireContext());
        progressDialog.setContentView(R.layout.dialog_progress);
        progressDialog.setCancelable(false);

        Window window = progressDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(window.getAttributes());
            params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics());
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.gravity = Gravity.CENTER;
            window.setAttributes(params);
        }

        progressMessage = progressDialog.findViewById(R.id.progressMessage);
        ProgressBar progressBar = progressDialog.findViewById(R.id.progressBar);

        ObjectAnimator progressAnimator = ObjectAnimator.ofInt(progressBar, "progress", 0, 100);
        progressAnimator.setDuration(2000);
        progressAnimator.setRepeatCount(ValueAnimator.INFINITE);
        progressAnimator.start();

        List<String> messages = new ArrayList<>(Arrays.asList(funnyMessages));
        Collections.shuffle(messages);
        final String[] currentMessages = messages.toArray(new String[0]);

        progressMessage.setText(currentMessages[0]);
        progressDialog.show();

        msgHandler = new Handler();
        final int[] currentIndex = {0};

        msgHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                currentIndex[0] = (currentIndex[0] + 1) % currentMessages.length;
                progressMessage.animate().alpha(0).setDuration(500).withEndAction(() -> {
                    progressMessage.setText(currentMessages[currentIndex[0]]);
                    progressMessage.animate().alpha(1).setDuration(500);
                });
                msgHandler.postDelayed(this, 4500);
            }
        }, 4500);
    }

    private void hideProgress() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            if (msgHandler != null) {
                msgHandler.removeCallbacksAndMessages(null);
            }
        }
    }

    private void pickLaptop(boolean isLeft) {
        if (allFavorites.size() < 2) {
            showFavoritesError();
            return;
        }

        int availableCount = 0;
        for (Review r : allFavorites) {
            if (isLeft) {
                if (rightReview == null || !r.getDocumentId().equals(rightReview.getDocumentId())) {
                    availableCount++;
                }
            } else {
                if (leftReview == null || !r.getDocumentId().equals(leftReview.getDocumentId())) {
                    availableCount++;
                }
            }
        }

        if (availableCount < 1) {
            Toast.makeText(getContext(),
                    "Morate imati različite laptopove za poređenje",
                    Toast.LENGTH_LONG).show();
            return;
        }

        List<Review> choices = new ArrayList<>();
        List<String> titles = new ArrayList<>();
        for (Review r : allFavorites) {
            if (isLeft && rightReview != null && r.getDocumentId().equals(rightReview.getDocumentId()))
                continue;
            if (!isLeft && leftReview != null && r.getDocumentId().equals(leftReview.getDocumentId()))
                continue;
            choices.add(r);
            titles.add(r.getOlxTitle());
        }
        CharSequence[] arr = titles.toArray(new CharSequence[0]);
        new AlertDialog.Builder(getContext())
                .setTitle(isLeft ? "Odaberi laptop" : "Uporedi sa")
                .setItems(arr, (d, i) -> {
                    Review sel = choices.get(i);
                    if (isLeft) {
                        leftReview = sel;
                        tvLeftTitle.setText(sel.getOlxTitle());
                        Glide.with(this).load(sel.getImageUrls().get(0)).into(ivLeft);
                    } else {
                        rightReview = sel;
                        tvRightTitle.setText(sel.getOlxTitle());
                        Glide.with(this).load(sel.getImageUrls().get(0)).into(ivRight);
                    }
                    btnCompare.setEnabled(leftReview != null && rightReview != null);
                })
                .show();
    }

    private void showFavoritesError() {
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Nedovoljno favorita")
                .setMessage("Treba vam minimum 2 različita laptopa u favoritima za poređenje\n\nTrenutno imate: " + allFavorites.size())
                .setPositiveButton("Dodaj favorite", (d, w) -> {
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).navigateToFavorites();
                    }
                })
                .setNegativeButton("Odustani", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.secondary));
        });

        dialog.show();
    }

    private void resetSelection() {
        leftReview = rightReview = null;
        ivLeft.setImageResource(R.drawable.ic_laptop_placeholder);
        ivRight.setImageResource(R.drawable.ic_laptop_placeholder);
        tvLeftTitle.setText("Odaberi laptop");
        tvRightTitle.setText("Uporedi sa");
        btnCompare.setEnabled(false);
    }

    private void startComparison() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Morate biti prijavljeni!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            return;
        }

        boolean newDay = isNewDay(lastFreeDate);
        if (newDay) {
            freeUsed = 0;
            lastFreeDate = System.currentTimeMillis();
        }

        boolean isFree = freeUsed < 3;
        if (!isFree && credits < 3) {
            Toast.makeText(getContext(), "Nedovoljno kredita! Poređenje košta 3 kredita", Toast.LENGTH_LONG).show();
            return;
        }

        if(isNewDay(lastFreeDate)) {
            firestore.collection("users").document(user.getUid())
                    .update("freeTierUsed", 0, "lastFreeTierDate", System.currentTimeMillis())
                    .addOnSuccessListener(aVoid -> {
                        freeUsed = 0;
                        lastFreeDate = System.currentTimeMillis();
                        proceedWithComparison(true);
                    });
        } else {
            proceedWithComparison(isFree);
        }
    }

    private void proceedWithComparison(final boolean isFree) {
        Toast.makeText(getContext(), "Poređenje košta 3 kredita", Toast.LENGTH_SHORT).show();
        showProgress();

        String prompt = buildPrompt();

        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        JSONObject body = new JSONObject();
        try {
            body.put("model", "tngtech/deepseek-r1t2-chimera:free");
            JSONArray msgs = new JSONArray();
            msgs.put(new JSONObject().put("role", "user").put("content", prompt));
            body.put("messages", msgs);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Request req = new Request.Builder()
                .url("https://openrouter.ai/api/v1/chat/completions")
                .addHeader("Authorization", "Bearer " + OPENROUTER_API_KEY)
                .post(RequestBody.create(JSON, body.toString()))
                .build();

        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(Call c, IOException e) {
                requireActivity().runOnUiThread(() -> {
                    hideProgress();
                    showSweetErrorMessage();
                });
            }

            @Override
            public void onResponse(Call c, Response r) throws IOException {
                String resp = r.body().string();
                try {
                    JSONObject root = new JSONObject(resp);
                    String ai = root.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");

                    List<String> sections = splitIntoSections(ai);

                    requireActivity().runOnUiThread(() -> {
                        hideProgress();

                        // Taking credits only if analysis is proceeded
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        // Update credit status
                        if (user != null) {
                            Map<String, Object> updates = new HashMap<>();
                            if(isFree) {
                                updates.put("freeTierUsed", FieldValue.increment(1));
                            } else {
                                updates.put("credits", FieldValue.increment(-3));
                            }
                            firestore.collection("users").document(user.getUid())
                                    .update(updates);
                        }

                        Intent i = new Intent(getActivity(), ComparisonSwipeActivity.class);
                        i.putStringArrayListExtra("ai_comparison_sections", new ArrayList<>(sections));
                        i.putStringArrayListExtra("left_images", new ArrayList<>(leftReview.getImageUrls()));
                        i.putStringArrayListExtra("right_images", new ArrayList<>(rightReview.getImageUrls()));
                        startActivity(i);
                    });

                } catch (JSONException ex) {
                    ex.printStackTrace();
                    requireActivity().runOnUiThread(() -> {
                        hideProgress();
                        showSweetErrorMessage();
                    });
                }
            }
        });
    }

    private boolean isNewDay(long lastTimestamp) {
        Calendar lastCal = Calendar.getInstance();
        lastCal.setTimeInMillis(lastTimestamp);

        Calendar nowCal = Calendar.getInstance();
        return lastCal.get(Calendar.DAY_OF_YEAR) != nowCal.get(Calendar.DAY_OF_YEAR) ||
                lastCal.get(Calendar.YEAR) != nowCal.get(Calendar.YEAR);
    }

    private void updateCreditDisplay() {
        String status;
        String costText = (freeUsed < 3) ?
                "Cijena poređenja: Besplatno" :
                "Cijena poređenja: 3 kredita";

        status = String.format("Besplatna dnevna poređenja: %d/3\nKrediti: %d\n%s",
                freeUsed, credits, costText);

        if(getView() != null) {
            TextView tvCredits = getView().findViewById(R.id.tvCreditStatus);
            tvCredits.setText(status);
        }
    }

    private void loadUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null) return;

        FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if(doc.exists()) {
                        credits = doc.getLong("credits") != null ? doc.getLong("credits") : 0;
                        freeUsed = doc.getLong("freeTierUsed") != null ? doc.getLong("freeTierUsed").intValue() : 0;
                        lastFreeDate = doc.getLong("lastFreeTierDate") != null ? doc.getLong("lastFreeTierDate") : 0;
                        updateCreditDisplay(); // Updates UI
                    }
                });
    }

    private String buildPrompt() {
        Calendar calendar = Calendar.getInstance();
        int mjesec = calendar.get(Calendar.MONTH);
        int godina = calendar.get(Calendar.YEAR);
        String[] mjeseci = new String[]{"januar", "februar", "mart", "april", "maj", "juni",
                "juli", "avgust", "septembar", "oktobar", "novembar", "decembar"};
        String datumString = mjeseci[mjesec] + " " + godina + ". godine";

        String prompt = "⚔️ Ultimativna komparacija: " + leftReview.getOlxTitle() + " vs " + rightReview.getOlxTitle() + "\n\n"

                + "📅 Ukoliko se pozivaš na vremenski okvir, trenutno je " + datumString + "\n\n"

                + "📜 Stroga pravila formata:\n"
                + "1. Format je: [emoji] [naslov]: [tekst u pasusu]\n"
                + "2. Ne pisati ništa van okvira formata iz stavke 1.\n"
                + "3. Ne služiti se '**' bold i drugim elementima (samo plain text)\n"
                + "4. Tekst sekcije direktno komparira OBA LAPTOPA\n"
                + "5. Poželjno je da opis svake sekcije bude što duži (200+ riječi)\n"
                + "6. Posljednje 3 sekcije moraju dati eksplicitnu preporuku\n\n"

                + "🔍 Podaci za lijevi model:\n" + leftReview.getAnalysisText() + "\n\n"
                + "🔍 Podaci za desni model:\n" + rightReview.getAnalysisText() + "\n\n"

                + "📝 STROGO KORISTI FORMAT PRI ODGOVORU (odgovor započni sa '🚀 Turbo...'):\n"
                + "🚀 Turbo Performansa: [Brzina u gaming/productivity apps sa konkretnim FPS brojevima]\n"
                + "🛡️ Durability Check: [Kvaliteta izrade i historija kvarova za svaki model]\n"
                + "🎮 Game Ready Test: [Kompatibilnost s 3 najpopularnije igre u BiH]\n"
                + "💼 Nomad Friendly: [Težina, debljina, kvaliteta baterije u putnom režimu]\n"
                + "🔮 Future-Proof Score: [Mogućnost nadogradnje za 3+ godina]\n"
                + "📸 Content Creator: [Performanse u Photoshop/Premiere Pro sa realnim render vremenima]\n"
                + "🚨 Hidden Flaws Exposed: [Skriveni problemi specifični za svaki model]\n"
                + "🏗️ Build Quality Face-Off: [Materijali, otpornost na habanje, cooling system]\n"
                + "🔄 Software Ecosystem: [Kompatibilnost s popularnim softverom u regiji]\n"
                + "💸 Value Decoded: [Cijena vs prosječna tržišna vrijednost u BiH]\n"
                + "💡 Savjet za Kupca: [Kome preporučujemo koji model i zašto]\n"
                + "🏆 Champion for Your Needs: [Definitivna preporuka za različite profile korisnika]\n\n"

                + "⚠️ Kritična pravila:\n"
                + "- VAŽNO: Obraćaš se meni kao potencijalnom kupcu ovog laptopa u prvom licu\n"
                + "- Odgovor pisati na bosanskom jeziku (ijekavici)\n"
                + "- U svakoj sekciji MORA postojati direktna komparacija (npr. 'Dok X ima..., Y nudi...')\n"
                + "- Za gaming sekcije koristi FPS podatke za aktuelne gejming naslove\n"
                + "- U '🚨 Hidden Flaws' otkrij minimum 2 specifična problema po modelu (ako postoje, ne traži iglu u sijenu)\n"
                + "- Kod baterije koristi realne scenarije (npr. 'Streaming videa na Netflixu')\n"
                + "- Za kraj daj minimalno 3 različita savjeta kupcima sa različitim prioritetima\n\n"

                + "🔍 Savjeti od koristi pri generisanju odgovora:\n"
                + "- Koristi lokalne reference (npr. cijene u BiH, OLX trendovi)\n"
                + "- Za svaku sekciju: 1 jedinstvena komparativna karakteristika\n"
                + "- Uključi anegdotske primjere (npr. 'Za studenta koji putuje Sarajevo-Tuzla...')\n"
                + "- Ako model stariji od 2019., naglasi rizik kupovine\n"
                + "- Bateriju analiziraj kroz tipične BiH scenarije\n"
                + "- Za garanciju: uporedi servisne centre u regiji\n"
                + "- U posljednje 3 sekcije uključi buying guide za pogodne pozicije spram laptopa (npr):\n"
                + "  1) Student 2) Gamer 3) Remote radnik 4) Ili neki specific use-case za laptop\n"
                + "- Neka buying guide bude jednostavno i simpatično objašnjeno, kao šlag na tortu";

        // Output in console (for debugging)
        // Log.d("PROMPT_BUILDER", "Generisani prompt:\n" + prompt);

        return prompt;
    }

    private List<String> splitIntoSections(String aiText) {
        List<String> sections = new ArrayList<>();
        String[] parts = aiText.split("(?=🛡️|🎮|💼|🔮|📸|🚨|🏗️|🔄|💸|💡|🏆)");
        for (String part : parts) {
            part = part.trim();
            if (!part.isEmpty()) {
                sections.add(part);
            }
        }
        return sections;
    }

    private void showSweetErrorMessage() {
        String[] errorMessages = FunnyMessages.errorMessages;
        int randomIndex = new Random().nextInt(errorMessages.length);
        String sweetMessage = errorMessages[randomIndex];

        new AlertDialog.Builder(requireContext())
                .setTitle("Ups!")
                .setMessage(sweetMessage + "\n\nServis trenutno nije dostupan. Molimo pokušajte kasnije.")
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopPeriodicRefresh();
        hideProgress();
    }

    private void stopPeriodicRefresh() {
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startPeriodicRefresh();
        loadAllFavorites();
    }

    private void startPeriodicRefresh() {
        refreshHandler = new Handler();
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                loadUserData(); // Refresh user data
                refreshHandler.postDelayed(this, 5000); // Schedule next run
            }
        };
        refreshHandler.post(refreshRunnable); // Initial call
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(favoritesListener != null) {
            favoritesListener.remove();
        }
        stopPeriodicRefresh();
    }
}