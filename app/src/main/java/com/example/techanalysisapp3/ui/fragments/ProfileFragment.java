package com.example.techanalysisapp3.ui.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.content.Context;

import com.example.techanalysisapp3.R;
import com.example.techanalysisapp3.model.Brand;
import com.example.techanalysisapp3.ui.activities.LoginActivity;
import com.example.techanalysisapp3.ui.activities.MainActivity;
import com.example.techanalysisapp3.ui.adapters.ProfilePictureAdapter;
import com.example.techanalysisapp3.util.BadgeUtils;
import com.example.techanalysisapp3.util.BrandUtils;
import com.example.techanalysisapp3.util.NotificationHelper;
import com.example.techanalysisapp3.util.TriviaRepository;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class ProfileFragment extends Fragment {

    private TextView tvEmail, tvName;
    private Button btnBuyCredits, logoutButton;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseUser user;
    private long credits = 0;
    private int[] creditPackages = {25, 75, 150, 300};
    private int[] slotPackages = {10, 25, 50, 100};
    private int[] slotPrices = {20, 45, 80, 150};
    private TextView tvSlotInfo;
    private Button btnBuySlots;
    private ImageView ivFavoriteBrand;
    private TextView tvBrandName, tvBrandDescription;
    private Button btnChangeBrand, btnViewOnOlx;
    private Brand currentFavoriteBrand;
    private Map<String, Brand> brandMap = new HashMap<>();
    private Switch switchNotifications;
    private SharedPreferences notificationPrefs;
    private static final String NOTIFICATION_PREFS = "notification_prefs";
    private static final String NOTIFICATIONS_ENABLED = "notifications_enabled";
    private ShapeableImageView ivProfilePicture;
    private ImageView ivEditPfp;
    private List<String> allPfps = new ArrayList<>();
    private List<String> unlockedPfps = new ArrayList<>();
    private List<String> purchasedPfps = new ArrayList<>();
    private List<String> lockedPfps = new ArrayList<>();
    private ProfilePictureAdapter adapter;
    private AlertDialog profilePictureDialog;
    private ScaleAnimation pulseAnimation;
    private String currentPfp;

    private TextView tvCoinValue;
    private View coinContainer;
    private Animation pulseAnimation2;
    private ImageView ivShineOverlay;

    private Handler handler = new Handler();
    private Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (user != null) {
                loadUserInfo();
            }
            handler.postDelayed(this, 5000); // osvježi svakih 5000 ms (5 sekundi)
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeBrands();
        notificationPrefs = requireContext().getSharedPreferences(NOTIFICATION_PREFS, Context.MODE_PRIVATE);
        setHasOptionsMenu(true);
    }

    private void initializeBrands() {
        brandMap.put("default", new Brand(
                "default",
                R.drawable.ic_laptop_placeholder,
                "Dobrodošli",
                getString(R.string.default_brand_short_description),
                getString(R.string.default_brand_full_description),
                "https://olx.ba/pretraga?attr=&attr_encoded=1&category_id=39"
        ));

        brandMap.put("acer", new Brand(
                "acer",
                R.drawable.acer,
                "Acer",
                getString(R.string.acer_short_description),
                getString(R.string.acer_full_description),
                "https://olx.ba/pretraga?attr=&attr_encoded=1&category_id=39&page=1&models=&brands=137&brand=137"
        ));

        brandMap.put("acer_predator", new Brand(
                "acer_predator",
                R.drawable.acer_predator,
                "Acer Predator",
                getString(R.string.acer_predator_short_description),
                getString(R.string.acer_predator_full_description),
                "https://olx.ba/pretraga?attr=&attr_encoded=1&category_id=39&page=1&models=&brands=1780&brand=1780"
        ));

        brandMap.put("acer_nitro", new Brand(
                "acer_nitro",
                R.drawable.acer_nitro,
                "Acer Nitro",
                getString(R.string.acer_nitro_short_description),
                getString(R.string.acer_nitro_full_description),
                "https://olx.ba/pretraga?attr=&attr_encoded=1&q=nitro&category_id=39&page=1&models=&brands=137&brand=137"
        ));

        brandMap.put("apple", new Brand(
                "apple",
                R.drawable.apple,
                "Apple",
                getString(R.string.apple_short_description),
                getString(R.string.apple_full_description),
                "https://olx.ba/pretraga?attr=&attr_encoded=1&category_id=39&models=&brands=1109&brand=1109&page=1"
        ));

        brandMap.put("asus", new Brand(
                "asus",
                R.drawable.asus,
                "Asus",
                getString(R.string.asus_short_description),
                getString(R.string.asus_full_description),
                "https://olx.ba/pretraga?attr=&attr_encoded=1&category_id=39&models=&brands=165&brand=165&page=1"
        ));

        brandMap.put("asus_rog", new Brand(
                "asus_rog",
                R.drawable.asus_rog,
                "ASUS ROG",
                getString(R.string.asus_rog_short_description),
                getString(R.string.asus_rog_full_description),
                "https://olx.ba/pretraga?attr=&attr_encoded=1&q=rog&category_id=39&page=1&models=&brands=165&brand=165"
        ));

        brandMap.put("asus_tuf_gaming", new Brand(
                "asus_tuf_gaming",
                R.drawable.asus_tuf_gaming,
                "ASUS TUF Gaming",
                getString(R.string.asus_tuf_gaming_short_description),
                getString(R.string.asus_tuf_gaming_full_description),
                "https://olx.ba/pretraga?attr=&attr_encoded=1&q=tuf&category_id=39&page=1&models=&brands=165&brand=165"
        ));

        brandMap.put("asus_zenbook", new Brand(
                "asus_zenbook",
                R.drawable.asus_zenbook,
                "ASUS ZenBook",
                getString(R.string.asus_zenbook_short_description),
                getString(R.string.asus_zenbook_full_description),
                "https://olx.ba/pretraga?attr=&attr_encoded=1&q=zenbook&category_id=39&page=1&models=&brands=165&brand=165"
        ));

        brandMap.put("dell", new Brand(
                "dell",
                R.drawable.dell,
                "Dell",
                getString(R.string.dell_short_description),
                getString(R.string.dell_full_description),
                "https://olx.ba/pretraga?attr=&attr_encoded=1&category_id=39&models=&brands=241&brand=241&page=1"
        ));

        brandMap.put("dell_xps", new Brand(
                "dell_xps",
                R.drawable.dell_xps,
                "Dell XPS",
                getString(R.string.dell_xps_short_description),
                getString(R.string.dell_xps_full_description),
                "https://olx.ba/pretraga?attr=&attr_encoded=1&q=xps&category_id=39&page=1&models=&brands=241&brand=241"
        ));

        brandMap.put("dell_alienware", new Brand(
                "dell_alienware",
                R.drawable.dell_alienware,
                "Dell Alienware",
                getString(R.string.dell_alienware_short_description),
                getString(R.string.dell_alienware_full_description),
                "https://olx.ba/pretraga?attr=&attr_encoded=1&category_id=39&models=&brands=699&brand=699&page=1"
        ));

        brandMap.put("gigabyte", new Brand(
                "gigabyte",
                R.drawable.gigabyte,
                "Gigabyte",
                getString(R.string.gigabyte_short_description),
                getString(R.string.gigabyte_full_description),
                "https://olx.ba/pretraga?attr=&attr_encoded=1&category_id=39&models=&brands=440&brand=440&page=1"
        ));

        brandMap.put("gigabyte_aero", new Brand(
                "gigabyte_aero",
                R.drawable.gigabyte_aero,
                "Gigabyte Aero",
                getString(R.string.gigabyte_aero_short_description),
                getString(R.string.gigabyte_aero_full_description),
                "https://olx.ba/pretraga?attr=&attr_encoded=1&q=aero&category_id=39&page=1&models=&brands=440&brand=440"
        ));

        brandMap.put("gigabyte_aorus", new Brand(
                "gigabyte_aorus",
                R.drawable.gigabyte_aorus,
                "Gigabyte Aorus",
                getString(R.string.gigabyte_aorus_short_description),
                getString(R.string.gigabyte_aorus_full_description),
                "https://olx.ba/pretraga?attr=&attr_encoded=1&q=aorus&category_id=39&page=1&models=&brands=440&brand=440"
        ));

        brandMap.put("hp", new Brand(
                "hp",
                R.drawable.hp,
                "HP",
                getString(R.string.hp_short_description),
                getString(R.string.hp_full_description),
                "https://olx.ba/pretraga?attr=&attr_encoded=1&category_id=39&models=&brands=456&brand=456&page=1"
        ));

        brandMap.put("hp_omen", new Brand(
                "hp_omen",
                R.drawable.hp_omen,
                "HP Omen",
                getString(R.string.hp_omen_short_description),
                getString(R.string.hp_omen_full_description),
                "https://olx.ba/pretraga?attr=&attr_encoded=1&category_id=39&models=&brands=1781&brand=1781&page=1"
        ));

        brandMap.put("hp_victus", new Brand(
                "hp_victus",
                R.drawable.hp_victus,
                "HP Victus",
                getString(R.string.hp_victus_short_description),
                getString(R.string.hp_victus_full_description),
                "https://olx.ba/pretraga?attr=&attr_encoded=1&q=victus&category_id=39&page=1&models=&brands=456&brand=456"
        ));

        brandMap.put("lenovo", new Brand(
                "lenovo",
                R.drawable.lenovo,
                "Lenovo",
                getString(R.string.lenovo_short_description),
                getString(R.string.lenovo_full_description),
                "https://olx.ba/pretraga?attr=&attr_encoded=1&category_id=39&models=&brands=986&brand=986&page=1"
        ));

        brandMap.put("lenovo_thinkpad", new Brand(
                "lenovo_thinkpad",
                R.drawable.lenovo_thinkpad,
                "Lenovo ThinkPad",
                getString(R.string.lenovo_thinkpad_short_description),
                getString(R.string.lenovo_thinkpad_full_description),
                "https://olx.ba/pretraga?attr=&attr_encoded=1&q=thinkpad&category_id=39&page=1&models=&brands=986&brand=986"
        ));

        brandMap.put("lenovo_yoga", new Brand(
                "lenovo_yoga",
                R.drawable.lenovo_yoga,
                "Lenovo Yoga",
                getString(R.string.lenovo_yoga_short_description),
                getString(R.string.lenovo_yoga_full_description),
                "https://olx.ba/pretraga?attr=&attr_encoded=1&q=yoga&category_id=39&page=1&models=&brands=986&brand=986"
        ));

        brandMap.put("lenovo_legion", new Brand(
                "lenovo_legion",
                R.drawable.lenovo_legion,
                "Lenovo Legion",
                getString(R.string.lenovo_legion_short_description),
                getString(R.string.lenovo_legion_full_description),
                "https://olx.ba/pretraga?attr=&attr_encoded=1&q=legion&category_id=39&page=1&models=&brands=986&brand=986"
        ));

        brandMap.put("lg_gram", new Brand(
                "lg_gram",
                R.drawable.lg_gram,
                "LG Gram",
                getString(R.string.lg_gram_short_description),
                getString(R.string.lg_gram_full_description),
                "https://olx.ba/pretraga?attr=&attr_encoded=1&category_id=39&models=&brands=142&brand=142&page=1"
        ));

        brandMap.put("microsoft_surface", new Brand(
                "microsoft_surface",
                R.drawable.microsoft_surface,
                "Microsoft Surface",
                getString(R.string.microsoft_surface_short_description),
                getString(R.string.microsoft_surface_full_description),
                "https://olx.ba/pretraga?attr=&attr_encoded=1&category_id=39&models=&brands=277&brand=277&page=1"
        ));

        brandMap.put("msi", new Brand(
                "msi",
                R.drawable.msi,
                "MSI",
                getString(R.string.msi_short_description),
                getString(R.string.msi_full_description),
                "https://olx.ba/pretraga?attr=&attr_encoded=1&category_id=39&models=&brands=346&brand=346&page=1"
        ));

        brandMap.put("msi_gaming", new Brand(
                "msi_gaming",
                R.drawable.msi_gaming,
                "MSI Gaming",
                getString(R.string.msi_gaming_short_description),
                getString(R.string.msi_gaming_full_description),
                "https://olx.ba/pretraga?attr=&attr_encoded=1&q=gaming&category_id=39&page=1&models=&brands=346&brand=346"
        ));

        brandMap.put("razer", new Brand(
                "razer",
                R.drawable.razer,
                "Razer",
                getString(R.string.razer_short_description),
                getString(R.string.razer_full_description),
                "https://olx.ba/pretraga?attr=&attr_encoded=1&category_id=39&models=&brands=649&brand=649&page=1"
        ));

        brandMap.put("samsung_galaxy_book", new Brand(
                "samsung_galaxy_book",
                R.drawable.samsung_galaxy_book,
                "Samsung Galaxy Book",
                getString(R.string.samsung_galaxy_book_short_description),
                getString(R.string.samsung_galaxy_book_full_description),
                "https://olx.ba/pretraga?attr=&attr_encoded=1&category_id=39&models=&brands=108&brand=108&page=1"
        ));

        brandMap.put("xmg", new Brand(
                "xmg",
                R.drawable.xmg,
                "XMG",
                getString(R.string.xmg_short_description),
                getString(R.string.xmg_full_description),
                "https://olx.ba/pretraga?attr=&attr_encoded=1&q=xmg&category_id=39"
        ));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvEmail = view.findViewById(R.id.tvEmail);
        tvName = view.findViewById(R.id.tvName);
        tvCoinValue = view.findViewById(R.id.tvCoinValue);
        coinContainer = view.findViewById(R.id.coinContainer);
        ivShineOverlay = view.findViewById(R.id.ivShineOverlay);
        btnBuyCredits = view.findViewById(R.id.btnBuyCredits);
        tvSlotInfo = view.findViewById(R.id.tvSlotInfo);
        btnBuySlots = view.findViewById(R.id.btnBuySlots);
        btnBuySlots.setOnClickListener(v -> showSlotPurchaseDialog());
        logoutButton = view.findViewById(R.id.logoutButton);

        ivFavoriteBrand = view.findViewById(R.id.ivFavoriteBrand);
        tvBrandName = view.findViewById(R.id.tvBrandName);
        tvBrandDescription = view.findViewById(R.id.tvBrandDescription);
        btnChangeBrand = view.findViewById(R.id.btnChangeBrand);
        btnViewOnOlx = view.findViewById(R.id.btnViewOnOlx);

        switchNotifications = view.findViewById(R.id.switchNotifications);

        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        ivEditPfp = view.findViewById(R.id.ivEditPfp);

        // Make both the profile picture and edit icon clickable
        View.OnClickListener pfpClickListener = v -> showProfilePictureDialog();
        ivProfilePicture.setOnClickListener(pfpClickListener);
        ivEditPfp.setOnClickListener(pfpClickListener);

        // Initialize all profile pictures
        for (int i = 1; i <= 66; i++) {
            allPfps.add("ic_pfp_" + i);
        }

        ivEditPfp.setOnClickListener(v -> showProfilePictureDialog());

        // Create pulse animation
        pulseAnimation = new ScaleAnimation(
                1f, 1.1f, // Start and end X scale
                1f, 1.1f, // Start and end Y scale
                Animation.RELATIVE_TO_SELF, 0.5f, // Pivot X
                Animation.RELATIVE_TO_SELF, 0.5f // Pivot Y
        );
        pulseAnimation.setDuration(300);
        pulseAnimation.setRepeatMode(Animation.REVERSE);
        pulseAnimation.setRepeatCount(1);
        pulseAnimation.setInterpolator(new OvershootInterpolator());

        setupProfilePictureListeners();

        btnChangeBrand.setOnClickListener(v -> showBrandSelectionDialog());
        btnViewOnOlx.setOnClickListener(v -> openOlxUrl());

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();

        if (user != null) {
            loadUserInfo();
        }

        pulseAnimation2 = new ScaleAnimation(
                1f, 1.1f,
                1f, 1.1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        pulseAnimation2.setDuration(300);
        pulseAnimation2.setRepeatMode(Animation.REVERSE);
        pulseAnimation2.setRepeatCount(0); // Only run once per click
        pulseAnimation2.setInterpolator(new OvershootInterpolator());

        coinContainer.setOnClickListener(v -> {
            // Play pulse animation
            v.clearAnimation();
            v.startAnimation(pulseAnimation2);

            // Play shine animation
            ivShineOverlay.setVisibility(View.VISIBLE);
            Animation shine = AnimationUtils.loadAnimation(getContext(), R.anim.shine_animation);
            shine.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    ivShineOverlay.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            ivShineOverlay.startAnimation(shine);
        });

        coinContainer.setOnLongClickListener(v -> {
            createCoinBurst();
            new Handler().postDelayed(this::showCreditInfoDialog, 500);
            return true;
        });

        btnBuyCredits.setOnClickListener(v -> showCreditPurchaseDialog());
        logoutButton.setOnClickListener(v -> logoutUser());

        TextView tvFooter = view.findViewById(R.id.tvFooter);

        // Initialize notification switch
        switchNotifications.setChecked(notificationPrefs.getBoolean(NOTIFICATIONS_ENABLED, true));

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            notificationPrefs.edit().putBoolean(NOTIFICATIONS_ENABLED, isChecked).apply();
            String message = isChecked ? "Notifikacije uključene" : "Notifikacije isključene";
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        });

        try {
            String versionName = requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0)
                    .versionName;
            tvFooter.setText("v" + versionName + " by demirhalilbasic");
        } catch (PackageManager.NameNotFoundException e) {
            tvFooter.setText("v1.9.7"); // fallback
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.post(refreshRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(refreshRunnable);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.profile_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit_profile:
                ((MainActivity) getActivity()).hideBottomNavigation();
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new EditProfileFragment())
                        .addToBackStack(null)
                        .commit();
                return true;

            case R.id.action_history:
                ((MainActivity) getActivity()).hideBottomNavigation();
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new HistoryFragment())
                        .addToBackStack(null)
                        .commit();
                return true;

            case R.id.action_description:
                ((MainActivity) getActivity()).hideBottomNavigation();
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new DescriptionFragment())
                        .addToBackStack(null)
                        .commit();
                return true;

            case R.id.action_test_notification:
                sendTestNotification();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sendTestNotification() {
        NotificationHelper.createNotificationChannel(requireContext());
        TriviaRepository repository = new TriviaRepository(requireContext());
        String trivia = repository.getRandomTestTrivia();

        if (trivia != null) {
            NotificationHelper.showTriviaNotification(requireContext(), trivia);
            Toast.makeText(getContext(), "Test notifikacija poslana!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Nema dostupnih trivija za test", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCreditDisplay() {
        tvCoinValue.setText(String.valueOf(credits));
    }

    private void showCreditInfoDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_credit_info, null);

        TextView tvTitle = dialogView.findViewById(R.id.tvTitle);
        TextView tvMessage = dialogView.findViewById(R.id.tvMessage);
        TextView tvSlotInfo = dialogView.findViewById(R.id.tvSlotPricing);
        Button btnBuy = dialogView.findViewById(R.id.btnBuyNow);

        String message;
        if (credits == 0) {
            tvTitle.setText("Dobrodošli u svijet kredita!");
            message = "Dnevno imate pravo na 3 besplatne analize.\n" +
                    "Za dodatne analize potrebni su krediti.\n\n" +
                    "Kupovinom kredita otključavate dodatne mogućnosti, uključujući više analiza i personalizaciju profila.";
        } else if (credits <= 20) {
            tvTitle.setText("Vaš kreditni status");
            message = String.format("Trenutno imate %d kredit%s.\n", credits, credits == 1 ? "" : "a");

            message += "\nOmogućene funkcije:\n";
            if (credits >= 5) {
                message += "- Promjena profilne slike (5 kredita)\n";
            } else {
                message += "- Promjena profilne slike (zahtijeva minimalno 5 kredita)\n";
            }
            message += "- Nekoliko dodatnih analiza\n\n";

            message += "Preporučujemo nadopunu kredita za stabilnije korištenje aplikacije.";
        } else {
            tvTitle.setText("Čestitamo na kreditnoj sigurnosti!");
            message = String.format("Imate %d kredita – odlična rezerva za aktivno korištenje.\n\n", credits) +
                    "Mogućnosti koje su vam dostupne:\n" +
                    "- Višestruke dodatne analize\n" +
                    "- Promjene profilne slike\n" +
                    "- Korištenje premium funkcija\n\n" +
                    "Razmislite o proširenju slotova za čuvanje omiljenih uređaja.";
        }

        tvMessage.setText(message);

        tvSlotInfo.setText(
                "Paketi proširenja slotova za favorite:\n" +
                        "+10 slotova – 20 kredita\n" +
                        "+25 slotova – 45 kredita\n" +
                        "+50 slotova – 80 kredita\n" +
                        "+100 slotova – 150 kredita\n\n" +
                        "Slotovi omogućavaju čuvanje više od 10 omiljenih laptopa istovremeno."
        );

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        btnBuy.setOnClickListener(v -> {
            dialog.dismiss();
            showCreditPurchaseDialog();
        });

        dialog.show();
    }

    private void createCoinBurst() {
        if (getView() == null) return;

        // Get the root view of the activity
        ViewGroup root = (ViewGroup) requireActivity().getWindow().getDecorView();
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;

        // Get coin container position
        int[] location = new int[2];
        coinContainer.getLocationOnScreen(location);
        int startX = location[0] + coinContainer.getWidth() / 2;
        int startY = location[1] + coinContainer.getHeight() / 2;

        for (int i = 0; i < 25; i++) { // Increase number of coins
            ImageView coin = new ImageView(getContext());
            coin.setImageResource(R.drawable.ic_coin_small);
            coin.setLayoutParams(new ViewGroup.LayoutParams(40, 40));

            // Random end position across entire screen
            int endX = (int) (Math.random() * screenWidth);
            int endY = screenHeight + 100; // Below screen bottom

            // Random starting angle (0-360 degrees)
            double angle = Math.random() * Math.PI * 2;

            // Calculate parabolic path (sine curve)
            float distanceX = endX - startX;
            float distanceY = endY - startY;
            float controlX = startX + distanceX / 2;
            float controlY = startY - 300; // Higher peak

            // Create path animation
            Path path = new Path();
            path.moveTo(startX, startY);
            path.quadTo(controlX, controlY, endX, endY);

            // Add to root
            root.addView(coin);

            // Create animations
            ObjectAnimator pathAnim = ObjectAnimator.ofFloat(
                    coin,
                    View.X,
                    View.Y,
                    path
            );
            pathAnim.setDuration(2000 + (int)(Math.random() * 1000));
            pathAnim.setInterpolator(new AccelerateInterpolator());

            ObjectAnimator rotateAnim = ObjectAnimator.ofFloat(
                    coin,
                    "rotation",
                    0f,
                    (float)(Math.random() * 720) - 360
            );
            rotateAnim.setDuration(1500);

            ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(
                    coin,
                    "alpha",
                    1f,
                    0f
            );
            alphaAnim.setDuration(500);
            alphaAnim.setStartDelay(1500);

            AnimatorSet set = new AnimatorSet();
            set.playTogether(pathAnim, rotateAnim, alphaAnim);
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    root.removeView(coin);
                }
            });
            set.start();
        }
    }

    public void loadUserInfo() {
        // Early exit if views are destroyed
        if (getView() == null || tvEmail == null) return;
        String uid = user.getUid();

        firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String email = documentSnapshot.getString("email");
                        String firstName = documentSnapshot.getString("firstName");
                        String lastName = documentSnapshot.getString("lastName");
                        this.credits = documentSnapshot.getLong("credits") != null ?
                                documentSnapshot.getLong("credits") : 0;
                        long freeUsed = documentSnapshot.getLong("freeTierUsed") != null ?
                                documentSnapshot.getLong("freeTierUsed") : 0;
                        long lastFreeDate = documentSnapshot.getLong("lastFreeTierDate") != null ?
                                documentSnapshot.getLong("lastFreeTierDate") : 0;
                        long purchasedSlots = documentSnapshot.getLong("purchasedSlots") != null ?
                                documentSnapshot.getLong("purchasedSlots") : 0;

                        // Load favorite brand
                        String brandKey = documentSnapshot.getString("favoriteBrand");
                        if (brandKey != null && brandMap.containsKey(brandKey)) {
                            currentFavoriteBrand = brandMap.get(brandKey);
                            updateBrandUI();
                        } else {
                            hideBrandSection();
                        }

                        if (documentSnapshot.contains("pfp")) {
                            currentPfp = documentSnapshot.getString("pfp");
                            if (currentPfp != null) {
                                int resId = getResources().getIdentifier(currentPfp, "drawable", requireContext().getPackageName());
                                ivProfilePicture.setImageResource(resId);
                            }
                        }

                        updateCreditDisplay();

                        firestore.collection("users").document(uid)
                                .collection("favorites")
                                .get()
                                .addOnSuccessListener(favoritesQuery -> {
                                    if (getView() == null || tvSlotInfo == null) return;

                                    long currentFavoritesCount = favoritesQuery.size();
                                    long totalSlots = 10 + purchasedSlots;

                                    String slotText = String.format("Slotovi: %d/%d (%s)",
                                            currentFavoritesCount,
                                            totalSlots,
                                            purchasedSlots > 0 ? "+" + purchasedSlots + " prošireno" : "Osnovni plan");
                                    tvSlotInfo.setText(slotText);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Greška pri učitavanju slotova", Toast.LENGTH_SHORT).show();
                                });

                        tvEmail.setText("Email: " + email);
                        tvName.setText(firstName + " " + lastName);

                        if (isNewDay(lastFreeDate)) {
                            updateFreeTier(0);
                        }

                        if (this.credits == 384) {
                            List<String> badges = (List<String>) documentSnapshot.get("badges");
                            if (badges == null || !badges.contains(BadgeUtils.BADGE_384_CREDITS)) {
                                BadgeUtils.unlockBadge(uid, BadgeUtils.BADGE_384_CREDITS, requireContext());
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Neuspješno učitavanje podataka.", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateBrandUI() {
        if (currentFavoriteBrand != null) {
            ivFavoriteBrand.setImageResource(currentFavoriteBrand.getLogoResId());
            tvBrandName.setText(currentFavoriteBrand.getName());
            tvBrandDescription.setText(currentFavoriteBrand.getShortDescription());
            btnViewOnOlx.setVisibility(View.VISIBLE);

            Button btnLearnMore = getView().findViewById(R.id.btnLearnMore);
            btnLearnMore.setOnClickListener(v -> showBrandDetailsDialog());
        }
    }

    private void showBrandDetailsDialog() {
        if (currentFavoriteBrand == null) return;

        // Inflate custom dialog layout
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_brand_details, null);

        // Set up UI components
        ImageView ivBrandLogo = dialogView.findViewById(R.id.ivDialogBrandLogo);
        TextView tvDialogBrandName = dialogView.findViewById(R.id.tvDialogBrandName);
        TextView tvFullDescription = dialogView.findViewById(R.id.tvFullDescription);
        Button btnDialogOlx = dialogView.findViewById(R.id.btnDialogOlx);

        ivBrandLogo.setImageResource(currentFavoriteBrand.getLogoResId());
        tvDialogBrandName.setText(currentFavoriteBrand.getName());
        tvFullDescription.setText(currentFavoriteBrand.getFullDescription());

        btnDialogOlx.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(currentFavoriteBrand.getOlxUrl()));
            startActivity(browserIntent);
        });

        // Create and show dialog
        new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setNegativeButton("Zatvori", null)
                .show();
    }

    private void hideBrandSection() {
        ivFavoriteBrand.setVisibility(View.GONE);
        tvBrandName.setVisibility(View.GONE);
        tvBrandDescription.setVisibility(View.GONE);
        btnViewOnOlx.setVisibility(View.GONE);
    }

    private void showBrandSelectionDialog() {
        if (getActivity() == null || getActivity().isFinishing() || isDetached()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Odaberite omiljeni brend");

        // Create fixed-order list (same as insertion order)
        List<Brand> brands = new ArrayList<>();
        brands.add(brandMap.get("acer"));
        brands.add(brandMap.get("acer_predator"));
        brands.add(brandMap.get("acer_nitro"));
        brands.add(brandMap.get("apple"));
        brands.add(brandMap.get("asus"));
        brands.add(brandMap.get("asus_rog"));
        brands.add(brandMap.get("asus_tuf_gaming"));
        brands.add(brandMap.get("asus_zenbook"));
        brands.add(brandMap.get("dell"));
        brands.add(brandMap.get("dell_xps"));
        brands.add(brandMap.get("dell_alienware"));
        brands.add(brandMap.get("gigabyte"));
        brands.add(brandMap.get("gigabyte_aero"));
        brands.add(brandMap.get("gigabyte_aorus"));
        brands.add(brandMap.get("hp"));
        brands.add(brandMap.get("hp_omen"));
        brands.add(brandMap.get("hp_victus"));
        brands.add(brandMap.get("lenovo"));
        brands.add(brandMap.get("lenovo_thinkpad"));
        brands.add(brandMap.get("lenovo_yoga"));
        brands.add(brandMap.get("lenovo_legion"));
        brands.add(brandMap.get("lg_gram"));
        brands.add(brandMap.get("microsoft_surface"));
        brands.add(brandMap.get("msi"));
        brands.add(brandMap.get("msi_gaming"));
        brands.add(brandMap.get("razer"));
        brands.add(brandMap.get("samsung_galaxy_book"));
        brands.add(brandMap.get("xmg"));

        BrandListAdapter adapter = new BrandListAdapter(getContext(), brands);

        builder.setAdapter(adapter, (dialog, which) -> {
            Brand selectedBrand = brands.get(which);
            saveFavoriteBrand(selectedBrand.getKey());
        });

        builder.setNegativeButton("Otkaži", null);
        builder.show();
    }

    private class BrandListAdapter extends ArrayAdapter<Brand> {
        private final Context context;
        private final List<Brand> brands;

        public BrandListAdapter(Context context, List<Brand> brands) {
            super(context, R.layout.item_brand, brands);
            this.context = context;
            this.brands = brands;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_brand, parent, false);
            }

            Brand brand = brands.get(position);
            if (brand == null) return convertView;

            ImageView ivLogo = convertView.findViewById(R.id.ivBrandLogo);
            TextView tvName = convertView.findViewById(R.id.tvBrandName);

            // null checks for views
            if (ivLogo != null) {
                ivLogo.setImageResource(brand.getLogoResId());
            }

            if (tvName != null && brand.getName() != null) {
                tvName.setText(brand.getName());
            } else {
                Log.e("BrandListAdapter", "tvName or brand name is null at position: " + position);
            }

            return convertView;
        }
    }

    private void saveFavoriteBrand(String brandKey) {
        firestore.collection("users").document(user.getUid())
                .update("favoriteBrand", brandKey)
                .addOnSuccessListener(aVoid -> {
                    currentFavoriteBrand = brandMap.get(brandKey);
                    updateBrandUI();
                    Toast.makeText(getContext(), "Omiljeni brend ažuriran!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Greška: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void openOlxUrl() {
        if (currentFavoriteBrand != null) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(currentFavoriteBrand.getOlxUrl()));
            startActivity(browserIntent);
        }
    }

    private void showCreditPurchaseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Kupi kredite");

        // custom layout for dialog items
        List<Map<String, Object>> items = new ArrayList<>();
        for (int i = 0; i < creditPackages.length; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("credits", creditPackages[i]);
            item.put("price", getPriceForPackage(creditPackages[i]) + ",00 BAM");
            items.add(item);
        }

        // custom adapter
        SimpleAdapter adapter = new SimpleAdapter(
                getContext(),
                items,
                R.layout.item_credit_package,
                new String[] {"credits", "price"},
                new int[] {R.id.tvCoinValue, R.id.tvPrice}
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                // null safety check
                FrameLayout coinContainer = view.findViewById(R.id.coinContainer);
                View innerCircle = view.findViewById(R.id.innerCircle);

                if (coinContainer != null) {
                    ScaleAnimation pulse = new ScaleAnimation(
                            1f, 1.1f,
                            1f, 1.1f,
                            Animation.RELATIVE_TO_SELF, 0.5f,
                            Animation.RELATIVE_TO_SELF, 0.5f
                    );
                    pulse.setDuration(300);
                    pulse.setRepeatMode(Animation.REVERSE);
                    pulse.setRepeatCount(1);
                    pulse.setInterpolator(new OvershootInterpolator());
                    coinContainer.startAnimation(pulse);
                }

                // inner circle size based on credit package
                if (innerCircle != null) {
                    int credits = creditPackages[position];
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) innerCircle.getLayoutParams();

                    // different margins for different credit packages
                    int margin;
                    if (credits < 150) {
                        margin = dpToPx(8); // larger inner circle for 25/75
                    } else {
                        margin = dpToPx(5); // even larger for 150/300
                    }

                    params.setMargins(margin, margin, margin, margin);
                    innerCircle.setLayoutParams(params);
                }

                return view;
            }
        };

        builder.setAdapter(adapter, (dialog, which) -> {
            int creditsToBuy = creditPackages[which];
            String price = getPriceForPackage(creditsToBuy) + ",00 BAM";

            // Confirmation dialog
            new AlertDialog.Builder(getContext())
                    .setTitle("Potvrda kupovine")
                    .setMessage("Da li ste sigurni da želite kupiti " + creditsToBuy +
                            " kredita za cijenu od " + price + "?")
                    .setPositiveButton("Kupi", (d, i) -> {
                        addCredits(creditsToBuy);
                        dialog.dismiss();
                    })
                    .setNegativeButton("Otkaži", null)
                    .show();
        });

        builder.setNegativeButton("Otkaži", null);
        builder.show();
    }

    private String getPriceForPackage(int credits) {
        switch (credits) {
            case 25: return "3";
            case 75: return "7";
            case 150: return "14";
            case 300: return "28";
            default: return "?";
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void showCoinConfetti(int creditAmount) {
        if (getActivity() == null) return;

        // Determine number of coins based on credit package
        int minCoins, maxCoins;
        if (creditAmount == 25) {
            minCoins = 15;
            maxCoins = 25;
        } else if (creditAmount == 75) {
            minCoins = 25;
            maxCoins = 35;
        } else if (creditAmount == 150) {
            minCoins = 35;
            maxCoins = 45;
        } else {
            minCoins = 45;
            maxCoins = 60;
        }

        int coinCount = minCoins + (int)(Math.random() * (maxCoins - minCoins));

        // Get root view of the activity
        ViewGroup root = (ViewGroup) getActivity().getWindow().getDecorView();
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;

        // Create multiple coin images with staggered timing
        for (int i = 0; i < coinCount; i++) {
            ImageView coin = new ImageView(getContext());
            coin.setImageResource(R.drawable.ic_coin_small);
            coin.setLayoutParams(new ViewGroup.LayoutParams(40, 40));

            // Random starting position at top - FIXED: spread across entire screen
            int startX = (int) (Math.random() * screenWidth);
            int startY = -50 - (int)(Math.random() * 300); // Vary starting height

            // Random end position at bottom - FIXED: spread across entire screen
            int endX = (int) (Math.random() * screenWidth);
            int endY = screenHeight + 100;

            // Random rotation
            int rotation = (int) (Math.random() * 3600) - 1800; // -1800 to +1800 degrees

            // Add to root
            root.addView(coin);
            coin.setX(startX);
            coin.setY(startY);

            // Create animations with staggered timing
            ObjectAnimator fallAnim = ObjectAnimator.ofFloat(coin, "y", startY, endY);
            fallAnim.setDuration(4000 + (int)(Math.random() * 3000)); // 4-7 seconds

            ObjectAnimator rotateAnim = ObjectAnimator.ofFloat(coin, "rotation", 0f, rotation);
            rotateAnim.setDuration(4000 + (int)(Math.random() * 3000));

            // Add horizontal sway - FIXED: random direction
            float swayDirection = (Math.random() > 0.5) ? 1f : -1f;
            ObjectAnimator swayAnim = ObjectAnimator.ofFloat(
                    coin,
                    "translationX",
                    0f,
                    swayDirection * (float)(Math.random() * screenWidth * 0.3)
            );
            swayAnim.setDuration(2000);
            swayAnim.setRepeatMode(ObjectAnimator.REVERSE);
            swayAnim.setRepeatCount(ObjectAnimator.INFINITE);

            ObjectAnimator fadeAnim = ObjectAnimator.ofFloat(coin, "alpha", 1f, 0f);
            fadeAnim.setDuration(1000);
            fadeAnim.setStartDelay(3000 + (int)(Math.random() * 2000)); // Fade after 3-5 seconds

            // Add bounce effect at the end
            ObjectAnimator bounceAnim = ObjectAnimator.ofFloat(coin, "y", endY, endY - 50, endY);
            bounceAnim.setDuration(500);
            bounceAnim.setStartDelay(3000 + (int)(Math.random() * 1000));
            bounceAnim.setInterpolator(new BounceInterpolator());

            AnimatorSet set = new AnimatorSet();
            set.playTogether(fallAnim, rotateAnim, swayAnim);
            set.play(fadeAnim).after(3000 + (int)(Math.random() * 2000));
            set.play(bounceAnim).with(fadeAnim);
            set.setStartDelay(i * 100); // Stagger start times

            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    root.removeView(coin);
                }
            });

            set.start();
        }
    }

    private void addCredits(int amount) {
        // Check if purchase would exceed the 384 credit limit
        if (credits + amount > 384) {
            showCreditLimitDialog();
            return;
        }

        firestore.collection("users").document(user.getUid())
                .update("credits", FieldValue.increment(amount))
                .addOnSuccessListener(aVoid -> {
                    this.credits += amount;
                    updateCreditDisplay();
                    Toast.makeText(getContext(), "Uspešno ste kupili " + amount + " kredita!", Toast.LENGTH_SHORT).show();
                    showCoinConfetti(amount); // Pass amount to determine coin count
                    loadUserInfo(); // Refresh credit display
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Greška pri kupovini: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showCreditLimitDialog() {
        String[] quotes = {
                "384 CUDA jezgri – broj koji je pokretao gaming prije nego je ray tracing postao moderan.",
                "Stariji GeForce GPU sa 384 CUDA jezgri i danas pokreće mnoge igre iznenađujuće glatko.",
                "384 GB RAM-a podržavaju samo najmoćniji mobilni radni strojevi – pravi alat za inžinjere, AI stručnjake i kreatore.",
                "Workstation laptopi s podrškom za 384 GB RAM-a mogu bez problema vrtiti više virtualnih mašina istovremeno.",
                "384 GB RAM-a je kapacitet koji koriste profesionalni render farm serveri – dostupno sada i u mobilnim uređajima.",
                "384-bitna sabirnica omogućava nevjerovatnu propusnost memorije – ključna u grafičkim karticama najvišeg ranga.",
                "Neki od najbržih desktop GPU-ova svih vremena koriste 384-bitnu memorijsku sabirnicu za maksimalni bandwidth.",
                "Horizontalna rezolucija 3840 piksela je osnova 4K standarda – za nevjerovatno oštru i detaljnu sliku.",
                "3840 × 2160 piksela – poznato kao 4K UHD – postao je novi standard za video produkciju i gledanje sadržaja.",
                "384 CUDA jezgri su bile zlatna sredina između efikasnosti i performansi za grafiku ranih 2010-ih.",
                "Laptop sa 384 CUDA jezgri nudio je hardversku akceleraciju za Adobe Premiere, čak i prije hardverskog renderinga.",
                "Broj 384 označava vrhunski balans između snage i efikasnosti u mnogim generacijama laptop GPU-a.",
                "Kod NVIDIA grafičkih čipova, 384 je bio broj koji je označavao prelazak iz entry-level u ozbiljni mainstream.",
                "Mnogi poznati 4K ekrani u laptopima koriste rezoluciju koja počinje sa 384 – jer svaka oštrina počinje odatle.",
                "Najnovije AI radne stanice omogućavaju do 384 GB RAM-a – snaga koju je nekad imalo 10 servera zajedno.",
                "384 CUDA jezgri bilo je dovoljno za solidan 3D dizajn, obradu videa i čak treniranje manjih AI modela.",
                "Profesionalni kreatori su birali 384-bitne sabirnice radi maksimalne brzine prilikom editovanja u realnom vremenu.",
                "384 CUDA jezgri i dan-danas mogu pokretati većinu popularnih esports igara u stabilnom framerate-u.",
                "Za mnoge 4K ekrane, broj 3840 je ono što garantuje potpunu preciznost boja i prostor za kreativnost.",
                "Neki modeli GeForce GTX i Quadro GPU-a su koristili upravo 384 CUDA jezgri kao sweet spot između snage i potrošnje.",
                "U vrhunskim mobilnim stanicama za CAD i video edit, 384 GB RAM-a je minimum za real-time obradu scena.",
                "Broj 384 označava granicu – ne zato što mora, već zato što iza nje leže samo ekstremi.",
                "Kada tehnologija koristi 384 – to nije slučajno. To je znak ozbiljne moći."
        };

        String quote = quotes[new Random().nextInt(quotes.length)];

        String baseMessage = "Ne možete imati više od 384 kredita na računu.\n\n";
        SpannableString styledMessage = new SpannableString(baseMessage + quote);
        styledMessage.setSpan(
                new StyleSpan(Typeface.ITALIC),
                baseMessage.length(),
                baseMessage.length() + quote.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        new AlertDialog.Builder(getContext())
                .setTitle("Kreditni limit dostignut!")
                .setMessage(styledMessage)
                .setPositiveButton("U redu", null)
                .setCancelable(false)
                .show();
    }

    public class BounceInterpolator implements Interpolator {
        private double mAmplitude = 1;
        private double mFrequency = 10;

        public BounceInterpolator() {}

        public BounceInterpolator(double amplitude, double frequency) {
            mAmplitude = amplitude;
            mFrequency = frequency;
        }

        public float getInterpolation(float time) {
            return (float) (-1 * Math.pow(Math.E, -time/ mAmplitude) *
                    Math.cos(mFrequency * time) + 1);
        }
    }

    private void showSlotPurchaseDialog() {
        firestore.collection("users").document(user.getUid())
                .get().addOnSuccessListener(documentSnapshot -> {
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
                        Toast.makeText(getContext(),
                                "Nema dostupnih paketa ili nedovoljno kredita",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    new AlertDialog.Builder(getContext())
                            .setTitle("Odaberite paket slotova")
                            .setItems(options.toArray(new String[0]), (dialog, which) -> {
                                int packageIndex = availablePackages.get(which);
                                purchaseSlots(packageIndex);
                            })
                            .setNegativeButton("Odustani", null)
                            .show();
                });
    }

    private void purchaseSlots(int packageIndex) {
        long price = slotPrices[packageIndex];
        long slots = slotPackages[packageIndex];

        firestore.runTransaction(transaction -> {
            try {
                DocumentSnapshot snapshot = transaction.get(
                        firestore.collection("users").document(user.getUid()));

                Long credits = snapshot.getLong("credits");
                Long purchased = snapshot.getLong("purchasedSlots");

                long currentCredits = credits != null ? credits : 0L;
                long currentPurchased = purchased != null ? purchased : 0L;

                if(currentCredits < price) {
                    throw new RuntimeException("Nedovoljno kredita");
                }
                if(currentPurchased >= slots) {
                    throw new RuntimeException("Već imate veći paket");
                }

                transaction.update(snapshot.getReference(),
                        "credits", currentCredits - price);
                transaction.update(snapshot.getReference(),
                        "purchasedSlots", slots);

                return null;
            } catch (Exception e) {
                Log.e("TransactionError", "Greška u transakciji: ", e);
                throw e;
            }
        }).addOnSuccessListener(aVoid -> {
            loadUserInfo();
            Toast.makeText(getContext(),
                    "Uspešno kupljeno " + slots + " dodatnih slotova!",
                    Toast.LENGTH_SHORT).show();

            if(getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).onSlotPurchaseSuccess();
            }
        }).addOnFailureListener(e -> {
            String errorMessage = e.getCause() != null ?
                    e.getCause().getMessage() : e.getMessage();
            Toast.makeText(getContext(),
                    "Greška: " + errorMessage,
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void logoutUser() {
        auth.signOut();
        startActivity(new Intent(getActivity(), LoginActivity.class));
        getActivity().finish();
    }

    private boolean isNewDay(long lastTimestamp) {
        Calendar lastCal = Calendar.getInstance();
        lastCal.setTimeInMillis(lastTimestamp);

        Calendar nowCal = Calendar.getInstance();

        return lastCal.get(Calendar.DAY_OF_YEAR) != nowCal.get(Calendar.DAY_OF_YEAR) ||
                lastCal.get(Calendar.YEAR) != nowCal.get(Calendar.YEAR);
    }

    private void updateFreeTier(int newValue) {
        firestore.collection("users").document(user.getUid())
                .update(
                        "freeTierUsed", newValue,
                        "lastFreeTierDate", System.currentTimeMillis()
                );
    }

    private void showProfilePictureDialog() {
        // Always inflate a NEW view
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_profile_pictures, null);
        GridView gridView = dialogView.findViewById(R.id.gridView);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.CustomDialog);
        builder.setView(dialogView);

        // Create dialog (DON'T SHOW IT YET)
        profilePictureDialog = builder.create();

        // Set dialog height to show 3.5 rows
        Window window = profilePictureDialog.getWindow();
        if (window != null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int screenHeight = displayMetrics.heightPixels;

            // Calculate height to show 3.5 rows (each row is 150dp)
            int rowHeight = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    150,
                    getResources().getDisplayMetrics()
            );
            int dialogHeight = (int) (rowHeight * 3.5);

            // Limit to 70% of screen height
            int maxHeight = (int) (screenHeight * 0.7);
            dialogHeight = Math.min(dialogHeight, maxHeight);

            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, dialogHeight);
            window.setBackgroundDrawableResource(android.R.color.transparent);

            // Add entrance animation
            window.setWindowAnimations(R.style.DialogAnimation);
        }

        // Now show the dialog
        profilePictureDialog.show();

        // Load data after showing dialog
        loadProfilePictureData(gridView);
    }

    private void loadProfilePictureData(GridView gridView) { // Add GridView parameter
        firestore.collection("users").document(user.getUid()).get().addOnSuccessListener(document -> {
            purchasedPfps = (List<String>) document.get("purchasedPfps");
            if (purchasedPfps == null) purchasedPfps = new ArrayList<>();

            // Separate unlocked and locked pictures
            unlockedPfps.clear();
            lockedPfps.clear();

            // First 10 are always free
            for (int i = 1; i <= 10; i++) {
                unlockedPfps.add("ic_pfp_" + i);
            }

            // Add purchased pictures
            unlockedPfps.addAll(purchasedPfps);

            // Create locked list (remove duplicates)
            for (String pfp : allPfps) {
                if (!unlockedPfps.contains(pfp)) {
                    lockedPfps.add(pfp);
                }
            }

            // Shuffle both lists
            Collections.shuffle(unlockedPfps);
            Collections.shuffle(lockedPfps);

            // Combine lists (unlocked first)
            List<String> allImages = new ArrayList<>(unlockedPfps);
            allImages.addAll(lockedPfps);

            // Initialize adapter with current context
            adapter = new ProfilePictureAdapter(requireContext(), allImages, unlockedPfps);
            gridView.setAdapter(adapter);

            gridView.setOnItemClickListener((parent, view, position, id) -> {
                String selectedPfp = allImages.get(position);
                handleProfilePictureSelection(selectedPfp);
            });
        });
    }

    private void handleProfilePictureSelection(String pfpName) {
        // Already unlocked
        if (unlockedPfps.contains(pfpName)) {
            updateProfilePicture(pfpName);
            return;
        }

        // Needs purchase
        new AlertDialog.Builder(getContext())
                .setTitle("Kupovina profila")
                .setMessage("Želite otključati ovu profilnu sliku za 5 kredita?")
                .setPositiveButton("Kupi", (dialog, which) -> purchaseProfilePicture(pfpName))
                .setNegativeButton("Otkaži", null)
                .show();
    }

    private void purchaseProfilePicture(String pfpName) {
        if (credits < 5) {
            Toast.makeText(getContext(), "Nedovoljno kredita!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update Firestore
        Map<String, Object> updates = new HashMap<>();
        updates.put("credits", FieldValue.increment(-5));
        updates.put("purchasedPfps", FieldValue.arrayUnion(pfpName));

        firestore.collection("users").document(user.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    purchasedPfps.add(pfpName);
                    unlockedPfps.add(pfpName);
                    lockedPfps.remove(pfpName);
                    updateProfilePicture(pfpName);
                    loadUserInfo(); // Refresh credit display
                    Toast.makeText(getContext(), "Slika uspješno kupljena!", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateProfilePicture(String pfpName) {
        // Update Firestore
        firestore.collection("users").document(user.getUid())
                .update("pfp", pfpName)
                .addOnSuccessListener(aVoid -> {
                    // Close dialog with animation
                    if (profilePictureDialog != null && profilePictureDialog.isShowing()) {
                        new Handler().postDelayed(() -> {
                            profilePictureDialog.dismiss();

                            // Update local UI with animation
                            int resId = getResources().getIdentifier(pfpName, "drawable", requireContext().getPackageName());
                            ivProfilePicture.setImageResource(resId);
                            ivProfilePicture.startAnimation(pulseAnimation);

                            // Show success message
                            Toast.makeText(getContext(), "Profilna slika promijenjena!", Toast.LENGTH_SHORT).show();
                        }, 300); // Short delay for smooth transition
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Greška: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setupProfilePictureListeners() {
        ivProfilePicture.setOnClickListener(v -> showEnlargedProfilePicture());

        ivProfilePicture.setOnLongClickListener(v -> {
            showUserPreview();
            return true;
        });

        ivEditPfp.setOnClickListener(v -> showProfilePictureDialog());
    }

    private void showUserPreview() {
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

        // Set current profile picture
        int resId = getResources().getIdentifier(currentPfp, "drawable", requireContext().getPackageName());
        if (resId != 0) {
            ivProfile.setImageResource(resId);
        }

        firestore.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        // Fetch username directly from Firestore
                        String username = document.getString("username");

                        if (username != null && !username.isEmpty()) {
                            tvUsername.setText(username); // Use username if available
                        } else {
                            // Fallback to first + last name if username missing
                            String firstName = document.getString("firstName");
                            String lastName = document.getString("lastName");
                            tvUsername.setText(firstName + " " + lastName);
                        }

                        // Join date
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
                            String joinText = "Pridružio se " + months[monthIndex] + " " + year;

                            if ("female".equals(gender)) {
                                joinText = "Pridružila se " + months[monthIndex] + " " + year;
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
                                        BadgeUtils.showBadgeInfoDialog(badge, requireContext()) // FIX
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

                        // Favorite brand
                        String brand = document.getString("favoriteBrand");
                        if (brand != null) {
                            int brandResId = BrandUtils.getBrandLogoResource(brand);
                            if (brandResId != 0) {
                                ivBrand.setImageResource(brandResId);
                            }
                        }

                        // Analysis count
                        Long totalAnalysis = document.getLong("totalAnalysisCount");
                        if (totalAnalysis != null) {
                            tvAnalysisCount.setText(String.format(Locale.getDefault(),
                                    "%d analiza ukupno", totalAnalysis));
                        }
                    }
                });
    }

    private void showEnlargedProfilePicture() {
        if (currentPfp == null || currentPfp.isEmpty()) return;

        int resId = getResources().getIdentifier(currentPfp, "drawable", requireContext().getPackageName());
        if (resId == 0) return;

        // Create transparent dialog with animation
        Dialog dialog = new Dialog(requireContext(), android.R.style.Theme_Translucent_NoTitleBar);
        dialog.setContentView(R.layout.dialog_enlarged_pfp);

        // Add animation
        Window window = dialog.getWindow();
        if (window != null) {
            window.setWindowAnimations(R.style.DialogAnimation);
        }

        ImageView enlargedImage = dialog.findViewById(R.id.ivEnlargedPfp);
        enlargedImage.setImageResource(resId);

        // Close dialog when clicked anywhere
        View rootView = dialog.findViewById(android.R.id.content);
        rootView.setOnClickListener(v -> dialog.dismiss());

        // Set dimmed background
        if (window != null) {
            window.setDimAmount(0.8f); // 80% dim
        }

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(refreshRunnable);

        // Oslobađanje svih view referenci
        tvEmail = null;
        tvName = null;
        tvCoinValue = null;
        coinContainer = null;
        ivShineOverlay = null;
        btnBuyCredits = null;
        tvSlotInfo = null;
        btnBuySlots = null;
        ivFavoriteBrand = null;
        tvBrandName = null;
        tvBrandDescription = null;
        btnChangeBrand = null;
        btnViewOnOlx = null;
        switchNotifications = null;
        ivProfilePicture = null;
        ivEditPfp = null;

        if (profilePictureDialog != null && profilePictureDialog.isShowing()) {
            profilePictureDialog.dismiss();
            profilePictureDialog = null;
        }
    }
}