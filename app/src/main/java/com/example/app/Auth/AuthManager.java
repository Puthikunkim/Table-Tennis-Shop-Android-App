package com.example.app.Auth;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.app.UI.ProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.HashMap;
import java.util.Map;

public class AuthManager {
    private static final String TAG = "AuthManager";
    private static AuthManager instance;
    private final FirebaseAuth mAuth;
    private final Context context;

    private AuthManager(Context context) {
        this.context = context.getApplicationContext();
        this.mAuth = FirebaseAuth.getInstance();
    }

    public static synchronized AuthManager getInstance(Context context) {
        if (instance == null) {
            instance = new AuthManager(context);
        }
        return instance;
    }

    @Nullable
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public void signIn(String email, String password, AuthCallback callback) {
        if (email.isEmpty() || password.isEmpty()) {
            callback.onError(new IllegalArgumentException("Please enter email and password"));
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        callback.onSuccess(user);
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        callback.onError(task.getException());
                    }
                });
    }

    public void createAccount(String email, String password, String name, AuthCallback callback) {
        if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
            callback.onError(new IllegalArgumentException("Please enter name, email and password"));
            return;
        }
        if (password.length() < 6) {
            callback.onError(new IllegalArgumentException("Password must be at least 6 characters long"));
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user == null) {
                            callback.onError(new IllegalStateException("User was null after successful account creation"));
                            return;
                        }

                        // Update display name
                        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build();

                        user.updateProfile(request)
                                .addOnCompleteListener(profileTask -> {
                                    if (profileTask.isSuccessful()) {
                                        Log.d(TAG, "User display name updated.");
                                        user.reload().addOnCompleteListener(reload -> callback.onSuccess(mAuth.getCurrentUser()));
                                    } else {
                                        Log.w(TAG, "Failed to update display name", profileTask.getException());
                                        callback.onSuccess(user);
                                    }
                                });
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        callback.onError(task.getException());
                    }
                });
    }

    public void signOut() {
        mAuth.signOut();
    }

    public interface AuthCallback {
        void onSuccess(@Nullable FirebaseUser user);
        void onError(Exception e);
    }
} 