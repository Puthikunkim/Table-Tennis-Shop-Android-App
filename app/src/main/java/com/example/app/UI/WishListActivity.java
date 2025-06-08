package com.example.app.UI;

import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.app.Auth.AuthManager;
import com.example.app.R;
import com.example.app.Data.FirestoreRepository;
import com.example.app.Model.TableTennisProduct;
import com.example.app.Adapters.WishListAdapter;
import com.example.app.Util.ErrorHandler;
import com.example.app.Util.NavigationUtils;
import com.example.app.Util.UIStateManager;
import com.example.app.databinding.ActivityWishListBinding;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays the user's wishlist.
 * Handles UI logic depending on whether the user is signed in and whether the wishlist has items.
 */
public class WishListActivity extends BaseActivity<ActivityWishListBinding> {
    private static final String TAG = "WishListActivity";

    private AuthManager authManager;
    private FirestoreRepository firestoreRepository;
    private WishListAdapter wishlistAdapter;
    private final List<TableTennisProduct> wishlistItems = new ArrayList<>();

    // Inflate layout using ViewBinding
    @Override
    protected ActivityWishListBinding inflateContentBinding() {
        return ActivityWishListBinding.inflate(getLayoutInflater());
    }

    // Set bottom nav menu item selected
    @Override
    protected int getSelectedMenuItemId() {
        return R.id.wish_list;
    }

    // Called once when activity is created
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authManager = AuthManager.getInstance(this);
        firestoreRepository = FirestoreRepository.getInstance();

        setupRecyclerView();

        // Redirect to ProfileActivity on sign-in button click
        binding.loggedOutWishlist.signInButtonWishlist.setOnClickListener(v ->
                NavigationUtils.navigateToActivity(this, ProfileActivity.class)
        );
    }

    // Refresh UI when activity is brought to the foreground
    @Override
    protected void onStart() {
        super.onStart();
        updateUI(authManager.getCurrentUser());
    }

    /**
     * Updates UI depending on user login state.
     * If logged in, loads wishlist.
     */
    private void updateUI(@Nullable FirebaseUser user) {
        if (user != null) {
            showWishlistState(); // Show RecyclerView for now
            loadWishlist(user.getUid());
        } else {
            showLoggedOutState(); // Show prompt to sign in
            clearLocalList();
        }
    }

    // Set up RecyclerView and adapter with item click listeners
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
                            NavigationUtils.navigateToActivity(
                                    WishListActivity.this,
                                    DetailsActivity.class,
                                    "productId",
                                    product.getId()
                            );
                        } else {
                            ErrorHandler.handleMissingDataError(WishListActivity.this, "Product ID");
                        }
                    }
                }
        );
        binding.recyclerViewWishlist.setAdapter(wishlistAdapter);
    }

    /** UI state switchers for logged-out, empty, or populated wishlist */

    private void showLoggedOutState() {
        binding.loggedOutWishlist.getRoot().setVisibility(View.VISIBLE);
        binding.recyclerViewWishlist.setVisibility(View.GONE);
        binding.emptyWishlist.getRoot().setVisibility(View.GONE);
        Log.d(TAG, "User not signed in. Showing logged-out message.");
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

    // Refresh UI based on current item count
    private void updateListVisibility() {
        if (wishlistItems.isEmpty()) {
            showEmptyState();
        } else {
            showWishlistState();
            wishlistAdapter.notifyDataSetChanged();
        }
    }

    // Clear local list data and refresh adapter
    private void clearLocalList() {
        wishlistItems.clear();
        wishlistAdapter.notifyDataSetChanged();
    }

    /**
     * Fetch wishlist products from Firestore and populate UI.
     */
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
                ErrorHandler.handleFirestoreError(WishListActivity.this, "load wishlist", e);
                wishlistItems.clear();
                showEmptyState();
            }
        });
    }

    /**
     * Remove a product from both Firestore and local list.
     */
    private void removeProductFromWishlist(TableTennisProduct product) {
        FirebaseUser currentUser = authManager.getCurrentUser();
        if (currentUser == null) {
            ErrorHandler.showUserError(this, "Please sign in to manage your wishlist.");
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
                        ErrorHandler.showUserError(WishListActivity.this,
                                product.getName() + " removed from wishlist.");
                        Log.d(TAG, product.getName() + " successfully removed.");
                    }

                    @Override
                    public void onError(Exception e) {
                        ErrorHandler.handleFirestoreError(WishListActivity.this, "remove from wishlist", e);
                    }
                }
        );
    }

    /**
     * Move a product from wishlist to cart:
     * 1. Add to Firestore cart
     * 2. Remove from Firestore wishlist
     * 3. Update UI
     */
    private void addToCartFromWishlist(TableTennisProduct product) {
        FirebaseUser currentUser = authManager.getCurrentUser();
        if (currentUser == null) {
            ErrorHandler.showUserError(this, "Please sign in to add to cart.");
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

                        // Chain: remove from wishlist after adding to cart
                        firestoreRepository.removeProductFromWishlist(
                                userId,
                                product.getId(),
                                new FirestoreRepository.WishlistOperationCallback() {
                                    @Override
                                    public void onSuccess() {
                                        wishlistItems.remove(product);
                                        updateListVisibility();
                                        ErrorHandler.showUserError(
                                                WishListActivity.this,
                                                product.getName() + " moved to cart."
                                        );
                                        Log.d(TAG, "Removed from wishlist after adding to cart: " + product.getName());
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        ErrorHandler.handleFirestoreError(
                                                WishListActivity.this,
                                                "remove from wishlist after adding to cart",
                                                e
                                        );
                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(Exception e) {
                        ErrorHandler.handleFirestoreError(WishListActivity.this, "add to cart", e);
                    }
                }
        );
    }
}
