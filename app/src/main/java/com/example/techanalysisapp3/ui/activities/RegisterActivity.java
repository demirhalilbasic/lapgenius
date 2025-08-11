package com.example.techanalysisapp3.ui.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.techanalysisapp3.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText firstName, lastName, dob, username, email, password;
    private ChipGroup genderChipGroup;
    private int defaultChipBackgroundColor;
    private Chip chipMale, chipFemale;
    private MaterialButton registerButton;
    private View progressBar;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private Long selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupDatePicker();
        setupRegisterButton();
        setupFieldNavigation();
    }

    private void initializeViews() {
        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        dob = findViewById(R.id.dob);
        genderChipGroup = findViewById(R.id.genderChipGroup);
        chipMale = findViewById(R.id.chipMale);
        chipFemale = findViewById(R.id.chipFemale);
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        registerButton = findViewById(R.id.registerButton);
        progressBar = findViewById(R.id.regProgressBar);

        // Get default chip background color
        TypedArray a = obtainStyledAttributes(null, com.google.android.material.R.styleable.Chip, 0, com.google.android.material.R.style.Widget_MaterialComponents_Chip_Choice);
        defaultChipBackgroundColor = a.getColor(com.google.android.material.R.styleable.Chip_chipBackgroundColor, ContextCompat.getColor(this, android.R.color.background_light));
        a.recycle();

        setupChipGroup();
    }

    private void setupDatePicker() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -13);
        long minDate = calendar.getTimeInMillis();

        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setEnd(minDate)
                .build();

        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Odaberite datum rođenja")
                .setSelection(minDate)
                .setCalendarConstraints(constraints)
                .build();

        dob.setOnClickListener(v -> picker.show(getSupportFragmentManager(), "DOB_PICKER"));
        picker.addOnPositiveButtonClickListener(selection -> {
            selectedDate = selection;
            dob.setText(picker.getHeaderText());
        });
    }

    private void setupRegisterButton() {
        registerButton.setOnClickListener(v -> registerUser());
    }

    private void setupChipGroup() {
        genderChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipMale)) {
                chipMale.setChipBackgroundColorResource(R.color.lilac);
                chipFemale.setChipBackgroundColor(ColorStateList.valueOf(defaultChipBackgroundColor));
            } else if (checkedIds.contains(R.id.chipFemale)) {
                chipFemale.setChipBackgroundColorResource(R.color.secondary);
                chipMale.setChipBackgroundColor(ColorStateList.valueOf(defaultChipBackgroundColor));
            } else {
                // Reset colors if none selected
                chipMale.setChipBackgroundColor(ColorStateList.valueOf(defaultChipBackgroundColor));
                chipFemale.setChipBackgroundColor(ColorStateList.valueOf(defaultChipBackgroundColor));
            }
        });

        // Set initial colors
        if (!genderChipGroup.isSelected()) {
            chipMale.setChipBackgroundColor(ColorStateList.valueOf(defaultChipBackgroundColor));
            chipFemale.setChipBackgroundColor(ColorStateList.valueOf(defaultChipBackgroundColor));
        }
    }

    private void setupFieldNavigation() {
        setEditorAction(firstName, lastName);
        setEditorAction(lastName, dob);
        setEditorAction(username, email);
        setEditorAction(email, password);

        password.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                registerButton.performClick();
                return true;
            }
            return false;
        });
    }

    private void setEditorAction(TextInputEditText current, View next) {
        current.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                next.requestFocus();
                return true;
            }
            return false;
        });
    }

    private void registerUser() {
        String fName = firstName.getText().toString().trim();
        String lName = lastName.getText().toString().trim();
        String userName = username.getText().toString().trim();
        String emailInput = email.getText().toString().trim();
        String pwInput = password.getText().toString().trim();

        // Validate gender
        String gender = getSelectedGender();
        if (gender == null) {
            Toast.makeText(this, "Odaberite spol", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate birth date
        if (!validateBirthDate()) return;

        // Validate fields
        if (!validateFields(fName, lName, userName, emailInput, pwInput)) return;

        // Format names
        String formattedFirstName = formatName(fName);
        String formattedLastName = formatName(lName);

        progressBar.setVisibility(View.VISIBLE);

        // Check if username is unique
        checkUsernameUnique(userName, isUnique -> {
            if (!isUnique) {
                progressBar.setVisibility(View.GONE);
                username.setError("Korisničko ime je zauzeto");
                username.requestFocus();
                return;
            }

            // Create user after username is confirmed unique
            auth.createUserWithEmailAndPassword(emailInput, pwInput)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            handleRegistrationSuccess(
                                    task.getResult().getUser(),
                                    formattedFirstName,
                                    formattedLastName,
                                    gender,
                                    userName,
                                    emailInput
                            );
                        } else {
                            Toast.makeText(
                                    RegisterActivity.this,
                                    "Greška pri registraciji: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    });
        });
    }

    // Check if username is unique in Firestore
    private void checkUsernameUnique(String username, UsernameCheckCallback callback) {
        db.collection("users")
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onResult(task.getResult().isEmpty());
                    } else {
                        callback.onResult(false);
                    }
                });
    }

    private interface UsernameCheckCallback {
        void onResult(boolean isUnique);
    }

    private String getSelectedGender() {
        int checkedChipId = genderChipGroup.getCheckedChipId();
        if (checkedChipId == R.id.chipMale) {
            return "male";
        } else if (checkedChipId == R.id.chipFemale) {
            return "female";
        }
        return null;
    }

    private boolean validateBirthDate() {
        if (selectedDate == null) {
            dob.setError("Odaberite datum rođenja");
            dob.requestFocus();
            return false;
        }

        Calendar minDob = Calendar.getInstance();
        minDob.add(Calendar.YEAR, -13);
        if (selectedDate > minDob.getTimeInMillis()) {
            dob.setError("Morate imati najmanje 13 godina");
            dob.requestFocus();
            return false;
        }
        return true;
    }

    private boolean validateFields(String fName, String lName, String userName, String email, String password) {
        return validateName(firstName, fName, "ime") &&
                validateName(lastName, lName, "prezime") &&
                validateUsername(userName) &&
                validateEmail(email) &&
                validatePassword(password);
    }

    private boolean validateName(TextInputEditText field, String value, String fieldName) {
        if (value.isEmpty()) {
            field.setError("Unesite " + fieldName);
            field.requestFocus();
            return false;
        }
        if (!value.matches("[A-ZŠĐČĆŽa-zšđčćž]+")) {
            field.setError("Samo slova su dozvoljena");
            field.requestFocus();
            return false;
        }
        if (value.length() > 30) {
            field.setError("Maksimalno 30 karaktera");
            field.requestFocus();
            return false;
        }
        return true;
    }

    private boolean validateUsername(String username) {
        if (username.isEmpty()) {
            this.username.setError("Unesite korisničko ime");
            this.username.requestFocus();
            return false;
        }
        if (!username.matches("^[a-zA-Z0-9._-]+$")) {
            this.username.setError("Dozvoljeni karakteri: slova, brojevi, _, ., -");
            this.username.requestFocus();
            return false;
        }
        if (username.length() > 15) {
            this.username.setError("Maksimalno 15 karaktera");
            this.username.requestFocus();
            return false;
        }
        return true;
    }

    private boolean validateEmail(String email) {
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            this.email.setError("Neispravan format emaila");
            this.email.requestFocus();
            return false;
        }
        return true;
    }

    private boolean validatePassword(String password) {
        String passwordRegex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        if (password.length() < 8) {
            this.password.setError("Minimalno 8 karaktera");
            this.password.requestFocus();
            return false;
        }
        if (!password.matches(passwordRegex)) {
            this.password.setError("Zahtijeva: veliko slovo, malo slovo, broj i specijalni znak (@$!%*?&)");
            this.password.requestFocus();
            return false;
        }
        return true;
    }

    private String formatName(String name) {
        if (name == null || name.isEmpty()) return name;
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }

    private void handleRegistrationSuccess(FirebaseUser user,
                                           String firstName,
                                           String lastName,
                                           String gender,
                                           String username,
                                           String email) {
        // Add account creation timestamp
        long createdAt = System.currentTimeMillis();

        Map<String, Object> userData = new HashMap<>();
        userData.put("firstName", firstName);
        userData.put("lastName", lastName);
        userData.put("dob", selectedDate);
        userData.put("gender", gender);
        userData.put("username", username);
        userData.put("email", email);
        userData.put("createdAt", createdAt); // Account creation timestamp
        userData.put("emailVerified", false);
        userData.put("favoriteBrand", "default");
        userData.put("credits", 10);
        userData.put("purchasedSlots", 0);
        userData.put("freeTierUsed", 0);
        userData.put("lastFreeTierDate", System.currentTimeMillis());
        userData.put("totalAnalysisCount", 0);
        userData.put("badges", new ArrayList<String>()); // Initialize empty badges list
        userData.put("badgeArraylist", new ArrayList<Double>()); // Initialize empty list
        userData.put("pfp", "ic_pfp_placeholder"); // Default profile picture
        userData.put("purchasedPfps", new ArrayList<String>());

        db.collection("users").document(user.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    startActivity(new Intent(this, MainActivity.class)
                            .putExtra("newUser", true));
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(
                        this,
                        "Greška pri spremanju podataka: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show());
    }
}