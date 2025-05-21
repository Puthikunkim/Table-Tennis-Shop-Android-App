package com.example.app.UI;

import com.example.app.R;
import com.example.app.databinding.ActivityWishListBinding;

public class WishListActivity extends BaseActivity<ActivityWishListBinding> {
    @Override
    protected ActivityWishListBinding inflateContentBinding() {
        return ActivityWishListBinding.inflate(getLayoutInflater());
    }

    @Override
    protected int getSelectedMenuItemId() {
        return R.id.wish_list;
    }
}