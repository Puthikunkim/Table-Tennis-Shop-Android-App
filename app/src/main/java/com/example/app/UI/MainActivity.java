package com.example.app.UI;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.app.Model.TableTennisProduct;
import com.example.app.R;
import com.example.app.adapters.RecommendationsAdapter;
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

        // 1) Category cards → ListActivity
        binding.cardBats.setOnClickListener(v -> openListActivity("bats"));
        binding.cardBalls.setOnClickListener(v -> openListActivity("balls"));
        binding.cardTables.setOnClickListener(v -> openListActivity("tables"));

        // 2) Setup Top Picks RecyclerView
        setupTopPicks();

        // 3) Load top picks from Firestore
        loadTopPicks();

        // 4) Load (or reuse) featured product
        loadFeaturedProduct();

        setupSearchBar();

        // Animations
        setupCategoryCardAnimations();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTopPicks(); // re‐query Firestore so the “views” have bumped
    }

    private void openListActivity(String categoryID) {
        Intent intent = new Intent(this, ListActivity.class);
        intent.putExtra("categoryID", categoryID);
        startActivity(intent);
    }

    private void setupTopPicks() {
        LinearLayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        binding.topPicksRecyclerView.setLayoutManager(lm);

        topAdapter = new RecommendationsAdapter(this, topList);
        binding.topPicksRecyclerView.setAdapter(topAdapter);

        topAdapter.setOnProductClickListener(product -> {
            String clickedId = product.getId();
            Log.d("MainActivity", "▶ TopPick tapped; product.getId() = [" + clickedId + "]");
            Toast.makeText(this, "Tapped TopPick with ID = " + clickedId, Toast.LENGTH_SHORT).show();

            if (clickedId == null || clickedId.isEmpty()) {
                Toast.makeText(
                        MainActivity.this,
                        "⚠️ clickedId was null/empty—cannot open Details.",
                        Toast.LENGTH_LONG
                ).show();
                return;
            }


            Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
            intent.putExtra("productId", clickedId);
            startActivity(intent);
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
                        Toast.makeText(MainActivity.this, "Failed to load top picks", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(MainActivity.this, "Failed to load featured product", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateFeaturedUI(TableTennisProduct featured) {
        binding.featuredTitle.setText(featured.getName());
        binding.featuredSubtitle.setText("The ultimate table tennis experience.");
        binding.featuredDescription.setText(featured.getDescription());

        binding.btnShopNow.setOnClickListener(v -> {
            Intent intent = new Intent(this, DetailsActivity.class);
            intent.putExtra("productId", featured.getId());
            startActivity(intent);
        });

        binding.btnViewAll.setOnClickListener(v -> {
            Intent intent = new Intent(this, ListActivity.class);
            intent.putExtra("categoryID", featured.getCategoryID());
            startActivity(intent);
        });
    }

    private void setupSearchBar() {
        EditText searchEditText = findViewById(R.id.searchEditText);

        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {

                String query = searchEditText.getText().toString().trim();
                if (!query.isEmpty()) {
                    Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                    intent.putExtra("searchQuery", query);
                    startActivity(intent);
                }
                return true;
            }
            return false;
        });

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
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            intent.putExtra("searchQuery", query);
            startActivity(intent);
        }
    }

    private void setupCategoryCardAnimations() {
        setupCardClickAnimation(findViewById(R.id.cardBats), () -> openListActivity("bats"));
        setupCardClickAnimation(findViewById(R.id.cardBalls), () -> openListActivity("balls"));
        setupCardClickAnimation(findViewById(R.id.cardTables), () -> openListActivity("tables"));
    }
    private void setupCardClickAnimation(View cardView, Runnable onClickAction) {
        cardView.setOnClickListener(v -> {
            v.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                        onClickAction.run();
                    }).start();
        });
    }

}
