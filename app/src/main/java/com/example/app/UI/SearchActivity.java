package com.example.app.UI;

import com.example.app.R;

import com.example.app.databinding.ActivitySearchBinding;

public class SearchActivity extends BaseActivity<ActivitySearchBinding> {
    @Override
    protected ActivitySearchBinding inflateBinding() {
        return ActivitySearchBinding.inflate(getLayoutInflater());
    }

    @Override
    protected int getSelectedMenuItemId() {
        return R.id.search;
    }
}
