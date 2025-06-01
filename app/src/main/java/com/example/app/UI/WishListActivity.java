package com.example.app.UI;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.app.R;
import com.example.app.Data.FirestoreRepository;
import com.example.app.Model.TableTennisProduct;
import com.example.app.adapters.WishListAdapter;
import com.example.app.databinding.ActivityWishListBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class WishListActivity extends BaseActivity<ActivityWishListBinding> {

    private static final String TAG = "WishListActivity";
    private FirebaseAuth mAuth;
    private FirestoreRepository firestoreRepository;
    private WishListAdapter wishlistAdapter;
    private List<TableTennisProduct> wishlistItems;

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

        mAuth = FirebaseAuth.getInstance();
        firestoreRepository = FirestoreRepository.getInstance();
        wishlistItems = new ArrayList<>();

        // Initialize RecyclerView
        binding.recyclerViewWishlist.setLayoutManager(new LinearLayoutManager(this));
        wishlistAdapter = new WishListAdapter(this, wishlistItems, new WishListAdapter.OnWishlistItemActionListener() {
            @Override
            public void onDeleteClick(TableTennisProduct product) {
                removeProductFromWishlist(product);
            }

            @Override
            public void onAddToCartClick(TableTennisProduct product) {
                // For now, just call the stub
                addToCartFromWishlist(product);
            }
        });
        binding.recyclerViewWishlist.setAdapter(wishlistAdapter);

        // Set listener for the "Sign In" button in the logged-out state
        binding.loggedOutWishlist.signInButtonWishlist.setOnClickListener(v -> {
            // Navigate to ProfileActivity for sign in
            Intent intent = new Intent(WishListActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // User is signed in
            binding.loggedOutWishlist.getRoot().setVisibility(android.view.View.GONE);
            binding.recyclerViewWishlist.setVisibility(android.view.View.VISIBLE);
            binding.emptyWishlist.getRoot().setVisibility(android.view.View.GONE); // Hide empty wishlist initially

            loadWishlist(user.getUid());
        } else {
            // User is signed out
            binding.loggedOutWishlist.getRoot().setVisibility(android.view.View.VISIBLE);
            binding.recyclerViewWishlist.setVisibility(android.view.View.GONE);
            binding.emptyWishlist.getRoot().setVisibility(android.view.View.GONE); // Also hide empty wishlist if logged out
            wishlistItems.clear(); // Clear local list if user signs out
            wishlistAdapter.notifyDataSetChanged();
            Log.d(TAG, "User not signed in. Showing logged out message.");
        }
    }

    private void loadWishlist(String userId) {
        firestoreRepository.getWishlistProducts(userId, new FirestoreRepository.WishlistProductsCallback() {
            @Override
            public void onSuccess(List<TableTennisProduct> products) {
                wishlistItems.clear();
                wishlistItems.addAll(products);
                wishlistAdapter.notifyDataSetChanged();
                if (products.isEmpty()) {
                    binding.emptyWishlist.getRoot().setVisibility(android.view.View.VISIBLE);
                    binding.recyclerViewWishlist.setVisibility(android.view.View.GONE);
                } else {
                    binding.emptyWishlist.getRoot().setVisibility(android.view.View.GONE);
                    binding.recyclerViewWishlist.setVisibility(android.view.View.VISIBLE);
                }
                Log.d(TAG, "Wishlist loaded successfully: " + products.size() + " items.");
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error loading wishlist: " + e.getMessage(), e);
                Toast.makeText(WishListActivity.this, "Error loading wishlist.", Toast.LENGTH_SHORT).show();
                binding.emptyWishlist.getRoot().setVisibility(android.view.View.VISIBLE); // Show empty state on error too
                binding.recyclerViewWishlist.setVisibility(android.view.View.GONE);
            }
        });
    }

    private void removeProductFromWishlist(TableTennisProduct product) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please sign in to manage your wishlist.", Toast.LENGTH_SHORT).show();
            return;
        }

        firestoreRepository.removeProductFromWishlist(currentUser.getUid(), product.getId(), new FirestoreRepository.WishlistOperationCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(WishListActivity.this, product.getName() + " removed from wishlist.", Toast.LENGTH_SHORT).show();
                // Remove from local list and update adapter
                wishlistItems.remove(product);
                wishlistAdapter.notifyDataSetChanged();
                if (wishlistItems.isEmpty()) {
                    binding.emptyWishlist.getRoot().setVisibility(android.view.View.VISIBLE);
                    binding.recyclerViewWishlist.setVisibility(android.view.View.GONE);
                }
                Log.d(TAG, product.getName() + " removed from wishlist successfully.");
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error removing product from wishlist: " + e.getMessage(), e);
                Toast.makeText(WishListActivity.this, "Failed to remove item: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Stub for “Add to Cart” – for now it just shows a Toast.
     * Will implement real Firestore logic in a later commit.
     */
    private void addToCartFromWishlist(TableTennisProduct product) {
        Toast.makeText(this, "Add to Cart clicked for " + product.getName(), Toast.LENGTH_SHORT).show();
    }
}
