package com.example.app.UI;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import android.widget.LinearLayout;

import com.example.app.Auth.AuthManager;
import com.example.app.Model.TableTennisProduct;
import com.example.app.R;
import com.example.app.Data.FirestoreRepository;
import com.example.app.Util.ErrorHandler;
import com.example.app.Util.NavigationUtils;
import com.example.app.Util.UIStateManager;
import com.example.app.databinding.ActivityProfileBinding;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Profile screen handles:
 * - Logged-in/logged-out UI states
 * - Sign in / Sign up / Sign out flows
 * - Showing cart and wishlist summaries
 * - Navigation to Cart and Wishlist screens
 */
public class ProfileActivity extends BaseActivity<ActivityProfileBinding> {
    private static final String TAG = "ProfileActivity";

    private AuthManager authManager;
    private FirestoreRepository firestoreRepository;
    private LinearLayout profileContentContainer;

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

        authManager = AuthManager.getInstance(this);
        firestoreRepository = FirestoreRepository.getInstance();
        profileContentContainer = findViewById(R.id.profileContentContainer);

        setupButtonListeners();     // Auth + form actions
        setupCartButtons();         // View / Clear Cart
        setupWishlistButtons();     // View / Clear Wishlist
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUI(authManager.getCurrentUser());  // Reflect logged-in state
    }

    // === UI Setup Methods ===

    /** All main button listeners for auth and form transitions */
    private void setupButtonListeners() {
        binding.buttonSignIn.setOnClickListener(v -> showSignInForm());
        binding.buttonCreateAccount.setOnClickListener(v -> showCreateForm());
        binding.closeSignIn.setOnClickListener(v -> returnToLoggedOutFromSignIn());
        binding.closeCreate.setOnClickListener(v -> returnToLoggedOutFromCreate());
        binding.submitSignIn.setOnClickListener(v -> handleSignIn());
        binding.submitCreate.setOnClickListener(v -> handleCreateAccount());
        binding.buttonSignOut.setOnClickListener(v -> handleSignOut());
    }

    /** Shows the sign-in form and hides other sections */
    private void showSignInForm() {
        UIStateManager.showViewAndHideOthers(
                profileContentContainer,
                binding.signInForm
        );
    }

    /** Shows the create account form and hides others */
    private void showCreateForm() {
        UIStateManager.showViewAndHideOthers(
                profileContentContainer,
                binding.createAccountForm
        );
    }

    /** Returns to main logged-out screen from sign-in */
    private void returnToLoggedOutFromSignIn() {
        UIStateManager.showViewAndHideOthers(
                profileContentContainer,
                binding.mainContentLoggedOut
        );
        clearSignInForm();
    }

    /** Returns to main logged-out screen from create-account */
    private void returnToLoggedOutFromCreate() {
        UIStateManager.showViewAndHideOthers(
                profileContentContainer,
                binding.mainContentLoggedOut
        );
        clearCreateAccountForm();
    }

    /** Shows the logged-in view and pulls cart/wishlist data */
    private void showLoggedInState(FirebaseUser user) {
        UIStateManager.showViewAndHideOthers(
                profileContentContainer,
                binding.mainContentLoggedIn
        );

        String displayName = (user.getDisplayName() != null) ? user.getDisplayName() : "User";
        binding.textWelcome.setText("Welcome, " + displayName + "!");
        binding.textEmail.setText("Email: " + user.getEmail());

        fetchCartSummary(user.getUid());
        fetchWishlistSummary(user.getUid());
    }

    /** Shows the guest view for signed-out users */
    private void showLoggedOutState() {
        UIStateManager.showViewAndHideOthers(
                profileContentContainer,
                binding.mainContentLoggedOut
        );
    }

    /** Updates profile screen based on current user state */
    private void updateUI(@Nullable FirebaseUser user) {
        if (user != null) {
            showLoggedInState(user);
        } else {
            showLoggedOutState();
        }
    }

    // === Auth Handlers ===

    /** Attempts sign-in and shows result */
    private void handleSignIn() {
        String email = binding.inputSignInEmail.getText().toString().trim();
        String password = binding.inputSignInPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showCustomToast("Please enter both email and password");
            return;
        }

        authManager.signIn(email, password, new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                updateUI(user);
                showCustomToast("Signed in as: " + (user != null ? user.getEmail() : ""));
                clearSignInForm();
            }

            @Override
            public void onError(Exception e) {
                showCustomToast("Authentication failed: " + e.getMessage());
                Log.e(TAG, "Sign in error", e);
                updateUI(null);
            }
        });
    }

    /** Attempts account creation, saves profile to Firestore */
    private void handleCreateAccount() {
        String email = binding.inputEmail.getText().toString().trim();
        String password = binding.inputCreatePassword.getText().toString().trim();
        String name = binding.inputName.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
            showCustomToast("Please fill in all fields");
            return;
        }

        if (password.length() < 6) {
            showCustomToast("Password must be at least 6 characters");
            return;
        }

        authManager.createAccount(email, password, name, new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                if (user != null) {
                    Map<String, Object> userProfile = new HashMap<>();
                    userProfile.put("email", user.getEmail());
                    userProfile.put("name", name);
                    userProfile.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

                    firestoreRepository.createUserProfile(user.getUid(), userProfile, new FirestoreRepository.UserProfileCallback() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "User profile created in Firestore");
                            showCustomToast("Account created and signed in as: " + name);
                            clearCreateAccountForm();
                            updateUI(user);
                        }

                        @Override
                        public void onError(Exception e) {
                            showCustomToast("Failed to save profile: " + e.getMessage());
                            Log.e(TAG, "Error saving profile", e);
                            clearCreateAccountForm();
                            updateUI(user);
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                showCustomToast("Registration failed: " + e.getMessage());
                Log.e(TAG, "Registration error", e);
                updateUI(null);
            }
        });
    }

    /** Signs the user out and updates UI */
    private void handleSignOut() {
        authManager.signOut();
        updateUI(null);
        showCustomToast("Signed out successfully");
    }

    private void clearSignInForm() {
        binding.inputSignInEmail.setText("");
        binding.inputSignInPassword.setText("");
    }

    private void clearCreateAccountForm() {
        binding.inputName.setText("");
        binding.inputEmail.setText("");
        binding.inputCreatePassword.setText("");
    }

    // === Cart/Wishlist Button Logic ===

    /** Hooks up cart-related button listeners */
    private void setupCartButtons() {
        binding.btnViewCart.setOnClickListener(v ->
                NavigationUtils.navigateToActivity(this, CartActivity.class)
        );

        binding.btnClearCart.setOnClickListener(v -> {
            FirebaseUser user = authManager.getCurrentUser();
            if (user == null) {
                showCustomToast("Please sign in to manage cart");
                return;
            }

            firestoreRepository.clearCart(user.getUid(), new FirestoreRepository.OperationCallback() {
                @Override
                public void onSuccess() {
                    showCustomToast("Cart cleared");
                    binding.cartItemCount.setText("You have 0 items in your cart.");
                }

                @Override
                public void onError(Exception e) {
                    showCustomToast("Failed to clear cart: " + e.getMessage());
                    Log.e(TAG, "Error clearing cart", e);
                }
            });
        });
    }

    /** Hooks up wishlist-related button listeners */
    private void setupWishlistButtons() {
        binding.btnViewWishlist.setOnClickListener(v ->
                NavigationUtils.navigateToActivity(this, WishListActivity.class)
        );

        binding.btnClearWishlist.setOnClickListener(v -> {
            FirebaseUser user = authManager.getCurrentUser();
            if (user == null) {
                showCustomToast("Please sign in to manage wishlist");
                return;
            }

            firestoreRepository.clearWishlist(user.getUid(), new FirestoreRepository.OperationCallback() {
                @Override
                public void onSuccess() {
                    showCustomToast("Wishlist cleared");
                    binding.wishlistItemCount.setText("You have 0 items in your wishlist.");
                }

                @Override
                public void onError(Exception e) {
                    showCustomToast("Failed to clear wishlist: " + e.getMessage());
                    Log.e(TAG, "Error clearing wishlist", e);
                }
            });
        });
    }

    // === Data Fetchers ===

    /** Shows cart item count summary (e.g. "You have 3 items in your cart.") */
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
                showCustomToast("Failed to load cart summary");
                Log.e(TAG, "Error loading cart summary", e);
                binding.cartItemCount.setText("Failed to load cart.");
            }
        });
    }

    /** Shows wishlist item count summary (e.g. "You have 2 items in your wishlist.") */
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
                showCustomToast("Failed to load wishlist summary");
                Log.e(TAG, "Error loading wishlist summary", e);
                binding.wishlistItemCount.setText("Failed to load wishlist.");
            }
        });
    }

    private void showCustomToast(String message) {
        View layout = getLayoutInflater().inflate(R.layout.custom_toast, null);
        
        TextView text = layout.findViewById(R.id.toast_text);
        text.setText(message);
        
        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 100);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}
