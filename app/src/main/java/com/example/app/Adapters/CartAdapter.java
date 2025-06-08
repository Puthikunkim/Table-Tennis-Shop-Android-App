package com.example.app.Adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.example.app.Data.FirestoreRepository;
import com.example.app.Model.TableTennisProduct;
import com.example.app.R;
import com.example.app.UI.DetailsActivity;
import com.example.app.Util.ImageLoader;
import com.example.app.Util.ToastUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter for displaying cart items in a ListView.
 * Supports incrementing, decrementing, and removing items with debounced Firestore updates.
 */
public class CartAdapter extends BaseAdapter {
    private static final long DEBOUNCE_DELAY = 500; // 500ms debounce for batch-like updates
    private static final String CURRENCY_FORMAT = "$%.2f";

    private final Context context;
    private final List<TableTennisProduct> cartItems;
    private final LayoutInflater inflater;
    private final FirestoreRepository repo;
    private final String userId;
    private final Runnable onCartChanged;
    private final Handler handler;
    private final Map<String, Runnable> pendingUpdates; // Stores pending quantity updates for debounce

    public CartAdapter(Context context, List<TableTennisProduct> cartItems, String userId, Runnable onCartChanged) {
        this.context = context;
        this.cartItems = cartItems;
        this.userId = userId;
        this.onCartChanged = onCartChanged;
        this.inflater = LayoutInflater.from(context);
        this.repo = FirestoreRepository.getInstance();
        this.handler = new Handler(Looper.getMainLooper());
        this.pendingUpdates = new HashMap<>();
    }

    @Override
    public int getCount() {
        return cartItems.size();
    }

    @Override
    public TableTennisProduct getItem(int position) {
        return cartItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Inflates and binds each cart item view
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        TableTennisProduct product = getItem(position);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_cart_product, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        bindViewHolder(holder, product, position);
        return convertView;
    }

    /**
     * Binds product data and click listeners to the view components.
     */
    private void bindViewHolder(ViewHolder holder, TableTennisProduct product, int position) {
        holder.productName.setText(product.getName());
        holder.productPrice.setText(String.format(CURRENCY_FORMAT, product.getPrice()));
        holder.quantityText.setText(String.valueOf(product.getCartQuantity()));
        holder.totalPrice.setText(String.format(CURRENCY_FORMAT, product.getCartQuantity() * product.getPrice()));

        // Load image if available
        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            ImageLoader.loadProductImage(context, holder.productImage, product.getImageUrls().get(0));
        }

        // Set up buttons and click behavior
        setupIncrementButton(holder, product);
        setupDecrementButton(holder, product, position);
        setupDeleteButton(holder, product, position);
        setupProductClick(holder.itemView, product);
    }

    /**
     * Increments product quantity and schedules a Firestore update.
     */
    private void setupIncrementButton(ViewHolder holder, TableTennisProduct product) {
        holder.incrementButton.setOnClickListener(v -> {
            int newQty = product.getCartQuantity() + 1;
            product.setCartQuantity(newQty);
            notifyDataSetChanged();         // Refresh UI
            onCartChanged.run();            // Notify parent (e.g., update total)
            scheduleDebouncedCartUpdate(product, 1);
        });
    }

    /**
     * Decrements product quantity or removes it if quantity == 1.
     */
    private void setupDecrementButton(ViewHolder holder, TableTennisProduct product, int position) {
        holder.decrementButton.setOnClickListener(v -> {
            int currentQty = product.getCartQuantity();
            if (currentQty > 1) {
                int newQty = currentQty - 1;
                product.setCartQuantity(newQty);
                notifyDataSetChanged();
                onCartChanged.run();
                scheduleDebouncedCartUpdate(product, -1);
            } else {
                removeItemFromCart(product, position); // Remove item if quantity drops to 0
            }
        });
    }

    /**
     * Deletes product from Firestore and removes it locally from the list.
     */
    private void setupDeleteButton(ViewHolder holder, TableTennisProduct product, int position) {
        holder.deleteButton.setOnClickListener(v -> removeItemFromCart(product, position));
    }

    /**
     * Navigates to DetailsActivity when the product is clicked.
     */
    private void setupProductClick(View itemView, TableTennisProduct product) {
        itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailsActivity.class);
            intent.putExtra("productId", product.getId());
            context.startActivity(intent);
        });
    }

    /**
     * Remove the item from the cart both in Firestore and the local list.
     */
    private void removeItemFromCart(TableTennisProduct product, int position) {
        repo.removeFromCart(userId, product.getId(), new FirestoreRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                cartItems.remove(position);
                notifyDataSetChanged();
                onCartChanged.run();
                if (position == 0) {
                    ToastUtils.showCustomToast(context, "Item removed from cart");
                }
            }

            @Override
            public void onError(Exception e) {
                ToastUtils.showCustomToast(context, "Failed to remove item: " + e.getMessage());
            }
        });
    }

    /**
     * Debounce Firestore updates when quantity is changed to avoid rapid network calls.
     */
    private void scheduleDebouncedCartUpdate(TableTennisProduct product, int deltaQty) {
        Runnable previous = pendingUpdates.get(product.getId());
        if (previous != null) {
            handler.removeCallbacks(previous); // Cancel previous pending update if any
        }

        Runnable updateTask = () -> {
            repo.addToCart(userId, product, deltaQty, null);
            pendingUpdates.remove(product.getId());
        };

        pendingUpdates.put(product.getId(), updateTask);
        handler.postDelayed(updateTask, DEBOUNCE_DELAY);
    }

    /**
     * ViewHolder pattern to cache view references for performance.
     */
    static class ViewHolder {
        final View itemView;
        final ImageView productImage;
        final TextView productName;
        final TextView productPrice;
        final TextView quantityText;
        final TextView totalPrice;
        final Button incrementButton;
        final Button decrementButton;
        final ImageButton deleteButton;

        ViewHolder(View itemView) {
            this.itemView = itemView;
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            quantityText = itemView.findViewById(R.id.quantityText);
            totalPrice = itemView.findViewById(R.id.totalPrice);
            incrementButton = itemView.findViewById(R.id.incrementButton);
            decrementButton = itemView.findViewById(R.id.decrementButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
