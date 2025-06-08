package com.example.app.UI;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.app.Model.TableTennisProduct;
import com.example.app.R;
import com.example.app.Adapters.RecommendationsAdapter;
import com.example.app.Util.AnimationUtils;
import com.example.app.Util.ErrorHandler;
import com.example.app.Util.NavigationUtils;
import com.example.app.Util.ToastUtils;
import com.example.app.databinding.ActivityMainBinding;
import com.example.app.Data.FirestoreRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Homepage of the app.
 * Shows top categories, featured product, top picks, and allows search.
 */
public class MainActivity extends BaseActivity<ActivityMainBinding> {

    private static final String TAG = "MainActivity";

    private RecommendationsAdapter topAdapter;
    private final List<TableTennisProduct> topList = new ArrayList<>();

    // Keeps the featured product cached to avoid reloading on every resume
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

        setupCategoryCards(); // Category buttons (bats, balls, tables)
        setupTopPicks(); // Horizontal top picks section
        loadTopPicks();  // Pull top picks from Firestore
        loadFeaturedProduct(); // Pull random featured product
        setupSearchBar(); // Wire up search input + icon
        setupCategoryCardAnimations(); // Add click animation for category cards
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTopPicks(); // Refresh top picks on return
    }

    /** Handles click events for each category card. */
    private void setupCategoryCards() {
        binding.cardBats.setOnClickListener(v -> openListActivity("bats"));
        binding.cardBalls.setOnClickListener(v -> openListActivity("balls"));
        binding.cardTables.setOnClickListener(v -> openListActivity("tables"));
    }

    /** Navigates to ListActivity with the selected category ID. */
    private void openListActivity(String categoryID) {
        if (categoryID == null || categoryID.isEmpty()) {
            ToastUtils.showCustomToast(this, "Invalid category selected.");
            return;
        }
        NavigationUtils.navigateToActivity(
                this,
                ListActivity.class,
                "categoryID",
                categoryID
        );
    }

    /** Initializes horizontal RecyclerView for top picks. */
    private void setupTopPicks() {
        LinearLayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        binding.topPicksRecyclerView.setLayoutManager(lm);

        topAdapter = new RecommendationsAdapter(this, topList);
        binding.topPicksRecyclerView.setAdapter(topAdapter);

        // On item click, open details screen
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

    /** Loads the top 10 most-viewed products from Firestore. */
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
                        ToastUtils.showCustomToast(MainActivity.this, "Failed to load top picks: " + e.getMessage());
                        Log.e(TAG, "Error loading top picks", e);
                    }
                });
    }

    /** Loads and displays a random featured product. */
    private void loadFeaturedProduct() {
        if (cachedFeaturedProduct != null) {
            updateFeaturedUI(cachedFeaturedProduct);
            return;
        }

        FirestoreRepository.getInstance().getRandomProduct(new FirestoreRepository.ProductDetailCallback() {
            @Override
            public void onSuccess(TableTennisProduct product) {
                if (product == null) {
                    ToastUtils.showCustomToast(MainActivity.this, "No featured products available");
                    return;
                }
                cachedFeaturedProduct = product;
                updateFeaturedUI(product);
            }

            @Override
            public void onError(Exception e) {
                ToastUtils.showCustomToast(MainActivity.this, "Failed to load featured product: " + e.getMessage());
                Log.e(TAG, "Error loading featured product", e);
            }
        });
    }

    /** Updates the featured product UI with the given product. */
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

    /** Hooks up the search bar + icon so both trigger navigation to SearchActivity. */
    private void setupSearchBar() {
        EditText searchEditText = findViewById(R.id.searchEditText);
        ImageView searchIcon = findViewById(R.id.searchIcon);

        // Trigger search when user hits Enter or Search on keyboard
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                performSearchFromEditText(searchEditText);
                return true;
            }
            return false;
        });

        // Trigger search when search icon is tapped
        searchIcon.setOnClickListener(v -> performSearchFromEditText(searchEditText));
    }

    /** Reads the search text and navigates to the Search screen. */
    private void performSearchFromEditText(EditText searchEditText) {
        String query = searchEditText.getText().toString().trim();
        if (query.isEmpty()) {
            ToastUtils.showCustomToast(this, "Please enter a search term.");
            return;
        }
        NavigationUtils.navigateToActivity(
                MainActivity.this,
                SearchActivity.class,
                "searchQuery",
                query
        );
    }

    /** Sets up click animations for category cards (bats, balls, tables). */
    private void setupCategoryCardAnimations() {
        setupCardClickAnimation(findViewById(R.id.cardBats), () -> openListActivity("bats"));
        setupCardClickAnimation(findViewById(R.id.cardBalls), () -> openListActivity("balls"));
        setupCardClickAnimation(findViewById(R.id.cardTables), () -> openListActivity("tables"));
    }

    /** Adds scale animation on tap, then runs the given action. */
    private void setupCardClickAnimation(View cardView, Runnable onClickAction) {
        cardView.setOnClickListener(v ->
                AnimationUtils.animateButton(v, onClickAction)
        );
    }
}
