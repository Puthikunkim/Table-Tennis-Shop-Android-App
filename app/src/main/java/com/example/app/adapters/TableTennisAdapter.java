package com.example.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.app.Model.TableTennisProduct;
import com.example.app.R;
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
    private FirestoreRepository firestoreRepository;

    public TableTennisAdapter(Context context, int resource, List<TableTennisProduct> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
        mProducts = objects;

        user = FirebaseAuth.getInstance().getCurrentUser();
        firestoreRepository = FirestoreRepository.getInstance();

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
                    // Ignore errors for now
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

        nameTextView.setText(product.getName());
        descTextView.setText(product.getDescription());
        priceTextView.setText(String.format("$%.2f", product.getPrice()));

        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            Glide.with(mContext)
                    .load(product.getImageUrls().get(0))
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_background)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.ic_launcher_background);
        }

        // Re‐fetch the user each time in case they logged in/out
        user = FirebaseAuth.getInstance().getCurrentUser();

        // Determine if this product is in the wishlist
        boolean isInWishlist = false;
        if (product.getId() != null) {
            isInWishlist = wishlistIds.contains(product.getId());
        }

        // Set heart icon based on presence in wishlist
        heartButton.setImageResource(
                isInWishlist
                        ? R.drawable.ic_filledheart
                        : R.drawable.ic_unfilledheart
        );

        // No click listener yet (commit 3 will add toggling behavior)
        heartButton.setOnClickListener(null);

        return itemView;
    }
}
