// MainActivity.java
package com.example.app.UI;

import com.example.app.R;

public class MainActivity extends BaseActivity {
    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_main;
    }

    @Override
    protected int getSelectedMenuItemId() {
        return R.id.home;
    }
}
