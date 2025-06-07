package com.example.app.UI;

import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

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
                NavigationUtils.navigateToActivity(this, ProfileActivity.class)
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

    /*** Helper methods to switch between:
     1) "logged‐out" message
     2) "empty wishlist" message
     3) "show RecyclerView" ***/

    private void showLoggedOutState() {
        UIStateManager.showViewAndHideOthers(
            (ViewGroup) binding.getRoot(),
            binding.loggedOutWishlist.getRoot()
        );
        Log.d(TAG, "User not signed in. Showing logged‐out message.");
    }

    private void showWishlistState() {
        UIStateManager.showViewAndHideOthers(
            (ViewGroup) binding.getRoot(),
            binding.recyclerViewWishlist
        );
    }

    private void showEmptyState() {
        UIStateManager.showViewAndHideOthers(
            (ViewGroup) binding.getRoot(),
            binding.emptyWishlist.getRoot()
        );
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
                ErrorHandler.handleFirestoreError(WishListActivity.this, "load wishlist", e);
                wishlistItems.clear();
                showEmptyState();
            }
        });
    }

    /*** Remove a single product from both Firestore and local list ***/

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
     * 1) Add to cart in Firestore
     * 2) Then remove from wishlist in Firestore
     * 3) Finally update local list and UI
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
                        // Now chain-remove from wishlist
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
