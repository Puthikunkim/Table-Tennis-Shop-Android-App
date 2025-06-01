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
        return R.id.home;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1) Initialize Firebase Auth & FirestoreRepository
        mAuth = FirebaseAuth.getInstance();
        firestoreRepository = FirestoreRepository.getInstance();

        // 2) Get the productId from Intent extras
        productId = getIntent().getStringExtra("productId");
        if (productId == null) {
            // If somehow no ID was passed, just finish
            Toast.makeText(this, "Product not specified.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // TODO: Set up Back button and Favorite button listeners
        // TODO: Load product details
    }

    @Override
    public void onStart() {
        super.onStart();
        // TODO: Re‐check whether the user is logged in and wishlist status
    }

    private void loadProductDetails() {
        // TODO: Implement Firestore fetch and UI population
    }

    private void checkIfInWishlist(FirebaseUser user, String productId) {
        // TODO: Implement Firestore wishlist check
    }

    private void onFavoriteClicked() {
        // TODO: Implement add/remove wishlist logic
    }
}
