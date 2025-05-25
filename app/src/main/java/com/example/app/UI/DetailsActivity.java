package com.example.app.UI;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;


import com.example.app.R;
import com.example.app.databinding.ActivityDetailsBinding;

public class DetailsActivity extends BaseActivity<ActivityDetailsBinding> {
    @Override
    protected ActivityDetailsBinding inflateContentBinding() {
        return ActivityDetailsBinding.inflate(getLayoutInflater());
    }

    @Override
    protected int getSelectedMenuItemId() {
        return R.id.home;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // binding is inherited from BaseActivity and initialised there
        binding.customDetailsBackButton.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }
}