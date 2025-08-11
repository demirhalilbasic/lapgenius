package com.example.techanalysisapp3.ui.fragments;

import static com.example.techanalysisapp3.util.AppConstants.RAWG_API_KEY;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.techanalysisapp3.R;
import com.example.techanalysisapp3.ui.activities.ShareProcessActivity;
import com.example.techanalysisapp3.ui.components.GameDetailsDialog;
import com.example.techanalysisapp3.ui.components.GameSearchDialog;
import com.example.techanalysisapp3.util.OnSwipeTouchListener;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GameSelectionFragment extends Fragment implements GameSearchDialog.OnGameSelectedListener {
    public int selectedGameId = -1;
    private String selectedGameName = "";
    private String selectedGameImage = "";
    private String selectedMinReq = "";
    private String selectedRecReq = "";
    private String selectedReleaseDate = "";
    private String selectedMetacritic = "";
    private String selectedDescription = "";
    private MaterialButton btnViewDetails;
    private MaterialButton btnMinSpecs;
    private MaterialButton btnRecSpecs;
    private TextView tvSpecsDetails;
    private boolean minSpecsVisible = true;
    private LinearLayout specsContainer;

    private MaterialButton btnSelectGame;
    private ImageView ivGameArtwork;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game_selection, container, false);

        btnSelectGame = view.findViewById(R.id.btnSelectGame);
        ivGameArtwork = view.findViewById(R.id.ivGameArtwork);
        btnViewDetails = view.findViewById(R.id.btnViewDetails);
        btnMinSpecs = view.findViewById(R.id.btnMinSpecs);
        btnRecSpecs = view.findViewById(R.id.btnRecSpecs);
        tvSpecsDetails = view.findViewById(R.id.tvSpecsDetails);
        specsContainer = view.findViewById(R.id.specsContainer);

        btnSelectGame.setOnClickListener(v -> showGameSearchDialog());
        btnViewDetails.setOnClickListener(v -> showGameDetailsDialog());

        btnMinSpecs.setOnClickListener(v -> showMinSpecs());
        btnRecSpecs.setOnClickListener(v -> showRecSpecs());

        tvSpecsDetails.setOnTouchListener(new OnSwipeTouchListener(requireContext()) {
            @Override
            public void onSwipeLeft() {
                showRecSpecs();
            }

            @Override
            public void onSwipeRight() {
                showMinSpecs();
            }
        });

        return view;
    }

    private void showMinSpecs() {
        minSpecsVisible = true;
        tvSpecsDetails.setText(selectedMinReq);
        updateSpecsButtonStyles();
    }

    private void showRecSpecs() {
        minSpecsVisible = false;
        tvSpecsDetails.setText(selectedRecReq);
        updateSpecsButtonStyles();
    }

    private void updateSpecsButtonStyles() {
        if (minSpecsVisible) {
            btnMinSpecs.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.primary))
            );
            btnRecSpecs.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.secondary))
            );
        } else {
            btnMinSpecs.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.secondary))
            );
            btnRecSpecs.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.primary))
            );
        }
    }

    @Override
    public void onGameSelected(int id, String name, String img) {
        selectedGameId = id;
        selectedGameName = name;
        selectedGameImage = img;

        btnViewDetails.setVisibility(View.GONE);
        btnMinSpecs.setVisibility(View.GONE);
        btnRecSpecs.setVisibility(View.GONE);
        tvSpecsDetails.setVisibility(View.GONE);

        ivGameArtwork.setVisibility(View.GONE);
        specsContainer.setVisibility(View.GONE);
        btnViewDetails.setVisibility(View.GONE);

        btnSelectGame.setText("Igra: " + name);

        ShareProcessActivity activity = (ShareProcessActivity) requireActivity();
        activity.setSelectedGameId(id);
        activity.setSelectedGameName(name);

        if (!selectedGameImage.isEmpty()) {
            Glide.with(this)
                    .load(selectedGameImage)
                    .placeholder(R.drawable.ic_game_placeholder)
                    .error(R.drawable.ic_game_placeholder)
                    .into(ivGameArtwork);
            ivGameArtwork.setVisibility(View.VISIBLE);
        }

        fetchGameDetails(id);
    }

    private void fetchGameDetails(int gameId) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://api.rawg.io/api/games/" + gameId + "?key=" + RAWG_API_KEY;

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    tvSpecsDetails.setText("Greška pri povezivanju sa serverom.");
                    tvSpecsDetails.setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String json = response.body().string();
                        JSONObject gameDetails = new JSONObject(json);

                        selectedReleaseDate = gameDetails.optString("released", "N/A");
                        selectedMetacritic = gameDetails.optString("metacritic", "N/A");
                        selectedDescription = gameDetails.optString("description", "");
                        selectedGameId = gameId;

                        JSONArray platforms = gameDetails.getJSONArray("platforms");
                        boolean pcRequirementsFound = false;

                        for (int i = 0; i < platforms.length(); i++) {
                            JSONObject platform = platforms.getJSONObject(i);
                            JSONObject platformObj = platform.getJSONObject("platform");
                            String platformName = platformObj.getString("name");

                            if ("PC".equalsIgnoreCase(platformName)) {
                                JSONObject requirements = platform.optJSONObject("requirements");
                                if (requirements != null) {
                                    if (requirements.has("minimum")) {
                                        selectedMinReq = requirements.getString("minimum")
                                                .replaceAll("<br\\s*/?>", "\n")
                                                .replaceAll("<[^>]+>", "");
                                    } else {
                                        selectedMinReq = "Minimalne specifikacije nisu dostupne";
                                    }

                                    if (requirements.has("recommended")) {
                                        selectedRecReq = requirements.getString("recommended")
                                                .replaceAll("<br\\s*/?>", "\n")
                                                .replaceAll("<[^>]+>", "");
                                    } else {
                                        selectedRecReq = "Preporučene specifikacije nisu dostupne";
                                    }

                                    pcRequirementsFound = true;
                                }
                                break;
                            }
                        }

                        final boolean finalPcRequirementsFound = pcRequirementsFound;

                        requireActivity().runOnUiThread(() -> {
                            if (finalPcRequirementsFound) {
                                ivGameArtwork.setVisibility(View.VISIBLE);
                                specsContainer.setVisibility(View.VISIBLE);
                                btnViewDetails.setVisibility(View.VISIBLE);

                                btnMinSpecs.setVisibility(View.VISIBLE);
                                btnRecSpecs.setVisibility(View.VISIBLE);
                                tvSpecsDetails.setVisibility(View.VISIBLE);
                                showMinSpecs();
                            } else {
                                ivGameArtwork.setVisibility(View.VISIBLE);
                                tvSpecsDetails.setText("Specifikacije za PC nisu dostupne.");
                                tvSpecsDetails.setVisibility(View.VISIBLE);
                                specsContainer.setVisibility(View.VISIBLE);
                                btnViewDetails.setVisibility(View.VISIBLE);
                            }
                            btnViewDetails.setVisibility(View.VISIBLE);
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                        requireActivity().runOnUiThread(() -> {
                            tvSpecsDetails.setText("Greška pri parsiranju podataka.");
                            tvSpecsDetails.setVisibility(View.VISIBLE);
                        });
                    }
                } else {
                    requireActivity().runOnUiThread(() -> {
                        tvSpecsDetails.setText("Greška pri učitavanju zahtjeva. (Kod: " + response.code() + ")");
                        tvSpecsDetails.setVisibility(View.VISIBLE);
                    });
                }
            }
        });
    }

    private void showGameDetailsDialog() {
        GameDetailsDialog dialog = new GameDetailsDialog();
        Bundle args = new Bundle();
        args.putString("name", selectedGameName);
        args.putString("image", selectedGameImage);
        args.putString("release_date", selectedReleaseDate);
        args.putString("metacritic", selectedMetacritic);
        args.putString("description", selectedDescription);
        args.putInt("game_id", selectedGameId);
        dialog.setArguments(args);
        dialog.show(getParentFragmentManager(), "game_details");
    }

    private void showGameSearchDialog() {
        GameSearchDialog dialog = new GameSearchDialog();
        dialog.setTargetFragment(this, 0);
        dialog.show(getParentFragmentManager(), "game_search");
    }
}