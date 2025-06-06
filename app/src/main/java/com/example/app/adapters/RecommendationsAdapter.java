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

import com.bumptech.glide.Glide;
import com.example.app.Data.FirestoreRepository;
import com.example.app.Model.TableTennisProduct;
import com.example.app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class RecommendationsAdapter extends RecyclerView.Adapter<RecommendationsAdapter.ViewHolder> {
    private final List<TableTennisProduct> products;
    private final Context context;
    private final FirestoreRepository firestoreRepository;
    private OnProductClickListener clickListener;

    public interface OnProductClickListener {
        void onProductClick(TableTennisProduct product);
    }

    public RecommendationsAdapter(Context context, List<TableTennisProduct> products) {
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
                .inflate(R.layout.item_recommendation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TableTennisProduct product = products.get(position);
        holder.name.setText(product.getName());
        holder.views.setText(product.getViews() + " views");

        List<String> imageUrls = product.getImageUrls();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            String imageUrl = imageUrls.get(0);
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.ic_launcher_background);
        }

        // 1) Card click → open product details

        // This allows for the animation process too
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                v.animate()
                        .scaleX(0.95f)
                        .scaleY(0.95f)
                        .setDuration(100)
                        .withEndAction(() -> {
                            v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                            clickListener.onProductClick(product);
                        })
                        .start();
            }
        });


        // 2) Initialize the heart icon state:
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // Not signed in → always show outline, tag = false
            holder.heartIcon.setImageResource(R.drawable.ic_wishlist);
            holder.heartIcon.setTag(false);
        } else {
            // Check Firestore: is this product already in the user's wishlist?
            String uid = currentUser.getUid();
            firestoreRepository.checkIfProductInWishlist(uid, product.getId(), new FirestoreRepository.WishlistOperationCallback() {
                @Override
                public void onSuccess() {
                    // Document exists → product is wishlisted
                    holder.heartIcon.setImageResource(R.drawable.ic_wishlist_filled);
                    holder.heartIcon.setTag(true);
                }

                @Override
                public void onError(Exception e) {
                    // Either "Product not in wishlist" or real Firestore error
                    // In either case, default to outline + tag=false
                    holder.heartIcon.setImageResource(R.drawable.ic_wishlist);
                    holder.heartIcon.setTag(false);
                }
            });
        }

        // 3) Heart‐icon click → toggle wishlist
        holder.heartIcon.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(context, "Please log in to add items to your wishlist", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = user.getUid();
            Boolean isCurrentlyWishlisted = (Boolean) holder.heartIcon.getTag();
            if (isCurrentlyWishlisted != null && isCurrentlyWishlisted) {
                // → remove from wishlist
                firestoreRepository.removeProductFromWishlist(uid, product.getId(), new FirestoreRepository.WishlistOperationCallback() {
                    @Override
                    public void onSuccess() {
                        holder.heartIcon.setImageResource(R.drawable.ic_wishlist);
                        holder.heartIcon.setTag(false);
                        Toast.makeText(context, product.getName() + " removed from wishlist", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(context, "Failed to remove from wishlist: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                // → add to wishlist
                firestoreRepository.addProductToWishlist(uid, product, new FirestoreRepository.WishlistOperationCallback() {
                    @Override
                    public void onSuccess() {
                        holder.heartIcon.setImageResource(R.drawable.ic_wishlist_filled);
                        holder.heartIcon.setTag(true);
                        Toast.makeText(context, product.getName() + " added to wishlist!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(context, "Failed to add to wishlist: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
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
