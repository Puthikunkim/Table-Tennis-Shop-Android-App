package com.example.app.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.app.Model.TableTennisProduct;
import com.example.app.databinding.ItemWishlistProductBinding;

import java.util.List;

public class WishListAdapter extends BaseProductAdapter<WishListAdapter.WishlistProductViewHolder> {
    private static final String CURRENCY_FORMAT = "$%.2f";

    private final OnWishlistItemActionListener listener;

    public interface OnWishlistItemActionListener {
        void onDeleteClick(TableTennisProduct product);
        void onAddToCartClick(TableTennisProduct product);
        void onProductClick(TableTennisProduct product);
    }

    public WishListAdapter(Context context, List<TableTennisProduct> productList, OnWishlistItemActionListener listener) {
        super(context, productList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public WishlistProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWishlistProductBinding binding = ItemWishlistProductBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new WishlistProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull WishlistProductViewHolder holder, int position) {
        TableTennisProduct product = products.get(position);
        holder.bind(product);
        setupProductClick(holder.itemView, product);
    }

    public class WishlistProductViewHolder extends BaseViewHolder {
        private final ItemWishlistProductBinding binding;
        private final TextView productDescription;
        private final Button addToCartButton;
        private final ImageButton deleteButton;

        public WishlistProductViewHolder(ItemWishlistProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.productDescription = binding.productDescription;
            this.addToCartButton = binding.addToCartButton;
            this.deleteButton = binding.deleteButton;
        }

        public void bind(TableTennisProduct product) {
            binding.productName.setText(product.getName());
            binding.productDescription.setText(product.getDescription());
            binding.productPrice.setText(String.format(CURRENCY_FORMAT, product.getPrice()));

            loadProductImage(binding.productImage, product);
            setupActionButtons(product);
        }

        private void setupActionButtons(TableTennisProduct product) {
            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(product);
                }
            });

            addToCartButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddToCartClick(product);
                }
            });
        }
    }
}