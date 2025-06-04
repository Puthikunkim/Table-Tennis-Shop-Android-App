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
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        // inflate child content and stick it into the container
        binding = inflateContentBinding();
        baseBinding.contentContainer.addView(binding.getRoot());

        // then do the bottom nav wiring
        setupBottomNavigation(baseBinding.bottomNavigation);

        // Logo nav
        setupLogoNavigation();
    }

    private void setupBottomNavigation(BottomNavigationView bottomNav) {
        bottomNav.setSelectedItemId(getSelectedMenuItemId());
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Class<?> targetActivity = null;

            if (id == R.id.home) {
                targetActivity = MainActivity.class;
            } else if (id == R.id.cart) {
                targetActivity = CartActivity.class;
            } else if (id == R.id.search) {
                targetActivity = SearchActivity.class;
            } else if (id == R.id.profile) {
                targetActivity = ProfileActivity.class;
            } else if (id == R.id.wish_list) {
                targetActivity = WishListActivity.class;
            }

            if (targetActivity != null) {
                Intent intent = new Intent(this, targetActivity);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }

    private void setupLogoNavigation() {
        baseBinding.toolbar.findViewById(R.id.logoImageView).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

}