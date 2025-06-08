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

    private static final int[] TAB_ORDER = {
            R.id.home, R.id.search, R.id.wish_list, R.id.cart, R.id.profile
    };

    private static int lastSelectedTab = R.id.home;


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
        bottomNav.setSelectedItemId(getSelectedMenuItemId());

        bottomNav.setOnItemSelectedListener(item -> {
            int newTabId = item.getItemId();
            Class<?> targetActivity = null;

            if (newTabId == R.id.home) {
                targetActivity = MainActivity.class;
            } else if (newTabId == R.id.cart) {
                targetActivity = CartActivity.class;
            } else if (newTabId == R.id.search) {
                targetActivity = SearchActivity.class;
            } else if (newTabId == R.id.profile) {
                targetActivity = ProfileActivity.class;
            } else if (newTabId == R.id.wish_list) {
                targetActivity = WishListActivity.class;
            }

            if (targetActivity != null && !this.getClass().equals(targetActivity)) {
                Intent intent = new Intent(this, targetActivity);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);

                // Determine direction
                int lastIndex = getTabIndex(lastSelectedTab);
                int newIndex = getTabIndex(newTabId);
                if (newIndex > lastIndex) {
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left); // forward
                } else {
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right); // backward
                }

                lastSelectedTab = newTabId;
                finish();
                return true;
            }

            return false;
        });
    }

    private int getTabIndex(int tabId) {
        for (int i = 0; i < TAB_ORDER.length; i++) {
            if (TAB_ORDER[i] == tabId) return i;
        }
        return -1;
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
