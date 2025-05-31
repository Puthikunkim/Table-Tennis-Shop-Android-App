package com.example.app.UI;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;  // ← add this

import com.example.app.Model.TableTennisProduct;
import com.example.app.R;
import com.example.app.adapters.TopPicksAdapter;
import com.example.app.databinding.ActivityMainBinding;
import com.example.app.Data.FirestoreRepository;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity<ActivityMainBinding> {

    private TopPicksAdapter topAdapter;
    private final List<TableTennisProduct> topList = new ArrayList<>();

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

        // 3) Load data from Firestore
        loadTopPicks();

        // 4) Show featured product
        loadFeaturedProduct();
    }

    private void openListActivity(String categoryID) {
        Intent intent = new Intent(this, ListActivity.class);
        intent.putExtra("categoryID", categoryID);
        startActivity(intent);
    }

    private void setupTopPicks() {
        // give it a horizontal layout manager
        LinearLayoutManager lm = new LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL,
                false
        );
        binding.topPicksRecyclerView.setLayoutManager(lm);

        topAdapter = new TopPicksAdapter(this, topList);
        binding.topPicksRecyclerView.setAdapter(topAdapter);

        topAdapter.setOnProductClickListener(product -> {
            Intent intent = new Intent(this, DetailsActivity.class);
            intent.putExtra("productId", product.getId());
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
                        Toast.makeText(
                                MainActivity.this,
                                "Failed to load top picks",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void loadFeaturedProduct() {
        FirestoreRepository.getInstance().getRandomProduct(new FirestoreRepository.ProductDetailCallback() {
            @Override
            public void onSuccess(TableTennisProduct featured) {
                binding.featuredTitle.setText(featured.getName());
                binding.featuredSubtitle.setText("The ultimate table tennis experience.");
                binding.featuredDescription.setText(featured.getDescription());

                binding.btnShopNow.setOnClickListener(v -> {
                    Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                    intent.putExtra("productId", featured.getId());
                    startActivity(intent);
                });

                binding.btnViewAll.setOnClickListener(v -> {
                    Intent intent = new Intent(MainActivity.this, ListActivity.class);
                    intent.putExtra("categoryID", featured.getCategoryID()); // Adjust field name if needed
                    startActivity(intent);
                });
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(MainActivity.this, "Failed to load featured product", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
