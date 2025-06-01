package com.example.app.adapters;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.Data.FirestoreRepository;
import com.example.app.Model.TableTennisProduct;
import com.example.app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class TopPicksAdapter extends RecyclerView.Adapter<TopPicksAdapter.ViewHolder> {
    private final List<TableTennisProduct> products;
    private final Context context;
    private final FirestoreRepository firestoreRepository;
    private OnProductClickListener clickListener;

    public interface OnProductClickListener {
        void onProductClick(TableTennisProduct product);
    }

    public TopPicksAdapter(Context context, List<TableTennisProduct> products) {
        this.context = context;
        this.products = products;
        this.firestoreRepository = FirestoreRepository.getInstance();
    }

    public void setOnProductClickListener(OnProductClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_top_pick, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TableTennisProduct product = products.get(position);
        holder.name.setText(product.getName());
        holder.views.setText(product.getViews() + " views");
        // TODO: load product.getImageUrl() into holder.image via Glide/Picasso/etc.

        // 1) Card click → open product details
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onProductClick(product);
            }
        });

        // 2) Initialize the heart icon state:
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // Not signed in → always show outline, tag = false
            holder.heartIcon.setImageResource(R.drawable.ic_wishlist);
            holder.heartIcon.setTag(false);
        }
// ... (rest of the code for authenticated users and click listener will be in subsequent commits)
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
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
