package com.example.app.UI;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.app.Model.TableTennisProduct;
import com.example.app.R;
import com.example.app.databinding.ActivityDetailsBinding;
import com.google.firebase.firestore.FirebaseFirestore;

public class DetailsActivity extends BaseActivity<ActivityDetailsBinding> {

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

        binding.customDetailsBackButton.setOnClickListener(v -> finish());

        String productId = getIntent().getStringExtra("productId");

        if (productId == null) {
            Toast.makeText(this, "No product ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("products")
                .document(productId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        TableTennisProduct product = doc.toObject(TableTennisProduct.class);
                        if (product != null) {
                            binding.textTitle.setText(product.getName());
                            binding.textDesc.setText(product.getDescription());
                            binding.textCategory.setText(product.getCategoryID());
                            binding.textPrice.setText(String.format("$%.2f", product.getPrice()));
                        }
                    } else {
                        Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load product", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
}
