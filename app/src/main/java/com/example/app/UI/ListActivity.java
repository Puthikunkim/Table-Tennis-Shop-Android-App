package com.example.app.UI;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.example.app.Model.TableTennisProduct;
import com.example.app.R;
import com.example.app.adaptors.TableTennisAdapter;
import com.example.app.databinding.ActivityListBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

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

        // Init adapter and connect to list
        adapter = new TableTennisAdapter(this, R.layout.list_item_product, productList);
        ListView listView = binding.list;
        listView.setAdapter(adapter);

        // Fetch Firestore data
        fetchProductData();
    }

    private void fetchProductData() {
        db.collection("products").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot results = task.getResult();
                List<TableTennisProduct> fetched = results.toObjects(TableTennisProduct.class);
                if (fetched.size() > 0) {
                    Log.d("Firestore", "Products loaded: " + fetched.size());
                    productList.clear();
                    productList.addAll(fetched);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(this, "No products found", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Failed to load products", Toast.LENGTH_LONG).show();
                Log.e("Firestore", "Error loading products", task.getException());
            }
        });
    }
}
