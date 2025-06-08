    package com.example.app.UI;

    import android.content.Intent;
    import android.os.Bundle;
    import android.text.TextUtils;
    import android.util.Log;
    import android.view.View;
    import android.widget.PopupMenu;
    import android.widget.Toast;
    import android.view.Gravity;
    import android.view.LayoutInflater;
    import android.widget.TextView;

    import androidx.recyclerview.widget.LinearLayoutManager;

    import com.example.app.Data.FirestoreRepository;
    import com.example.app.Model.TableTennisProduct;
    import com.example.app.R;
    import com.example.app.Adapters.ProductAdapter;
    import com.example.app.databinding.ActivityListBinding;
    import com.example.app.Util.ErrorHandler;
    import com.example.app.Util.NavigationUtils;

    import java.util.ArrayList;
    import java.util.Comparator;
    import java.util.List;

    /**
     * Displays a list of products based on the selected category.
     * Supports sorting (price/name) and navigating to product details.
     */
    public class ListActivity extends BaseActivity<ActivityListBinding> {
        private static final String TAG = "ListActivity";

        private final List<TableTennisProduct> productList = new ArrayList<>();
        private ProductAdapter adapter;

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

            // If category is missing, we cancel and show a toast
            if (!setupTitleAndBackButton()) return;

            setupAdapter();
            loadProducts();

            binding.btnSort.setOnClickListener(this::showSortMenu);
        }

        /**
         * Gets category from intent and sets it as the page title.
         * Also wires up the back button.
         * Returns false if category is missing, in which case we finish the screen early.
         */
        private boolean setupTitleAndBackButton() {
            String rawCategory = getIntent().getStringExtra("categoryID");
            if (TextUtils.isEmpty(rawCategory)) {
                ErrorHandler.handleMissingDataError(this, "Category");
                finish();
                return false;
            }

            String displayName = rawCategory.substring(0, 1).toUpperCase() + rawCategory.substring(1);
            binding.customListTitle.setText(displayName);
            binding.customListBackButton.setOnClickListener(v -> {
                finish();
                NavigationUtils.slideOutOnBack(this);
            });

            return true;
        }

        /**
         * Sets up RecyclerView and adapter, plus click listener to open product details.
         */
        private void setupAdapter() {
            adapter = new ProductAdapter(this, productList);
            adapter.setOnProductClickListener(product -> {
                String productId = product.getId();
                if (TextUtils.isEmpty(productId)) {
                    ErrorHandler.handleMissingDataError(this, "Product ID");
                } else {
                    NavigationUtils.navigateToActivity(
                            ListActivity.this,
                            DetailsActivity.class,
                            "productId",
                            productId
                    );
                }
            });

            binding.list.setLayoutManager(new LinearLayoutManager(this));
            binding.list.setAdapter(adapter);
        }

        /**
         * Loads all products for the given category from Firestore.
         * Updates the adapter once data is loaded.
         */
        private void loadProducts() {
            String categoryID = getIntent().getStringExtra("categoryID");
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
                            ErrorHandler.handleFirestoreError(ListActivity.this, "load products", e);
                        }
                    });
        }

        /**
         * Shows a popup menu with sorting options (price, name).
         * Uses the selected menu item to sort the list accordingly.
         */
        private void showSortMenu(View anchor) {
            PopupMenu popup = new PopupMenu(this, anchor);
            popup.getMenuInflater().inflate(R.menu.menu_sort, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                SortOption option = SortOption.fromMenuId(item.getItemId());
                if (option != null) {
                    productList.sort(option.comparator);
                    adapter.notifyDataSetChanged();
                    return true;
                }
                return false;
            });

            popup.show();
        }

        private void showCustomToast(String message) {
            View layout = getLayoutInflater().inflate(R.layout.custom_toast, null);

            TextView text = layout.findViewById(R.id.toast_text);
            text.setText(message);

            Toast toast = new Toast(getApplicationContext());
            toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 100);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setView(layout);
            toast.show();
        }

        private void showToast(String message) {
            showCustomToast(message);
        }

        /**
         * Enum that maps popup menu items to sorting logic.
         * Keeps all sorting behavior organized in one place.
         */
        private enum SortOption {
            PRICE_ASC(R.id.sort_price_asc, Comparator.comparingDouble(TableTennisProduct::getPrice)),
            PRICE_DESC(R.id.sort_price_desc,
                    Comparator.comparingDouble(TableTennisProduct::getPrice).reversed()),
            NAME_ASC(R.id.sort_name_asc,
                    Comparator.comparing(TableTennisProduct::getName, String.CASE_INSENSITIVE_ORDER)),
            NAME_DESC(R.id.sort_name_desc,
                    Comparator.comparing(TableTennisProduct::getName, String.CASE_INSENSITIVE_ORDER)
                            .reversed());

            final int menuId;
            final Comparator<TableTennisProduct> comparator;

            SortOption(int menuId, Comparator<TableTennisProduct> comparator) {
                this.menuId = menuId;
                this.comparator = comparator;
            }

            static SortOption fromMenuId(int id) {
                for (SortOption opt : values()) {
                    if (opt.menuId == id) return opt;
                }
                return null;
            }
        }
    }
