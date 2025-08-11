package com.example.techanalysisapp3.ui.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.techanalysisapp3.ui.adapters.CityReviewsAdapter;
import com.example.techanalysisapp3.R;
import com.example.techanalysisapp3.model.Review;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapActivity extends AppCompatActivity {

    private MapView mapView;
    private FirebaseFirestore db;
    private List<Review> reviews = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_map);

        Configuration.getInstance().setUserAgentValue(getPackageName());
        mapView = findViewById(R.id.map);
        db = FirebaseFirestore.getInstance();

        setupMap();
        loadFavorites();
    }

    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(8.5);
        mapView.getController().setCenter(new GeoPoint(43.9159, 17.6791));
    }

    private void loadFavorites() {
        FirebaseAuth.getInstance().addAuthStateListener(auth -> {
            if (auth.getCurrentUser() != null) {
                db.collection("users")
                        .document(auth.getCurrentUser().getUid())
                        .collection("favorites")
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            reviews.clear();
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                Review review = document.toObject(Review.class);
                                review.setDocumentId(document.getId()); // Postavi ID dokumenta
                                reviews.add(review);
                            }
                            showCityPins();
                        });
            }
        });
    }

    private void showCityPins() {
        mapView.getOverlays().clear();

        // 1) Group by city without computeIfAbsent
        Map<String, List<Review>> byCity = new HashMap<>();
        for (Review r : reviews) {
            // skip if there's no latitude ili longitude
            if (r.getLatitude() == null || r.getLongitude() == null
                    || r.getLatitude().trim().isEmpty() || r.getLongitude().trim().isEmpty()) {
                continue;
            }

            String city = (r.getCityName() != null && !r.getCityName().isEmpty())
                    ? r.getCityName()
                    : String.format(Locale.getDefault(), "%.4f,%.4f",
                    Double.parseDouble(r.getLatitude().trim()),
                    Double.parseDouble(r.getLongitude().trim()));

            if (!byCity.containsKey(city)) {
                byCity.put(city, new ArrayList<>());
            }
            byCity.get(city).add(r);
        }

        // 2) Calculate central location and place pin (for every city)
        for (Map.Entry<String, List<Review>> entry : byCity.entrySet()) {
            String city = entry.getKey();
            List<Review> cityList = entry.getValue();

            double sumLat = 0, sumLon = 0;
            for (Review rr : cityList) {
                // safe parsing, already filtered null/empty
                sumLat += Double.parseDouble(rr.getLatitude().trim());
                sumLon += Double.parseDouble(rr.getLongitude().trim());
            }
            GeoPoint center = new GeoPoint(sumLat / cityList.size(),
                    sumLon / cityList.size());

            Marker marker = new Marker(mapView);
            marker.setPosition(center);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setIcon(makeCountIcon(cityList.size()));

            marker.setTitle(city + " (" + cityList.size() + " oglasa)");
            marker.setOnMarkerClickListener((m, mv) -> {
                showCityReviews(city, cityList);
                return true;
            });

            mapView.getOverlays().add(marker);
        }

        mapView.invalidate();
    }

    private BitmapDrawable makeCountIcon(int count) {
        int size = (int)(50 * getResources().getDisplayMetrics().density);
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.RED);
        c.drawCircle(size/2f, size/2f, size/2f, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(size/2f);
        paint.setTextAlign(Paint.Align.CENTER);
        c.drawText(String.valueOf(count), size/2f, size/2f + size*0.15f, paint);

        return new BitmapDrawable(getResources(), bmp);
    }

    private void showCityReviews(String city, List<Review> list) {
        BottomSheetDialog dlg = new BottomSheetDialog(this);
        View v = getLayoutInflater().inflate(R.layout.bottom_sheet_city_reviews, null);

        TextView tvTitle = v.findViewById(R.id.tvCityTitle);
        RecyclerView rv    = v.findViewById(R.id.rvCityReviews);
        tvTitle.setText(city);

        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new CityReviewsAdapter(list, review -> {
            // when user clicks on specific review on map
            showReviewDetails(review);
            dlg.dismiss();
        }));

        dlg.setContentView(v);
        dlg.show();
    }

    private void showReviewDetails(Review r) {
        BottomSheetDialog dlg = new BottomSheetDialog(this);
        View v = getLayoutInflater().inflate(R.layout.bottom_sheet_review, null);

        ImageView iv = v.findViewById(R.id.ivPreview);
        TextView tvTitle = v.findViewById(R.id.tvTitle);
        TextView tvLoc   = v.findViewById(R.id.tvLocation);
        Button btnOpen   = v.findViewById(R.id.btnOpen);

        tvTitle.setText(r.getOlxTitle());
        tvLoc.setText("📍 " + r.getCityName());
        if (!r.getImageUrls().isEmpty()) {
            Glide.with(this).load(r.getImageUrls().get(0)).into(iv);
        }

        btnOpen.setOnClickListener(x -> {
            Intent i = new Intent(this, AnalysisSwipeActivity.class);
            i.putExtra("ai_analysis", r.getAnalysisText());
            i.putStringArrayListExtra("image_list", new ArrayList<>(r.getImageUrls()));
            i.putExtra("olx_url", r.getOlxUrl());
            i.putExtra("olx_title", r.getOlxTitle());
            i.putExtra("city_name", r.getCityName());
            i.putExtra("latitude", r.getLatitude());
            i.putExtra("longitude", r.getLongitude());
            i.putExtra("from_favorites", true);
            i.putExtra("document_id", r.getDocumentId());
            startActivity(i);
            dlg.dismiss();
        });

        dlg.setContentView(v);
        dlg.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data after reopening an activity
        loadFavorites();
    }
}
