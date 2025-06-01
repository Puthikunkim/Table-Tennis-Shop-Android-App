package com.example.app.UI;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.app.Model.TableTennisProduct;
import com.example.app.R;
import com.example.app.databinding.ActivityDetailsBinding;
import com.example.app.Data.FirestoreRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DetailsActivity extends BaseActivity<ActivityDetailsBinding> {

    private int quantity = 1;
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

        // Back button
        binding.customDetailsBackButton.setOnClickListener(v -> finish());

        // Retrieve product ID from intent
        String productId = getIntent().getStringExtra("productId");
        if (productId == null) {
            Toast.makeText(this, "No product ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load product details
        FirestoreRepository.getInstance().getProductById(productId, new FirestoreRepository.ProductDetailCallback() {
            @Override
            public void onSuccess(TableTennisProduct product) {
                currentProduct = product;
                binding.textTitle.setText(product.getName());
                binding.textDesc.setText(product.getDescription());
                binding.textCategory.setText(product.getCategoryID());
                binding.textPrice.setText(String.format("$%.2f", product.getPrice()));
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(DetailsActivity.this, "Failed to load product", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        // Handle quantity increase
        binding.btnIncrease.setOnClickListener(v -> {
            quantity++;
            binding.textQuantity.setText(String.valueOf(quantity));
        });

        // Handle quantity decrease
        binding.btnDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                binding.textQuantity.setText(String.valueOf(quantity));
            }
        });

        // Handle "Add to Cart"
        binding.btnAddToCart.setOnClickListener(v -> {
            if (currentProduct == null) {
                Toast.makeText(this, "Product not loaded yet", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "Please sign in first", Toast.LENGTH_SHORT).show();
                return;
            }

            FirestoreRepository.getInstance().addToCart(
                    user.getUid(),
                    currentProduct,
                    quantity,
                    new FirestoreRepository.WishlistOperationCallback() {
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
    }
}
