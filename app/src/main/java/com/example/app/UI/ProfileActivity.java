package com.example.app.UI;

import android.content.Intent;
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

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends BaseActivity<ActivityProfileBinding> {

    private static final String TAG = "ProfileActivity";
    private FirebaseAuth mAuth;
    private FirestoreRepository firestoreRepository; // Changed from FirebaseFirestore db;

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

        // Initialize Firebase Auth and FirestoreRepository
        mAuth = FirebaseAuth.getInstance();
        firestoreRepository = FirestoreRepository.getInstance(); // Get the singleton instance

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
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // User is signed in
            binding.mainContentLoggedOut.setVisibility(View.GONE);
            binding.signInForm.setVisibility(View.GONE);
            binding.createAccountForm.setVisibility(View.GONE);
            binding.mainContentLoggedIn.setVisibility(View.VISIBLE);

            binding.textWelcome.setText("Welcome, " + (user.getDisplayName() != null ? user.getDisplayName() : "User") + "!");
            binding.textEmail.setText("Email: " + user.getEmail());

            // You can fetch more user data from Firestore here if needed
            // For example, if you stored their full name in a 'users' collection
            // You would use firestoreRepository to get user data:
            // firestoreRepository.getUserProfile(user.getUid(), new FirestoreRepository.UserProfileCallback() {
            //     @Override
            //     public void onSuccess(Map<String, Object> userProfileData) {
            //         // Update UI with userProfileData
            //     }
            //     @Override
            //     public void onError(Exception e) {
            //         Log.e(TAG, "Error fetching user profile", e);
            //     }
            // });

        } else {
            // User is signed out
            binding.mainContentLoggedOut.setVisibility(View.VISIBLE);
            binding.mainContentLoggedIn.setVisibility(View.GONE);
            binding.signInForm.setVisibility(View.GONE);
            binding.createAccountForm.setVisibility(View.GONE);
        }
    }

    private final View.OnClickListener signInButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            binding.signInForm.setVisibility(View.VISIBLE);
            binding.createAccountForm.setVisibility(View.GONE);
            binding.mainContentLoggedOut.setVisibility(View.GONE);
        }
    };

    private final View.OnClickListener createAccountButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            binding.createAccountForm.setVisibility(View.VISIBLE);
            binding.signInForm.setVisibility(View.GONE);
            binding.mainContentLoggedOut.setVisibility(View.GONE);
        }
    };

    private final View.OnClickListener closeSignInClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            binding.signInForm.setVisibility(View.GONE);
            binding.mainContentLoggedOut.setVisibility(View.VISIBLE); // Show logged out options again
            clearSignInForm();
        }
    };

    private final View.OnClickListener closeCreateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            binding.createAccountForm.setVisibility(View.GONE);
            binding.mainContentLoggedOut.setVisibility(View.VISIBLE); // Show logged out options again
            clearCreateAccountForm();
        }
    };

    private final View.OnClickListener submitSignInClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String email = binding.inputSignInEmail.getText().toString().trim();
            String password = binding.inputSignInPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(ProfileActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(ProfileActivity.this, task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                            Toast.makeText(ProfileActivity.this, "Signed in as: " + user.getEmail(), Toast.LENGTH_SHORT).show();
                            clearSignInForm();
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(ProfileActivity.this, "Authentication failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                            updateUI(null);
                        }
                    });
        }
    };

    private final View.OnClickListener submitCreateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String email = binding.inputEmail.getText().toString().trim();
            String password = binding.inputCreatePassword.getText().toString().trim();
            String name = binding.inputName.getText().toString().trim(); // ✅ Get name

            if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
                Toast.makeText(ProfileActivity.this, "Please enter name, email and password", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.length() < 6) {
                Toast.makeText(ProfileActivity.this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(ProfileActivity.this, task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            if (user != null) {
                                // ✅ Update Firebase Auth display name
                                user.updateProfile(new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .build()).addOnCompleteListener(profileTask -> {
                                    if (profileTask.isSuccessful()) {
                                        Log.d(TAG, "User display name updated.");
                                    } else {
                                        Log.w(TAG, "Failed to update display name", profileTask.getException());
                                    }
                                });

                                // ✅ Save user profile in Firestore
                                Map<String, Object> userProfile = new HashMap<>();
                                userProfile.put("email", user.getEmail());
                                userProfile.put("name", name); // Save name
                                userProfile.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

                                firestoreRepository.createUserProfile(user.getUid(), userProfile, new FirestoreRepository.UserProfileCallback() {
                                    @Override
                                    public void onSuccess() {
                                        Log.d(TAG, "User profile created in Firestore via repository");
                                        updateUI(user);
                                        Toast.makeText(ProfileActivity.this, "Account created and signed in as: " + name, Toast.LENGTH_SHORT).show();
                                        clearCreateAccountForm();
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Log.w(TAG, "Error creating user profile in Firestore via repository", e);
                                        updateUI(user);
                                        Toast.makeText(ProfileActivity.this, "Account created, but profile data save failed: " + e.getMessage(),
                                                Toast.LENGTH_LONG).show();
                                        clearCreateAccountForm();
                                    }
                                });

                            } else {
                                Log.e(TAG, "User was null after successful createUserWithEmailAndPassword");
                                updateUI(null);
                                Toast.makeText(ProfileActivity.this, "Error: User data missing after account creation.", Toast.LENGTH_LONG).show();
                            }

                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(ProfileActivity.this, "Account creation failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                            updateUI(null);
                        }
                    });
        }
    };

    // Sign Out
    private final View.OnClickListener signOutButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mAuth.signOut();
            updateUI(null); // Update UI to logged-out state
            Toast.makeText(ProfileActivity.this, "Signed out successfully", Toast.LENGTH_SHORT).show();
        }
    };

    // Helper methods to clear forms
    private void clearSignInForm() {
        binding.inputSignInEmail.setText("");
        binding.inputSignInPassword.setText("");
    }

    private void clearCreateAccountForm() {
        binding.inputName.setText(""); // 👈 Clear name field too
        binding.inputEmail.setText("");
        binding.inputCreatePassword.setText("");
    }
}