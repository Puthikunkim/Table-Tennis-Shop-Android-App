package com.example.app.UI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.R;
import com.example.app.adapters.RecentSearchAdapter;
import com.example.app.adapters.SearchResultAdapter;
import com.example.app.Model.TableTennisProduct;
import com.example.app.databinding.ActivitySearchBinding;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.app.Data.FirestoreRepository;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
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
    private RecyclerView searchResultsRecyclerView;
    private SearchResultAdapter searchResultAdapter;
    private final List<TableTennisProduct> searchResults = new ArrayList<>();
    private SharedPreferences prefs;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final AtomicBoolean isProcessing = new AtomicBoolean(false);
    private Runnable pendingAction;

    private final List<TableTennisProduct> fullResults = new ArrayList<>();
    private final List<TableTennisProduct> filteredResults = new ArrayList<>();

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
            setupSearchResultsRecyclerView();
            setupClickListeners();
            loadRecentSearches();
            setupRootClickListener();
            // Hide recent searches initially
            binding.recentSearchesContainer.setVisibility(View.GONE);
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
        }

        binding.btnSort.setOnClickListener(v -> showSortMenu(v));
    }

    private void initializeViews() {
        searchEditText = binding.searchEditText;
        clearButton = binding.clearButton;
        recentSearchesRecyclerView = binding.recentSearchesRecyclerView;
        clearHistoryButton = binding.clearHistoryButton;
        searchResultsRecyclerView = binding.searchResultsRecyclerView;
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    private void setupRecyclerView() {
        if (recentSearchesRecyclerView != null) {
            recentSearchAdapter = new RecentSearchAdapter(this);
            recentSearchesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            recentSearchesRecyclerView.setAdapter(recentSearchAdapter);
        }
    }

    private void setupSearchResultsRecyclerView() {
        if (searchResultsRecyclerView != null) {
            searchResultAdapter = new SearchResultAdapter(this, filteredResults);

            searchResultAdapter.setOnProductClickListener(product -> {
                if (product.getId() != null) {
                    Intent intent = new Intent(SearchActivity.this, DetailsActivity.class);
                    intent.putExtra("productId", product.getId());
                    startActivity(intent);
                } else {
                    Toast.makeText(SearchActivity.this,
                            "Missing product ID",
                            Toast.LENGTH_SHORT).show();
                }
            });


            searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            searchResultsRecyclerView.setAdapter(searchResultAdapter);
        }
    }

    private void setupRootClickListener() {
        binding.getRoot().setOnClickListener(v -> {
            binding.recentSearchesContainer.setVisibility(View.GONE);
        });

        binding.searchEditText.setOnClickListener(v -> {
            binding.recentSearchesContainer.setVisibility(View.VISIBLE);
        });
    }

    private void setupClickListeners() {
        if (clearButton != null) {
            clearButton.setOnClickListener(v -> {
                if (searchEditText != null) {
                    searchEditText.setText("");
                    searchResults.clear();
                    if (searchResultAdapter != null) searchResultAdapter.notifyDataSetChanged();
                    searchResultsRecyclerView.setVisibility(View.GONE);
                    binding.recentSearchesContainer.setVisibility(View.VISIBLE);
                }
            });
        }

        if (clearHistoryButton != null) {
            clearHistoryButton.setOnClickListener(v -> debounce(() -> clearSearchHistory()));
        }

        if (searchEditText != null) {
            searchEditText.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    binding.recentSearchesContainer.setVisibility(View.VISIBLE);
                    searchResultsRecyclerView.setVisibility(View.GONE);
                    searchEditText.setEnabled(true);
                } else {
                    binding.recentSearchesContainer.setVisibility(View.GONE);
                    searchEditText.setEnabled(false);
                }
            });

            searchEditText.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    String query = searchEditText.getText().toString().trim();
                    if (!query.isEmpty()) {
                        debounce(() -> {
                            addToRecentSearches(query);
                            searchProducts(query);
                        });
                    }
                    binding.recentSearchesContainer.setVisibility(View.GONE);
                    return true;
                }
                return false;
            });

            // Add TextWatcher for live search
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    String query = s.toString().trim();
                    if (!query.isEmpty()) {
                        debounce(() -> searchProducts(query));
                    } else {
                        searchResults.clear();
                        if (searchResultAdapter != null) searchResultAdapter.notifyDataSetChanged();
                        searchResultsRecyclerView.setVisibility(View.GONE);
                        binding.recentSearchesContainer.setVisibility(View.VISIBLE);
                    }
                }
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

    private void searchProducts(String query) {
        FirestoreRepository.getInstance()
                .searchProducts(query, new FirestoreRepository.ProductsCallback() {
                    @Override
                    public void onSuccess(List<TableTennisProduct> products) {
                        fullResults.clear();
                        fullResults.addAll(products);
                        filteredResults.clear();
                        filteredResults.addAll(products); // default: no filter
                        searchResultAdapter.notifyDataSetChanged();

                        binding.sortFilterContainer.setVisibility(View.VISIBLE); // shows Sort/Filter row

                    }
                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Error searching products", e);
                    }
                });
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

    private void sortList(String field, boolean ascending) {
        Comparator<TableTennisProduct> comparator;

        if ("price".equals(field)) {
            comparator = Comparator.comparingDouble(TableTennisProduct::getPrice);
        } else if ("name".equals(field)) {
            comparator = Comparator.comparing(TableTennisProduct::getName, String.CASE_INSENSITIVE_ORDER);
        } else {
            return;
        }

        if (!ascending) {
            comparator = comparator.reversed();
        }

        filteredResults.sort(comparator);
        searchResultAdapter.notifyDataSetChanged();
    }

    private void showSortMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenuInflater().inflate(R.menu.menu_sort, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.sort_price_asc) sortList("price", true);
            else if (id == R.id.sort_price_desc) sortList("price", false);
            else if (id == R.id.sort_name_asc) sortList("name", true);
            else if (id == R.id.sort_name_desc) sortList("name", false);
            return true;
        });

        popup.show();
    }

}