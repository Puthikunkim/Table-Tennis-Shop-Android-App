package com.example.app.UI;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.app.Model.TableTennisProduct;
import com.example.app.R;
import com.example.app.adapters.TableTennisAdapter;
import com.example.app.databinding.ActivityListBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.app.Data.FirestoreRepository;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class ListActivity extends BaseActivity<ActivityListBinding> {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final List<TableTennisProduct> productList = new LinkedList<>();
    private TableTennisAdapter adapter;

    @Override
    protected ActivityListBinding inflateContentBinding() {
        return ActivityListBinding.inflate(getLayoutInflater());
    }

    @Override
    protected int getSelectedMenuItemId() {
        return R.id.home;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String rawCategory = getIntent().getStringExtra("categoryID");
        String category = rawCategory;
        if (category != null && category.length() > 0) {
            category = category.substring(0, 1).toUpperCase() + category.substring(1);
        }
        binding.customListTitle.setText(category);

        binding.customListBackButton.setOnClickListener(v -> finish());

        adapter = new TableTennisAdapter(this, R.layout.list_item_product, productList);
        ListView listView = binding.list;
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            TableTennisProduct selected = productList.get(position);
            if (selected.getId() != null) {
                Intent intent = new Intent(ListActivity.this, DetailsActivity.class);
                intent.putExtra("productId", selected.getId());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Missing product ID", Toast.LENGTH_SHORT).show();
            }
        });

        String categoryID = getIntent().getStringExtra("categoryID");
        if (categoryID != null) {
            fetchProductDataByCategory(categoryID);
        } else {
            Toast.makeText(this, "Category not specified", Toast.LENGTH_SHORT).show();
        }

        binding.btnSort.setOnClickListener(v -> showSortMenu(v));
    }

    private void fetchProductDataByCategory(String categoryID) {
        FirestoreRepository.getInstance()
                .getProductsByCategory(categoryID, new FirestoreRepository.ProductsCallback() {
                    @Override
                    public void onSuccess(List<TableTennisProduct> products) {
                        productList.clear();
                        productList.addAll(products);
                        adapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onError(Exception e) {
                        Log.e("Firestore", "Failed to load " + categoryID, e);
                        Toast.makeText(ListActivity.this, "Failed to load products", Toast.LENGTH_LONG).show();
                    }
                });

    }

    private void showSortMenu(android.view.View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenuInflater().inflate(R.menu.menu_sort, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.sort_price_asc) {
                sortList("price", true);
                return true;
            } else if (id == R.id.sort_price_desc) {
                sortList("price", false);
                return true;
            } else if (id == R.id.sort_name_asc) {
                sortList("name", true);
                return true;
            } else if (id == R.id.sort_name_desc) {
                sortList("name", false);
                return true;
            } else {
                return false;
            }
        });

        popup.show();
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

        productList.sort(comparator);
        adapter.notifyDataSetChanged();
    }


}
