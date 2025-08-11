package com.example.techanalysisapp3.ui.activities;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.techanalysisapp3.ui.fragments.CompareFragment;
import com.example.techanalysisapp3.ui.fragments.FavoritesFragment;
import com.example.techanalysisapp3.ui.fragments.HistoryFragment;
import com.example.techanalysisapp3.ui.fragments.HomeFragment;
import com.example.techanalysisapp3.ui.fragments.LeaderboardFragment;
import com.example.techanalysisapp3.ui.fragments.ProfileFragment;
import com.example.techanalysisapp3.R;
import com.example.techanalysisapp3.util.NotificationHelper;
import com.example.techanalysisapp3.util.TriviaReceiver;
import com.example.techanalysisapp3.viewmodel.SharedViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final int NOTIFICATION_PERMISSION_CODE = 100;

    private FirebaseAuth mAuth;
    private BottomNavigationView bottomNav;
    private final Map<Integer, Fragment> fragmentMap = new HashMap<>();
    private int currentSelectedItem = R.id.navigation_home;
    private SharedViewModel sharedViewModel;
    private GestureDetector gestureDetector;
    private static final int MIN_SWIPE_DISTANCE = 100;
    private static final int MIN_SWIPE_VELOCITY = 100;
    private float startX = 0;
    private boolean isSwiping = false;
    private ViewGroup fragmentContainer;
    private boolean fromAnalysis = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Add this after Firebase initialization
        NotificationHelper.createNotificationChannel(this);
        requestNotificationPermission();

        // 1. Initialize Firebase FIRST
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        // 2. Set content view BEFORE initializing UI components
        setContentView(R.layout.activity_main);

        // 3. Initialize ViewModel AFTER setting content view
        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        // 4. Initialize UI components
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        bottomNav = findViewById(R.id.bottomNav); // This is now initialized

        // 5. Handle intents AFTER UI initialization
        handleIntent(getIntent());

        //setContentView(R.layout.activity_main);

        // Nova provjera za dolazak iz AnalysisSwipeActivity
        if(getIntent().hasExtra("from_analysis")) {
            fromAnalysis = true;
        }

        gestureDetector = new GestureDetector(this, new GestureListener());

        scheduleDailyTrivia();

        /*
        mAuth = FirebaseAuth.getInstance();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
         */

        bottomNav = findViewById(R.id.bottomNav);
        fragmentMap.put(R.id.navigation_home,     new HomeFragment());
        fragmentMap.put(R.id.navigation_favorites,new FavoritesFragment());
        fragmentMap.put(R.id.navigation_compare,  new CompareFragment());
        fragmentMap.put(R.id.navigation_leaderboard,  new LeaderboardFragment());
        fragmentMap.put(R.id.navigation_profile,  new ProfileFragment());

        if (savedInstanceState == null) {
            showFragment(fragmentMap.get(R.id.navigation_home));
        } else {
            currentSelectedItem = savedInstanceState.getInt("currentItem", R.id.navigation_home);
            bottomNav.setSelectedItemId(currentSelectedItem);
        }

        // Automated navigation to profile if we come from AnalysisSwipeActivity
        if(fromAnalysis) {
            bottomNav.setSelectedItemId(R.id.navigation_profile);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.action_history) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new HistoryFragment())
                        .addToBackStack(null)      // ensure Back pops back here :contentReference[oaicite:3]{index=3}
                        .commit();
                hideKeyboard();
                return true;
            }
            hideKeyboard();

            int id = item.getItemId();
            if (currentSelectedItem != id) {
                currentSelectedItem = id;
                showFragment(fragmentMap.get(id));
                return true;
            }
            return false;
        });

        fragmentContainer = findViewById(R.id.fragment_container);
        fragmentContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    return false;  // don’t consume-let system/back press handle it
                }

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        handleSwipeMove(event);
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        handleSwipeEnd();
                        return true;
                }
                return false;
            }
        });

        //fabHelp = findViewById(R.id.fabHelp);

        // At first, only show on Home
        //fabHelp.setVisibility(View.VISIBLE);

        bottomNav.setOnItemSelectedListener(item -> {
            hideKeyboard();
            int id = item.getItemId();

            // Show only on home
            //fabHelp.setVisibility(id == R.id.navigation_home ? View.VISIBLE : View.GONE);

            if (currentSelectedItem != id) {
                currentSelectedItem = id;
                showFragment(fragmentMap.get(id));
                return true;
            }
            return false;
        });

        // Hook tutorial from MainActivity
        /*fabHelp.setOnClickListener(v -> {
            Fragment f = fragmentMap.get(currentSelectedItem);
            if (f instanceof HomeFragment) {
                ((HomeFragment) f).showTutorial(); // reset + start
            }
        });*/
    }

    public void onSlotPurchaseSuccess() {
        if(fromAnalysis) {
            setResult(RESULT_OK);
            finish();
        }
    }

    private void handleSwipeMove(MotionEvent event) {
        float deltaX = event.getX() - startX;
        int currentIndex = getCurrentNavIndex();

        // Limit swipe functionality only if next/previous section exists
        if ((deltaX > 0 && currentIndex == 0) || (deltaX < 0 && currentIndex == navOrder.length - 1)) {
            return;
        }

        isSwiping = true;
        float progress = Math.abs(deltaX) / fragmentContainer.getWidth();

        // Find target fragment
        int targetIndex = deltaX > 0 ? currentIndex - 1 : currentIndex + 1;
        Fragment currentFragment = fragmentMap.get(navOrder[currentIndex]);
        Fragment targetFragment = getOrCreateFragment(navOrder[targetIndex]);

        // Set positions of fragments
        if (deltaX > 0) { // Swipe right
            targetFragment.getView().setTranslationX(-fragmentContainer.getWidth() + deltaX);
            currentFragment.getView().setTranslationX(deltaX);
        } else { // Swipe left
            targetFragment.getView().setTranslationX(fragmentContainer.getWidth() + deltaX);
            currentFragment.getView().setTranslationX(deltaX);
        }

        // Update alpha for overlay effect
        currentFragment.getView().setAlpha(1 - progress);
        targetFragment.getView().setAlpha(progress);
    }

    private void handleSwipeEnd() {
        if (!isSwiping) return;

        int currentIndex = getCurrentNavIndex();
        Fragment currentFragment = fragmentMap.get(navOrder[currentIndex]);
        View currentView = currentFragment.getView();
        float translationX = currentView.getTranslationX();
        float progress = Math.abs(translationX) / currentView.getWidth();

        // Decide if swipe is enough for transition
        if (progress > 0.5) {
            int targetIndex = translationX > 0 ? currentIndex - 1 : currentIndex + 1;
            bottomNav.setSelectedItemId(navOrder[targetIndex]);
        }

        // Reset positions and alpha
        currentView.animate().translationX(0).alpha(1).setDuration(200).start();
        for (int i = 0; i < navOrder.length; i++) {
            if (i != currentIndex) {
                Fragment f = fragmentMap.get(navOrder[i]);
                if (f.isAdded()) {
                    f.getView().animate().translationX(0).alpha(0).setDuration(200).start();
                }
            }
        }
        isSwiping = false;
    }

    private void scheduleDailyTrivia() {
        // Check if notifications are enabled
        SharedPreferences prefs = getSharedPreferences("notification_prefs", Context.MODE_PRIVATE);
        boolean notificationsEnabled = prefs.getBoolean("notifications_enabled", true);

        if (!notificationsEnabled) {
            return; // Don't schedule if notifications are disabled
        }

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, TriviaReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        );

        // Cancel any existing alarms
        alarmManager.cancel(pendingIntent);

        // Set alarm to trigger at random time between 9 AM and 9 PM
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        // Set random hour (9-21) and random minute
        Random random = new Random();
        int hour = 9 + random.nextInt(12); // 9-20
        int minute = random.nextInt(60);

        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        // If time already passed today, schedule for tomorrow
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Set repeating alarm with inexact timing to save battery
        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );
    }

    private Fragment getOrCreateFragment(int navId) {
        Fragment fragment = fragmentMap.get(navId);
        if (!fragment.isAdded()) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .hide(fragment)
                    .commitNow();
        }
        fragment.getView().setAlpha(0);
        return fragment;
    }

    // Helper method to hide keyboard
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) return;

        View view = getCurrentFocus();
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // Important for cases when activity is already ongoing
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String url = extractUrl(intent);
        if (url == null) return;

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Redirect to login with URL
            Intent loginIntent = new Intent(this, LoginActivity.class);
            loginIntent.putExtra("shared_url", url);
            startActivity(loginIntent);
            finish();
        } else {
            // Start the step-by-step process
            Intent processIntent = new Intent(this, ShareProcessActivity.class);
            processIntent.putExtra("olx_url", url);
            startActivity(processIntent);
            finish();
        }
    }

    private String extractUrl(Intent intent) {
        if (intent == null) return null;

        if (Intent.ACTION_SEND.equals(intent.getAction())) {
            return intent.getStringExtra(Intent.EXTRA_TEXT);
        } else if (intent.hasExtra("shared_url")) {
            return intent.getStringExtra("shared_url");
        }
        return null;
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notifikacije su onemogućene!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showFragment(Fragment fragment) {
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.setCustomAnimations(R.anim.fade_in, R.anim.fade_out); // Dodajte animacije

        for (Fragment f : fragmentMap.values()) {
            if (f.isAdded()) {
                if (f == fragment) {
                    tx.show(f);
                } else {
                    tx.hide(f);
                }
            } else {
                if (f == fragment) {
                    tx.add(R.id.fragment_container, f);
                }
            }
        }
        tx.commit();
    }

    public void navigateToFavorites() {
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.navigation_favorites);
        }
    }

    private int[] navOrder = {
            R.id.navigation_home,
            R.id.navigation_favorites,
            R.id.navigation_compare,
            R.id.navigation_leaderboard,
            R.id.navigation_profile
    };

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                return false;
            }

            float deltaX = e2.getX() - e1.getX();
            float deltaY = e2.getY() - e1.getY();

            if (Math.abs(deltaX) > Math.abs(deltaY)
                    && Math.abs(deltaX) > MIN_SWIPE_DISTANCE
                    && Math.abs(velocityX) > MIN_SWIPE_VELOCITY) {

                if (deltaX > 0) {
                    handleSwipeRight();
                } else {
                    handleSwipeLeft();
                }
                return true;
            }
            return false;
        }
    }

    private int getCurrentNavIndex() {
        for (int i = 0; i < navOrder.length; i++) {
            if (navOrder[i] == currentSelectedItem) {
                return i;
            }
        }
        return 0;
    }

    private void handleSwipeLeft() {
        int currentIndex = getCurrentNavIndex();
        if (currentIndex < navOrder.length - 1) {
            bottomNav.setSelectedItemId(navOrder[currentIndex + 1]);
        }
    }

    private void handleSwipeRight() {
        int currentIndex = getCurrentNavIndex();
        if (currentIndex > 0) {
            bottomNav.setSelectedItemId(navOrder[currentIndex - 1]);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (gestureDetector.onTouchEvent(event)) {
            return true;
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentItem", currentSelectedItem);
    }

    @Override
    protected void onPause() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        super.onPause();
    }

    public void hideBottomNavigation() {
        bottomNav.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            showBottomNavigation();
        } else {
            super.onBackPressed();
        }
    }

    public void showBottomNavigation() {
        bottomNav.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is logged-in after opening the app again
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}
