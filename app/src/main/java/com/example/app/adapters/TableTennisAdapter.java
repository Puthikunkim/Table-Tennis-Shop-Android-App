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

public class TableTennisAdapter extends ArrayAdapter<TableTennisProduct> {

    private final Context mContext;
    private final int mResource;
    private final List<TableTennisProduct> mProducts;
    private final Set<String> wishlistIds = new HashSet<>();

    private FirebaseUser user;
    private final FirestoreRepository firestoreRepository;

    public TableTennisAdapter(Context context, int resource, List<TableTennisProduct> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
        mProducts = objects;

        user = FirebaseAuth.getInstance().getCurrentUser();
        firestoreRepository = FirestoreRepository.getInstance();

        // If the user is already logged in, preload their wishlist IDs:
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
                    // Now that we know which IDs are in the wishlist, update all hearts:
                    notifyDataSetChanged();
                }

                @Override
                public void onError(Exception e) {
                    // We could log or show a toast, but for now just ignore.
                }
            });
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemView = convertView;
        if (itemView == null) {
            itemView = LayoutInflater.from(mContext).inflate(mResource, parent, false);
        }

        TableTennisProduct product = mProducts.get(position);

        TextView nameTextView = itemView.findViewById(R.id.textViewProductName);
        TextView descTextView = itemView.findViewById(R.id.textViewProductDescription);
        TextView priceTextView = itemView.findViewById(R.id.textViewProductPrice);
        ImageView imageView = itemView.findViewById(R.id.imageViewProduct);
        ImageButton heartButton = itemView.findViewById(R.id.btnWishlist);

        // Populate text fields:
        nameTextView.setText(product.getName());
        descTextView.setText(product.getDescription());
        priceTextView.setText(String.format("$%.2f", product.getPrice()));

        // (Optional) If you have Glide or Picasso set up, load the product image URL:
        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            Glide.with(mContext)
                    .load(product.getImageUrls().get(0))
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_background)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.ic_launcher_background);
        }

        // 1) Re‐fetch the current user in case they just signed in/out:
        user = FirebaseAuth.getInstance().getCurrentUser();

        // 2) Determine if this product is currently in the wishlist:
        boolean isInWishlist = false;
        if (product.getId() != null) {
            isInWishlist = wishlistIds.contains(product.getId());
        }

        // 3) Set the correct heart icon:
        heartButton.setImageResource(
                isInWishlist
                        ? R.drawable.ic_filledheart
                        : R.drawable.ic_unfilledheart
        );

        // 4) Now wire up onClick:
        heartButton.setOnClickListener(v -> {
            // If not logged in, tell them to sign in:
            if (user == null) {
                Toast.makeText(
                        mContext,
                        "Please sign in to add items to your wishlist",
                        Toast.LENGTH_SHORT
                ).show();
                // Launch ProfileActivity (or your login screen):
                Intent signInIntent = new Intent(mContext, ProfileActivity.class);
                mContext.startActivity(signInIntent);
                return;
            }

            // If product.getId() is null for some reason, do nothing
            if (product.getId() == null) return;

            // Re‐compute “is it currently in wishlist?” (in case it changed):
            boolean currentlyIn = wishlistIds.contains(product.getId());
            if (currentlyIn) {
                // === REMOVE from wishlist in Firestore ===
                firestoreRepository.removeProductFromWishlist(
                        user.getUid(),
                        product.getId(),
                        new FirestoreRepository.WishlistOperationCallback() {
                            @Override
                            public void onSuccess() {
                                // Update local set + UI:
                                wishlistIds.remove(product.getId());
                                notifyDataSetChanged();
                            }

                            @Override
                            public void onError(Exception e) {
                                // You could show a Toast if you want:
                                // Toast.makeText(mContext, "Failed to remove from wishlist", Toast.LENGTH_SHORT).show();
                            }
                        }
                );
            } else {
                // === ADD to wishlist in Firestore ===
                firestoreRepository.addProductToWishlist(
                        user.getUid(),
                        product,
                        new FirestoreRepository.WishlistOperationCallback() {
                            @Override
                            public void onSuccess() {
                                // Update local set + UI:
                                wishlistIds.add(product.getId());
                                notifyDataSetChanged();
                            }

                            @Override
                            public void onError(Exception e) {
                                // You could show a Toast if you want:
                                // Toast.makeText(mContext, "Failed to add to wishlist", Toast.LENGTH_SHORT).show();
                            }
                        }
                );
            }
        });

        return itemView;
    }
}
