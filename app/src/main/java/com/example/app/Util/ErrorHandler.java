package com.example.app.Util;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.example.app.R;

/**
 * Utility class for handling common error scenarios across activities
 */
public class ErrorHandler {
    private static final String TAG = "ErrorHandler";
    
    /**
     * Shows a custom toast message with the given message
     */
    private static void showCustomToast(Context context, String message) {
        View layout = LayoutInflater.from(context).inflate(R.layout.custom_toast, null);
        
        TextView text = layout.findViewById(R.id.toast_text);
        text.setText(message);
        
        Toast toast = new Toast(context.getApplicationContext());
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 100);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
    
    /**
     * Shows a toast message and logs the error
     */
    public static void showError(Context context, String message, Exception e) {
        String errorMessage = message + (e != null ? ": " + e.getMessage() : "");
        showCustomToast(context, errorMessage);
        if (e != null) {
            Log.e(TAG, errorMessage, e);
        }
    }
    
    /**
     * Shows a toast message for user-friendly errors
     */
    public static void showUserError(Context context, String message) {
        showCustomToast(context, message);
    }
    
    /**
     * Handles authentication errors
     */
    public static void handleAuthError(Context context, Exception e) {
        showError(context, "Authentication failed", e);
    }
    
    /**
     * Handles Firestore operation errors
     */
    public static void handleFirestoreError(Context context, String operation, Exception e) {
        showError(context, "Failed to " + operation, e);
    }
    
    /**
     * Handles network errors
     */
    public static void handleNetworkError(Context context) {
        showUserError(context, "Please check your internet connection");
    }
    
    /**
     * Handles missing data errors
     */
    public static void handleMissingDataError(Context context, String dataType) {
        showUserError(context, dataType + " not found");
    }
} 