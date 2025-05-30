package com.example.app.UI;

import android.os.Bundle;
import androidx.annotation.Nullable;
import com.example.app.R;
import com.example.app.Data.FirestoreRepository;
import com.example.app.databinding.ActivityProfileBinding;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends BaseActivity<ActivityProfileBinding> {

    private FirebaseAuth mAuth;
    private FirestoreRepository firestoreRepository;

    @Override
    protected ActivityProfileBinding inflateContentBinding() {
        return ActivityProfileBinding.inflate(getLayoutInflater());
    }

    @Override
    protected int getSelectedMenuItemId() {
        return R.id.profile;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Auth and FirestoreRepository
        mAuth = FirebaseAuth.getInstance();
        firestoreRepository = FirestoreRepository.getInstance();
    }
}
