package com.example.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.app.Model.TableTennisProduct;
import com.example.app.R;
import com.example.app.UI.ProfileActivity;
import com.example.app.Data.FirestoreRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private final Context context;
    private final List<TableTennisProduct> products;
    private final Set<String> wishlistIds = new HashSet<>();
    private FirebaseUser user;
    private final FirestoreRepository firestoreRepository;
    private OnProductClickListener clickListener;

    public interface OnProductClickListener {
        void onProductClick(TableTennisProduct product);
    }

    public ProductAdapter(Context context, List<TableTennisProduct> products) {
        this.context = context;
        this.products = products;
        this.user = FirebaseAuth.getInstance().getCurrentUser();
        this.firestoreRepository = FirestoreRepository.getInstance();

        // If the user is already logged in, preload their wishlist IDs
        if (user != null) {
            firestoreRepository.getWishlistProducts(user.getUid(), new FirestoreRepository.WishlistProductsCallback() {
                @Override
                public void onSuccess(List<TableTennisProduct> wishlistItems) {
                    wishlistIds.clear();
                    for (TableTennisProduct item : wishlistItems) {
                        if (item.getId() != null) {
                            wishlistIds.add(item.getId());
                        }
                    }
                    notifyDataSetChanged();
                }

                @Override
                public void onError(Exception e) {
                    // We could log or show a toast, but for now just ignore
                }
            });
        }
    }

    public void setOnProductClickListener(OnProductClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_search_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TableTennisProduct product = products.get(position);

        holder.productName.setText(product.getName());
        holder.productDescription.setText(product.getDescription());
        holder.productPrice.setText(String.format("$%.2f", product.getPrice()));

        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            Glide.with(context)
                    .load(product.getImageUrls().get(0))
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_background)
                    .into(holder.productImage);
        } else {
            holder.productImage.setImageResource(R.drawable.ic_launcher_background);
        }

        user = FirebaseAuth.getInstance().getCurrentUser();
        boolean isInWishlist = product.getId() != null && wishlistIds.contains(product.getId());

        holder.btnWishlist.setImageResource(
                isInWishlist ? R.drawable.ic_filledheart : R.drawable.ic_unfilledheart
        );

        holder.btnWishlist.setOnClickListener(v -> {
            v.animate()
                    .scaleX(1.4f)
                    .scaleY(1.4f)
                    .setDuration(120)
                    .withEndAction(() -> {
                        v.animate().scaleX(1f).scaleY(1f).setDuration(120).start();

                        if (user == null) {
                            Toast.makeText(context, "Please sign in to add items to your wishlist", Toast.LENGTH_SHORT).show();
                            Intent signInIntent = new Intent(context, ProfileActivity.class);
                            context.startActivity(signInIntent);
                            return;
                        }

                        if (product.getId() == null) return;

                        boolean currentlyIn = wishlistIds.contains(product.getId());
                        if (currentlyIn) {
                            firestoreRepository.removeProductFromWishlist(user.getUid(), product.getId(), new FirestoreRepository.WishlistOperationCallback() {
                                @Override
                                public void onSuccess() {
                                    wishlistIds.remove(product.getId());
                                    notifyDataSetChanged();
                                }

                                @Override
                                public void onError(Exception e) {}
                            });
                        } else {
                            firestoreRepository.addProductToWishlist(user.getUid(), product, new FirestoreRepository.WishlistOperationCallback() {
                                @Override
                                public void onSuccess() {
                                    wishlistIds.add(product.getId());
                                    notifyDataSetChanged();
                                }

                                @Override
                                public void onError(Exception e) {}
                            });
                        }
                    }).start();
        });

        holder.itemView.setOnClickListener(v -> {
            v.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                        if (clickListener != null) {
                            clickListener.onProductClick(product);
                        }
                    }).start();
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void setProducts(List<TableTennisProduct> newProducts) {
        products.clear();
        products.addAll(newProducts);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productDescription, productPrice;
        ImageButton btnWishlist;

        ViewHolder(View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productDescription = itemView.findViewById(R.id.productDescription);
            productPrice = itemView.findViewById(R.id.productPrice);
            btnWishlist = itemView.findViewById(R.id.btnWishlist);
        }
    }
} 