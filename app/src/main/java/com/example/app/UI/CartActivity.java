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
        mAuth = FirebaseAuth.getInstance();

        // Wire up the Sign In button inside the "loggedOutCart" include
        // (but we won't set its listener until updateUI() runs, since updateUI()
        // is where we actually make that layout visible).
        View loggedOutRoot = binding.loggedOutCart.getRoot();
        Button signInButton = loggedOutRoot.findViewById(R.id.signInButtonCart);
        signInButton.setOnClickListener(v -> {
            // Navigate to ProfileActivity to force sign in
            Intent intent = new Intent(CartActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        setupCheckoutButton();

    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    /**
     * Toggle between:
     *   1) "Not logged in" view  → show loggedOutCart, hide everything else
     *   2) "Logged in" view      → hide loggedOutCart; then either load cart or show empty
     */
    private void updateUI(FirebaseUser user) {
        // References to the three main states in activity_cart.xml:
        View loggedOutView = binding.loggedOutCart.getRoot();
        ListView cartListView = binding.cartListView;
        View emptyCartView = binding.emptyCart.getRoot();
        View checkoutTotalView = binding.checkoutTotal.getRoot();

        if (user == null) {
            // ===== User is NOT signed in: show the "log out" placeholder =====
            loggedOutView.setVisibility(View.VISIBLE);
            cartListView.setVisibility(View.GONE);
            emptyCartView.setVisibility(View.GONE);
            checkoutTotalView.setVisibility(View.GONE);

            Log.d(TAG, "User not signed in. Showing logged‐out cart placeholder.");
        } else {
            // ===== User is signed in: hide "logged out" view, load cart from Firestore =====
            loggedOutView.setVisibility(View.GONE);
            // Initially hide ListView and empty view until we know if there are items
            cartListView.setVisibility(View.GONE);
            emptyCartView.setVisibility(View.GONE);
            checkoutTotalView.setVisibility(View.GONE);

            // Fetch cart items from Firestore
            FirestoreRepository.getInstance().getCartItems(user.getUid(), new FirestoreRepository.ProductsCallback() {
                @Override
                public void onSuccess(List<TableTennisProduct> products) {
                    cartItems.clear();
                    cartItems.addAll(products);

                    if (cartItems.isEmpty()) {
                        // Show the "empty cart" placeholder
                        emptyCartView.setVisibility(View.VISIBLE);
                        cartListView.setVisibility(View.GONE);
                        checkoutTotalView.setVisibility(View.GONE);
                    } else {
                        // Show the ListView and the checkout total bar
                        emptyCartView.setVisibility(View.GONE);
                        cartListView.setVisibility(View.VISIBLE);
                        checkoutTotalView.setVisibility(View.VISIBLE);

                        // Set up adapter (or notify if already created)
                        if (adapter == null) {
                            adapter = new CartAdapter(
                                    CartActivity.this,
                                    cartItems,
                                    user.getUid(),
                                    CartActivity.this::updateCartUI
                            );
                            cartListView.setAdapter(adapter);
                        } else {
                            adapter.notifyDataSetChanged();
                        }
                        updateCartUI();
                    }

                    Log.d(TAG, "Cart loaded: " + products.size() + " items.");
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Error loading cart: " + e.getMessage(), e);
                    Toast.makeText(CartActivity.this, "Failed to load cart.", Toast.LENGTH_SHORT).show();

                    // On error, treat as "empty"
                    cartItems.clear();
                    emptyCartView.setVisibility(View.VISIBLE);
                    cartListView.setVisibility(View.GONE);
                    checkoutTotalView.setVisibility(View.GONE);
                }
            });
        }
    }

    /**
     * Called whenever items in the cart change (e.g. user increments/decrements quantity, or removes an item).
     * We recompute subtotal and toggle empty vs. checkout bar.
     */
    private void updateCartUI() {
        // Recompute totals
        double subtotal = 0.0;
        for (TableTennisProduct item : cartItems) {
            subtotal += item.getPrice() * item.getCartQuantity();
        }

        // Find the subtotalText and totalText inside checkoutTotal include
        View checkoutView = binding.checkoutTotal.getRoot();
        if (checkoutView != null) {
            TextView subtotalText = checkoutView.findViewById(R.id.subtotalText);
            TextView totalText = checkoutView.findViewById(R.id.totalText);
            if (subtotalText != null && totalText != null) {
                subtotalText.setText(String.format("$%.2f", subtotal));
                totalText.setText(String.format("$%.2f", subtotal));
            }
        }

        // Toggle visibility between emptyCart vs checkoutTotal (+ ListView)
        if (cartItems.isEmpty()) {
            binding.emptyCart.getRoot().setVisibility(View.VISIBLE);
            binding.cartListView.setVisibility(View.GONE);
            binding.checkoutTotal.getRoot().setVisibility(View.GONE);
        } else {
            binding.emptyCart.getRoot().setVisibility(View.GONE);
            binding.cartListView.setVisibility(View.VISIBLE);
            binding.checkoutTotal.getRoot().setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // In case user came back from ProfileActivity and just signed in,
        // ensure we re‐call updateUI(...) with the latest user.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void handleCheckout() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please sign in to checkout", Toast.LENGTH_SHORT).show();
            return;
        }

        FirestoreRepository.getInstance().clearCart(user.getUid(), new FirestoreRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                cartItems.clear();
                if (adapter != null) adapter.notifyDataSetChanged();
                updateCartUI();
                Toast.makeText(CartActivity.this, "Cart checked out successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(CartActivity.this, "Checkout failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupCheckoutButton() {
        View checkoutTotalView = findViewById(R.id.checkoutTotal);
        if (checkoutTotalView != null) {
            Button checkoutButton = checkoutTotalView.findViewById(R.id.checkoutButton);
            if (checkoutButton != null) {
                checkoutButton.setOnClickListener(v -> handleCheckout());
            }
        }
    }


}
