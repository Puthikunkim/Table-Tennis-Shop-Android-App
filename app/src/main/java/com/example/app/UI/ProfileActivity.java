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

        mAuth = FirebaseAuth.getInstance();
        firestoreRepository = FirestoreRepository.getInstance();

        // Set up all button listeners in one place, using lambdas and helper methods
        binding.buttonSignIn.setOnClickListener(v -> showSignInForm());
        binding.buttonCreateAccount.setOnClickListener(v -> showCreateForm());
        binding.closeSignIn.setOnClickListener(v -> returnToLoggedOutFromSignIn());
        binding.closeCreate.setOnClickListener(v -> returnToLoggedOutFromCreate());
        binding.submitSignIn.setOnClickListener(v -> handleSignIn());
        binding.submitCreate.setOnClickListener(v -> handleCreateAccount());
        binding.buttonSignOut.setOnClickListener(v -> handleSignOut());

        setupCartButtons();
        setupWishlistButtons();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Update UI based on current auth state
        updateUI(mAuth.getCurrentUser());
    }

    /*** UI‐state helper methods ***/

    private void showSignInForm() {
        binding.signInForm.setVisibility(View.VISIBLE);
        binding.createAccountForm.setVisibility(View.GONE);
        binding.mainContentLoggedOut.setVisibility(View.GONE);
    }

    private void showCreateForm() {
        binding.createAccountForm.setVisibility(View.VISIBLE);
        binding.signInForm.setVisibility(View.GONE);
        binding.mainContentLoggedOut.setVisibility(View.GONE);
    }

    private void returnToLoggedOutFromSignIn() {
        binding.signInForm.setVisibility(View.GONE);
        binding.mainContentLoggedOut.setVisibility(View.VISIBLE);
        clearSignInForm();
    }

    private void returnToLoggedOutFromCreate() {
        binding.createAccountForm.setVisibility(View.GONE);
        binding.mainContentLoggedOut.setVisibility(View.VISIBLE);
        clearCreateAccountForm();
    }

    private void showLoggedInState(FirebaseUser user) {
        binding.mainContentLoggedOut.setVisibility(View.GONE);
        binding.signInForm.setVisibility(View.GONE);
        binding.createAccountForm.setVisibility(View.GONE);
        binding.mainContentLoggedIn.setVisibility(View.VISIBLE);

        String displayName = (user.getDisplayName() != null) ? user.getDisplayName() : "User";
        binding.textWelcome.setText("Welcome, " + displayName + "!");
        binding.textEmail.setText("Email: " + user.getEmail());

        fetchCartSummary(user.getUid());
        fetchWishlistSummary(user.getUid());
    }

    private void showLoggedOutState() {
        binding.mainContentLoggedOut.setVisibility(View.VISIBLE);
        binding.mainContentLoggedIn.setVisibility(View.GONE);
        binding.signInForm.setVisibility(View.GONE);
        binding.createAccountForm.setVisibility(View.GONE);
    }

    private void updateUI(@Nullable FirebaseUser user) {
        if (user != null) {
            showLoggedInState(user);
        } else {
            showLoggedOutState();
        }
    }

    /*** Authentication callbacks ***/

    private void handleSignIn() {
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
                        Toast.makeText(this, "Signed in as: " + (user != null ? user.getEmail() : ""), Toast.LENGTH_SHORT).show();
                        clearSignInForm();
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(this,
                                "Authentication failed: " + (task.getException() != null ? task.getException().getMessage() : ""),
                                Toast.LENGTH_LONG).show();
                        updateUI(null);
                    }
                });
    }

    private void handleCreateAccount() {
        String email = binding.inputEmail.getText().toString().trim();
        String password = binding.inputCreatePassword.getText().toString().trim();
        String name = binding.inputName.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
            Toast.makeText(this, "Please enter name, email and password", Toast.LENGTH_SHORT).show();
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
                        if (user == null) {
                            Log.e(TAG, "User was null after successful account creation");
                            Toast.makeText(this, "Error: User data missing after account creation.", Toast.LENGTH_LONG).show();
                            updateUI(null);
                            return;
                        }

                        // 1) Update display name:
                        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build();

                        user.updateProfile(request)
                                .addOnCompleteListener(profileTask -> {
                                    if (profileTask.isSuccessful()) {
                                        Log.d(TAG, "User display name updated.");
                                        // Force a reload so display name appears right away:
                                        user.reload().addOnCompleteListener(reload -> updateUI(mAuth.getCurrentUser()));
                                    } else {
                                        Log.w(TAG, "Failed to update display name", profileTask.getException());
                                        updateUI(user);
                                    }
                                });

                        // 2) Save additional data to Firestore:
                        Map<String, Object> userProfile = new HashMap<>();
                        userProfile.put("email", user.getEmail());
                        userProfile.put("name", name);
                        userProfile.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

                        firestoreRepository.createUserProfile(user.getUid(), userProfile, new FirestoreRepository.UserProfileCallback() {
                            @Override
                            public void onSuccess() {
                                Log.d(TAG, "User profile created in Firestore");
                                Toast.makeText(ProfileActivity.this,
                                        "Account created and signed in as: " + name,
                                        Toast.LENGTH_SHORT).show();
                                clearCreateAccountForm();
                                updateUI(user);
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.w(TAG, "Error saving profile in Firestore", e);
                                Toast.makeText(ProfileActivity.this,
                                        "Account created, but profile save failed: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                                clearCreateAccountForm();
                                updateUI(user);
                            }
                        });

                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(this,
                                "Account creation failed: " + (task.getException() != null ? task.getException().getMessage() : ""),
                                Toast.LENGTH_LONG).show();
                        updateUI(null);
                    }
                });
    }

    private void handleSignOut() {
        mAuth.signOut();
        updateUI(null);
        Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();
    }

    /*** Form‐clearing helpers ***/

    private void clearSignInForm() {
        binding.inputSignInEmail.setText("");
        binding.inputSignInPassword.setText("");
    }

    private void clearCreateAccountForm() {
        binding.inputName.setText("");
        binding.inputEmail.setText("");
        binding.inputCreatePassword.setText("");
    }

    /*** Cart & Wishlist setup ***/

    private void setupCartButtons() {
        // "View Cart" simply launches CartActivity
        binding.btnViewCart.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, CartActivity.class));
        });

        // "Clear Cart" checks for a signed-in user, calls repository.clearCart, updates UI
        binding.btnClearCart.setOnClickListener(v -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) return;

            firestoreRepository.clearCart(user.getUid(), new FirestoreRepository.OperationCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(ProfileActivity.this, "Cart cleared!", Toast.LENGTH_SHORT).show();
                    binding.cartItemCount.setText("You have 0 items in your cart.");
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(ProfileActivity.this, "Failed to clear cart: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void setupWishlistButtons() {
        binding.btnViewWishlist.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, WishListActivity.class));
        });

        binding.btnClearWishlist.setOnClickListener(v -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) return;

            firestoreRepository.clearWishlist(user.getUid(), new FirestoreRepository.OperationCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(ProfileActivity.this, "Wishlist cleared!", Toast.LENGTH_SHORT).show();
                    binding.wishlistItemCount.setText("You have 0 items in your wishlist.");
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(ProfileActivity.this, "Failed to clear wishlist: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    /*** Summary‐fetching helpers ***/

    private void fetchCartSummary(String userId) {
        firestoreRepository.getCartItems(userId, new FirestoreRepository.ProductsCallback() {
            @Override
            public void onSuccess(List<TableTennisProduct> products) {
                int totalItems = 0;
                for (TableTennisProduct product : products) {
                    totalItems += product.getCartQuantity();
                }

                String summary = (totalItems == 1)
                        ? "You have 1 item in your cart."
                        : "You have " + totalItems + " items in your cart.";

                binding.cartItemCount.setText(summary);
            }

            @Override
            public void onError(Exception e) {
                binding.cartItemCount.setText("Failed to load cart.");
            }
        });
    }

    private void fetchWishlistSummary(String userId) {
        firestoreRepository.getWishlistProducts(userId, new FirestoreRepository.WishlistProductsCallback() {
            @Override
            public void onSuccess(List<TableTennisProduct> products) {
                int totalItems = products.size();
                String summary = (totalItems == 1)
                        ? "You have 1 item in your wishlist."
                        : "You have " + totalItems + " items in your wishlist.";

                binding.wishlistItemCount.setText(summary);
            }

            @Override
            public void onError(Exception e) {
                binding.wishlistItemCount.setText("Failed to load wishlist.");
            }
        });
    }
}
