package com.example.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.bumptech.glide.Glide;
import com.example.app.Data.FirestoreRepository;
import com.example.app.Model.TableTennisProduct;
import com.example.app.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartAdapter extends BaseAdapter {

    private final Context context;
    private final List<TableTennisProduct> cartItems;
    private final LayoutInflater inflater;
    private final FirestoreRepository repo = FirestoreRepository.getInstance();
    private final String userId;
    private final Runnable onCartChanged;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Map<String, Runnable> pendingUpdates = new HashMap<>();

    public CartAdapter(Context context, List<TableTennisProduct> cartItems, String userId, Runnable onCartChanged) {
        this.context = context;
        this.cartItems = cartItems;
        this.userId = userId;
        this.onCartChanged = onCartChanged;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return cartItems.size();
    }

    @Override
    public Object getItem(int position) {
        return cartItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        ImageView productImage;
        TextView productName, productPrice, quantityText, totalPrice;
        Button incrementButton, decrementButton;
        ImageButton deleteButton;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        TableTennisProduct product = cartItems.get(position);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_cart_product, parent, false);
            holder = new ViewHolder();
            holder.productImage = convertView.findViewById(R.id.productImage);
            holder.productName = convertView.findViewById(R.id.productName);
            holder.productPrice = convertView.findViewById(R.id.productPrice);
            holder.quantityText = convertView.findViewById(R.id.quantityText);
            holder.totalPrice = convertView.findViewById(R.id.totalPrice);
            holder.incrementButton = convertView.findViewById(R.id.incrementButton);
            holder.decrementButton = convertView.findViewById(R.id.decrementButton);
            holder.deleteButton = convertView.findViewById(R.id.deleteButton);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.productName.setText(product.getName());
        holder.productPrice.setText(String.format("$%.2f", product.getPrice()));
        holder.quantityText.setText(String.valueOf(product.getCartQuantity()));
        holder.totalPrice.setText(String.format("$%.2f", product.getCartQuantity() * product.getPrice()));

        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            Glide.with(context).load(product.getImageUrls().get(0)).into(holder.productImage);
        }

        holder.incrementButton.setOnClickListener(v -> {
            int previousQty = product.getCartQuantity();
            int newQty = previousQty + 1;
            product.setCartQuantity(newQty);
            notifyDataSetChanged();
            onCartChanged.run();

            scheduleDebouncedCartUpdate(product, newQty - previousQty);
        });

        holder.decrementButton.setOnClickListener(v -> {
            int currentQty = product.getCartQuantity();
            if (currentQty > 1) {
                int newQty = currentQty - 1;
                product.setCartQuantity(newQty);
                notifyDataSetChanged();
                onCartChanged.run();

                scheduleDebouncedCartUpdate(product, newQty - currentQty);
            } else {
                repo.removeFromCart(userId, product.getId(), new FirestoreRepository.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(context, "Item removed from cart", Toast.LENGTH_SHORT).show();
                        cartItems.remove(position);
                        notifyDataSetChanged();
                        onCartChanged.run();
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(context, "Failed to remove item", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            repo.removeFromCart(userId, product.getId(), new FirestoreRepository.OperationCallback() {
                @Override
                public void onSuccess() {
                    cartItems.remove(position);
                    notifyDataSetChanged();
                    onCartChanged.run();
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(context, "Failed to delete item", Toast.LENGTH_SHORT).show();
                }
            });
        });

        convertView.setOnClickListener(v -> {
            Intent intent = new Intent(context, com.example.app.UI.DetailsActivity.class);
            intent.putExtra("productId", product.getId());
            context.startActivity(intent);
        });

        return convertView;
    }

    private void scheduleDebouncedCartUpdate(TableTennisProduct product, int deltaQty) {
        Runnable previous = pendingUpdates.get(product.getId());
        if (previous != null) {
            handler.removeCallbacks(previous);
        }

        Runnable updateTask = () -> {
            repo.addToCart(userId, product, deltaQty, null);
            pendingUpdates.remove(product.getId());
        };

        pendingUpdates.put(product.getId(), updateTask);
        handler.postDelayed(updateTask, 500); // 500ms debounce
    }
}