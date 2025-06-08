package com.example.app.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.app.Model.TableTennisProduct;
import com.example.app.R;
import com.example.app.UI.ProfileActivity;
import com.example.app.Data.FirestoreRepository;
import com.example.app.Util.ToastUtils;
import com.example.app.Util.AnimationUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Adapter used to display product items in a RecyclerView (e.g., search or home screen).
 * Supports showing product info and letting users toggle wishlist state.
 */
public class ProductAdapter extends BaseProductAdapter<ProductAdapter.ViewHolder> {

    private final Set<String> wishlistIds; // Tracks which products are in the wishlist
    private FirebaseUser user;

    public ProductAdapter(Context context, List<TableTennisProduct> products) {
        super(context, products);
        this.wishlistIds = new HashSet<>();
        this.user = FirebaseAuth.getInstance().getCurrentUser();
        loadWishlistItems(); // Preload wishlist IDs to show correct heart icons
    }

    /**
     * Loads the user's wishlist items from Firestore and stores their IDs locally.
     */
    private void loadWishlistItems() {
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
                    notifyDataSetChanged(); // Refresh UI once data is loaded
                }

                @Override
                public void onError(Exception e) {
                    // Fail silently – show default state
                }
            });
        }
    }

    /**
     * Inflate and return a new ViewHolder for a product item.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_search_result, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Bind a product's data and behavior to the given ViewHolder.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TableTennisProduct product = products.get(position);
        bindProductData(holder, product);
        setupWishlistButton(holder, product);
    }

    /**
     * Populates the ViewHolder with product name, description, price, and image.
     */
    private void bindProductData(ViewHolder holder, TableTennisProduct product) {
        holder.productName.setText(product.getName());
        holder.productDescription.setText(product.getDescription());
        holder.productPrice.setText(String.format("$%.2f", product.getPrice()));
        loadProductImage(holder.productImage, product);
        setupProductClick(holder.itemView, product);
    }

    /**
     * Sets the heart icon state and sets up click listener for wishlist toggling.
     */
    private void setupWishlistButton(ViewHolder holder, TableTennisProduct product) {
        boolean isInWishlist = product.getId() != null && wishlistIds.contains(product.getId());
        holder.btnWishlist.setImageResource(
                isInWishlist ? R.drawable.ic_filledheart : R.drawable.ic_unfilledheart
        );

        holder.btnWishlist.setOnClickListener(v -> handleWishlistClick(v, product));
    }

    /**
     * Handles when a user taps the heart icon (with animation).
     * If logged out, they are prompted to sign in.
     */

    private void handleWishlistClick(View view, TableTennisProduct product) {
        AnimationUtils.animateButton(view, () -> {
            if (user == null) {
                promptUserToSignIn();
                return;
            }

            if (product.getId() == null) return;

            boolean currentlyIn = wishlistIds.contains(product.getId());
            toggleWishlistState(product, currentlyIn);
        });
    }


    /**
     * Redirects the user to the profile screen to sign in if they're not authenticated.
     */
    private void promptUserToSignIn() {
        ToastUtils.showCustomToast(context, "Please sign in to add items to your wishlist");
        Intent signInIntent = new Intent(context, ProfileActivity.class);
        context.startActivity(signInIntent);
    }

    /**
     * Toggles the wishlist state for a product depending on whether it's already saved.
     */
    private void toggleWishlistState(TableTennisProduct product, boolean currentlyIn) {
        if (currentlyIn) {
            removeFromWishlist(product);
        } else {
            addToWishlist(product);
        }
    }

    /**
     * Adds the given product to the user's wishlist in Firestore and updates UI.
     */
    private void addToWishlist(TableTennisProduct product) {
        firestoreRepository.addProductToWishlist(user.getUid(), product, new FirestoreRepository.WishlistOperationCallback() {
            @Override
            public void onSuccess() {
                wishlistIds.add(product.getId());
                notifyDataSetChanged(); // Update heart icon
            }

            @Override
            public void onError(Exception e) {
                // Silent fail – state will remain unchanged
            }
        });
    }

    /**
     * Removes the given product from the user's wishlist in Firestore and updates UI.
     */
    private void removeFromWishlist(TableTennisProduct product) {
        firestoreRepository.removeProductFromWishlist(user.getUid(), product.getId(), new FirestoreRepository.WishlistOperationCallback() {
            @Override
            public void onSuccess() {
                wishlistIds.remove(product.getId());
                notifyDataSetChanged(); // Update heart icon
            }

            @Override
            public void onError(Exception e) {
                // Silent fail – state will remain unchanged
            }
        });
    }

    /**
     * Replaces the current product list with a new one and refreshes the adapter.
     */
    public void setProducts(List<TableTennisProduct> newProducts) {
        products.clear();
        products.addAll(newProducts);
        notifyDataSetChanged();
    }

    /**
     * ViewHolder subclass that holds references to the product item views.
     */
    static class ViewHolder extends BaseViewHolder {
        final TextView productDescription;
        final ImageButton btnWishlist;

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
