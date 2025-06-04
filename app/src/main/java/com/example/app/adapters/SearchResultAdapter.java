package com.example.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.Model.TableTennisProduct;
import com.example.app.R;
import com.example.app.Data.FirestoreRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.Set;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {

    // Declare custom listener interface here:
    public interface OnProductClickListener {
        void onProductClick(TableTennisProduct product);
    }

    private final List<TableTennisProduct> products;
    private final Context context;
    private OnProductClickListener clickListener;  // Uses the interface declared above
    private final Set<String> wishlistIds = new java.util.HashSet<>();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private final FirestoreRepository firestoreRepository = FirestoreRepository.getInstance();

    public SearchResultAdapter(Context context, List<TableTennisProduct> products) {
        this.context = context;
        this.products = products;
        user = FirebaseAuth.getInstance().getCurrentUser();
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
                    // Optionally handle error
                }
            });
        }
    }

    /** Setter so the Activity can register its callback */
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
        holder.productPrice.setText("$" + String.format("%.2f", product.getPrice()));
        // TODO: load image into holder.productImage

        // Wishlist logic
        user = FirebaseAuth.getInstance().getCurrentUser();
        boolean isInWishlist = product.getId() != null && wishlistIds.contains(product.getId());
        holder.btnWishlist.setImageResource(
            isInWishlist ? R.drawable.ic_filledheart : R.drawable.ic_unfilledheart
        );
        holder.btnWishlist.setOnClickListener(v -> {
            if (user == null) {
                Toast.makeText(context, "Please sign in to add items to your wishlist", Toast.LENGTH_SHORT).show();
                Intent signInIntent = new Intent(context, com.example.app.UI.ProfileActivity.class);
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
        });

        // Wire up the click to custom listener:
        // Adding in the code for the animations
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

    /** If you ever need to swap the list out */
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
