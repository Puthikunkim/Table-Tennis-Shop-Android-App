package com.example.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // Consider adding Glide for image loading
import com.example.app.Model.TableTennisProduct;
import com.example.app.R; // Ensure this is correct
import com.example.app.databinding.ItemWishlistProductBinding; // Assuming you're using view binding

import java.util.List;

public class WishListAdapter extends BaseProductAdapter<WishListAdapter.WishlistProductViewHolder> {

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
        ItemWishlistProductBinding binding = ItemWishlistProductBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new WishlistProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull WishlistProductViewHolder holder, int position) {
        TableTennisProduct product = products.get(position);
        holder.bind(product);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductClick(product);
            }
        });
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
            binding.productPrice.setText(String.format("$%.2f", product.getPrice()));

            // Load image using Glide (add Glide dependency if you haven't)
            if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
                Glide.with(context)
                        .load(product.getImageUrls().get(0)) // Load the first image URL
                        .placeholder(R.drawable.ic_launcher_foreground) // Add a placeholder drawable
                        .error(R.drawable.ic_launcher_background) // Add an error drawable
                        .into(binding.productImage);
            } else {
                binding.productImage.setImageResource(R.drawable.ic_launcher_background); // Default image
            }

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