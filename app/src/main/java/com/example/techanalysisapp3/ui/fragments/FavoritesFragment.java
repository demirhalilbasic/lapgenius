package com.example.techanalysisapp3.ui.fragments;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.techanalysisapp3.ui.adapters.FavoritesAdapter;
import com.example.techanalysisapp3.R;
import com.example.techanalysisapp3.model.FunnyMessages;
import com.example.techanalysisapp3.model.Review;
import com.example.techanalysisapp3.ui.activities.MapActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FavoritesFragment extends Fragment {
    private RecyclerView recyclerView;
    private FavoritesAdapter adapter;
    private List<Review> allReviews = new ArrayList<>();
    private List<Review> filteredReviews = new ArrayList<>();

    private String currentSearchQuery = "";
    private String currentSortField = "timestamp";
    private Query.Direction currentSortDirection = Query.Direction.DESCENDING;

    private SearchView searchView;
    private EditText searchEditText;
    private TextView emptyStateText;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        requireActivity().getWindow()
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.reviewsRecycler);
        emptyStateText = view.findViewById(R.id.emptyStateText);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FavoritesAdapter(filteredReviews);
        recyclerView.setAdapter(adapter);
        loadFavoritesFromFirestore();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu,
                                    @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.favorites_menu, menu);

        // ---- SearchView setup ----
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setIconifiedByDefault(false);

        ImageView magIcon = searchView.findViewById(
                androidx.appcompat.R.id.search_mag_icon);
        if (magIcon != null) {
            magIcon.setImageResource(R.drawable.ic_search);
            magIcon.setColorFilter(
                    ContextCompat.getColor(requireContext(), android.R.color.white),
                    PorterDuff.Mode.SRC_IN);
        }

        searchEditText = searchView.findViewById(
                androidx.appcompat.R.id.search_src_text);
        searchEditText.setHintTextColor(
                ContextCompat.getColor(requireContext(), android.R.color.white));
        searchEditText.setTextColor(
                ContextCompat.getColor(requireContext(), android.R.color.white));
        ImageView closeIcon = searchView.findViewById(
                androidx.appcompat.R.id.search_close_btn);
        if (closeIcon != null) {
            closeIcon.setColorFilter(
                    ContextCompat.getColor(requireContext(), android.R.color.white),
                    PorterDuff.Mode.SRC_IN);
        }

        searchView.setQueryHint("Pretraži favorite...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }
            @Override
            public boolean onQueryTextChange(String newText) {
                currentSearchQuery = newText.trim().toLowerCase();
                applyClientSideFilter();
                return true;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // --- sorting ----
        if (id == R.id.sort_date_desc) {
            currentSortField     = "timestamp";
            currentSortDirection = Query.Direction.DESCENDING;
            item.setChecked(true);
            loadFavoritesFromFirestore();
            return true;
        }
        else if (id == R.id.sort_date_asc) {
            currentSortField     = "timestamp";
            currentSortDirection = Query.Direction.ASCENDING;
            item.setChecked(true);
            loadFavoritesFromFirestore();
            return true;
        }
        else if (id == R.id.sort_title_asc) {
            currentSortField     = "olxTitle";
            currentSortDirection = Query.Direction.ASCENDING;
            item.setChecked(true);
            loadFavoritesFromFirestore();
            return true;
        }
        else if (id == R.id.sort_title_desc) {
            currentSortField     = "olxTitle";
            currentSortDirection = Query.Direction.DESCENDING;
            item.setChecked(true);
            loadFavoritesFromFirestore();
            return true;
        }

        // --- showing map favorites ---
        else if (id == R.id.menu_map) {
            Intent intent = new Intent(getActivity(), MapActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadFavoritesFromFirestore() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        CollectionReference ref = FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .collection("favorites");

        Query query = ref
                .orderBy(currentSortField, currentSortDirection)
                .orderBy(currentSortField.equals("olxTitle")
                        ? "timestamp" : "olxTitle");

        query.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Favorites", "Greška pri učitavanju", error);
                return;
            }
            allReviews.clear();
            if (value != null) {
                for (var doc : value.getDocuments()) {
                    Review r = doc.toObject(Review.class);
                    r.setDocumentId(doc.getId());
                    allReviews.add(r);
                }
            }
            applyClientSideFilter();
        });
    }

    private void applyClientSideFilter() {
        filteredReviews.clear();
        if (currentSearchQuery.isEmpty()) {
            filteredReviews.addAll(allReviews);
        } else {
            for (Review r : allReviews) {
                if (r.getOlxTitle() != null &&
                        r.getOlxTitle().toLowerCase()
                                .contains(currentSearchQuery)) {
                    filteredReviews.add(r);
                }
            }
        }
        adapter.notifyDataSetChanged();

        // Showing message for empty list (no favorites)
        if (filteredReviews.isEmpty()) {
            String[] messages = FunnyMessages.noFavoritesMessages;
            int randomIndex = new Random().nextInt(messages.length);
            emptyStateText.setText(messages[randomIndex]);
            emptyStateText.setVisibility(View.VISIBLE);
        } else {
            emptyStateText.setVisibility(View.GONE);
        }
    }
}
