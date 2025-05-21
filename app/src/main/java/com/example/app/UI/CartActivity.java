package com.example.app.UI;

import com.example.app.R;

public class CartActivity extends BaseActivity {
    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_cart;
    }

    @Override
    protected int getSelectedMenuItemId() {
        return R.id.cart;
    }
}
