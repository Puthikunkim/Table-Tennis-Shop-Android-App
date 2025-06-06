package com.example.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.app.Model.TableTennisProduct;
import com.example.app.R;
import com.example.app.UI.ProfileActivity;
import com.example.app.Data.FirestoreRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProductAdapter extends BaseProductAdapter<ProductAdapter.ViewHolder> {
    private final Set<String> wishlistIds = new HashSet<>();
    private FirebaseUser user;

    public ProductAdapter(Context context, List<TableTennisProduct> products) {
        super(context, products);
        this.user = FirebaseAuth.getInstance().getCurrentUser();

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

        loadProductImage(holder.productImage, product);
        setupProductClick(holder.itemView, product);

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
    }

    public void setProducts(List<TableTennisProduct> newProducts) {
        products.clear();
        products.addAll(newProducts);
        notifyDataSetChanged();
    }

    static class ViewHolder extends BaseViewHolder {
        TextView productDescription;
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