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

        // Category cards → ListActivity with selected category
        binding.cardBats.setOnClickListener(v -> openListActivity("Bats"));
        binding.cardBalls.setOnClickListener(v -> openListActivity("Balls"));
        binding.cardTables.setOnClickListener(v -> openListActivity("Tables"));
    }

    private void openListActivity(String categoryID) {
        Intent intent = new Intent(this, ListActivity.class);
        intent.putExtra("categoryID", categoryID); // 🔥 pass category
        startActivity(intent);
    }
}
