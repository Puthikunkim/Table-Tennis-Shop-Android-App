package com.example.app.UI;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.app.Auth.AuthManager;
import com.example.app.Data.FirestoreRepository;
import com.example.app.Model.TableTennisProduct;
import com.example.app.R;
import com.example.app.Adapters.ImageSliderAdapter;
import com.example.app.Adapters.RecommendationsAdapter;
import com.example.app.Util.AnimationUtils;
import com.example.app.databinding.ActivityDetailsBinding;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays product details, images, wishlist toggles, add-to-cart,
 * and category-based product recommendations.
 */
public class DetailsActivity extends BaseActivity<ActivityDetailsBinding> {
    private static final String TAG = "DetailsActivity";
    private static final long ANIMATION_DURATION = 120;

    private AuthManager authManager;
    private FirestoreRepository firestoreRepository;

    private String productId;
    private TableTennisProduct currentProduct;
    private boolean isWishlisted;
    private int quantity = 1;

    private RecommendationsAdapter recommendationsAdapter;

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

        // Auth and data setup
        authManager = AuthManager.getInstance(this);
        firestoreRepository = FirestoreRepository.getInstance();

        // Get product ID from intent
        productId = getIntent().getStringExtra("productId");
        if (productId == null) {
            showCustomToast("Product not specified.");
            finish();
            return;
        }

        // Hook up UI handlers
        setupBackButton();
        setupQuantityControls();
        setupFavoriteButton();
        setupAddToCartButton();

        // Load the actual product info
        loadProductDetails();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWishlistIcon(); // Refresh heart icon on return
    }

    /** Goes back to previous screen. */
    private void setupBackButton() {
        binding.customDetailsBackButton.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
    }


    /** Quantity selector buttons (+ / -). */
    private void setupQuantityControls() {
        binding.textQuantity.setText(String.valueOf(quantity));
        binding.btnIncrease.setOnClickListener(v -> changeQuantity(1));
        binding.btnDecrease.setOnClickListener(v -> changeQuantity(-1));
    }

    /** Changes item quantity, with a minimum of 1. */
    private void changeQuantity(int delta) {
        int newQuantity = quantity + delta;
        if (newQuantity >= 1) {
            quantity = newQuantity;
            binding.textQuantity.setText(String.valueOf(quantity));
        }
    }

    /** Shows a custom toast with the specified message. */
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

    /** Handles wishlist heart toggle logic (with animation). */
    private void setupFavoriteButton() {
        binding.btnFavorite.setOnClickListener(v ->
                AnimationUtils.animateButton(v, () -> {
                    FirebaseUser user = authManager.getCurrentUser();
                    if (user == null) {
                        showCustomToast("Please sign in to add items to your wishlist");
                        startActivity(new Intent(DetailsActivity.this, ProfileActivity.class));
                        return;
                    }
                    if (currentProduct == null) {
                        showCustomToast("Product data not loaded yet");
                        return;
                    }
                    toggleWishlist(user.getUid(), currentProduct.getId());
                })
        );
    }

    /** Adds/removes product from wishlist and updates heart icon. */
    private void toggleWishlist(String uid, String pid) {
        if (!isWishlisted) {
            firestoreRepository.addProductToWishlist(uid, currentProduct, new FirestoreRepository.WishlistOperationCallback() {
                @Override
                public void onSuccess() {
                    isWishlisted = true;
                    binding.btnFavorite.setImageResource(R.drawable.ic_wishlist_filled_black);
                    showCustomToast(currentProduct.getName() + " added to wishlist");
                    Log.d(TAG, "Product added to wishlist: " + pid);
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Error adding to wishlist: " + e.getMessage(), e);
                    showCustomToast("Failed to add to wishlist: " + e.getMessage());
                }
            });
        } else {
            firestoreRepository.removeProductFromWishlist(uid, pid, new FirestoreRepository.WishlistOperationCallback() {
                @Override
                public void onSuccess() {
                    isWishlisted = false;
                    binding.btnFavorite.setImageResource(R.drawable.ic_wishlist_black);
                    showCustomToast(currentProduct.getName() + " removed from wishlist");
                    Log.d(TAG, "Product removed from wishlist: " + pid);
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Error removing from wishlist: " + e.getMessage(), e);
                    showCustomToast("Failed to remove from wishlist: " + e.getMessage());
                }
            });
        }
    }

    /** Checks and sets heart icon based on current wishlist state. */
    private void updateWishlistIcon() {
        FirebaseUser user = authManager.getCurrentUser();
        if (user == null || currentProduct == null) {
            binding.btnFavorite.setImageResource(R.drawable.ic_wishlist_black);
            isWishlisted = false;
            return;
        }
        firestoreRepository.checkIfProductInWishlist(user.getUid(), currentProduct.getId(),
                new FirestoreRepository.WishlistOperationCallback() {
                    @Override
                    public void onSuccess() {
                        isWishlisted = true;
                        binding.btnFavorite.setImageResource(R.drawable.ic_wishlist_filled_black
                        );
                    }

                    @Override
                    public void onError(Exception e) {
                        isWishlisted = false;
                        binding.btnFavorite.setImageResource(R.drawable.ic_wishlist_black);
                    }
                });
    }

    /** Adds product to cart in Firestore. */
    private void setupAddToCartButton() {
        binding.btnAddToCart.setOnClickListener(v ->
                AnimationUtils.animateButton(v, () -> {
                    if (currentProduct == null) {
                        showCustomToast("Product not loaded yet");
                        return;
                    }
                    FirebaseUser user = authManager.getCurrentUser();
                    if (user == null) {
                        showCustomToast("Please sign in to add to cart");
                        return;
                    }
                    firestoreRepository.addToCart(user.getUid(), currentProduct, quantity,
                            new FirestoreRepository.OperationCallback() {
                                @Override
                                public void onSuccess() {
                                    showCustomToast("Added to cart");
                                }

                                @Override
                                public void onError(Exception e) {
                                    showCustomToast("Failed to add to cart");
                                }
                            });
                })
        );
    }

    /** Loads product info from Firestore and hooks up everything. */
    private void loadProductDetails() {
        firestoreRepository.getProductById(productId, new FirestoreRepository.ProductDetailCallback() {
            @Override
            public void onSuccess(TableTennisProduct product) {
                currentProduct = product;
                if (product == null) {
                    showCustomToast("Error: Product not found");
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
                showCustomToast("Failed to load product details");
                finish();
            }
        });
    }

    /** Logs a view count increase in Firestore. */
    private void incrementViews(String pid) {
        firestoreRepository.incrementProductViews(pid);
    }

    /** Sets text fields: name, desc, price, and category. */
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

    /** Sets up the image slider (ViewPager2) and nav buttons. */
    private void setupImageSlider(List<String> imageUrls) {
        ImageSliderAdapter sliderAdapter = new ImageSliderAdapter(imageUrls);
        binding.viewPagerImages.setAdapter(sliderAdapter);

        new TabLayoutMediator(binding.tabLayoutDots, binding.viewPagerImages,
                (tab, position) -> tab.setCustomView(R.layout.custom_tab_dot)
        ).attach();

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

    /** Loads other products in the same category (except current one). */
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

                recommendationsAdapter = new RecommendationsAdapter(DetailsActivity.this, filtered);
                recommendationsAdapter.setOnProductClickListener(clickedProduct -> {
                    Intent intent = new Intent(DetailsActivity.this, DetailsActivity.class);
                    intent.putExtra("productId", clickedProduct.getId());
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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
