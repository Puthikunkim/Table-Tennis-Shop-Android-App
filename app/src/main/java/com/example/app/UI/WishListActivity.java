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
        // TODO: implement
        return null;
    }

    @Override
    protected int getSelectedMenuItemId() {
        // TODO: implement
        return 0;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: implement
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
