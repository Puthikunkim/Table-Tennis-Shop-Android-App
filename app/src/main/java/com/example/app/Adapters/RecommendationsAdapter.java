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

public class RecommendationsAdapter extends BaseProductAdapter<RecommendationsAdapter.ViewHolder> {
    private static final String VIEWS_FORMAT = "%d views";

    public RecommendationsAdapter(Context context, List<TableTennisProduct> products) {
        super(context, products);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_recommendation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TableTennisProduct product = products.get(position);
        bindProductData(holder, product);
    }

    private void bindProductData(ViewHolder holder, TableTennisProduct product) {
        holder.name.setText(product.getName());
        holder.views.setText(String.format(VIEWS_FORMAT, product.getViews()));
        loadProductImage(holder.image, product);
        setupProductClick(holder.itemView, product);
        setupWishlistButton(holder.heartIcon, product);
    }

    static class ViewHolder extends BaseViewHolder {
        final ImageView image;
        final TextView name;
        final TextView views;
        final ImageView heartIcon;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.topPickImage);
            name = itemView.findViewById(R.id.topPickName);
            views = itemView.findViewById(R.id.topPickViews);
            heartIcon = itemView.findViewById(R.id.heartIcon);
        }
    }
}
