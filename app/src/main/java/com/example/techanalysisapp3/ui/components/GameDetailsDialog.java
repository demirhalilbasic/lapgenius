package com.example.techanalysisapp3.ui.components;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.techanalysisapp3.R;
import com.example.techanalysisapp3.util.AppConstants;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GameDetailsDialog extends DialogFragment {

    private boolean showingTranslated = false;
    private String originalDescription = "";
    private String translatedDescription = "";

    private TextView tvDescription;
    private MaterialButton btnTranslate;
    private ImageView ivScreenshot;
    private List<String> screenshotUrls = new ArrayList<>();
    private Handler screenshotHandler = new Handler();
    private int currentScreenshotIndex = 0;
    private int gameId;
    private List<String> screenshotsToShow = new ArrayList<>(); // Limited set for slideshow

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_game_details, container, false);

        // Initialize views
        btnTranslate = view.findViewById(R.id.btnTranslate);
        tvDescription = view.findViewById(R.id.tvDescription);
        ivScreenshot = view.findViewById(R.id.ivScreenshot);

        // Get arguments
        Bundle args = getArguments();
        if (args == null) return view;

        String name = args.getString("name", "");
        String releaseDate = args.getString("release_date", "N/A");
        String metacritic = args.getString("metacritic", "N/A");
        String description = args.getString("description", "");
        gameId = args.getInt("game_id", -1);

        // Setup views
        TextView tvName = view.findViewById(R.id.tvGameName);
        TextView tvReleaseDate = view.findViewById(R.id.tvReleaseDate);
        TextView tvMetacritic = view.findViewById(R.id.tvMetacritic);
        TextView tvMetacriticExplanation = view.findViewById(R.id.tvMetacriticExplanation);
        MaterialButton btnClose = view.findViewById(R.id.btnClose);

        // Load data
        tvName.setText(name);
        tvReleaseDate.setText(formatReleaseDate(releaseDate));

        // Handle metacritic rating
        if ("N/A".equals(metacritic) || "null".equalsIgnoreCase(metacritic)) {
            tvMetacritic.setText("Metacritic ocjena nije dostupna za ovu igru");
            tvMetacriticExplanation.setVisibility(View.GONE);
        } else {
            try {
                int score = Integer.parseInt(metacritic);
                tvMetacritic.setText("Metacritic ocjena: " + score + "/100");
                tvMetacriticExplanation.setText(getMetacriticExplanation(score));
                tvMetacriticExplanation.setVisibility(View.VISIBLE);
            } catch (NumberFormatException e) {
                tvMetacritic.setText("Metacritic ocjena nije dostupna za ovu igru");
                tvMetacriticExplanation.setVisibility(View.GONE);
            }
        }

        // Handle description
        if (description == null || description.isEmpty()) {
            tvDescription.setText("Opis igre nije dostupan");
            btnTranslate.setVisibility(View.GONE);
        } else {
            originalDescription = cleanHtml(description);
            tvDescription.setText("Učitavanje prevoda...");
            btnTranslate.setVisibility(View.VISIBLE);
            btnTranslate.setText("Prevodim...");
            btnTranslate.setEnabled(false);

            // Always attempt translation
            translateDescription(originalDescription);
        }

        // Fetch screenshots
        if (gameId != -1) {
            fetchGameScreenshots(gameId);
        }

        btnClose.setOnClickListener(v -> dismiss());
        btnTranslate.setOnClickListener(v -> toggleTranslation());

        return view;
    }

    private String formatReleaseDate(String dateStr) {
        if ("N/A".equals(dateStr)) {
            return "Datum izlaska: N/A";
        }

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date date = inputFormat.parse(dateStr);

            String[] monthNames = {
                    "januar", "februar", "mart", "april", "maj", "juni",
                    "juli", "avgust", "septembar", "oktobar", "novembar", "decembar"
            };

            // Extract date components
            SimpleDateFormat dayFormat = new SimpleDateFormat("d", Locale.US);
            SimpleDateFormat monthFormat = new SimpleDateFormat("M", Locale.US);
            SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.US);

            int month = Integer.parseInt(monthFormat.format(date)) - 1;
            String day = dayFormat.format(date);
            String year = yearFormat.format(date);

            return "Datum izlaska: " + day + ". " + monthNames[month] + " " + year + ". godine";
        } catch (ParseException | NumberFormatException e) {
            return "Datum izlaska: " + dateStr;
        }
    }

    private String getMetacriticExplanation(int score) {
        if (score >= 97)
            return "🌟 Vrhunski klasik – remek-djelo koje definiše žanr. Must-play!";
        if (score >= 94)
            return "🔥 Fantastična igra – vrhunska izvedba, gotovo bez grešaka.";
        if (score >= 90)
            return "🏆 Izuzetna – kvalitet, atmosfera i gameplay su na zavidnom nivou.";
        if (score >= 87)
            return "🎮 Vrlo dobra – čista zabava uz minimalne zamjerke.";
        if (score >= 84)
            return "👍 Stabilna preporuka – kvalitetna i polirana, ali nije za svakoga.";
        if (score >= 80)
            return "👌 Dobra – zadovoljava većinu gamera, iako bez 'wow' faktora.";
        if (score >= 77)
            return "🙂 Prijatna – nema većih problema, ali fali nešto za izvrsnost.";
        if (score >= 74)
            return "🤔 Mješovita – ima potencijala, ali i par frustrirajućih trenutaka.";
        if (score >= 70)
            return "😐 Okej – prosječno iskustvo, za fanove žanra može biti vrijedno.";
        if (score >= 65)
            return "⚠️ Slabija – zanimljive ideje, ali izvedba šepa.";
        if (score >= 60)
            return "❌ Razočaravajuće – više problema nego prednosti.";
        if (score >= 55)
            return "👎 Loša – tehnički i sadržajno ispod prosjeka.";
        if (score >= 50)
            return "💤 Dosadna – većina gamera će se brzo zasititi.";
        if (score >= 45)
            return "🪫 Nema isporuke – neispunjena obećanja i loša optimizacija.";
        if (score >= 40)
            return "💥 Problematična – loš balans, bugovi, nedovršeno.";
        if (score >= 30)
            return "🚫 Gotovo neigrivo – samo za najtvrdokornije fanove.";
        if (score >= 20)
            return "💩 Veoma loše – izbjegavati pod svaku cijenu.";
        if (score >= 10)
            return "🥴 Katastrofa – loš dizajn, još lošija realizacija.";
        if (score > 0)
            return "😵 Smeće – gubljenje vremena, para i živaca.";
        return "❓ Bez ocjene – nije moguće ocijeniti ili tehnički problemi.";
    }

    private void fetchGameScreenshots(int gameId) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://api.rawg.io/api/games/" + gameId + "/screenshots?key=" + AppConstants.RAWG_API_KEY;

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() ->
                        Glide.with(requireContext())
                                .load(R.drawable.ic_game_placeholder)
                                .into(ivScreenshot)
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String json = response.body().string();
                        JSONObject jsonResponse = new JSONObject(json);
                        JSONArray results = jsonResponse.getJSONArray("results");

                        // Clear previous data
                        screenshotUrls.clear();
                        screenshotsToShow.clear();

                        // Collect all URLs
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject screenshot = results.getJSONObject(i);
                            screenshotUrls.add(screenshot.getString("image"));
                        }

                        // Select max 6 screenshots for caching
                        int maxScreenshots = Math.min(screenshotUrls.size(), 6);
                        if (maxScreenshots > 0) {
                            screenshotsToShow = new ArrayList<>(screenshotUrls.subList(0, maxScreenshots));
                        }

                        requireActivity().runOnUiThread(() -> {
                            if (!screenshotsToShow.isEmpty()) {
                                // Load first screenshot
                                Glide.with(requireContext())
                                        .load(screenshotsToShow.get(0))
                                        .placeholder(R.drawable.ic_game_placeholder)
                                        .error(R.drawable.ic_game_placeholder)
                                        .transition(DrawableTransitionOptions.withCrossFade(500))
                                        .into(ivScreenshot);

                                // Preload ALL selected screenshots into cache
                                for (String url : screenshotsToShow) {
                                    Glide.with(requireContext())
                                            .load(url)
                                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                                            .preload();
                                }

                                // Start slideshow with cached images
                                startScreenshotSlideshow();
                            } else {
                                // Show placeholder if no screenshots
                                Glide.with(requireContext())
                                        .load(R.drawable.ic_game_placeholder)
                                        .into(ivScreenshot);
                            }
                        });
                    } catch (Exception e) {
                        requireActivity().runOnUiThread(() ->
                                Glide.with(requireContext())
                                        .load(R.drawable.ic_game_placeholder)
                                        .into(ivScreenshot)
                        );
                    }
                } else {
                    requireActivity().runOnUiThread(() ->
                            Glide.with(requireContext())
                                    .load(R.drawable.ic_game_placeholder)
                                    .into(ivScreenshot)
                    );
                }
            }
        });
    }

    private void startScreenshotSlideshow() {
        if (screenshotsToShow.isEmpty() || screenshotsToShow.size() < 2) return;

        Runnable screenshotRunnable = new Runnable() {
            @Override
            public void run() {
                if (getActivity() == null || getActivity().isFinishing()) return;

                int nextIndex = (currentScreenshotIndex + 1) % screenshotsToShow.size();

                // Load directly from cache without placeholder
                Glide.with(requireContext())
                        .load(screenshotsToShow.get(nextIndex))
                        .error(R.drawable.ic_game_placeholder)
                        .transition(DrawableTransitionOptions.withCrossFade(500)) // Smoother transition
                        .into(ivScreenshot);

                currentScreenshotIndex = nextIndex;
                screenshotHandler.postDelayed(this, 5000);
            }
        };

        // Start slideshow after initial delay
        screenshotHandler.postDelayed(screenshotRunnable, 5000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        screenshotHandler.removeCallbacksAndMessages(null);
        screenshotUrls.clear();
        screenshotsToShow.clear(); // Clear cache on destroy
    }

    private String cleanHtml(String html) {
        if (html == null) return "";

        // Clean HTML tags
        String cleaned = html.replaceAll("<br\\s*/?>", "\n")
                .replaceAll("<[^>]+>", "")
                .replaceAll("&nbsp;", " ");

        // Strict 499 character limit with ellipsis
        int maxLength = 499;
        if (cleaned.length() > maxLength) {
            cleaned = cleaned.substring(0, maxLength) + "...";
        }
        return cleaned;
    }

    private void toggleTranslation() {
        if (showingTranslated) {
            // Show original description
            tvDescription.setText(originalDescription);
            btnTranslate.setText("Prevedi na bosanski");
            showingTranslated = false;
        } else {
            // Show translated description
            if (!translatedDescription.isEmpty()) {
                tvDescription.setText(translatedDescription);
                btnTranslate.setText("Prikaži na engleskom");
                showingTranslated = true;
            } else {
                Toast.makeText(requireContext(), "Prevod nije dostupan", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void translateDescription(String text) {
        // Enforce strict 499 character limit for translation
        if (text.length() > 499) {
            text = text.substring(0, 499);
        }

        // Use free translation API (MyMemory)
        String encodedText = Uri.encode(text);
        String url = "https://api.mymemory.translated.net/get?q=" + encodedText + "&langpair=en|bs";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Greška pri prevodu", Toast.LENGTH_SHORT).show();
                    tvDescription.setText(originalDescription);
                    btnTranslate.setText("Prevedi na bosanski");
                    btnTranslate.setEnabled(true);
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String json = response.body().string();
                        JSONObject jsonObject = new JSONObject(json);
                        JSONObject responseData = jsonObject.getJSONObject("responseData");
                        String translatedText = responseData.getString("translatedText");

                        // Clean up translation
                        translatedText = translatedText.replace("\\n", "\n");

                        // Apply same 499 character limit to translated text
                        if (translatedText.length() > 499) {
                            translatedText = translatedText.substring(0, 499) + "...";
                        }
                        translatedDescription = translatedText;

                        requireActivity().runOnUiThread(() -> {
                            // Show translated text by default
                            tvDescription.setText(translatedDescription);
                            btnTranslate.setText("Prikaži na engleskom");
                            btnTranslate.setEnabled(true);
                            showingTranslated = true;
                        });
                    } catch (Exception e) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Greška pri parsiranju prevoda", Toast.LENGTH_SHORT).show();
                            tvDescription.setText(originalDescription);
                            btnTranslate.setText("Prevedi na bosanski");
                            btnTranslate.setEnabled(true);
                        });
                    }
                } else {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Greška pri prevodu", Toast.LENGTH_SHORT).show();
                        tvDescription.setText(originalDescription);
                        btnTranslate.setText("Prevedi na bosanski");
                        btnTranslate.setEnabled(true);
                    });
                }
            }
        });
    }
}