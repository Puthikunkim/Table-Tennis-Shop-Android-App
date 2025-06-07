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
import com.example.app.databinding.ActivityCartBinding;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

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

        binding.loggedOutCart.getRoot().setVisibility(View.GONE);
        binding.loggedOutCart.signInButtonCart.setOnClickListener(v ->
                NavigationUtils.navigateToActivity(this, ProfileActivity.class)
        );

        setupCheckoutButton();
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUI(authManager.getCurrentUser());
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI(authManager.getCurrentUser());
    }

    private void updateUI(@Nullable FirebaseUser user) {
        if (user == null) {
            showLoggedOutState();
        } else {
            hideAllStates();
            loadCartForUser(user.getUid());
        }
    }

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
                ErrorHandler.handleFirestoreError(CartActivity.this, "load cart", e);
                cartItems.clear();
                if (adapter != null) adapter.notifyDataSetChanged();
                showEmptyState();
            }
        });
    }

    private void ensureAdapterExists(String userId) {
        if (adapter == null) {
            ListView cartListView = binding.cartListView;
            adapter = new CartAdapter(
                    CartActivity.this,
                    cartItems,
                    userId,
                    CartActivity.this::updateCartUI
            );
            cartListView.setAdapter(adapter);
        }
    }

    private void updateCartUI() {
        double subtotal = 0.0;
        for (TableTennisProduct item : cartItems) {
            subtotal += item.getPrice() * item.getCartQuantity();
        }

        View checkoutRoot = binding.checkoutTotal.getRoot();
        TextView subtotalText = checkoutRoot.findViewById(R.id.subtotalText);
        TextView totalText = checkoutRoot.findViewById(R.id.totalText);
        subtotalText.setText(String.format("$%.2f", subtotal));
        totalText.setText(String.format("$%.2f", subtotal));

        if (cartItems.isEmpty()) {
            showEmptyState();
        } else {
            showCartState();
        }
    }

    private void handleCheckout() {
        FirebaseUser user = authManager.getCurrentUser();
        if (user == null) {
            ErrorHandler.showUserError(this, "Please sign in to checkout");
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
                        ErrorHandler.showUserError(CartActivity.this, "Cart checked out successfully");
                    }

                    @Override
                    public void onError(Exception e) {
                        ErrorHandler.handleFirestoreError(CartActivity.this, "checkout", e);
                    }
                }
        );
    }

    private void setupCheckoutButton() {
        View checkoutRoot = binding.checkoutTotal.getRoot();
        checkoutRoot.setVisibility(View.GONE);

        Button checkoutButton = checkoutRoot.findViewById(R.id.checkoutButton);
        checkoutButton.setOnClickListener(v -> 
            AnimationUtils.animateButton(v, this::handleCheckout)
        );
    }

    private void showLoggedOutState() {
        UIStateManager.showViewAndHideOthers(
            (ViewGroup) binding.getRoot(),
            binding.loggedOutCart.getRoot()
        );
        Log.d(TAG, "User not signed in. Showing logged‐out cart placeholder.");
    }

    private void showEmptyState() {
        UIStateManager.showViewAndHideOthers(
            (ViewGroup) binding.getRoot(),
            binding.emptyCart.getRoot()
        );
    }

    private void showCartState() {
        UIStateManager.showViewsAndHideOthers(
            (ViewGroup) binding.getRoot(),
            binding.cartListView,
            binding.checkoutTotal.getRoot()
        );
    }

    private void hideAllStates() {
        binding.loggedOutCart.getRoot().setVisibility(View.GONE);
        binding.emptyCart.getRoot().setVisibility(View.GONE);
        binding.cartListView.setVisibility(View.GONE);
        binding.checkoutTotal.getRoot().setVisibility(View.GONE);
    }
}
