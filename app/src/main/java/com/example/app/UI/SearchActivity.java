package com.example.app.UI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.R;
import com.example.app.Adapters.RecentSearchAdapter;
import com.example.app.Adapters.ProductAdapter;
import com.example.app.Model.TableTennisProduct;
import com.example.app.databinding.ActivitySearchBinding;
import com.example.app.Data.FirestoreRepository;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class SearchActivity extends BaseActivity<ActivitySearchBinding>
        implements RecentSearchAdapter.OnSearchClickListener {

    private static final String TAG = "SearchActivity";
    private static final String PREFS_NAME = "SearchPrefs";
    private static final String RECENT_SEARCHES_KEY = "recentSearches";
    private static final long DEBOUNCE_DELAY = 300;

    private EditText searchEditText;
    private ImageButton clearButton;
    private RecyclerView recentSearchesRecyclerView;
    private TextView clearHistoryButton;
    private RecentSearchAdapter recentSearchAdapter;

    private RecyclerView searchResultsRecyclerView;
    private ProductAdapter searchResultAdapter;

    private SharedPreferences prefs;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final AtomicBoolean isProcessing = new AtomicBoolean(false);
    private Runnable pendingAction;

    private final List<TableTennisProduct> fullResults = new ArrayList<>();
    private final List<TableTennisProduct> filteredResults = new ArrayList<>();

    private String selectedCategory = "all";
    private String sortField = "name";
    private boolean sortAscending = true;

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
            bindViews();
            setupRecentSearchesList();
            setupSearchResultsList();
            setupClickListeners();
            loadRecentSearches();
            hideRecentSearchesContainer();
        } catch (Exception e) {
            // In case of binding glitches, log but prevent crash
            android.util.Log.e(TAG, "Error in onCreate", e);
        }

        // Sort & Filter buttons
        binding.btnSort.setOnClickListener(this::showSortMenu);
        binding.btnFilter.setOnClickListener(this::showFilterMenu);

        String initialQuery = getIntent().getStringExtra("searchQuery");
        boolean shouldFocus = getIntent().getBooleanExtra("auto_focus", false);
        if (initialQuery != null && !initialQuery.trim().isEmpty()) {
            performSearch(initialQuery);
        } else if (shouldFocus) {
            focusSearchField();
        }
    }

    private void bindViews() {
        searchEditText = binding.searchEditText;
        clearButton = binding.clearButton;
        recentSearchesRecyclerView = binding.recentSearchesRecyclerView;
        clearHistoryButton = binding.clearHistoryButton;
        searchResultsRecyclerView = binding.searchResultsRecyclerView;
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    private void setupRecentSearchesList() {
        recentSearchAdapter = new RecentSearchAdapter(this);
        recentSearchesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recentSearchesRecyclerView.setAdapter(recentSearchAdapter);
    }

    private void setupSearchResultsList() {
        searchResultAdapter = new ProductAdapter(this, filteredResults);
        searchResultAdapter.setOnProductClickListener(product -> {
            String productId = product.getId();
            if (productId != null) {
                Intent intent = new Intent(SearchActivity.this, DetailsActivity.class);
                intent.putExtra("productId", productId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Missing product ID", Toast.LENGTH_SHORT).show();
            }
        });

        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchResultsRecyclerView.setAdapter(searchResultAdapter);
    }

    private void setupClickListeners() {
        // Clear text button
        clearButton.setOnClickListener(v -> {
            searchEditText.setText("");
            filteredResults.clear();
            searchResultAdapter.notifyDataSetChanged();
            hideSearchResults();
            showRecentSearchesContainer();
        });

        // Clear history button (debounced)
        clearHistoryButton.setOnClickListener(v -> debounce(this::clearSearchHistory));

        // Tapping outside search field hides recent-searches
        binding.getRoot().setOnClickListener(v -> hideRecentSearchesContainer());
        searchEditText.setOnClickListener(v -> showRecentSearchesContainer());

        // "Search" action on keyboard
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            boolean isSearchAction = actionId == EditorInfo.IME_ACTION_SEARCH
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            if (isSearchAction) {
                String queryText = searchEditText.getText().toString().trim();
                if (!queryText.isEmpty()) {
                    debounce(() -> {
                        addToRecentSearches(queryText);
                        searchProducts(queryText);
                    });
                }
                hideRecentSearchesContainer();
                return true;
            }
            return false;
        });

        // Live-search as user types (debounced)
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                String queryText = s.toString().trim();
                if (!queryText.isEmpty()) {
                    debounce(() -> searchProducts(queryText));
                } else {
                    filteredResults.clear();
                    searchResultAdapter.notifyDataSetChanged();
                    hideSearchResults();
                    showRecentSearchesContainer();
                }
            }
        });
    }

    private void debounce(Runnable action) {
        if (isProcessing.get()) return;
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
        String json = prefs.getString(RECENT_SEARCHES_KEY, null);
        List<String> searches = new ArrayList<>();
        if (json != null) {
            Type type = new TypeToken<List<String>>() {}.getType();
            List<String> saved = new Gson().fromJson(json, type);
            if (saved != null) {
                searches = saved;
            }
        }
        recentSearchAdapter.setSearches(searches);
    }

    private void saveRecentSearches(List<String> searches) {
        String json = new Gson().toJson(searches);
        prefs.edit().putString(RECENT_SEARCHES_KEY, json).apply();
    }

    private void addToRecentSearches(String query) {
        recentSearchAdapter.addSearch(query);
        saveRecentSearches(recentSearchAdapter.getSearches());
    }

    private void clearSearchHistory() {
        recentSearchAdapter.setSearches(new ArrayList<>());
        saveRecentSearches(new ArrayList<>());
    }

    private void searchProducts(String query) {
        FirestoreRepository.getInstance().searchProducts(query, new FirestoreRepository.ProductsCallback() {
            @Override
            public void onSuccess(List<TableTennisProduct> products) {
                fullResults.clear();
                fullResults.addAll(products);
                applyFilterAndSort();
                binding.sortFilterContainer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(Exception e) {
                android.util.Log.e(TAG, "Error searching products", e);
            }
        });
    }

    private void applyFilterAndSort() {
        if (fullResults.isEmpty()) {
            hideSearchResults();
            return;
        }

        filteredResults.clear();
        for (TableTennisProduct product : fullResults) {
            String cat = product.getCategoryID();
            if ("all".equals(selectedCategory) ||
                    (cat != null && cat.equalsIgnoreCase(selectedCategory))) {
                filteredResults.add(product);
            }
        }

        Comparator<TableTennisProduct> comparator = ("price".equals(sortField))
                ? Comparator.comparingDouble(TableTennisProduct::getPrice)
                : Comparator.comparing(TableTennisProduct::getName, String.CASE_INSENSITIVE_ORDER);

        if (!sortAscending) {
            comparator = comparator.reversed();
        }

        filteredResults.sort(comparator);
        searchResultAdapter.notifyDataSetChanged();
        showSearchResults();
    }

    private void sortList(String field, boolean ascending) {
        sortField = field;
        sortAscending = ascending;
        if (!fullResults.isEmpty()) {
            applyFilterAndSort();
        }
    }

    private void showSortMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenuInflater().inflate(R.menu.menu_sort, popup.getMenu());

        // Use if/else instead of switch to avoid "constant expression required"
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.sort_price_asc) {
                    sortList("price", true);
                } else if (id == R.id.sort_price_desc) {
                    sortList("price", false);
                } else if (id == R.id.sort_name_asc) {
                    sortList("name", true);
                } else if (id == R.id.sort_name_desc) {
                    sortList("name", false);
                }
                return true;
            }
        });

        popup.show();
    }

    private void showFilterMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenuInflater().inflate(R.menu.menu_filter, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.filter_all) {
                    selectedCategory = "all";
                } else if (id == R.id.filter_bats) {
                    selectedCategory = "bats";
                } else if (id == R.id.filter_balls) {
                    selectedCategory = "balls";
                } else if (id == R.id.filter_tables) {
                    selectedCategory = "tables";
                }
                applyFilterAndSort();
                return true;
            }
        });

        popup.show();
    }

    private void performSearch(String query) {
        if (query == null || query.trim().isEmpty()) return;
        searchEditText.setText(query);
        searchEditText.setSelection(query.length());
        addToRecentSearches(query);
        searchProducts(query);
        hideRecentSearchesContainer();
    }

    private void focusSearchField() {
        searchEditText.requestFocus();
        showRecentSearchesContainer();
        searchEditText.post(() -> {
            InputMethodManager imm =
                    (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
            }
        });
    }

    private void showRecentSearchesContainer() {
        binding.recentSearchesContainer.setVisibility(View.VISIBLE);
    }

    private void hideRecentSearchesContainer() {
        binding.recentSearchesContainer.setVisibility(View.GONE);
    }

    private void showSearchResults() {
        searchResultsRecyclerView.setVisibility(View.VISIBLE);
    }

    private void hideSearchResults() {
        searchResultsRecyclerView.setVisibility(View.GONE);
    }

    @Override
    public void onSearchClick(String search) {
        performSearch(search);
    }

    @Override
    public void onSearchRemove(String search) {
        debounce(() -> saveRecentSearches(recentSearchAdapter.getSearches()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(pendingAction);
    }
}
