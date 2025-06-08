package com.example.app.UI;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.app.Auth.AuthManager;
import com.example.app.Data.FirestoreRepository;
import com.example.app.Model.TableTennisProduct;
import com.example.app.R;
import com.example.app.Util.AnimationUtils;
import com.example.app.Util.ErrorHandler;
import com.example.app.Util.NavigationUtils;
import com.example.app.Util.UIStateManager;
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
    private final List<TableTennisProduct> cartItems = new ArrayList<>();
    private String userId;

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
            userId = user.getUid();
            loadCartForUser(userId);
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
                Log.d("CartDebug", "onSuccess: products.size() = " + products.size());
                if (cartItems.isEmpty()) {
                    Log.d("CartDebug", "Cart is empty, showing empty state");
                    showEmptyState();
                } else {
                    Log.d("CartDebug", "Cart has items, showing cart state");
                    showCartState();
                    showCartItems();
                    updateCartUI();
                }
            }
            @Override
            public void onError(Exception e) {
                Log.d("CartDebug", "onError: " + e.getMessage());
                cartItems.clear();
                showCartItems();
                showEmptyState();
            }
        });
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
            showCustomToast("Cart is empty");
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
            showCustomToast("Please sign in to checkout");
            return;
        }

        if (cartItems.isEmpty()) {
            showCustomToast("Your cart is empty");
            return;
        }

        FirestoreRepository.getInstance().clearCart(
                user.getUid(),
                new FirestoreRepository.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        cartItems.clear();
                        updateCartUI();
                        showCustomToast("Cart checked out successfully");
                    }

                    @Override
                    public void onError(Exception e) {
                        showCustomToast("Failed to checkout: " + e.getMessage());
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
        binding.cartItemsContainer.setVisibility(View.GONE);
        binding.emptyCart.getRoot().setVisibility(View.GONE);
        binding.loggedOutCart.getRoot().setVisibility(View.VISIBLE);
        binding.checkoutTotal.getRoot().setVisibility(View.GONE);
        Log.d(TAG, "User not signed in. Showing logged‐out cart placeholder.");
    }

    /** Shows the "empty cart" state when user is signed in but has no items. */
    private void showEmptyState() {
        binding.cartItemsContainer.setVisibility(View.GONE);
        binding.loggedOutCart.getRoot().setVisibility(View.GONE);
        binding.emptyCart.getRoot().setVisibility(View.VISIBLE);
        binding.checkoutTotal.getRoot().setVisibility(View.GONE);
    }

    /** Shows the list of cart items + checkout area. */
    private void showCartState() {
        binding.cartItemsContainer.setVisibility(View.VISIBLE);
        binding.loggedOutCart.getRoot().setVisibility(View.GONE);
        binding.emptyCart.getRoot().setVisibility(View.GONE);
        binding.checkoutTotal.getRoot().setVisibility(View.VISIBLE);
    }

    /** Hides all possible cart states to prep for a new one. */
    private void hideAllStates() {
        binding.loggedOutCart.getRoot().setVisibility(View.GONE);
        binding.emptyCart.getRoot().setVisibility(View.GONE);
        binding.cartItemsContainer.setVisibility(View.GONE);
        binding.checkoutTotal.getRoot().setVisibility(View.GONE);
    }

    private void showCartItems() {
        binding.cartItemsContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < cartItems.size(); i++) {
            TableTennisProduct product = cartItems.get(i);
            final int position = i;
            View itemView = inflater.inflate(R.layout.item_cart_product, binding.cartItemsContainer, false);
            ((TextView) itemView.findViewById(R.id.productName)).setText(product.getName());
            ((TextView) itemView.findViewById(R.id.productPrice)).setText(String.format("$%.2f", product.getPrice()));
            ((TextView) itemView.findViewById(R.id.quantityText)).setText(String.valueOf(product.getCartQuantity()));
            ((TextView) itemView.findViewById(R.id.totalPrice)).setText(String.format("$%.2f", product.getCartQuantity() * product.getPrice()));
            // Load image if available
            ImageView productImage = itemView.findViewById(R.id.productImage);
            if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
                com.example.app.Util.ImageLoader.loadProductImage(this, productImage, product.getImageUrls().get(0));
            } else {
                productImage.setImageResource(R.drawable.ic_launcher_background);
            }
            itemView.findViewById(R.id.incrementButton).setOnClickListener(v -> {
                product.setCartQuantity(product.getCartQuantity() + 1);
                FirestoreRepository.getInstance().addToCart(userId, product, 1, null);
                showCartItems();
                updateCartUI();
            });
            itemView.findViewById(R.id.decrementButton).setOnClickListener(v -> {
                if (product.getCartQuantity() > 1) {
                    product.setCartQuantity(product.getCartQuantity() - 1);
                    FirestoreRepository.getInstance().addToCart(userId, product, -1, null);
                    showCartItems();
                    updateCartUI();
                } else {
                    removeFromCart(product, position);
                }
            });
            itemView.findViewById(R.id.deleteButton).setOnClickListener(v -> removeFromCart(product, position));
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(this, DetailsActivity.class);
                intent.putExtra("productId", product.getId());
                startActivity(intent);
            });
            binding.cartItemsContainer.addView(itemView);
        }
        binding.cartItemsContainer.setVisibility(View.VISIBLE);
    }

    private void removeFromCart(TableTennisProduct product, int position) {
        FirestoreRepository.getInstance().removeFromCart(userId, product.getId(), new FirestoreRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                cartItems.remove(position);
                showCartItems();
                updateCartUI();
                showCustomToast("Item removed from cart");
            }
            @Override
            public void onError(Exception e) {
                showCustomToast("Failed to remove item: " + e.getMessage());
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
