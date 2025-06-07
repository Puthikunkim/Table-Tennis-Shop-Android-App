package com.example.app.UI;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.app.Model.TableTennisProduct;
import com.example.app.R;
import com.example.app.Adapters.RecommendationsAdapter;
import com.example.app.Util.AnimationUtils;
import com.example.app.Util.ErrorHandler;
import com.example.app.Util.NavigationUtils;
import com.example.app.databinding.ActivityMainBinding;
import com.example.app.Data.FirestoreRepository;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity<ActivityMainBinding> {

    private static final String TAG = "MainActivity";

    private RecommendationsAdapter topAdapter;
    private final List<TableTennisProduct> topList = new ArrayList<>();

    // 🔒 Cache featured product per app session
    private static TableTennisProduct cachedFeaturedProduct = null;

    @Override
    protected ActivityMainBinding inflateContentBinding() {
        return ActivityMainBinding.inflate(getLayoutInflater());
    }

    @Override
    protected int getSelectedMenuItemId() {
        return R.id.home;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupCategoryCards();
        setupTopPicks();
        loadTopPicks();
        loadFeaturedProduct();
        setupSearchBar();
        setupCategoryCardAnimations();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTopPicks();
    }

    private void setupCategoryCards() {
        binding.cardBats.setOnClickListener(v -> openListActivity("bats"));
        binding.cardBalls.setOnClickListener(v -> openListActivity("balls"));
        binding.cardTables.setOnClickListener(v -> openListActivity("tables"));
    }

    private void openListActivity(String categoryID) {
        NavigationUtils.navigateToActivity(
            this,
            ListActivity.class,
            "categoryID",
            categoryID
        );
    }

    private void setupTopPicks() {
        LinearLayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        binding.topPicksRecyclerView.setLayoutManager(lm);

        topAdapter = new RecommendationsAdapter(this, topList);
        binding.topPicksRecyclerView.setAdapter(topAdapter);

        topAdapter.setOnProductClickListener(product -> {
            String clickedId = product.getId();
            Log.d(TAG, "▶ TopPick tapped; product.getId() = [" + clickedId + "]");

            if (clickedId == null || clickedId.isEmpty()) {
                ErrorHandler.handleMissingDataError(this, "Product ID");
                return;
            }

            NavigationUtils.navigateToActivity(
                MainActivity.this,
                DetailsActivity.class,
                "productId",
                clickedId
            );
        });
    }

    private void loadTopPicks() {
        FirestoreRepository.getInstance()
                .getTopViewedProducts(10, new FirestoreRepository.ProductsCallback() {
                    @Override
                    public void onSuccess(List<TableTennisProduct> products) {
                        topList.clear();
                        topList.addAll(products);
                        topAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Exception e) {
                        ErrorHandler.handleFirestoreError(MainActivity.this, "load top picks", e);
                    }
                });
    }

    private void loadFeaturedProduct() {
        if (cachedFeaturedProduct != null) {
            updateFeaturedUI(cachedFeaturedProduct);
            return;
        }

        FirestoreRepository.getInstance().getRandomProduct(new FirestoreRepository.ProductDetailCallback() {
            @Override
            public void onSuccess(TableTennisProduct product) {
                cachedFeaturedProduct = product;
                updateFeaturedUI(product);
            }

            @Override
            public void onError(Exception e) {
                ErrorHandler.handleFirestoreError(MainActivity.this, "load featured product", e);
            }
        });
    }

    private void updateFeaturedUI(TableTennisProduct featured) {
        binding.featuredTitle.setText(featured.getName());
        binding.featuredSubtitle.setText("The ultimate table tennis experience.");
        binding.featuredDescription.setText(featured.getDescription());

        binding.btnShopNow.setOnClickListener(v -> 
            NavigationUtils.navigateToActivity(
                this,
                DetailsActivity.class,
                "productId",
                featured.getId()
            )
        );

        binding.btnViewAll.setOnClickListener(v -> 
            NavigationUtils.navigateToActivity(
                this,
                ListActivity.class,
                "categoryID",
                featured.getCategoryID()
            )
        );
    }

    private void setupSearchBar() {
        EditText searchEditText = findViewById(R.id.searchEditText);
        ImageView searchIcon = findViewById(R.id.searchIcon);

        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                performSearchFromEditText(searchEditText);
                return true;
            }
            return false;
        });

        searchIcon.setOnClickListener(v -> performSearchFromEditText(searchEditText));
    }

    private void performSearchFromEditText(EditText searchEditText) {
        String query = searchEditText.getText().toString().trim();
        if (!query.isEmpty()) {
            NavigationUtils.navigateToActivity(
                MainActivity.this,
                SearchActivity.class,
                "searchQuery",
                query
            );
        }
    }

    private void setupCategoryCardAnimations() {
        setupCardClickAnimation(findViewById(R.id.cardBats), () -> openListActivity("bats"));
        setupCardClickAnimation(findViewById(R.id.cardBalls), () -> openListActivity("balls"));
        setupCardClickAnimation(findViewById(R.id.cardTables), () -> openListActivity("tables"));
    }

    private void setupCardClickAnimation(View cardView, Runnable onClickAction) {
        cardView.setOnClickListener(v -> 
            AnimationUtils.animateButton(v, onClickAction)
        );
    }
}
