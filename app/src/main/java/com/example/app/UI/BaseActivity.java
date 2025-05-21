package com.example.app.UI;

import com.example.app.R;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

import com.example.app.databinding.ActivityBaseBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BaseActivity<ContentBinding extends ViewBinding>
        extends AppCompatActivity {

    private ActivityBaseBinding baseBinding;
    protected ContentBinding binding;  // this will be the screen's binding

    /** Subclasses inflate their own content binding here. */
    protected abstract ContentBinding inflateContentBinding();

    /** Which nav item should be selected */
    protected abstract int getSelectedMenuItemId();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // inflate the base layout
        baseBinding = ActivityBaseBinding.inflate(getLayoutInflater());
        setContentView(baseBinding.getRoot());

        // hook up the toolbar
        setSupportActionBar(baseBinding.toolbar);

        // inflate child content and stick it into the container
        binding = inflateContentBinding();
        baseBinding.contentContainer.addView(binding.getRoot());

        // then do the bottom nav wiring
        setupBottomNavigation(baseBinding.bottomNavigation);
    }

    private void setupBottomNavigation(BottomNavigationView bottomNav) {
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
