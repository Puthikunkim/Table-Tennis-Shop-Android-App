package com.example.app.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.app.Model.TableTennisProduct;
import com.example.app.R;

import java.util.List;

/**
 * Adapter for displaying recommended products, such as in a "Top Picks" or "You might also like" section.
 * Shows the product image, name, price, view count, and a heart icon for wishlist interaction.
 */
public class RecommendationsAdapter extends BaseProductAdapter<RecommendationsAdapter.ViewHolder> {

    private static final String VIEWS_FORMAT = "%d views"; // Format string for view count
    private static final String PRICE_FORMAT = "$%.2f"; // Format string for price

    public RecommendationsAdapter(Context context, List<TableTennisProduct> products) {
        super(context, products);
    }

    /**
     * Inflate the layout for a single recommendation item.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_recommendation, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Bind a product to the given ViewHolder.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TableTennisProduct product = products.get(position);
        bindProductData(holder, product);
    }

    /**
     * Populates the ViewHolder with product info and sets up click/wishlist logic.
     */
    private void bindProductData(ViewHolder holder, TableTennisProduct product) {
        holder.name.setText(product.getName());
        holder.price.setText(String.format(PRICE_FORMAT, product.getPrice()));
        holder.views.setText(String.format(VIEWS_FORMAT, product.getViews()));

        // Load the product's image
        loadProductImage(holder.image, product);

        // Set up tap-to-view details
        setupProductClick(holder.itemView, product);

        // Set up heart icon for wishlist toggling
        setupWishlistButton(holder.heartIcon, product);
    }

    /**
     * ViewHolder subclass holds all views for a single recommended product item.
     */
    static class ViewHolder extends BaseViewHolder {
        final ImageView image;      // Product image
        final TextView name;        // Product name
        final TextView price;       // Product price
        final TextView views;       // View count
        final ImageView heartIcon;  // Wishlist toggle icon

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.topPickImage);
            name = itemView.findViewById(R.id.topPickName);
            price = itemView.findViewById(R.id.topPickPrice);
            views = itemView.findViewById(R.id.topPickViews);
            heartIcon = itemView.findViewById(R.id.heartIcon);
        }
    }
}
