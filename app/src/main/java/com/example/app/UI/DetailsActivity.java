package com.example.app.UI;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.app.Model.TableTennisProduct;
import com.example.app.R;
import com.example.app.databinding.ActivityDetailsBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.app.Data.FirestoreRepository;

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

        FirestoreRepository.getInstance()
                .getProductById(productId, new FirestoreRepository.ProductDetailCallback() {
                    @Override
                    public void onSuccess(TableTennisProduct product) {
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

    }
}
