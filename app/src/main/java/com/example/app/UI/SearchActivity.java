package com.example.app.UI;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.R;
import com.example.app.adapters.RecentSearchAdapter;
import com.example.app.databinding.ActivitySearchBinding;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class SearchActivity extends BaseActivity<ActivitySearchBinding> implements RecentSearchAdapter.OnSearchClickListener {
    private static final String TAG = "SearchActivity";
    private static final String PREFS_NAME = "SearchPrefs";
    private static final String RECENT_SEARCHES_KEY = "recentSearches";
    private static final int MAX_RECENT_SEARCHES = 10;
    private static final long DEBOUNCE_DELAY = 300;

    private EditText searchEditText;
    private ImageButton clearButton;
    private RecyclerView recentSearchesRecyclerView;
    private TextView clearHistoryButton;
    private RecentSearchAdapter recentSearchAdapter;
    private SharedPreferences prefs;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final AtomicBoolean isProcessing = new AtomicBoolean(false);
    private Runnable pendingAction;

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
        
        try {
            initializeViews();
            setupRecyclerView();
            setupClickListeners();
            loadRecentSearches();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
        }
    }

    private void initializeViews() {
        searchEditText = binding.searchEditText;
        clearButton = binding.clearButton;
        recentSearchesRecyclerView = binding.recentSearchesRecyclerView;
        clearHistoryButton = binding.clearHistoryButton;
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    private void setupRecyclerView() {
        if (recentSearchesRecyclerView != null) {
            recentSearchAdapter = new RecentSearchAdapter(this);
            recentSearchesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            recentSearchesRecyclerView.setAdapter(recentSearchAdapter);
        }
    }

    private void setupClickListeners() {
        if (clearButton != null) {
            clearButton.setOnClickListener(v -> {
                if (searchEditText != null) {
                    searchEditText.setText("");
                }
            });
        }

        if (clearHistoryButton != null) {
            clearHistoryButton.setOnClickListener(v -> debounce(() -> clearSearchHistory()));
        }

        if (searchEditText != null) {
            searchEditText.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    String query = searchEditText.getText().toString().trim();
                    if (!query.isEmpty()) {
                        debounce(() -> addToRecentSearches(query));
                    }
                    return true;
                }
                return false;
            });
        }
    }

    private void debounce(Runnable action) {
        if (isProcessing.get()) {
            return;
        }

        handler.removeCallbacks(pendingAction);
        pendingAction = () -> {
            try {
                isProcessing.set(true);
                action.run();
            } finally {
                isProcessing.set(false);
            }
        };
        handler.postDelayed(pendingAction, DEBOUNCE_DELAY);
    }

    private void loadRecentSearches() {
        try {
            if (prefs != null && recentSearchAdapter != null) {
                String json = prefs.getString(RECENT_SEARCHES_KEY, null);
                List<String> searches = new ArrayList<>();
                if (json != null) {
                    Type type = new TypeToken<List<String>>(){}.getType();
                    searches = new Gson().fromJson(json, type);
                    if (searches == null) {
                        searches = new ArrayList<>();
                    }
                }
                recentSearchAdapter.setSearches(searches);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading recent searches", e);
        }
    }

    private void saveRecentSearches(List<String> searches) {
        try {
            if (prefs != null && searches != null) {
                String json = new Gson().toJson(searches);
                prefs.edit().putString(RECENT_SEARCHES_KEY, json).apply();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving recent searches", e);
        }
    }

    private void addToRecentSearches(String search) {
        try {
            if (recentSearchAdapter != null && search != null) {
                recentSearchAdapter.addSearch(search);
                saveRecentSearches(recentSearchAdapter.getSearches());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error adding recent search", e);
        }
    }

    private void clearSearchHistory() {
        try {
            if (recentSearchAdapter != null) {
                recentSearchAdapter.setSearches(new ArrayList<>());
                saveRecentSearches(new ArrayList<>());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error clearing search history", e);
        }
    }

    @Override
    public void onSearchClick(String search) {
        try {
            if (searchEditText != null && search != null) {
                searchEditText.setText(search);
                searchEditText.setSelection(search.length());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling search click", e);
        }
    }

    @Override
    public void onSearchRemove(String search) {
        try {
            if (recentSearchAdapter != null) {
                debounce(() -> saveRecentSearches(recentSearchAdapter.getSearches()));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error removing search", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(pendingAction);
    }
}