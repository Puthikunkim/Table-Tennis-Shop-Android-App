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

    static class ViewHolder {
        TextView nameTextView, descTextView, priceTextView;
        ImageView imageView;
        ImageButton heartButton;
        View productInfoLayout;
    }

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
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(mResource, parent, false);
            holder = new ViewHolder();
            holder.nameTextView = convertView.findViewById(R.id.textViewProductName);
            holder.descTextView = convertView.findViewById(R.id.textViewProductDescription);
            holder.priceTextView = convertView.findViewById(R.id.textViewProductPrice);
            holder.imageView = convertView.findViewById(R.id.imageViewProduct);
            holder.heartButton = convertView.findViewById(R.id.btnWishlist);
            holder.productInfoLayout = convertView.findViewById(R.id.productInfoLayout);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        TableTennisProduct product = mProducts.get(position);

        holder.nameTextView.setText(product.getName());
        holder.descTextView.setText(product.getDescription());
        holder.priceTextView.setText(String.format("$%.2f", product.getPrice()));

        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            Glide.with(mContext)
                    .load(product.getImageUrls().get(0))
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_background)
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.ic_launcher_background);
        }

        user = FirebaseAuth.getInstance().getCurrentUser();
        boolean isInWishlist = product.getId() != null && wishlistIds.contains(product.getId());

        holder.heartButton.setImageResource(
                isInWishlist ? R.drawable.ic_filledheart : R.drawable.ic_unfilledheart
        );

        holder.heartButton.setOnClickListener(v -> {
            if (user == null) {
                Toast.makeText(mContext, "Please sign in to add items to your wishlist", Toast.LENGTH_SHORT).show();
                Intent signInIntent = new Intent(mContext, ProfileActivity.class);
                mContext.startActivity(signInIntent);
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

        // Adding in the code for the animations on the list views
        holder.productInfoLayout.setOnClickListener(v -> {
            if (product.getId() != null) {
                v.animate()
                        .scaleX(0.95f)
                        .scaleY(0.95f)
                        .setDuration(100)
                        .withEndAction(() -> {
                            v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                            Intent intent = new Intent(mContext, com.example.app.UI.DetailsActivity.class);
                            intent.putExtra("productId", product.getId());
                            mContext.startActivity(intent);
                        })
                        .start();
            } else {
                Toast.makeText(mContext, "Product ID missing", Toast.LENGTH_SHORT).show();
            }
        });


        return convertView;
    }

}
