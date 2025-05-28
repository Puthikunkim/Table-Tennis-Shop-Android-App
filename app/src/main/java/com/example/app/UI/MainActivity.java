package com.example.app.UI;

import android.content.Intent;
import android.os.Bundle;

import com.example.app.R;
import com.example.app.databinding.ActivityMainBinding;

public class MainActivity extends BaseActivity<ActivityMainBinding> {

    @Override
    protected ActivityMainBinding inflateContentBinding() {
        return ActivityMainBinding.inflate(getLayoutInflater());
    }

    @Override
    protected int getSelectedMenuItemId() {
        return R.id.home;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // "Details" button
        binding.detailsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
            startActivity(intent);
        });

        // Bats Card → ListActivity with category = "bats"
        binding.cardBats.setOnClickListener(v -> openListActivity("bats"));

        // Balls Card → ListActivity with category = "balls"
        binding.cardBalls.setOnClickListener(v -> openListActivity("balls"));

        // Tables Card → ListActivity with category = "tables"
        binding.cardTables.setOnClickListener(v -> openListActivity("tables"));
    }

    private void openListActivity(String categoryID) {
        Intent intent = new Intent(this, ListActivity.class);
        intent.putExtra("categoryID", categoryID);
        startActivity(intent);
    }
}
