package com.example.app.UI;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.R;
import com.example.app.Data.FirestoreRepository;
import com.example.app.Model.TableTennisProduct;
import com.example.app.adapters.WishListAdapter;
import com.example.app.databinding.ActivityWishListBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class WishListActivity extends BaseActivity<ActivityWishListBinding> {

    private static final String TAG = "WishListActivity";
    private FirebaseAuth mAuth;
    private FirestoreRepository firestoreRepository;
    private WishListAdapter wishlistAdapter;
    private List<TableTennisProduct> wishlistItems;

    @Override
    protected ActivityWishListBinding inflateContentBinding() {
        return ActivityWishListBinding.inflate(getLayoutInflater());
    }

    @Override
    protected int getSelectedMenuItemId() {
        return R.id.wish_list;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        firestoreRepository = FirestoreRepository.getInstance();
        wishlistItems = new ArrayList<>();

        // Initialize RecyclerView
        binding.recyclerViewWishlist.setLayoutManager(new LinearLayoutManager(this));
        wishlistAdapter = new WishListAdapter(this, wishlistItems, new WishListAdapter.OnWishlistItemActionListener() {
            @Override
            public void onDeleteClick(TableTennisProduct product) {
                removeProductFromWishlist(product);
            }

            @Override
            public void onAddToCartClick(TableTennisProduct product) {
                Toast.makeText(WishListActivity.this, "Added " + product.getName() + " to cart!", Toast.LENGTH_SHORT).show();
            }
        });
        binding.recyclerViewWishlist.setAdapter(wishlistAdapter);

        // "Sign In" button when logged out
        binding.loggedOutWishlist.signInButtonWishlist.setOnClickListener(v -> {
            Intent intent = new Intent(WishListActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        // TODO: implement
    }

    private void updateUI(FirebaseUser user) {
        // TODO: implement
    }

    private void loadWishlist(String userId) {
        // TODO: implement
    }

    private void removeProductFromWishlist(TableTennisProduct product) {
        // TODO: implement
    }
}
