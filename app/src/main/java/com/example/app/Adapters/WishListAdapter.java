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

/**
 * Adapter for showing wishlist items in a RecyclerView.
 * Each item includes product info, a "Delete" button, and an "Add to Cart" button.
 */
public class WishListAdapter extends BaseProductAdapter<WishListAdapter.WishlistProductViewHolder> {
    private static final String CURRENCY_FORMAT = "$%.2f";

    private final OnWishlistItemActionListener listener;

    // Interface to notify the activity when a user interacts with a wishlist item
    public interface OnWishlistItemActionListener {
        void onDeleteClick(TableTennisProduct product);     // When the delete button is clicked
        void onAddToCartClick(TableTennisProduct product);  // When the "Add to Cart" button is clicked
        void onProductClick(TableTennisProduct product);    // When the item itself is clicked
    }

    // Constructor
    public WishListAdapter(Context context, List<TableTennisProduct> productList, OnWishlistItemActionListener listener) {
        super(context, productList);
        this.listener = listener;
    }

    /**
     * Inflates the layout for each wishlist item.
     */
    @NonNull
    @Override
    public WishlistProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWishlistProductBinding binding = ItemWishlistProductBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new WishlistProductViewHolder(binding);
    }

    /**
     * Binds product data to the ViewHolder.
     */
    @Override
    public void onBindViewHolder(@NonNull WishlistProductViewHolder holder, int position) {
        TableTennisProduct product = products.get(position);
        holder.bind(product);                          // Set text/image/button states
        setupProductClick(holder.itemView, product);   // Set up click to view details
    }

    /**
     * ViewHolder that holds all views for a single wishlist product item.
     */
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

        /**
         * Populates the item with product data and sets up button logic.
         */
        public void bind(TableTennisProduct product) {
            binding.productName.setText(product.getName());
            binding.productDescription.setText(product.getDescription());
            binding.productPrice.setText(String.format(CURRENCY_FORMAT, product.getPrice()));

            // Load the image from the product's first image URL
            loadProductImage(binding.productImage, product);

            // Set up "Add to Cart" and "Delete" buttons
            setupActionButtons(product);
        }

        /**
         * Attaches listeners to buttons in the wishlist item.
         */
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
