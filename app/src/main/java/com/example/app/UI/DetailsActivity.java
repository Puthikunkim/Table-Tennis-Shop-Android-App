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
            Toast.makeText(this, "Product not specified.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 3) Set up the Back button
        binding.customDetailsBackButton.setOnClickListener(v -> finish());

        // 4) Set up “Favorite” (wishlist) button listener
        binding.btnFavorite.setOnClickListener(v -> onFavoriteClicked());

        // 5) Load product details from Firestore
        loadProductDetails();
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentProduct != null) {
            checkIfInWishlist(currentUser, currentProduct.getId());
        } else {
            binding.btnFavorite.setImageResource(R.drawable.ic_wishlist);
            isWishlisted = false;
        }
    }

    private void loadProductDetails() {
        firestoreRepository.getProductById(productId, new FirestoreRepository.ProductDetailCallback() {
            @Override
            public void onSuccess(TableTennisProduct product) {
                currentProduct = product;
                if (product == null) {
                    Toast.makeText(DetailsActivity.this, "Error: Product not found.", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                // 1) Populate title, description, price, category
                binding.textTitle.setText(product.getName());
                binding.textDesc.setText(product.getDescription());
                binding.textPrice.setText(String.format("$%.2f", product.getPrice()));
                binding.textCategory.setText(product.getCategoryID());

                // 2) Set up image slider (ViewPager2)
                List<String> imageUrls = product.getImageUrls();
                ImageSliderAdapter sliderAdapter = new ImageSliderAdapter(imageUrls);
                binding.viewPagerImages.setAdapter(sliderAdapter);

                // (Optional) Hook up Prev/Next arrows for the ViewPager2:
                binding.btnPrev.setOnClickListener(v -> {
                    int prevIndex = binding.viewPagerImages.getCurrentItem() - 1;
                    if (prevIndex >= 0) {
                        binding.viewPagerImages.setCurrentItem(prevIndex, true);
                    }
                });
                binding.btnNext.setOnClickListener(v -> {
                    int nextIndex = binding.viewPagerImages.getCurrentItem() + 1;
                    if (nextIndex < imageUrls.size()) {
                        binding.viewPagerImages.setCurrentItem(nextIndex, true);
                    }
                });

                // 3) After loading, check wishlist status if user is signed in
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    checkIfInWishlist(user, product.getId());
                } else {
                    // If user is not signed in, show “not wishlisted”
                    binding.btnFavorite.setImageResource(R.drawable.ic_wishlist);
                    isWishlisted = false;
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error fetching product details: " + e.getMessage(), e);
                Toast.makeText(DetailsActivity.this, "Failed to load product details.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void checkIfInWishlist(FirebaseUser user, String productId) {
        firestoreRepository.checkIfProductInWishlist(
                user.getUid(),
                productId,
                new FirestoreRepository.WishlistOperationCallback() {
                    @Override
                    public void onSuccess() {
                        // Document exists in wishlist ⇒ already wishlisted
                        isWishlisted = true;
                        binding.btnFavorite.setImageResource(R.drawable.ic_wishlist_filled);
                    }

                    @Override
                    public void onError(Exception e) {
                        // If the document doesn’t exist, we get an error ⇒ not wishlisted
                        isWishlisted = false;
                        binding.btnFavorite.setImageResource(R.drawable.ic_wishlist);
                        // Note: We are not “logging” here because permission errors would be unexpected.
                    }
                }
        );
    }

    private void onFavoriteClicked() {
        // TODO: Implement add/remove wishlist logic
    }
}
