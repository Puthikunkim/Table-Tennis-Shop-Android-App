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

        // Category cards → ListActivity with selected category
        binding.cardBats.setOnClickListener(v -> openListActivity("bats"));
        binding.cardBalls.setOnClickListener(v -> openListActivity("balls"));
        binding.cardTables.setOnClickListener(v -> openListActivity("tables"));
    }

    private void openListActivity(String categoryID) {
        Intent intent = new Intent(this, ListActivity.class);
        intent.putExtra("categoryID", categoryID); // 🔥 pass category
        startActivity(intent);
    }
}
