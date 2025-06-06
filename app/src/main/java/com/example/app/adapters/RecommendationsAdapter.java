package com.example.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.app.Data.FirestoreRepository;
import com.example.app.Model.TableTennisProduct;
import com.example.app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class RecommendationsAdapter extends BaseProductAdapter<RecommendationsAdapter.ViewHolder> {

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
        holder.name.setText(product.getName());
        holder.views.setText(product.getViews() + " views");

        loadProductImage(holder.image, product);
        setupProductClick(holder.itemView, product);
        setupWishlistButton(holder.heartIcon, product);
    }

    static class ViewHolder extends BaseViewHolder {
        ImageView image;
        TextView name, views;
        ImageView heartIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.topPickImage);
            name = itemView.findViewById(R.id.topPickName);
            views = itemView.findViewById(R.id.topPickViews);
            heartIcon = itemView.findViewById(R.id.heartIcon);
        }
    }
}
