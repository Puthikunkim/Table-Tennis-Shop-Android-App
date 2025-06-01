package com.example.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.app.Model.TableTennisProduct;
import com.example.app.R;
import com.example.app.Data.FirestoreRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TableTennisAdapter extends ArrayAdapter<TableTennisProduct> {

    private int mResource;
    private Context mContext;
    private List<TableTennisProduct> mProducts;
    private Set<String> wishlistIds = new HashSet<>();

    private FirebaseUser user;
    private FirestoreRepository firestoreRepository;

    public TableTennisAdapter(Context context, int resource, List<TableTennisProduct> objects) {
        super(context, resource, objects);
        mResource = resource;
        mContext = context;
        mProducts = objects;

        user = FirebaseAuth.getInstance().getCurrentUser();
        firestoreRepository = FirestoreRepository.getInstance();

        if (user != null) {
            firestoreRepository.getWishlistProducts(user.getUid(), new FirestoreRepository.WishlistProductsCallback() {
                @Override
                public void onSuccess(List<TableTennisProduct> wishlistItems) {
                    for (TableTennisProduct item : wishlistItems) {
                        wishlistIds.add(item.getId());
                    }
                    notifyDataSetChanged();
                }

                @Override
                public void onError(Exception e) {
                    // Handle error
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
        ImageView heartButton = itemView.findViewById(R.id.btnWishlist);

        nameTextView.setText(product.getName());
        descTextView.setText(product.getDescription());
        priceTextView.setText(String.format("$%.2f", product.getPrice()));
        imageView.setImageResource(0); // placeholder

        if (heartButton != null) {
            // Prevent the heart button from consuming clicks
            heartButton.setFocusable(false);
            heartButton.setFocusableInTouchMode(false);

            boolean isInWishlist = wishlistIds.contains(product.getId());
            heartButton.setImageResource(isInWishlist ? R.drawable.ic_filledheart : R.drawable.ic_unfilledheart);

            heartButton.setOnClickListener(v -> {
                if (user == null) return;

                if (isInWishlist) {
                    firestoreRepository.removeProductFromWishlist(user.getUid(), product.getId(), new FirestoreRepository.WishlistOperationCallback() {
                        @Override
                        public void onSuccess() {
                            wishlistIds.remove(product.getId());
                            notifyDataSetChanged();
                        }

                        @Override
                        public void onError(Exception e) { }
                    });
                } else {
                    firestoreRepository.addProductToWishlist(user.getUid(), product, new FirestoreRepository.WishlistOperationCallback() {
                        @Override
                        public void onSuccess() {
                            wishlistIds.add(product.getId());
                            notifyDataSetChanged();
                        }

                        @Override
                        public void onError(Exception e) { }
                    });
                }
            });
        }

        return itemView;
    }
}
