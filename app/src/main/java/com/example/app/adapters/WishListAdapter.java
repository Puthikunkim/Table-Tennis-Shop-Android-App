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

import com.example.app.Model.TableTennisProduct;
import com.example.app.R;
import com.example.app.databinding.ItemWishlistProductBinding;

import java.util.List;

public class WishListAdapter extends RecyclerView.Adapter<WishListAdapter.WishlistProductViewHolder> {

    private final Context context;
    private final List<TableTennisProduct> productList;
    private final OnWishlistItemActionListener listener;

    public interface OnWishlistItemActionListener {
        void onDeleteClick(TableTennisProduct product);
        void onAddToCartClick(TableTennisProduct product);
    }

    public WishListAdapter(Context context, List<TableTennisProduct> productList, OnWishlistItemActionListener listener) {
        this.context = context;
        this.productList = productList;
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
        TableTennisProduct product = productList.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public class WishlistProductViewHolder extends RecyclerView.ViewHolder {
        private final ItemWishlistProductBinding binding;

        public WishlistProductViewHolder(ItemWishlistProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(TableTennisProduct product) {
            binding.productName.setText(product.getName());
            binding.productDescription.setText(product.getDescription());
            binding.productPrice.setText(String.format("$%.2f", product.getPrice()));
            // Image loading and button listeners will be added in subsequent commits
        }
    }
}