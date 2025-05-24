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

        // To test details activity
        binding.detailsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
            startActivity(intent);
        });
    }
}