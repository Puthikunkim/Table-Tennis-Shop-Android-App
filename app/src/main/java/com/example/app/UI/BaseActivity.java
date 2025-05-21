package com.example.app.UI;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.app.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * A base activity that sets up the BottomNavigationView and handles navigation between
 * Home, Cart, and Search activities. Subclasses only need to provide their layout
 * and the menu item ID to be selected.
 */
public abstract class BaseActivity extends AppCompatActivity {

    /**
     * @return the layout resource ID for this activity (e.g. R.layout.activity_main)
     */
    @LayoutRes
    protected abstract int getLayoutResourceId();

    /**
     * @return the menu item ID that should be selected in the BottomNavigationView
     *         (e.g. R.id.home, R.id.cart, or R.id.search)
     */
    protected abstract int getSelectedMenuItemId();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        // Highlight the current menu item
        bottomNavigationView.setSelectedItemId(getSelectedMenuItemId());

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.home && getSelectedMenuItemId() != R.id.home) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.cart && getSelectedMenuItemId() != R.id.cart) {
                startActivity(new Intent(this, CartActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.search && getSelectedMenuItemId() != R.id.search) {
                startActivity(new Intent(this, SearchActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.profile && getSelectedMenuItemId() != R.id.profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.wish_list && getSelectedMenuItemId() != R.id.wish_list) {
                startActivity(new Intent(this, WishListActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }
}