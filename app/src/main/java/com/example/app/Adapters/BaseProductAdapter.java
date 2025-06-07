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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public abstract class BaseProductAdapter<T extends BaseProductAdapter.BaseViewHolder> extends RecyclerView.Adapter<T> {
    protected final Context context;
    protected final List<TableTennisProduct> products;
    protected final FirestoreRepository firestoreRepository;
    protected OnProductClickListener clickListener;
    protected OnWishlistChangeListener wishlistListener;

    public interface OnProductClickListener {
        void onProductClick(TableTennisProduct product);
    }

    public interface OnWishlistChangeListener {
        void onWishlistChanged(TableTennisProduct product, boolean added);
    }

    public BaseProductAdapter(Context context, List<TableTennisProduct> products) {
        this.context = context;
        this.products = products;
        this.firestoreRepository = FirestoreRepository.getInstance();
    }

    public void setOnProductClickListener(OnProductClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnWishlistChangeListener(OnWishlistChangeListener listener) {
        this.wishlistListener = listener;
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    protected void loadProductImage(ImageView imageView, TableTennisProduct product) {
        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            ImageLoader.loadProductImage(context, imageView, product.getImageUrls().get(0));
        } else {
            imageView.setImageResource(R.drawable.ic_launcher_background);
        }
    }

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

        heartIcon.setOnClickListener(v -> handleWishlistClick(heartIcon, product));
    }

    private void updateWishlistButtonState(ImageView heartIcon, boolean isWishlisted) {
        heartIcon.setImageResource(isWishlisted ? R.drawable.ic_wishlist_filled : R.drawable.ic_wishlist);
        heartIcon.setTag(isWishlisted);
    }

    private void handleWishlistClick(ImageView heartIcon, TableTennisProduct product) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(context, "Please log in to add items to your wishlist", Toast.LENGTH_SHORT).show();
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

    private void addToWishlist(String uid, TableTennisProduct product, ImageView heartIcon) {
        firestoreRepository.addProductToWishlist(uid, product, new FirestoreRepository.WishlistOperationCallback() {
            @Override
            public void onSuccess() {
                updateWishlistButtonState(heartIcon, true);
                notifyWishlistChanged(product, true);
                Toast.makeText(context, product.getName() + " added to wishlist!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(context, "Failed to add to wishlist: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void removeFromWishlist(String uid, TableTennisProduct product, ImageView heartIcon) {
        firestoreRepository.removeProductFromWishlist(uid, product.getId(), new FirestoreRepository.WishlistOperationCallback() {
            @Override
            public void onSuccess() {
                updateWishlistButtonState(heartIcon, false);
                notifyWishlistChanged(product, false);
                Toast.makeText(context, product.getName() + " removed from wishlist", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(context, "Failed to remove from wishlist: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void notifyWishlistChanged(TableTennisProduct product, boolean added) {
        if (wishlistListener != null) {
            wishlistListener.onWishlistChanged(product, added);
        }
    }

    public static class BaseViewHolder extends RecyclerView.ViewHolder {
        protected ImageView productImage;
        protected TextView productName;
        protected TextView productPrice;

        public BaseViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
} 