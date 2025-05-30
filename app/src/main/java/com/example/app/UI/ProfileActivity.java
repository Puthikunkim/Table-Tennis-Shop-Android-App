package com.example.app.UI;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import com.example.app.R;
import com.example.app.Data.FirestoreRepository;
import com.example.app.databinding.ActivityProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends BaseActivity<ActivityProfileBinding> {

    private FirebaseAuth mAuth;
    private FirestoreRepository firestoreRepository;
    private static final String TAG = "ProfileActivity";

    @Override
    protected ActivityProfileBinding inflateContentBinding() {
        return ActivityProfileBinding.inflate(getLayoutInflater());
    }

    @Override
    protected int getSelectedMenuItemId() {
        return R.id.profile;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        firestoreRepository = FirestoreRepository.getInstance();

        // Assign listeners
        binding.buttonSignIn.setOnClickListener(signInButtonClickListener);
        binding.buttonCreateAccount.setOnClickListener(createAccountButtonClickListener);
        binding.closeSignIn.setOnClickListener(closeSignInClickListener);
        binding.closeCreate.setOnClickListener(closeCreateClickListener);
        binding.submitSignIn.setOnClickListener(submitSignInClickListener);
        binding.submitCreate.setOnClickListener(submitCreateClickListener);
        binding.buttonSignOut.setOnClickListener(signOutButtonClickListener);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        updateUI(user);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            binding.mainContentLoggedOut.setVisibility(View.GONE);
            binding.signInForm.setVisibility(View.GONE);
            binding.createAccountForm.setVisibility(View.GONE);
            binding.mainContentLoggedIn.setVisibility(View.VISIBLE);

            binding.textWelcome.setText(
                    "Welcome, " + (user.getDisplayName() != null ? user.getDisplayName() : "User") + "!"
            );
            binding.textEmail.setText("Email: " + user.getEmail());
        } else {
            binding.mainContentLoggedOut.setVisibility(View.VISIBLE);
            binding.mainContentLoggedIn.setVisibility(View.GONE);
            binding.signInForm.setVisibility(View.GONE);
            binding.createAccountForm.setVisibility(View.GONE);
        }
    }

    private final View.OnClickListener submitSignInClickListener = view -> {
        String email = binding.inputSignInEmail.getText().toString().trim();
        String password = binding.inputSignInPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                        Toast.makeText(this, "Signed in as: " + user.getEmail(), Toast.LENGTH_SHORT).show();
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(this,
                                "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                        updateUI(null);
                    }
                });
    };

    private final View.OnClickListener submitCreateClickListener = view -> {
        String email = binding.inputEmail.getText().toString().trim();
        String password = binding.inputCreatePassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Map<String, Object> userProfile = new HashMap<>();
                            userProfile.put("email", user.getEmail());
                            userProfile.put("createdAt", FieldValue.serverTimestamp());

                            firestoreRepository.createUserProfile(
                                    user.getUid(),
                                    userProfile,
                                    new FirestoreRepository.UserProfileCallback() {
                                        @Override
                                        public void onSuccess() {
                                            Log.d(TAG, "User profile created");
                                            updateUI(user);
                                            Toast.makeText(ProfileActivity.this,
                                                    "Account created and signed in as: " + user.getEmail(),
                                                    Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            Log.w(TAG, "Profile save failed", e);
                                            updateUI(user);
                                            Toast.makeText(ProfileActivity.this,
                                                    "Created but profile save failed: " + e.getMessage(),
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    }
                            );
                        }
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(this,
                                "Account creation failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                        updateUI(null);
                    }
                });
    };


    private final View.OnClickListener signInButtonClickListener = v -> { /* TODO */ };
    private final View.OnClickListener createAccountButtonClickListener = v -> { /* TODO */ };
    private final View.OnClickListener closeSignInClickListener = v -> { /* TODO */ };
    private final View.OnClickListener closeCreateClickListener = v -> { /* TODO */ };
    private final View.OnClickListener signOutButtonClickListener = v -> { /* TODO */ };
}
