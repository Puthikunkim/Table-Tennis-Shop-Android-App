package com.example.app.UI;

import com.example.app.R;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BaseActivity<B extends ViewBinding> extends AppCompatActivity {
    protected B binding;

    /** Subclasses must inflate their binding here. */
    protected abstract B inflateBinding();

    /** Which nav menu item should be highlighted */
    protected abstract int getSelectedMenuItemId();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = inflateBinding();
        setContentView(binding.getRoot());
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        // cast binding to any generated subclass that has bottom_navigation
        BottomNavigationView bottomNav = (BottomNavigationView) binding.getRoot().findViewById(R.id.bottom_navigation);

        bottomNav.setSelectedItemId(getSelectedMenuItemId());
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.home && getSelectedMenuItemId() != R.id.home) {
                startActivity(new Intent(this, MainActivity.class));
            } else if (id == R.id.cart && getSelectedMenuItemId() != R.id.cart) {
                startActivity(new Intent(this, CartActivity.class));
            } else if (id == R.id.search && getSelectedMenuItemId() != R.id.search) {
                startActivity(new Intent(this, SearchActivity.class));
            } else if (id == R.id.profile && getSelectedMenuItemId() != R.id.profile) {
                startActivity(new Intent(this, ProfileActivity.class));
            } else if (id == R.id.wish_list && getSelectedMenuItemId() != R.id.wish_list) {
                startActivity(new Intent(this, WishListActivity.class));
            } else {
                return false;
            }
            overridePendingTransition(0, 0);
            finish();
            return true;
        });
    }
}
