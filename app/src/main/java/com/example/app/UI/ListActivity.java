package com.example.app.UI;

import android.os.Bundle;

import com.example.app.R;
import com.example.app.UI.BaseActivity;
import com.example.app.databinding.ActivityListBinding;

public class ListActivity extends BaseActivity<ActivityListBinding> {

    @Override
    protected ActivityListBinding inflateContentBinding() {
        return ActivityListBinding.inflate(getLayoutInflater());
    }

    @Override
    protected int getSelectedMenuItemId() {
        return R.id.home; // Or whatever ID makes sense

    }
}
