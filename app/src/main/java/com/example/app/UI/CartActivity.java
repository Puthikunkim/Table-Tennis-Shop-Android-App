package com.example.app.UI;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.app.Data.FirestoreRepository;
import com.example.app.Model.TableTennisProduct;
import com.example.app.R;
import com.example.app.adapters.CartAdapter;
import com.example.app.databinding.ActivityCartBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends BaseActivity<ActivityCartBinding> {

    private CartAdapter adapter;
    private final List<TableTennisProduct> cartItems = new ArrayList<>();

    @Override
    protected ActivityCartBinding inflateContentBinding() {
        return ActivityCartBinding.inflate(getLayoutInflater());
    }

    @Override
    protected int getSelectedMenuItemId() {
        return R.id.cart;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please sign in to view your cart", Toast.LENGTH_SHORT).show();
            return;
        }

        FirestoreRepository.getInstance().getCartItems(user.getUid(), new FirestoreRepository.ProductsCallback() {
            @Override
            public void onSuccess(List<TableTennisProduct> products) {
                cartItems.clear();
                cartItems.addAll(products);

                adapter = new CartAdapter(CartActivity.this, cartItems, user.getUid(), CartActivity.this::updateCartUI);
                binding.cartListView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                updateCartUI();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(CartActivity.this, "Failed to load cart", Toast.LENGTH_SHORT).show();
                cartItems.clear();
                updateCartViews();
            }
        });
    }

    private void updateTotals() {
        double subtotal = 0.0;
        for (TableTennisProduct item : cartItems) {
            subtotal += item.getPrice() * item.getCartQuantity();
        }

        View checkoutTotalView = findViewById(R.id.checkoutTotal);
        if (checkoutTotalView != null) {
            android.widget.TextView subtotalText = checkoutTotalView.findViewById(R.id.subtotalText);
            android.widget.TextView totalText = checkoutTotalView.findViewById(R.id.totalText);
            if (subtotalText != null && totalText != null) {
                subtotalText.setText(String.format("$%.2f", subtotal));
                totalText.setText(String.format("$%.2f", subtotal));
            }
        }
    }

    private void updateCartViews() {
        View emptyCartView = findViewById(R.id.emptyCart);
        View checkoutTotalView = findViewById(R.id.checkoutTotal);

        if (cartItems.isEmpty()) {
            if (emptyCartView != null) emptyCartView.setVisibility(View.VISIBLE);
            if (checkoutTotalView != null) checkoutTotalView.setVisibility(View.GONE);
        } else {
            if (emptyCartView != null) emptyCartView.setVisibility(View.GONE);
            if (checkoutTotalView != null) checkoutTotalView.setVisibility(View.VISIBLE);
        }
    }

    private void updateCartUI() {
        updateTotals();
        updateCartViews();
    }
}
