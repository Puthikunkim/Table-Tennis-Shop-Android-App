package com.example.app.Adapters;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.Data.FirestoreRepository;
import com.example.app.Model.TableTennisProduct;
import com.example.app.R;
import com.example.app.Util.ImageLoader;
import com.example.app.Util.ToastUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import android.view.LayoutInflater;

/**
 * Abstract base adapter for displaying products.
 * Handles image loading, wishlist logic, and click animations.
 * Designed to be extended by specific product adapters (e.g., wishlist or catalog views).
 */
public abstract class BaseProductAdapter<T extends BaseProductAdapter.BaseViewHolder> extends RecyclerView.Adapter<T> {
    protected final Context context;
    protected final List<TableTennisProduct> products;
    protected final FirestoreRepository firestoreRepository;
    protected OnProductClickListener clickListener;
    protected OnWishlistChangeListener wishlistListener;

    // Interface for handling product item clicks
    public interface OnProductClickListener {
        void onProductClick(TableTennisProduct product);
    }

    // Interface for notifying wishlist state changes (add/remove)
    public interface OnWishlistChangeListener {
        void onWishlistChanged(TableTennisProduct product, boolean added);
    }

    // Constructor
    public BaseProductAdapter(Context context, List<TableTennisProduct> products) {
        this.context = context;
        this.products = products;
        this.firestoreRepository = FirestoreRepository.getInstance();
    }

    // Setter for click listener
    public void setOnProductClickListener(OnProductClickListener listener) {
        this.clickListener = listener;
    }

    // Setter for wishlist change listener
    public void setOnWishlistChangeListener(OnWishlistChangeListener listener) {
        this.wishlistListener = listener;
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    // Load the product's first image or fallback if unavailable
    protected void loadProductImage(ImageView imageView, TableTennisProduct product) {
        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            ImageLoader.loadProductImage(context, imageView, product.getImageUrls().get(0));
        } else {
            imageView.setImageResource(R.drawable.ic_launcher_background);
        }
    }

    // Sets up product item click animation and callback
    protected void setupProductClick(View itemView, TableTennisProduct product) {
        itemView.setOnClickListener(v -> {
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

    /**
     * Initializes the heart icon for wishlist toggle.
     * 1. Checks if product is in wishlist
     * 2. Updates icon state
     * 3. Sets up click logic
     */
    protected void setupWishlistButton(ImageView heartIcon, TableTennisProduct product) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            updateWishlistButtonState(heartIcon, false);
            return;
        }

        String uid = currentUser.getUid();
        firestoreRepository.checkIfProductInWishlist(uid, product.getId(), new FirestoreRepository.WishlistOperationCallback() {
            @Override
            public void onSuccess() {
                updateWishlistButtonState(heartIcon, true);
            }

            @Override
            public void onError(Exception e) {
                updateWishlistButtonState(heartIcon, false);
            }
        });

        heartIcon.setOnClickListener(v -> {
            v.animate()
                    .scaleX(1.3f)
                    .scaleY(1.3f)
                    .setDuration(150)
                    .withEndAction(() -> {
                        v.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(150)
                                .start();

                        handleWishlistClick((ImageView) v, product); // same toggle logic
                    })
                    .start();
        });

    }

    // Updates the visual heart icon and stores new state as tag
    private void updateWishlistButtonState(ImageView heartIcon, boolean isWishlisted) {
        heartIcon.setImageResource(isWishlisted ? R.drawable.ic_wishlist_filled_grey : R.drawable.ic_wishlist_grey);
        heartIcon.setTag(isWishlisted);
    }

    // Handles logic when user taps wishlist heart icon
    private void handleWishlistClick(ImageView heartIcon, TableTennisProduct product) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            ToastUtils.showCustomToast(context, "Please log in to add items to your wishlist");
            return;
        }

        String uid = user.getUid();
        Boolean isCurrentlyWishlisted = (Boolean) heartIcon.getTag();
        boolean newState = !(isCurrentlyWishlisted != null && isCurrentlyWishlisted);

        if (newState) {
            addToWishlist(uid, product, heartIcon);
        } else {
            removeFromWishlist(uid, product, heartIcon);
        }
    }

    // Adds product to wishlist in Firestore and updates UI
    private void addToWishlist(String uid, TableTennisProduct product, ImageView heartIcon) {
        firestoreRepository.addProductToWishlist(uid, product, new FirestoreRepository.WishlistOperationCallback() {
            @Override
            public void onSuccess() {
                updateWishlistButtonState(heartIcon, true);
                notifyWishlistChanged(product, true);
                ToastUtils.showCustomToast(context, product.getName() + " added to wishlist!");
            }

            @Override
            public void onError(Exception e) {
                ToastUtils.showCustomToast(context, "Failed to add to wishlist: " + e.getMessage());
            }
        });
    }

    // Removes product from wishlist in Firestore and updates UI
    private void removeFromWishlist(String uid, TableTennisProduct product, ImageView heartIcon) {
        firestoreRepository.removeProductFromWishlist(uid, product.getId(), new FirestoreRepository.WishlistOperationCallback() {
            @Override
            public void onSuccess() {
                updateWishlistButtonState(heartIcon, false);
                notifyWishlistChanged(product, false);
                ToastUtils.showCustomToast(context, product.getName() + " removed from wishlist");
            }

            @Override
            public void onError(Exception e) {
                ToastUtils.showCustomToast(context, "Failed to remove from wishlist: " + e.getMessage());
            }
        });
    }

    // Notify external listener about wishlist change (e.g., UI update)
    private void notifyWishlistChanged(TableTennisProduct product, boolean added) {
        if (wishlistListener != null) {
            wishlistListener.onWishlistChanged(product, added);
        }
    }

    /**
     * Base ViewHolder class to be extended by specific adapters.
     * Holds references to product views like image, name, and price.
     */
    public static class BaseViewHolder extends RecyclerView.ViewHolder {
        protected ImageView productImage;
        protected TextView productName;
        protected TextView productPrice;

        public BaseViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
