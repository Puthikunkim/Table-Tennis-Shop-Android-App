package com.example.app.UI;

import com.example.app.R;

import android.os.Bundle;
import com.example.app.databinding.ActivityMainBinding;

public class MainActivity extends BaseActivity<ActivityMainBinding> {
    @Override
    protected ActivityMainBinding inflateBinding() {
        // inflate the binding
        return ActivityMainBinding.inflate(getLayoutInflater());
    }

    @Override
    protected int getSelectedMenuItemId() {
        return R.id.home;
    }
}
