package com.example.app.UI;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.app.Auth.AuthManager;
import com.example.app.R;
import com.example.app.Data.FirestoreRepository;
import com.example.app.Model.TableTennisProduct;
import com.example.app.Adapters.WishListAdapter;
import com.example.app.databinding.ActivityWishListBinding;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class WishListActivity extends BaseActivity<ActivityWishListBinding> {
    private static final String TAG = "WishListActivity";

    private AuthManager authManager;
    private FirestoreRepository firestoreRepository;
    private WishListAdapter wishlistAdapter;
    private final List<TableTennisProduct> wishlistItems = new ArrayList<>();

    @Override
    protected ActivityWishListBinding inflateContentBinding() {
        return ActivityWishListBinding.inflate(getLayoutInflater());
    }

    @Override
    protected int getSelectedMenuItemId() {
        return R.id.wish_list;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authManager = AuthManager.getInstance(this);
        firestoreRepository = FirestoreRepository.getInstance();

        setupRecyclerView();
        binding.loggedOutWishlist.signInButtonWishlist.setOnClickListener(v ->
                startActivity(new Intent(WishListActivity.this, ProfileActivity.class))
        );
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUI(authManager.getCurrentUser());
    }

    // Show either logged‐in wishlist or logged‐out message
    private void updateUI(@Nullable FirebaseUser user) {
        if (user != null) {
            showWishlistState();         // hide logged‐out, hide empty view for now
            loadWishlist(user.getUid());
        } else {
            showLoggedOutState();
            clearLocalList();
        }
    }

    // Initialize RecyclerView + Adapter
    private void setupRecyclerView() {
        binding.recyclerViewWishlist.setLayoutManager(new LinearLayoutManager(this));
        wishlistAdapter = new WishListAdapter(
                this,
                wishlistItems,
                new WishListAdapter.OnWishlistItemActionListener() {
                    @Override
                    public void onDeleteClick(TableTennisProduct product) {
                        removeProductFromWishlist(product);
                    }

                    @Override
                    public void onAddToCartClick(TableTennisProduct product) {
                        addToCartFromWishlist(product);
                    }

                    @Override
                    public void onProductClick(TableTennisProduct product) {
                        if (product.getId() != null) {
                            Intent intent = new Intent(WishListActivity.this, DetailsActivity.class);
                            intent.putExtra("productId", product.getId());
                            startActivity(intent);
                        } else {
                            Toast.makeText(WishListActivity.this,
                                    "Product ID is missing",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
        binding.recyclerViewWishlist.setAdapter(wishlistAdapter);
    }

    /*** Helper methods to switch between:
     1) "logged‐out" message
     2) "empty wishlist" message
     3) "show RecyclerView" ***/

    private void showLoggedOutState() {
        binding.loggedOutWishlist.getRoot().setVisibility(View.VISIBLE);
        binding.recyclerViewWishlist.setVisibility(View.GONE);
        binding.emptyWishlist.getRoot().setVisibility(View.GONE);
        Log.d(TAG, "User not signed in. Showing logged‐out message.");
    }

    private void showWishlistState() {
        binding.loggedOutWishlist.getRoot().setVisibility(View.GONE);
        binding.recyclerViewWishlist.setVisibility(View.VISIBLE);
        binding.emptyWishlist.getRoot().setVisibility(View.GONE);
    }

    private void showEmptyState() {
        binding.loggedOutWishlist.getRoot().setVisibility(View.GONE);
        binding.recyclerViewWishlist.setVisibility(View.GONE);
        binding.emptyWishlist.getRoot().setVisibility(View.VISIBLE);
    }

    // After changing wishlistItems (add/remove), call this to show/hide RecyclerView vs. empty view
    private void updateListVisibility() {
        if (wishlistItems.isEmpty()) {
            showEmptyState();
        } else {
            showWishlistState();
            wishlistAdapter.notifyDataSetChanged();
        }
    }

    private void clearLocalList() {
        wishlistItems.clear();
        wishlistAdapter.notifyDataSetChanged();
    }

    /*** Load all wishlist products from Firestore ***/

    private void loadWishlist(String userId) {
        firestoreRepository.getWishlistProducts(userId, new FirestoreRepository.WishlistProductsCallback() {
            @Override
            public void onSuccess(List<TableTennisProduct> products) {
                wishlistItems.clear();
                wishlistItems.addAll(products);
                updateListVisibility();
                Log.d(TAG, "Wishlist loaded: " + products.size() + " items.");
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error loading wishlist: " + e.getMessage(), e);
                Toast.makeText(WishListActivity.this,
                        "Error loading wishlist.",
                        Toast.LENGTH_SHORT).show();
                // If error, treat it like "empty"
                wishlistItems.clear();
                showEmptyState();
            }
        });
    }

    /*** Remove a single product from both Firestore and local list ***/

    private void removeProductFromWishlist(TableTennisProduct product) {
        FirebaseUser currentUser = authManager.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this,
                    "Please sign in to manage your wishlist.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        firestoreRepository.removeProductFromWishlist(
                currentUser.getUid(),
                product.getId(),
                new FirestoreRepository.WishlistOperationCallback() {
                    @Override
                    public void onSuccess() {
                        wishlistItems.remove(product);
                        updateListVisibility();
                        Toast.makeText(WishListActivity.this,
                                product.getName() + " removed from wishlist.",
                                Toast.LENGTH_SHORT).show();
                        Log.d(TAG, product.getName() + " successfully removed.");
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Error removing product: " + e.getMessage(), e);
                        Toast.makeText(WishListActivity.this,
                                "Failed to remove item: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    /**
     * 1) Add to cart in Firestore
     * 2) Then remove from wishlist in Firestore
     * 3) Finally update local list and UI
     */
    private void addToCartFromWishlist(TableTennisProduct product) {
        FirebaseUser currentUser = authManager.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this,
                    "Please sign in to add to cart.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        firestoreRepository.addToCart(
                userId,
                product,
                1,
                new FirestoreRepository.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Added to cart: " + product.getName());
                        // Now chain-remove from wishlist
                        firestoreRepository.removeProductFromWishlist(
                                userId,
                                product.getId(),
                                new FirestoreRepository.WishlistOperationCallback() {
                                    @Override
                                    public void onSuccess() {
                                        wishlistItems.remove(product);
                                        updateListVisibility();
                                        Toast.makeText(
                                                WishListActivity.this,
                                                product.getName() + " moved to cart.",
                                                Toast.LENGTH_SHORT
                                        ).show();
                                        Log.d(TAG, "Removed from wishlist after adding to cart: " + product.getName());
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Log.e(TAG, "Failed to remove after adding to cart: " + e.getMessage(), e);
                                        Toast.makeText(
                                                WishListActivity.this,
                                                "Added to cart but couldn't remove from wishlist.",
                                                Toast.LENGTH_LONG
                                        ).show();
                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Error adding to cart: " + e.getMessage(), e);
                        Toast.makeText(
                                WishListActivity.this,
                                "Failed to add to cart: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }
}
