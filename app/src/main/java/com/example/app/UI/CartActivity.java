package com.example.app.UI;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.app.Data.FirestoreRepository;
import com.example.app.Model.TableTennisProduct;
import com.example.app.R;
import com.example.app.adapters.CartAdapter;
import com.example.app.databinding.ActivityCartBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends BaseActivity<ActivityCartBinding> {
    private static final String TAG = "CartActivity";

    private FirebaseAuth mAuth;
    private CartAdapter adapter;                   // Only initialized once we know userId
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

        mAuth = FirebaseAuth.getInstance();

        // Wire up the Sign In button inside the "loggedOutCart" include
        binding.loggedOutCart.getRoot().setVisibility(View.GONE);
        binding.loggedOutCart.signInButtonCart.setOnClickListener(v ->
                startActivity(new Intent(CartActivity.this, ProfileActivity.class))
        );

        setupCheckoutButton();
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUI(mAuth.getCurrentUser());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // If the user just signed in on ProfileActivity, re-run updateUI
        updateUI(mAuth.getCurrentUser());
    }

    /**
     * Decide which “state” to show:
     *   – If user == null → show "logged‐out" include
     *   – If user != null → hide all, then load cart for that user
     */
    private void updateUI(@Nullable FirebaseUser user) {
        if (user == null) {
            showLoggedOutState();
        } else {
            hideAllStates();
            loadCartForUser(user.getUid());
        }
    }

    /*** Load all items from Firestore for this user ***/
    private void loadCartForUser(String userId) {
        // Hide everything until Firestore returns
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
                Log.e(TAG, "Error loading cart: " + e.getMessage(), e);
                Toast.makeText(CartActivity.this, "Failed to load cart.", Toast.LENGTH_SHORT).show();

                cartItems.clear();
                if (adapter != null) adapter.notifyDataSetChanged();
                showEmptyState();
            }
        });
    }

    /**
     * Only create the adapter once we know a valid userId.
     * If adapter already exists, we do NOT recreate it—just notifyDataSetChanged().
     */
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

    /*** COMPUTE TOTALS & TOGGLE VIEWS AFTER ANY CHANGE ***/
    private void updateCartUI() {
        // 1) Recompute subtotal
        double subtotal = 0.0;
        for (TableTennisProduct item : cartItems) {
            subtotal += item.getPrice() * item.getCartQuantity();
        }

        // 2) Update the two TextViews inside checkoutTotal include
        View checkoutRoot = binding.checkoutTotal.getRoot();
        TextView subtotalText = checkoutRoot.findViewById(R.id.subtotalText);
        TextView totalText = checkoutRoot.findViewById(R.id.totalText);
        subtotalText.setText(String.format("$%.2f", subtotal));
        totalText.setText(String.format("$%.2f", subtotal));

        // 3) If cart is now empty, show the empty‐cart placeholder; otherwise show list+checkout
        if (cartItems.isEmpty()) {
            showEmptyState();
        } else {
            showCartState();
        }
    }

    /*** “Checkout” button logic ***/
    private void handleCheckout() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please sign in to checkout", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(CartActivity.this,
                                "Cart checked out successfully",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(CartActivity.this,
                                "Checkout failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void setupCheckoutButton() {
        View checkoutRoot = binding.checkoutTotal.getRoot();
        checkoutRoot.setVisibility(View.GONE);

        Button checkoutButton = checkoutRoot.findViewById(R.id.checkoutButton);
        checkoutButton.setOnClickListener(v -> {
            v.animate()
                    .scaleX(1.1f).scaleY(1.1f).setDuration(120)
                    .withEndAction(() -> {
                        v.animate().scaleX(1f).scaleY(1f).setDuration(120).start();
                        handleCheckout();
                    })
                    .start();
        });
    }

    /*** SMALL HELPERS TO SWITCH “Which View Is VISIBLE?” ***/

    private void showLoggedOutState() {
        binding.loggedOutCart.getRoot().setVisibility(View.VISIBLE);
        binding.cartListView.setVisibility(View.GONE);
        binding.emptyCart.getRoot().setVisibility(View.GONE);
        binding.checkoutTotal.getRoot().setVisibility(View.GONE);
        Log.d(TAG, "User not signed in. Showing logged‐out cart placeholder.");
    }

    private void showEmptyState() {
        binding.loggedOutCart.getRoot().setVisibility(View.GONE);
        binding.cartListView.setVisibility(View.GONE);
        binding.emptyCart.getRoot().setVisibility(View.VISIBLE);
        binding.checkoutTotal.getRoot().setVisibility(View.GONE);
    }

    private void showCartState() {
        binding.loggedOutCart.getRoot().setVisibility(View.GONE);
        binding.emptyCart.getRoot().setVisibility(View.GONE);
        binding.cartListView.setVisibility(View.VISIBLE);
        binding.checkoutTotal.getRoot().setVisibility(View.VISIBLE);
    }

    private void hideAllStates() {
        binding.loggedOutCart.getRoot().setVisibility(View.GONE);
        binding.emptyCart.getRoot().setVisibility(View.GONE);
        binding.cartListView.setVisibility(View.GONE);
        binding.checkoutTotal.getRoot().setVisibility(View.GONE);
    }
}
