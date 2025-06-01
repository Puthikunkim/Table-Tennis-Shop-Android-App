package com.example.app.UI;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.app.Model.TableTennisProduct;
import com.example.app.R;
import com.example.app.Data.FirestoreRepository;
import com.example.app.databinding.ActivityProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends BaseActivity<ActivityProfileBinding> {

    private static final String TAG = "ProfileActivity";
    private FirebaseAuth mAuth;
    private FirestoreRepository firestoreRepository;

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
        firestoreRepository = FirestoreRepository.getInstance();

        // Assign button listeners
        binding.buttonSignIn.setOnClickListener(signInButtonClickListener);
        binding.buttonCreateAccount.setOnClickListener(createAccountButtonClickListener);
        binding.closeSignIn.setOnClickListener(closeSignInClickListener);
        binding.closeCreate.setOnClickListener(closeCreateClickListener);
        binding.submitSignIn.setOnClickListener(submitSignInClickListener);
        binding.submitCreate.setOnClickListener(submitCreateClickListener);
        binding.buttonSignOut.setOnClickListener(signOutButtonClickListener);

        setupCartButtons();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is already signed in and update the UI
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    // Updates the UI based on whether the user is signed in
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // Show the logged-in UI and populate user info
            binding.mainContentLoggedOut.setVisibility(View.GONE);
            binding.signInForm.setVisibility(View.GONE);
            binding.createAccountForm.setVisibility(View.GONE);
            binding.mainContentLoggedIn.setVisibility(View.VISIBLE);

            binding.textWelcome.setText("Welcome, " + (user.getDisplayName() != null ? user.getDisplayName() : "User") + "!");
            binding.textEmail.setText("Email: " + user.getEmail());


            // Ensure the UI updates properly
            fetchCartSummary(user.getUid());

        } else {
            // Show the logged-out UI
            binding.mainContentLoggedOut.setVisibility(View.VISIBLE);
            binding.mainContentLoggedIn.setVisibility(View.GONE);
            binding.signInForm.setVisibility(View.GONE);
            binding.createAccountForm.setVisibility(View.GONE);
        }
    }

    // Opens the sign-in form
    private final View.OnClickListener signInButtonClickListener = view -> {
        binding.signInForm.setVisibility(View.VISIBLE);
        binding.createAccountForm.setVisibility(View.GONE);
        binding.mainContentLoggedOut.setVisibility(View.GONE);
    };

    // Opens the create-account form
    private final View.OnClickListener createAccountButtonClickListener = view -> {
        binding.createAccountForm.setVisibility(View.VISIBLE);
        binding.signInForm.setVisibility(View.GONE);
        binding.mainContentLoggedOut.setVisibility(View.GONE);
    };

    // Closes the sign-in form and resets to logged-out state
    private final View.OnClickListener closeSignInClickListener = view -> {
        binding.signInForm.setVisibility(View.GONE);
        binding.mainContentLoggedOut.setVisibility(View.VISIBLE);
        clearSignInForm();
    };

    // Closes the create-account form and resets to logged-out state
    private final View.OnClickListener closeCreateClickListener = view -> {
        binding.createAccountForm.setVisibility(View.GONE);
        binding.mainContentLoggedOut.setVisibility(View.VISIBLE);
        clearCreateAccountForm();
    };

    // Handles sign-in logic
    private final View.OnClickListener submitSignInClickListener = view -> {
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
    };

    // Handles account creation logic
    private final View.OnClickListener submitCreateClickListener = view -> {
        String email = binding.inputEmail.getText().toString().trim();
        String password = binding.inputCreatePassword.getText().toString().trim();
        String name = binding.inputName.getText().toString().trim();

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
                            // Set the display name in Firebase Authentication
                            user.updateProfile(new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build()).addOnCompleteListener(profileTask -> {
                                if (profileTask.isSuccessful()) {
                                    Log.d(TAG, "User display name updated.");

                                    // Reload user to get updated display name immediately
                                    user.reload().addOnCompleteListener(reloadTask -> {
                                        FirebaseUser refreshedUser = mAuth.getCurrentUser();
                                        updateUI(refreshedUser);
                                    });

                                } else {
                                    Log.w(TAG, "Failed to update display name", profileTask.getException());
                                    updateUI(user);
                                }
                            });

                            // Store additional user data in Firestore
                            Map<String, Object> userProfile = new HashMap<>();
                            userProfile.put("email", user.getEmail());
                            userProfile.put("name", name);
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
    };

    // Handles sign out logic
    private final View.OnClickListener signOutButtonClickListener = view -> {
        mAuth.signOut();
        updateUI(null);
        Toast.makeText(ProfileActivity.this, "Signed out successfully", Toast.LENGTH_SHORT).show();
    };

    // Clears the sign-in form inputs
    private void clearSignInForm() {
        binding.inputSignInEmail.setText("");
        binding.inputSignInPassword.setText("");
    }

    // Clears the create-account form inputs
    private void clearCreateAccountForm() {
        binding.inputName.setText("");
        binding.inputEmail.setText("");
        binding.inputCreatePassword.setText("");
    }
}