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
                        clearSignInForm();
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(this,
                                "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                        updateUI(null);
                    }
                });
    };


    private final View.OnClickListener signInButtonClickListener = v -> { /* TODO */ };
    private final View.OnClickListener createAccountButtonClickListener = v -> { /* TODO */ };
    private final View.OnClickListener closeSignInClickListener = v -> { /* TODO */ };
    private final View.OnClickListener closeCreateClickListener = v -> { /* TODO */ };
    private final View.OnClickListener submitSignInClickListener = v -> { /* TODO */ };
    private final View.OnClickListener submitCreateClickListener = v -> { /* TODO */ };
    private final View.OnClickListener signOutButtonClickListener = v -> { /* TODO */ };
}
