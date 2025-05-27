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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

        // ✅ Temporarily seed products when this screen is opened
        // this is for bats
        //        seedTestProducts(); // ← You can delete this after testing

        seedTestTables();

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

//    private void seedTestProducts() {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//        List<Map<String, Object>> products = Arrays.asList(
//                createProduct("Pro Carbon Bat", 89.99, "5-ply carbon blade with max spin and speed.", Arrays.asList("carbon", "offensive")),
//                createProduct("Spin Master 3000", 74.50, "Control-focused paddle for advanced players.", Arrays.asList("spin", "control")),
//                createProduct("Speed Demon", 59.99, "Ultra-light bat with high velocity.", Arrays.asList("lightweight", "speed")),
//                createProduct("Balance Pro", 64.95, "Evenly balanced bat for consistent gameplay.", Arrays.asList("balance")),
//                createProduct("Rookie Trainer", 29.99, "Perfect starter bat for beginners.", Arrays.asList("beginner")),
//                createProduct("Smash King", 79.49, "Built for power smashes and aggressive play.", Arrays.asList("power", "aggressive")),
//                createProduct("Feather Touch", 52.25, "Exceptional control and feel.", Arrays.asList("lightweight", "control")),
//                createProduct("All-Round Pro", 68.00, "Great for both offense and defense.", Arrays.asList("balanced")),
//                createProduct("Elite Carbon Blade", 99.99, "Professional-level carbon blade for serious players.", Arrays.asList("elite", "carbon")),
//                createProduct("Control Beast", 73.10, "Maximizes shot placement and accuracy.", Arrays.asList("control", "precision"))
//        );
//
//        for (Map<String, Object> product : products) {
//            db.collection("products")
//                    .add(product)
//                    .addOnSuccessListener(doc -> Log.d("Firestore", "Added: " + doc.getId()))
//                    .addOnFailureListener(e -> Log.e("Firestore", "Error", e));
//        }
//    }
//
//    private Map<String, Object> createProduct(String name, double price, String desc, List<String> tags) {
//        Map<String, Object> product = new HashMap<>();
//        product.put("name", name);
//        product.put("description", desc);
//        product.put("price", price);
//        product.put("imageUrls", Collections.singletonList(""));
//        product.put("tags", tags);
//        product.put("views", 0);
//        product.put("cartQuantity", 0);
//        product.put("categoryID", "bats");
//        product.put("isWishlisted", false);
//        return product;
//    }

    private void seedTestTables() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        List<Map<String, Object>> tables = Arrays.asList(
                createTable("Pro Tournament Table", 649.99, "ITTF-approved table with professional bounce.", Arrays.asList("professional", "ittf")),
                createTable("Foldable Practice Table", 299.50, "Space-saving design with easy fold mechanism.", Arrays.asList("foldable", "practice")),
                createTable("Outdoor All-Weather Table", 559.00, "Weatherproof table suitable for outdoor use.", Arrays.asList("outdoor", "weatherproof")),
                createTable("Compact Home Table", 199.99, "Perfect for casual play at home.", Arrays.asList("home", "compact")),
                createTable("Club Champion", 439.75, "Sturdy table ideal for clubs and schools.", Arrays.asList("club", "durable")),
                createTable("Junior Play Table", 149.95, "Smaller table designed for kids.", Arrays.asList("junior", "kids")),
                createTable("Tournament Fold Pro", 599.00, "Professional-level table with foldable design.", Arrays.asList("foldable", "tournament")),
                createTable("Recreational Basic", 179.00, "Affordable table for entry-level fun.", Arrays.asList("recreational", "budget")),
                createTable("Rollaway Match Table", 489.30, "Easy-to-move table with lockable wheels.", Arrays.asList("portable", "match")),
                createTable("Heavy Duty Trainer", 525.45, "Durable table built for intense training.", Arrays.asList("training", "heavy-duty"))
        );

        for (Map<String, Object> table : tables) {
            db.collection("tables")
                    .add(table)
                    .addOnSuccessListener(doc -> Log.d("Firestore", "Table Added: " + doc.getId()))
                    .addOnFailureListener(e -> Log.e("Firestore", "Error adding table", e));
        }
    }

    private Map<String, Object> createTable(String name, double price, String description, List<String> tags) {
        Map<String, Object> table = new HashMap<>();
        table.put("name", name);
        table.put("price", price);
        table.put("description", description);
        table.put("tags", tags);
        return table;
    }


}
