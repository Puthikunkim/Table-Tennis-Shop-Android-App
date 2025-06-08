package com.example.app.UI;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.app.Auth.AuthManager;
import com.example.app.Data.FirestoreRepository;
import com.example.app.Model.TableTennisProduct;
import com.example.app.R;
import com.example.app.Adapters.CartAdapter;
import com.example.app.Util.AnimationUtils;
import com.example.app.Util.ErrorHandler;
import com.example.app.Util.NavigationUtils;
import com.example.app.Util.UIStateManager;
import com.example.app.Util.ToastUtils;
import com.example.app.databinding.ActivityCartBinding;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles displaying and managing the user's cart.
 * Shows different states (empty, logged out, cart full),
 * and allows removing items or checking out.
 */
public class CartActivity extends BaseActivity<ActivityCartBinding> {
    private static final String TAG = "CartActivity";

    private AuthManager authManager;
    private CartAdapter adapter;
    private final List<TableTennisProduct> cartItems = new ArrayList<>();

    @Override
    protected ActivityCartBinding inflateContentBinding() {
        return ActivityCartBinding.inflate(getLayoutInflater());
    }

    @Override
    protected int getSelectedMenuItemId() {
        return R.id.cart;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authManager = AuthManager.getInstance(this);

        // Hide logged-out UI by default (we toggle it in updateUI)
        binding.loggedOutCart.getRoot().setVisibility(View.GONE);

        // Hook up sign-in button if user is not logged in
        binding.loggedOutCart.signInButtonCart.setOnClickListener(v ->
                NavigationUtils.navigateToActivity(this, ProfileActivity.class)
        );

        // Set up logic for the checkout button
        setupCheckoutButton();
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUI(authManager.getCurrentUser()); // Refresh cart on screen start
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI(authManager.getCurrentUser()); // Also refresh when returning from another screen
    }

    /**
     * Loads the cart depending on whether user is logged in or not.
     */
    private void updateUI(@Nullable FirebaseUser user) {
        if (user == null) {
            showLoggedOutState();
        } else {
            hideAllStates();
            loadCartForUser(user.getUid());
        }
    }

    /**
     * Pulls cart items from Firestore for a given user.
     */
    private void loadCartForUser(String userId) {
        hideAllStates();

        FirestoreRepository.getInstance().getCartItems(userId, new FirestoreRepository.ProductsCallback() {
            @Override
            public void onSuccess(List<TableTennisProduct> products) {
                cartItems.clear();
                cartItems.addAll(products);

                if (cartItems.isEmpty()) {
                    showEmptyState();
                } else {
                    showCartState();
                    ensureAdapterExists(userId);
                    adapter.notifyDataSetChanged();
                    updateCartUI();
                }

                Log.d(TAG, "Cart loaded: " + products.size() + " items.");
            }

            @Override
            public void onError(Exception e) {
                ToastUtils.showCustomToast(CartActivity.this, "Error loading cart: " + e.getMessage());
                Log.e(TAG, "Error loading cart", e);
                cartItems.clear();
                if (adapter != null) adapter.notifyDataSetChanged();
                showEmptyState();
            }
        });
    }

    /**
     * Initializes the list adapter if not already created.
     */
    private void ensureAdapterExists(String userId) {
        if (adapter == null) {
            ListView cartListView = binding.cartListView;
            adapter = new CartAdapter(
                    CartActivity.this,
                    cartItems,
                    userId,
                    CartActivity.this::updateCartUI // Callback to recalculate totals
            );
            cartListView.setAdapter(adapter);
        }
    }

    /**
     * Calculates and displays subtotal + total price.
     */
    private void updateCartUI() {
        double subtotal = 0.0;
        for (TableTennisProduct item : cartItems) {
            subtotal += item.getPrice() * item.getCartQuantity();
        }

        View checkoutRoot = binding.checkoutTotal.getRoot();
        TextView subtotalText = checkoutRoot.findViewById(R.id.subtotalText);
        TextView totalText = checkoutRoot.findViewById(R.id.totalText);

        subtotalText.setText(String.format("$%.2f", subtotal));
        totalText.setText(String.format("$%.2f", subtotal)); // no tax/shipping yet

        if (cartItems.isEmpty()) {
            showEmptyState();
            ToastUtils.showCustomToast(this, "Cart is empty");
        } else {
            showCartState();
        }
    }

    /**
     * Called when the user taps "Checkout".
     * Clears the cart from Firestore and updates UI.
     */
    private void handleCheckout() {
        FirebaseUser user = authManager.getCurrentUser();
        if (user == null) {
            ToastUtils.showCustomToast(this, "Please sign in to checkout");
            return;
        }

        if (cartItems.isEmpty()) {
            ToastUtils.showCustomToast(this, "Your cart is empty");
            return;
        }

        FirestoreRepository.getInstance().clearCart(
                user.getUid(),
                new FirestoreRepository.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        cartItems.clear();
                        if (adapter != null) adapter.notifyDataSetChanged();
                        updateCartUI();
                        ToastUtils.showCustomToast(CartActivity.this, "Cart checked out successfully");
                    }

                    @Override
                    public void onError(Exception e) {
                        ToastUtils.showCustomToast(CartActivity.this, "Failed to checkout: " + e.getMessage());
                        Log.e(TAG, "Error during checkout", e);
                    }
                }
        );
    }

    /**
     * Hook up checkout button animation + action.
     */
    private void setupCheckoutButton() {
        View checkoutRoot = binding.checkoutTotal.getRoot();
        checkoutRoot.setVisibility(View.GONE); // hidden by default

        Button checkoutButton = checkoutRoot.findViewById(R.id.checkoutButton);
        checkoutButton.setOnClickListener(v ->
                AnimationUtils.animateButton(v, this::handleCheckout)
        );
    }

    /** Shows the UI for logged-out users (placeholder screen). */
    private void showLoggedOutState() {
        UIStateManager.showViewAndHideOthers(
                (ViewGroup) binding.getRoot(),
                binding.loggedOutCart.getRoot()
        );
        Log.d(TAG, "User not signed in. Showing logged‐out cart placeholder.");
    }

    /** Shows the "empty cart" state when user is signed in but has no items. */
    private void showEmptyState() {
        UIStateManager.showViewAndHideOthers(
                (ViewGroup) binding.getRoot(),
                binding.emptyCart.getRoot()
        );
    }

    /** Shows the list of cart items + checkout area. */
    private void showCartState() {
        UIStateManager.showViewsAndHideOthers(
                (ViewGroup) binding.getRoot(),
                binding.cartListView,
                binding.checkoutTotal.getRoot()
        );
    }

    /** Hides all possible cart states to prep for a new one. */
    private void hideAllStates() {
        binding.loggedOutCart.getRoot().setVisibility(View.GONE);
        binding.emptyCart.getRoot().setVisibility(View.GONE);
        binding.cartListView.setVisibility(View.GONE);
        binding.checkoutTotal.getRoot().setVisibility(View.GONE);
    }
}
