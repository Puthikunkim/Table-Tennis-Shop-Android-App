package com.example.app.UI;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.app.R;
import com.example.app.databinding.ActivityProfileBinding;

public class ProfileActivity extends BaseActivity<ActivityProfileBinding> {

    @Override
    protected ActivityProfileBinding inflateContentBinding() {
        return ActivityProfileBinding.inflate(getLayoutInflater());
    }

    @Override
    protected int getSelectedMenuItemId() {
        return R.id.profile;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Assign listeners
        binding.buttonSignIn.setOnClickListener(signInButtonClickListener);
        binding.buttonCreateAccount.setOnClickListener(createAccountButtonClickListener);
        binding.closeSignIn.setOnClickListener(closeSignInClickListener);
        binding.closeCreate.setOnClickListener(closeCreateClickListener);
        binding.submitSignIn.setOnClickListener(submitSignInClickListener);
        binding.submitCreate.setOnClickListener(submitCreateClickListener);
    }

    // Show Sign In form
    private final View.OnClickListener signInButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            binding.signInForm.setVisibility(View.VISIBLE);
            binding.createAccountForm.setVisibility(View.GONE);
        }
    };

    // Show Create Account form
    private final View.OnClickListener createAccountButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            binding.createAccountForm.setVisibility(View.VISIBLE);
            binding.signInForm.setVisibility(View.GONE);
        }
    };

    // Close Sign In form
    private final View.OnClickListener closeSignInClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            binding.signInForm.setVisibility(View.GONE);
        }
    };

    // Close Create Account form
    private final View.OnClickListener closeCreateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            binding.createAccountForm.setVisibility(View.GONE);
        }
    };

    // Submit Sign In
    private final View.OnClickListener submitSignInClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String username = binding.inputUsername.getText().toString().trim();
            String password = binding.inputPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(ProfileActivity.this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ProfileActivity.this, "Signing in as: " + username, Toast.LENGTH_SHORT).show();
                binding.signInForm.setVisibility(View.GONE);
            }
        }
    };

    // Submit Create Account
    private final View.OnClickListener submitCreateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String email = binding.inputEmail.getText().toString().trim();
            String password = binding.inputCreatePassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(ProfileActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ProfileActivity.this, "Creating account for: " + email, Toast.LENGTH_SHORT).show();
                binding.createAccountForm.setVisibility(View.GONE);
            }
        }
    };
}
