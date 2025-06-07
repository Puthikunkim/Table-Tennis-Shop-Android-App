package com.example.app.UI;

import com.example.app.R;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

import com.example.app.databinding.ActivityBaseBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

// Generic base class for all activities that use bottom nav + toolbar.
// This wraps common setup logic so we don't repeat it everywhere.
public abstract class BaseActivity<ContentBinding extends ViewBinding>
        extends AppCompatActivity {

    private ActivityBaseBinding baseBinding; // base layout with nav + toolbar
    protected ContentBinding binding;        // the screen-specific binding we inflate below

    /** Child activity provides its layout binding here. */
    protected abstract ContentBinding inflateContentBinding();

    /** Each screen tells us which bottom nav item should be selected. */
    protected abstract int getSelectedMenuItemId();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate and set the base layout (includes toolbar + nav)
        baseBinding = ActivityBaseBinding.inflate(getLayoutInflater());
        setContentView(baseBinding.getRoot());

        // Set up the toolbar without a default title
        setSupportActionBar(baseBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(""); // we handle titles manually or in XML
        }

        // Inflate the specific screen's content and add it to the container in base layout
        binding = inflateContentBinding();
        baseBinding.contentContainer.addView(binding.getRoot());

        // Wire up the bottom navigation bar with logic to switch activities
        setupBottomNavigation(baseBinding.bottomNavigation);

        // Clicking the logo takes you home (MainActivity)
        setupLogoNavigation();
    }

    /**
     * Handles switching activities when a bottom nav item is selected.
     * Avoids multiple instances of the same screen using FLAG_ACTIVITY_SINGLE_TOP.
     */
    private void setupBottomNavigation(BottomNavigationView bottomNav) {
        bottomNav.setSelectedItemId(getSelectedMenuItemId()); // highlight the current tab

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Class<?> targetActivity = null;

            // Decide which activity to launch based on nav item clicked
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

            // Launch the selected screen if it's different
            if (targetActivity != null) {
                Intent intent = new Intent(this, targetActivity);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0); // no animation between tabs
                finish(); // close the current activity
                return true;
            }
            return false;
        });
    }

    /**
     * Clicking the logo in the toolbar brings you back to the homepage.
     * We reuse the fade transition here for a smooth UX.
     */
    private void setupLogoNavigation() {
        baseBinding.toolbar.findViewById(R.id.logoImageView).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }
}
