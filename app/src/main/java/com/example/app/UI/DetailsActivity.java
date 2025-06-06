package com.example.app.UI;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.app.Data.FirestoreRepository;
import com.example.app.Model.TableTennisProduct;
import com.example.app.R;
import com.example.app.adapters.ImageSliderAdapter;
import com.example.app.adapters.TopPicksAdapter;
import com.example.app.databinding.ActivityDetailsBinding;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * DetailsActivity shows the product details, allows adding to cart, toggling wishlist, and
 * displays category-based recommendations.
 */
public class DetailsActivity extends BaseActivity<ActivityDetailsBinding> {
    private static final String TAG = "DetailsActivity";
    private static final long ANIMATION_DURATION = 120;

    private FirebaseAuth mAuth;
    private FirestoreRepository firestoreRepository;

    private String productId;
    private TableTennisProduct currentProduct;
    private boolean isWishlisted;
    private int quantity = 1;

    private TopPicksAdapter recommendationsAdapter;

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

        // Initialize FirebaseAuth and FirestoreRepository
        mAuth = FirebaseAuth.getInstance();
        firestoreRepository = FirestoreRepository.getInstance();

        // Obtain productId from Intent extras
        productId = getIntent().getStringExtra("productId");
        if (productId == null) {
            Toast.makeText(this, "Product not specified.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupBackButton();
        setupQuantityControls();
        setupFavoriteButton();
        setupAddToCartButton();

        loadProductDetails();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWishlistIcon();
    }

    /** Sets the back button to finish the activity. */
    private void setupBackButton() {
        binding.customDetailsBackButton.setOnClickListener(v -> finish());
    }

    /** Initializes increment/decrement quantity buttons. */
    private void setupQuantityControls() {
        binding.textQuantity.setText(String.valueOf(quantity));
        binding.btnIncrease.setOnClickListener(v -> changeQuantity(1));
        binding.btnDecrease.setOnClickListener(v -> changeQuantity(-1));
    }

    /**
     * Changes the quantity by delta (+1 or -1), ensuring it stays >= 1,
     * and updates the TextView.
     */
    private void changeQuantity(int delta) {
        int newQuantity = quantity + delta;
        if (newQuantity >= 1) {
            quantity = newQuantity;
            binding.textQuantity.setText(String.valueOf(quantity));
        }
    }

    /** Sets up the "Favorite" button click listener with animation and toggle logic. */
    private void setupFavoriteButton() {
        binding.btnFavorite.setOnClickListener(v ->
                animateButton(v, () -> {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user == null) {
                        Toast.makeText(this,
                                "Please sign in to add items to your wishlist.",
                                Toast.LENGTH_LONG).show();
                        startActivity(new Intent(DetailsActivity.this, ProfileActivity.class));
                        return;
                    }
                    if (currentProduct == null) {
                        Toast.makeText(this,
                                "Product data not loaded yet.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    toggleWishlist(user.getUid(), currentProduct.getId());
                })
        );
    }

    /**
     * Toggles the wishlist status: adds if not wishlisted, removes if already wishlisted.
     */
    private void toggleWishlist(String uid, String pid) {
        if (!isWishlisted) {
            firestoreRepository.addProductToWishlist(uid, currentProduct, new FirestoreRepository.WishlistOperationCallback() {
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
            });
        } else {
            firestoreRepository.removeProductFromWishlist(uid, pid, new FirestoreRepository.WishlistOperationCallback() {
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
            });
        }
    }

    /** Updates the wishlist icon based on whether the current product is wishlisted. */
    private void updateWishlistIcon() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || currentProduct == null) {
            binding.btnFavorite.setImageResource(R.drawable.ic_wishlist);
            isWishlisted = false;
            return;
        }
        firestoreRepository.checkIfProductInWishlist(user.getUid(), currentProduct.getId(),
                new FirestoreRepository.WishlistOperationCallback() {
                    @Override
                    public void onSuccess() {
                        isWishlisted = true;
                        binding.btnFavorite.setImageResource(R.drawable.ic_wishlist_filled);
                    }

                    @Override
                    public void onError(Exception e) {
                        isWishlisted = false;
                        binding.btnFavorite.setImageResource(R.drawable.ic_wishlist);
                    }
                });
    }

    /** Sets up the "Add to Cart" button listener with animation and repository call. */
    private void setupAddToCartButton() {
        binding.btnAddToCart.setOnClickListener(v ->
                animateButton(v, () -> {
                    if (currentProduct == null) {
                        Toast.makeText(this, "Product not loaded yet", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user == null) {
                        Toast.makeText(this, "Please sign in to add to cart", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    firestoreRepository.addToCart(user.getUid(), currentProduct, quantity,
                            new FirestoreRepository.OperationCallback() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(DetailsActivity.this,
                                            "Added to cart", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onError(Exception e) {
                                    Toast.makeText(DetailsActivity.this,
                                            "Failed to add to cart", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
        );
    }

    /**
     * Applies a scale animation to the provided view, then runs the given action.
     * @param v The view to animate.
     * @param afterAnimation The action to run after the scaling animation completes.
     */
    private void animateButton(View v, Runnable afterAnimation) {
        v.animate()
                .scaleX(1.15f)
                .scaleY(1.15f)
                .setDuration(ANIMATION_DURATION)
                .withEndAction(() -> {
                    v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(ANIMATION_DURATION)
                            .withEndAction(afterAnimation)
                            .start();
                })
                .start();
    }

    /**
     * Loads product details from Firestore, populates UI, sets up image slider,
     * initializes recommendations, and updates wishlist icon.
     */
    private void loadProductDetails() {
        firestoreRepository.getProductById(productId, new FirestoreRepository.ProductDetailCallback() {
            @Override
            public void onSuccess(TableTennisProduct product) {
                currentProduct = product;
                if (product == null) {
                    Toast.makeText(DetailsActivity.this,
                            "Error: Product not found.", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                incrementViews(product.getId());
                bindProductInfo(product);
                setupImageSlider(product.getImageUrls());
                binding.textQuantity.setText(String.valueOf(quantity));

                String categoryId = product.getCategoryID();
                if (categoryId != null && !categoryId.isEmpty()) {
                    setupRecommendations(categoryId);
                }

                updateWishlistIcon();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error fetching product details: " + e.getMessage(), e);
                Toast.makeText(DetailsActivity.this,
                        "Failed to load product details.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    /** Increments the view count for this product in Firestore. */
    private void incrementViews(String pid) {
        firestoreRepository.incrementProductViews(pid);
    }

    /**
     * Populates UI fields: title, description, price, and category.
     */
    private void bindProductInfo(TableTennisProduct product) {
        binding.textTitle.setText(product.getName());
        binding.textDesc.setText(product.getDescription());
        binding.textPrice.setText(String.format("$%.2f", product.getPrice()));

        String category = product.getCategoryID();
        if (category != null && !category.isEmpty()) {
            category = category.substring(0, 1).toUpperCase() + category.substring(1);
        }
        binding.textCategory.setText(category);
    }

    /**
     * Configures the ViewPager2 image slider and its previous/next buttons.
     */
    private void setupImageSlider(List<String> imageUrls) {
        ImageSliderAdapter sliderAdapter = new ImageSliderAdapter(imageUrls);
        binding.viewPagerImages.setAdapter(sliderAdapter);

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
    }

    /**
     * Fetches other products in the same category, filters out the current product,
     * and displays them in a horizontal RecyclerView.
     */
    private void setupRecommendations(String categoryId) {
        firestoreRepository.getProductsByCategory(categoryId, new FirestoreRepository.ProductsCallback() {
            @Override
            public void onSuccess(List<TableTennisProduct> products) {
                List<TableTennisProduct> filtered = new ArrayList<>();
                for (TableTennisProduct p : products) {
                    if (!p.getId().equals(currentProduct.getId())) {
                        filtered.add(p);
                    }
                }

                if (filtered.isEmpty()) {
                    binding.rvRecommendations.setVisibility(View.GONE);
                    return;
                }

                binding.rvRecommendations.setVisibility(View.VISIBLE);
                LinearLayoutManager llm = new LinearLayoutManager(
                        DetailsActivity.this,
                        LinearLayoutManager.HORIZONTAL,
                        false
                );
                binding.rvRecommendations.setLayoutManager(llm);

                recommendationsAdapter = new TopPicksAdapter(DetailsActivity.this, filtered);
                recommendationsAdapter.setOnProductClickListener(clickedProduct -> {
                    Intent intent = new Intent(DetailsActivity.this, DetailsActivity.class);
                    intent.putExtra("productId", clickedProduct.getId());
                    startActivity(intent);
                });

                binding.rvRecommendations.setAdapter(recommendationsAdapter);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to load recommendations: " + e.getMessage(), e);
            }
        });
    }
}
