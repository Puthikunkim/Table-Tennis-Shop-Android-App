package com.example.app.UI;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.example.app.Data.FirestoreRepository;
import com.example.app.Model.TableTennisProduct;
import com.example.app.R;
import com.example.app.adapters.ImageSliderAdapter;
import com.example.app.adapters.TopPicksAdapter;
import com.example.app.databinding.ActivityDetailsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
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

    // Holds current quantity selected by user (for Add to Cart)
    private int quantity = 1;

    private TopPicksAdapter recommendationsAdapter;

    @Override
    protected ActivityDetailsBinding inflateContentBinding() {
        return ActivityDetailsBinding.inflate(getLayoutInflater());
    }

    @Override
    protected int getSelectedMenuItemId() {
        // If you have a bottom navigation or drawer, make sure this returns the correct menu item ID
        return R.id.home; // or whichever item corresponds to "Details"
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

        // 3) Set up the Back button
        binding.customDetailsBackButton.setOnClickListener(v -> finish());

        // 4) Set up "Favorite" (wishlist) button listener
        binding.btnFavorite.setOnClickListener(v -> onFavoriteClicked());

        // 5) Set up quantity increase/decrease listeners
        binding.btnIncrease.setOnClickListener(v -> {
            quantity++;
            binding.textQuantity.setText(String.valueOf(quantity));
        });
        binding.btnDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                binding.textQuantity.setText(String.valueOf(quantity));
            }
        });

        // 6) Set up "Add to Cart" button listener
        binding.btnAddToCart.setOnClickListener(v -> {
            if (currentProduct == null) {
                Toast.makeText(this, "Product not loaded yet", Toast.LENGTH_SHORT).show();
                return;
            }
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "Please sign in to add to cart", Toast.LENGTH_SHORT).show();
                return;
            }
            String userId = user.getUid();
            FirestoreRepository.getInstance().addToCart(userId, currentProduct, quantity,
                    new FirestoreRepository.OperationCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(DetailsActivity.this, "Added to cart", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(DetailsActivity.this, "Failed to add to cart", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // 7) Load product details from Firestore
        loadProductDetails();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Every time this screen comes into view, re-check whether the user is logged in,
        // and if so, whether this product is already in their wishlist.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentProduct != null) {
            checkIfInWishlist(currentUser, currentProduct.getId());
        } else {
            // If user is not logged in, always show the "not wishlisted" icon
            binding.btnFavorite.setImageResource(R.drawable.ic_wishlist);
            isWishlisted = false;
        }
    }

    /**
     * Fetch product details from Firestore (by productId),
     * populate all UI widgets (title, description, image slider, price, etc.),
     * set up recommendations, and then check if it's in the wishlist (if user is already signed in).
     */
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

                Log.d("DetailsActivity", "About to increment views for " + product.getId());
                // Increment the views counter
                firestoreRepository.incrementProductViews(product.getId());

                // 1) Populate title, description, price, category, etc.
                binding.textTitle.setText(product.getName());
                binding.textDesc.setText(product.getDescription());
                binding.textPrice.setText(String.format("$%.2f", product.getPrice()));
                String category = product.getCategoryID();
                if (category != null && !category.isEmpty()) {
                    category = category.substring(0, 1).toUpperCase() + category.substring(1);
                }
                binding.textCategory.setText(category);

                // 2) Set up image slider (ViewPager2)
                List<String> imageUrls = product.getImageUrls();
                ImageSliderAdapter sliderAdapter = new ImageSliderAdapter(imageUrls);
                binding.viewPagerImages.setAdapter(sliderAdapter);

                // Hook up Prev/Next arrows for the ViewPager2:
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

                // 3) Initialize quantity text
                binding.textQuantity.setText(String.valueOf(quantity));

                // 4) Set up “You Might Like” recommendations after currentProduct is non-null
                String categoryId = currentProduct.getCategoryID();
                if (categoryId != null && !categoryId.isEmpty()) {
                    setupRecommendations(categoryId);
                }

                // 5) After loading, check wishlist status if user is signed in
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    checkIfInWishlist(user, product.getId());
                } else {
                    // If user is not signed in, show "not wishlisted"
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

    /**
     * Checks if the given productId exists in the user's wishlist subcollection.
     * If it exists, we set isWishlisted = true and use the "filled heart" icon.
     * If it does NOT exist, we set isWishlisted = false and use the "outline heart" icon.
     */
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
                        // If the document doesn't exist or there's an error ⇒ not wishlisted
                        isWishlisted = false;
                        binding.btnFavorite.setImageResource(R.drawable.ic_wishlist);
                    }
                }
        );
    }

    /**
     * Called when the user taps the "favorite" (wishlist) button.
     *  - If not logged in ⇒ show a Toast & optionally redirect to sign-in screen.
     *  - If logged in and not already wishlisted ⇒ add to wishlist.
     *  - If logged in and already wishlisted ⇒ remove from wishlist.
     */
    private void onFavoriteClicked() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            // 1) User is not signed in ⇒ prompt to log in
            Toast.makeText(DetailsActivity.this, "Please sign in to add items to your wishlist.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(DetailsActivity.this, ProfileActivity.class);
            startActivity(intent);
            return;
        }

        if (currentProduct == null) {
            Toast.makeText(DetailsActivity.this, "Product data not loaded yet.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();
        String pid = currentProduct.getId();

        if (!isWishlisted) {
            // 2) Not in wishlist ⇒ add it
            firestoreRepository.addProductToWishlist(uid, currentProduct,
                    new FirestoreRepository.WishlistOperationCallback() {
                        @Override
                        public void onSuccess() {
                            isWishlisted = true;
                            binding.btnFavorite.setImageResource(R.drawable.ic_wishlist_filled);
                            Toast.makeText(DetailsActivity.this,
                                    currentProduct.getName() + " added to wishlist.",
                                    Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Product added to wishlist: " + pid);
                        }
                        @Override
                        public void onError(Exception e) {
                            Log.e(TAG, "Error adding to wishlist: " + e.getMessage(), e);
                            Toast.makeText(DetailsActivity.this,
                                    "Failed to add to wishlist: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
            );
        } else {
            // 3) Already in wishlist ⇒ remove it
            firestoreRepository.removeProductFromWishlist(uid, pid,
                    new FirestoreRepository.WishlistOperationCallback() {
                        @Override
                        public void onSuccess() {
                            isWishlisted = false;
                            binding.btnFavorite.setImageResource(R.drawable.ic_wishlist);
                            Toast.makeText(DetailsActivity.this,
                                    currentProduct.getName() + " removed from wishlist.",
                                    Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Product removed from wishlist: " + pid);
                        }
                        @Override
                        public void onError(Exception e) {
                            Log.e(TAG, "Error removing from wishlist: " + e.getMessage(), e);
                            Toast.makeText(DetailsActivity.this,
                                    "Failed to remove from wishlist: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
            );
        }
    }

    /**
     * Fetches other products in the same category and displays them as horizontal "You Might Like" recommendations.
     */
    private void setupRecommendations(String categoryId) {
        firestoreRepository.getProductsByCategory(categoryId, new FirestoreRepository.ProductsCallback() {
            @Override
            public void onSuccess(List<TableTennisProduct> products) {
                // 1) Remove the current product from the list (so it doesn’t show itself)
                List<TableTennisProduct> filtered = new ArrayList<>();
                for (TableTennisProduct p : products) {
                    if (currentProduct != null && !p.getId().equals(currentProduct.getId())) {
                        filtered.add(p);
                    }
                }

                // 2) If there are no other products, hide the RecyclerView
                if (filtered.isEmpty()) {
                    binding.rvRecommendations.setVisibility(View.GONE);
                    return;
                }

                // 3) Otherwise, set up the RecyclerView (horizontal layout)
                binding.rvRecommendations.setVisibility(View.VISIBLE);
                LinearLayoutManager llm = new LinearLayoutManager(
                        DetailsActivity.this,
                        LinearLayoutManager.HORIZONTAL,
                        false
                );
                binding.rvRecommendations.setLayoutManager(llm);

                // 4) Create the adapter and give it the filtered list
                recommendationsAdapter = new TopPicksAdapter(DetailsActivity.this, filtered);

                // 5) Wire up click listener so that tapping on a “recommendation” launches a new DetailsActivity
                recommendationsAdapter.setOnProductClickListener(new TopPicksAdapter.OnProductClickListener() {
                    @Override
                    public void onProductClick(TableTennisProduct clickedProduct) {
                        Intent intent = new Intent(DetailsActivity.this, DetailsActivity.class);
                        intent.putExtra("productId", clickedProduct.getId());
                        startActivity(intent);
                        // Optionally finish() here if you don’t want the back stack to keep piling up
                    }
                });

                // 6) Finally, attach the adapter
                binding.rvRecommendations.setAdapter(recommendationsAdapter);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to load recommendations: " + e.getMessage(), e);
            }
        });
    }
}
