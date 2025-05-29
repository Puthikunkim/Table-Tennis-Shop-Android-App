package com.example.app.UI;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.app.Model.TableTennisProduct;
import com.example.app.R;
import com.example.app.adapters.TopPicksAdapter;
import com.example.app.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

import com.example.app.Data.FirestoreRepository;

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

        // Category cards → ListActivity with selected category
        binding.cardBats.setOnClickListener(v -> openListActivity("bats"));
        binding.cardBalls.setOnClickListener(v -> openListActivity("balls"));
        binding.cardTables.setOnClickListener(v -> openListActivity("tables"));
    }

    private void openListActivity(String categoryID) {
        Intent intent = new Intent(this, ListActivity.class);
        intent.putExtra("categoryID", categoryID); // 🔥 pass category
        startActivity(intent);
    }

    private void setupTopPicks() {
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
                        Toast.makeText(MainActivity.this,
                                "Failed to load top picks", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
