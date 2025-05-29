package com.example.app.UI;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.example.app.Model.TableTennisProduct;
import com.example.app.R;
import com.example.app.adaptors.TableTennisAdapter;
import com.example.app.databinding.ActivityListBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.app.Data.FirestoreRepository;

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
}
