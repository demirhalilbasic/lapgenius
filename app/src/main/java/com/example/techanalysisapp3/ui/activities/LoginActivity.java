package com.example.techanalysisapp3.ui.activities;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.techanalysisapp3.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Random;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText emailField, passwordField;
    private MaterialButton loginButton;
    private View progressBar;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        progressBar = findViewById(R.id.progressBar);
        findViewById(R.id.tvForgot).setOnClickListener(v -> showPasswordResetDialog());

        loginButton.setOnClickListener(v -> loginUser());
        findViewById(R.id.tvRegisterPrompt).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        passwordField.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loginButton.performClick();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return true;
            }
            return false;
        });

        setupLogoAnimation();
    }

    private void setupLogoAnimation() {
        final LinearLayout logoContainer = findViewById(R.id.logoContainer);
        final int minDistance = 3;

        final List<Integer> allLogos = Arrays.asList(
                R.drawable.acer,
                R.drawable.acer_predator,
                R.drawable.acer_nitro,
                R.drawable.apple,
                R.drawable.asus,
                R.drawable.asus_rog,
                R.drawable.asus_tuf_gaming,
                R.drawable.asus_zenbook,
                R.drawable.dell,
                R.drawable.dell_xps,
                R.drawable.dell_alienware,
                R.drawable.gigabyte,
                R.drawable.gigabyte_aero,
                R.drawable.gigabyte_aorus,
                R.drawable.hp,
                R.drawable.hp_omen,
                R.drawable.hp_victus,
                R.drawable.lenovo,
                R.drawable.lenovo_thinkpad,
                R.drawable.lenovo_yoga,
                R.drawable.lenovo_legion,
                R.drawable.lg_gram,
                R.drawable.microsoft_surface,
                R.drawable.msi,
                R.drawable.msi_gaming,
                R.drawable.razer,
                R.drawable.samsung_galaxy_book,
                R.drawable.xmg
        );

        // Dimensions
        float density = getResources().getDisplayMetrics().density;
        final int desiredHeightPx = (int) (100 * density);
        final int marginPx = (int) (20 * density);

        // Recent buffer
        final Deque<Integer> recent = new ArrayDeque<>();
        final int initialCount = 20;
        for (int i = 0; i < initialCount; i++) {
            appendNextLogo(logoContainer, allLogos, recent, minDistance, desiredHeightPx, marginPx);
        }

        // Animator
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(10_000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());

        animator.addUpdateListener(animation -> {
            float fraction = animation.getAnimatedFraction();
            float translation = -fraction * logoContainer.getWidth();
            logoContainer.setTranslationX(translation);

            View firstChild = logoContainer.getChildAt(0);
            if (firstChild != null && -translation >= firstChild.getWidth() + marginPx * 2) {
                if (firstChild instanceof ImageView) {
                    ((ImageView) firstChild).setImageDrawable(null);
                }
                logoContainer.removeViewAt(0);
                appendNextLogo(logoContainer, allLogos, recent, minDistance, desiredHeightPx, marginPx);
                animation.setCurrentPlayTime(0);
            }
        });

        logoContainer.post(() -> {
            logoContainer.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
            animator.start();
        });
    }

    private void appendNextLogo(LinearLayout container,
                                List<Integer> allLogos,
                                Deque<Integer> recent,
                                int minDistance,
                                int heightPx,
                                int marginPx) {
        // Filter candidates
        List<Integer> candidates = new ArrayList<>(allLogos);
        candidates.removeAll(recent);
        if (candidates.isEmpty()) candidates = new ArrayList<>(allLogos);

        // Pick one
        int next = candidates.get(new Random().nextInt(candidates.size()));

        // Update recent buffer
        recent.addLast(next);
        if (recent.size() > minDistance) recent.removeFirst();

        // Measure aspect
        Context ctx = container.getContext();
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(ctx.getResources(), next, opts);
        float aspect = (float) opts.outWidth / opts.outHeight;
        int widthPx = (int) (heightPx * aspect);

        // Create ImageView
        ImageView iv = new ImageView(ctx);
        iv.setImageResource(next);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(widthPx, heightPx);
        lp.setMargins(marginPx, 0, marginPx, 0);
        iv.setLayoutParams(lp);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);

        container.addView(iv);
    }

    private void loginUser() {
        String email = emailField.getText().toString().trim();
        String pw = passwordField.getText().toString().trim();
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.setError("Neispravan email");
            return;
        }
        if (pw.length() < 6) {
            passwordField.setError("Min 6 karaktera");
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        auth.signInWithEmailAndPassword(email, pw)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(this, MainActivity.class);
                        // Forward shared_url if exists
                        if (getIntent().hasExtra("shared_url")) {
                            intent.putExtra("shared_url", getIntent().getStringExtra("shared_url"));
                        }
                        startActivity(intent);
                        finish();
                    } else {
                        String err = parseError(task.getException());
                        Toast.makeText(this, err, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private String parseError(Exception e) {
        if (e instanceof FirebaseAuthInvalidUserException)
            return "Korisnik ne postoji";
        if (e instanceof FirebaseAuthInvalidCredentialsException)
            return "Pogrešan email ili lozinka";
        return "Greška: " + e.getMessage();
    }

    private void showPasswordResetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Resetovanje Lozinke");

        // Create input layout
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(50, 10, 50, 10);

        TextInputLayout inputLayout = new TextInputLayout(this);
        inputLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        TextInputEditText input = new TextInputEditText(inputLayout.getContext());
        input.setHint("Unesite vaš email");
        input.setInputType(EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        // Pre-fill with existing email if valid
        String existingEmail = emailField.getText().toString().trim();
        if (!existingEmail.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(existingEmail).matches()) {
            input.setText(existingEmail);
        }

        inputLayout.addView(input);
        container.addView(inputLayout);

        builder.setView(container);

        builder.setPositiveButton("Pošalji", (dialog, which) -> {
            String email = input.getText().toString().trim();
            if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                sendPasswordResetEmail(email);
            } else {
                Toast.makeText(this, "Unesite validan email", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Otkaži", null);
        builder.show();
    }

    private void sendPasswordResetEmail(String email) {
        progressBar.setVisibility(View.VISIBLE);
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(
                                LoginActivity.this,
                                "Uputstvo za resetovanje lozinke je poslato na email",
                                Toast.LENGTH_LONG
                        ).show();
                    } else {
                        Toast.makeText(
                                LoginActivity.this,
                                "Greška: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        showExitConfirmationDialog();
    }

    private void showExitConfirmationDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Izlazak iz aplikacije")
                .setMessage("Da li želite da izađete iz aplikacije?")
                .setPositiveButton("Izađi", null)
                .setNegativeButton("Odustani", null)
                .setCancelable(false)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setTextColor(ContextCompat.getColor(this, R.color.secondary));
            positiveButton.setOnClickListener(v -> finishAffinity());

            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            negativeButton.setTextColor(ContextCompat.getColor(this, R.color.primary));
            negativeButton.setOnClickListener(v -> dialog.dismiss());
        });

        dialog.show();
    }
}