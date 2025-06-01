package com.example.app.UI;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

import com.example.app.Data.FirestoreRepository;
import com.example.app.Model.TableTennisProduct;
import com.example.app.R;
import com.example.app.adapters.ImageSliderAdapter;
import com.example.app.databinding.ActivityDetailsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class DetailsActivity extends BaseActivity<ActivityDetailsBinding> {
    private static final String TAG = "DetailsActivity";

    // Firestore & Auth references
    private FirebaseAuth mAuth;
    private FirestoreRepository firestoreRepository;

    // The product ID passed via Intent
    private String productId;

    // Keep track of whether the product is currently in the user's wishlist
    private boolean isWishlisted = false;

    // The loaded product (once fetched)
    private TableTennisProduct currentProduct;

    @Override
    protected ActivityDetailsBinding inflateContentBinding() {
        return ActivityDetailsBinding.inflate(getLayoutInflater());
    }

    @Override
    protected int getSelectedMenuItemId() {
        // If you have a bottom navigation or drawer, make sure this returns the correct menu item ID
        return R.id.home; // or whichever item corresponds to “Details”
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: Initialize Firebase Auth & FirestoreRepository
        // TODO: Get productId from Intent extras
        // TODO: Set up Back button and Favorite button listeners
        // TODO: Load product details
    }

    @Override
    public void onStart() {
        super.onStart();
        // TODO: Re‐check whether the user is logged in and wishlist status
    }

    /**
     * Fetch product details from Firestore (by productId),
     * populate all UI widgets (title, description, image slider, price, etc.),
     * and then check if it’s in the wishlist (if user is already signed in).
     */
    private void loadProductDetails() {
        // TODO: Implement Firestore fetch and UI population
    }

    /**
     * Checks if the given productId exists in the user’s wishlist subcollection.
     * If it exists, we set isWishlisted = true and use the “filled heart” icon.
     * If it does NOT exist, we set isWishlisted = false and use the “outline heart” icon.
     */
    private void checkIfInWishlist(FirebaseUser user, String productId) {
        // TODO: Implement Firestore wishlist check
    }

    /**
     * Called when the user taps the “favorite” (wishlist) button.
     *  - If not logged in ⇒ show a Toast & optionally redirect to sign-in screen.
     *  - If logged in and not already wishlisted ⇒ add to wishlist.
     *  - If logged in and already wishlisted ⇒ remove from wishlist.
     */
    private void onFavoriteClicked() {
        // TODO: Implement add/remove wishlist logic
    }
}
