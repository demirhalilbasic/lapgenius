package com.example.techanalysisapp3.ui.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.techanalysisapp3.R;
import com.example.techanalysisapp3.model.FunnyMessages;
import com.example.techanalysisapp3.ui.activities.LoginActivity;
import com.example.techanalysisapp3.ui.activities.MainActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class EditProfileFragment extends Fragment {

    private TextInputEditText etFirstName, etLastName, etPassword;
    private TextInputEditText etUsername, etEmail;
    private TextInputLayout tilPassword;
    private Button btnSave, btnCancel, btnDelete;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private String originalFirstName, originalLastName;
    private boolean isSaveEnabled = true;
    private Handler cooldownHandler = new Handler(Looper.getMainLooper());
    private static final String PREFS_NAME = "EditProfilePrefs";
    private static final String KEY_LAST_SAVE_TIME = "lastSaveTime";
    private SharedPreferences sharedPreferences;
    private LinearLayout emailVerificationWarning;
    private ImageView ivVerificationIcon;
    private TextView tvVerificationText;
    private MaterialButton btnSendVerification;
    private boolean isEmailVerified = false;
    private MaterialButton btnChangeEmail;
    private FirebaseAuth.AuthStateListener authStateListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        setupViews(view);
        loadUserData();
        setupListeners();
        checkCooldown();

        return view;
    }

    private void setupViews(View view) {
        etFirstName = view.findViewById(R.id.etFirstName);
        etLastName = view.findViewById(R.id.etLastName);
        etPassword = view.findViewById(R.id.etPassword);
        tilPassword = view.findViewById(R.id.tilPassword);
        btnSave = view.findViewById(R.id.btnSave);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnDelete = view.findViewById(R.id.btnDelete);
        btnChangeEmail = view.findViewById(R.id.btnChangeEmail);

        etUsername = view.findViewById(R.id.etUsername);
        etEmail = view.findViewById(R.id.etEmail);
        etUsername.setEnabled(false);
        etEmail.setEnabled(false);

        emailVerificationWarning = view.findViewById(R.id.emailVerificationWarning);
        ivVerificationIcon = view.findViewById(R.id.ivVerificationIcon);
        tvVerificationText = view.findViewById(R.id.tvVerificationText);
        btnSendVerification = view.findViewById(R.id.btnSendVerification);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
    }

    private void loadUserData() {
        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        originalFirstName = documentSnapshot.getString("firstName");
                        originalLastName = documentSnapshot.getString("lastName");

                        etFirstName.setText(originalFirstName);
                        etLastName.setText(originalLastName);
                        etUsername.setText(documentSnapshot.getString("username"));
                        etEmail.setText(user.getEmail());

                        Boolean verified = documentSnapshot.getBoolean("emailVerified");
                        isEmailVerified = verified != null ? verified : false;
                        updateEmailVerificationUI();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Greška pri učitavanju podataka: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveChanges());
        btnCancel.setOnClickListener(v -> requireActivity().onBackPressed());
        btnDelete.setOnClickListener(v -> showDeleteConfirmation());
        btnChangeEmail.setOnClickListener(v -> showChangeEmailDialog());

        etPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilPassword.setError(null);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void saveChanges() {
        long lastSaveTime = sharedPreferences.getLong(KEY_LAST_SAVE_TIME, 0);
        long currentTime = System.currentTimeMillis();
        long cooldownDuration = 5 * 60 * 1000;
        long remainingTime = cooldownDuration - (currentTime - lastSaveTime);

        if (!isSaveEnabled) {
            if (remainingTime > 0) {
                int minutes = (int) (remainingTime / (60 * 1000));
                String message;
                if (minutes == 1) {
                    message = "Pričekajte još 1 minutu prije nove promjene!";
                } else if (minutes > 1) {
                    message = "Pričekajte još " + minutes + " minuta prije nove promjene!";
                } else {
                    message = "Još malo... strpljenja!";
                }
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                return;
            } else {
                isSaveEnabled = true;
                btnSave.setAlpha(1f);
            }
        }

        String newFirstName = etFirstName.getText().toString().trim();
        String newLastName = etLastName.getText().toString().trim();
        String newPassword = etPassword.getText().toString().trim();

        if (!validateName(newFirstName, etFirstName, "ime")) return;
        if (!validateName(newLastName, etLastName, "prezime")) return;
        if (!TextUtils.isEmpty(newPassword) && !validatePassword(newPassword)) return;

        updateUserData(newFirstName, newLastName, newPassword);
    }

    private boolean validateName(String name, TextInputEditText field, String fieldName) {
        if (name.isEmpty()) {
            field.setError("Unesite " + fieldName);
            return false;
        }
        if (!name.matches("[A-ZŠĐČĆŽ][a-zšđčćž]+")) {
            field.setError("Neispravan format");
            return false;
        }
        if (name.length() > 30) {
            field.setError("Maksimalno 30 karaktera");
            return false;
        }
        return true;
    }

    private boolean validatePassword(String password) {
        String passwordRegex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        if (!password.matches(passwordRegex)) {
            tilPassword.setError("Zahtijeva: veliko slovo, malo slovo, broj i specijalni znak (@$!%*?&)");
            return false;
        }
        return true;
    }

    private void updateUserData(String firstName, String lastName, String password) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("firstName", firstName);
        updates.put("lastName", lastName);

        db.collection("users").document(user.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (!TextUtils.isEmpty(password)) {
                        user.updatePassword(password)
                                .addOnSuccessListener(aVoid1 -> completeUpdate())
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(), "Greška pri promjeni lozinke: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        completeUpdate();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Greška pri ažuriranju: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void checkCooldown() {
        long lastSaveTime = sharedPreferences.getLong(KEY_LAST_SAVE_TIME, 0);
        long currentTime = System.currentTimeMillis();
        long cooldownDuration = 5 * 60 * 1000;
        long remainingTime = cooldownDuration - (currentTime - lastSaveTime);

        if (remainingTime > 0) {
            isSaveEnabled = false;
            btnSave.setAlpha(0.5f); // visually indicate cooldown
        } else {
            isSaveEnabled = true;
            btnSave.setAlpha(1f);
        }
    }

    private void completeUpdate() {
        Toast.makeText(getContext(), "Promjene spremljene!", Toast.LENGTH_SHORT).show();
        isSaveEnabled = false;
        btnSave.setAlpha(0.5f);
        long currentTime = System.currentTimeMillis();
        sharedPreferences.edit().putLong(KEY_LAST_SAVE_TIME, currentTime).apply();
        cooldownHandler.postDelayed(() -> {
            isSaveEnabled = true;
            btnSave.setAlpha(1f);
        }, 5 * 60 * 1000);

        Fragment parent = getParentFragment();
        if (parent instanceof ProfileFragment) {
            ((ProfileFragment) parent).loadUserInfo();
        }
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showBottomNavigation();
        }
        requireActivity().onBackPressed();
    }

    private void showChangeEmailDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Promjena email adrese");

        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(50, 10, 50, 10);

        TextView tvCurrentEmail = new TextView(requireContext());
        tvCurrentEmail.setText("Trenutni email: " + user.getEmail());
        tvCurrentEmail.setTextSize(16);
        tvCurrentEmail.setPadding(0, 0, 0, 20);
        container.addView(tvCurrentEmail);

        TextInputLayout newEmailLayout = new TextInputLayout(requireContext());
        TextInputEditText etNewEmail = new TextInputEditText(newEmailLayout.getContext());
        etNewEmail.setHint("Novi email");
        etNewEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        newEmailLayout.addView(etNewEmail);
        container.addView(newEmailLayout);

        TextInputLayout passwordLayout = new TextInputLayout(requireContext());
        TextInputEditText etPassword = new TextInputEditText(passwordLayout.getContext());
        etPassword.setHint("Trenutna lozinka");
        etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        passwordLayout.addView(etPassword);
        container.addView(passwordLayout);

        builder.setView(container);

        builder.setPositiveButton("Promijeni", (dialog, which) -> {
            String newEmail = etNewEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (!isValidEmail(newEmail)) {
                Toast.makeText(requireContext(), "Unesite ispravan email format", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.isEmpty()) {
                Toast.makeText(requireContext(), "Unesite lozinku za potvrdu", Toast.LENGTH_SHORT).show();
                return;
            }

            changeUserEmail(newEmail, password);
        });

        builder.setNegativeButton("Otkaži", null);

        etNewEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isValidEmail(s.toString())) {
                    newEmailLayout.setError("Neispravan format emaila");
                } else {
                    newEmailLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void changeUserEmail(String newEmail, String password) {
        db.collection("users")
                .whereEqualTo("email", newEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            Toast.makeText(
                                    requireContext(),
                                    "Ovaj email je već u upotrebi",
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }

                        proceedWithEmailChange(newEmail, password);
                    } else {
                        Toast.makeText(
                                requireContext(),
                                "Greška pri provjeri emaila: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void proceedWithEmailChange(String newEmail, String password) {
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);

        user.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        user.verifyBeforeUpdateEmail(newEmail)
                                .addOnCompleteListener(updateTask -> {
                                    if (updateTask.isSuccessful()) {
                                        showEmailChangedDialog(newEmail);
                                    } else {
                                        Toast.makeText(
                                                requireContext(),
                                                "Greška: " + updateTask.getException().getMessage(),
                                                Toast.LENGTH_LONG
                                        ).show();
                                    }
                                });
                    } else {
                        Toast.makeText(
                                requireContext(),
                                "Pogrešna lozinka: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void showEmailChangedDialog(String newEmail) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Zahtjev za promjenu emaila poslan");

        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(50, 30, 50, 30);

        TextView tvMessage = new TextView(requireContext());
        tvMessage.setText("Poslali smo verifikacioni email na:\n\n" + newEmail +
                "\n\nMolimo potvrdite novu adresu.\n\n" +
                "NAPOMENA: Nakon potvrde, email će biti automatski sinkroniziran.");
        tvMessage.setTextSize(16);
        tvMessage.setGravity(Gravity.CENTER);
        container.addView(tvMessage);

        builder.setView(container);
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showDeleteConfirmation() {
        int randomIndex = new Random().nextInt(FunnyMessages.GOODBYE_MESSAGES.length);
        String message = FunnyMessages.GOODBYE_MESSAGES[randomIndex];

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Obriši nalog?");
        builder.setMessage(message + "\n\nUnesite lozinku za potvrdu:");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("DA, OBRIŠI", (d, which) -> {
            String password = input.getText().toString().trim();
            if (!password.isEmpty()) {
                deleteAccount(password);
            } else {
                Toast.makeText(getContext(), "Morate unijeti lozinku!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Odustani", null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positive.setTextColor(ContextCompat.getColor(requireContext(), R.color.secondary));
        });
        dialog.show();
    }

    private void deleteAccount(String password) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (user == null || user.getEmail() == null) return;

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
        user.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        db.collection("users").document(user.getUid())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    user.delete()
                                            .addOnSuccessListener(aVoid1 -> {
                                                Toast.makeText(getContext(), "Nalog obrisan", Toast.LENGTH_SHORT).show();
                                                auth.signOut();
                                                startActivity(new Intent(getActivity(), LoginActivity.class));
                                                requireActivity().finish();
                                            })
                                            .addOnFailureListener(e ->
                                                    Toast.makeText(
                                                            getContext(),
                                                            "Greška pri brisanju: " + (e.getMessage() != null ? e.getMessage() : "Nepoznata greška"), // Rukovanje null porukama
                                                            Toast.LENGTH_SHORT
                                                    ).show());
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(
                                                getContext(),
                                                "Greška pri brisanju podataka: " + (e.getMessage() != null ? e.getMessage() : "Nepoznata greška"),
                                                Toast.LENGTH_SHORT
                                        ).show());
                    } else {
                        String error = task.getException() != null ?
                                task.getException().getMessage() :
                                "Nepoznata greška";
                        Toast.makeText(
                                getContext(),
                                "Pogrešna lozinka: " + error,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void updateEmailVerificationUI() {
        if (isEmailVerified) {
            emailVerificationWarning.setVisibility(View.VISIBLE);
            ivVerificationIcon.setImageResource(R.drawable.ic_success);
            tvVerificationText.setText(R.string.email_verified_success);
            emailVerificationWarning.setBackgroundResource(R.drawable.bg_success);
            btnSendVerification.setVisibility(View.GONE);
        } else {
            emailVerificationWarning.setVisibility(View.VISIBLE);
            ivVerificationIcon.setImageResource(R.drawable.ic_warning);
            tvVerificationText.setText(R.string.email_not_verified_warning);
            emailVerificationWarning.setBackgroundResource(R.drawable.bg_warning);
            btnSendVerification.setVisibility(View.VISIBLE);

            btnSendVerification.setOnClickListener(v -> sendVerificationEmail());
        }
    }

    private void sendVerificationEmail() {
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            btnSendVerification.setText(R.string.verification_sent);
                            btnSendVerification.setBackgroundTintList(
                                    ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.transparent)));
                            btnSendVerification.setStrokeColor(ColorStateList.valueOf(
                                    ContextCompat.getColor(requireContext(), R.color.secondary)));
                            btnSendVerification.setEnabled(false);

                            Toast.makeText(
                                    getContext(),
                                    "Verifikacioni email je poslat!",
                                    Toast.LENGTH_LONG
                            ).show();
                        } else {
                            Toast.makeText(
                                    getContext(),
                                    "Greška: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
        }
    }

    private void syncEmailWithFirestore(FirebaseUser currentUser) {
        db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String firestoreEmail = documentSnapshot.getString("email");
                        String authEmail = currentUser.getEmail();
                        boolean firestoreVerified = Boolean.TRUE.equals(
                                documentSnapshot.getBoolean("emailVerified")
                        );

                        if (firestoreEmail != null && authEmail != null &&
                                (!firestoreEmail.equals(authEmail) || !firestoreVerified)) {

                            Map<String, Object> updates = new HashMap<>();
                            updates.put("email", authEmail);
                            updates.put("emailVerified", true);

                            db.collection("users").document(currentUser.getUid())
                                    .update(updates)
                                    .addOnSuccessListener(aVoid -> {
                                        if (isAdded()) {
                                            etEmail.setText(authEmail);
                                            isEmailVerified = true;
                                            updateEmailVerificationUI();
                                            Toast.makeText(
                                                    getContext(),
                                                    "Email adresa ažurirana",
                                                    Toast.LENGTH_SHORT
                                            ).show();
                                        }
                                    })
                                    .addOnFailureListener(e -> Log.e("SYNC_EMAIL", "Greška pri ažuriranju", e));
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("SYNC_EMAIL", "Greška pri dohvaćanju podataka", e));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authStateListener = firebaseAuth -> {
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if (currentUser != null) {
                if (currentUser.isEmailVerified()) {
                    syncEmailWithFirestore(currentUser);
                }

                db.collection("users").document(currentUser.getUid())
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                Boolean verified = documentSnapshot.getBoolean("emailVerified");
                                isEmailVerified = verified != null ? verified : false;
                                updateEmailVerificationUI();
                            }
                        });
            }
        };

        FirebaseAuth.getInstance().addAuthStateListener(authStateListener);

        FirebaseAuth.getInstance().addAuthStateListener(firebaseAuth -> {
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if (currentUser != null && currentUser.isEmailVerified()) {
                db.collection("users").document(currentUser.getUid())
                        .update("emailVerified", true)
                        .addOnSuccessListener(aVoid -> {
                            isEmailVerified = true;
                            updateEmailVerificationUI();
                        });
            }
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).showBottomNavigation();
                        }
                        requireActivity().getSupportFragmentManager().popBackStack();
                    }
                }
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (authStateListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
        }

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showBottomNavigation();
        }
        cooldownHandler.removeCallbacksAndMessages(null);
    }

}
