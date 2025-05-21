package com.example.app.UI;

import com.example.app.R;

import com.example.app.databinding.ActivityProfileBinding;

public class ProfileActivity extends BaseActivity<ActivityProfileBinding> {
    @Override
    protected ActivityProfileBinding inflateBinding() {
        return ActivityProfileBinding.inflate(getLayoutInflater());
    }

    @Override
    protected int getSelectedMenuItemId() {
        return R.id.profile;
    }
}
