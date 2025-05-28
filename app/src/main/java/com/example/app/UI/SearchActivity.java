package com.example.app.UI;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.app.R;
import com.example.app.databinding.ActivitySearchBinding;

public class SearchActivity extends BaseActivity<ActivitySearchBinding> {
    private EditText searchEditText;
    private ImageButton clearButton;

    @Override
    protected ActivitySearchBinding inflateContentBinding() {
        return ActivitySearchBinding.inflate(getLayoutInflater());
    }

    @Override
    protected int getSelectedMenuItemId() {
        return R.id.search;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize views
        searchEditText = binding.searchEditText;
        clearButton = binding.clearButton;

        // Set up clear button click listener
        clearButton.setOnClickListener(v -> {
            searchEditText.setText("");
        });
    }
}