package com.example.app.UI;

import com.example.app.R;

import com.example.app.databinding.ActivityCartBinding;

public class CartActivity extends BaseActivity<ActivityCartBinding> {
    @Override
    protected ActivityCartBinding inflateBinding() {
        return ActivityCartBinding.inflate(getLayoutInflater());
    }

    @Override
    protected int getSelectedMenuItemId() {
        return R.id.cart;
    }
}
