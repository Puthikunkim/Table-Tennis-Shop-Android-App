package com.example.app.Util;

import android.content.Context;
import android.util.Log;

/**
 * Utility class for handling common error scenarios across activities
 */
public class ErrorHandler {
    private static final String TAG = "ErrorHandler";

    /**
     * Shows a toast message and logs the error
     */
    public static void showError(Context context, String message, Exception e) {
        String errorMessage = message + (e != null ? ": " + e.getMessage() : "");
        ToastUtils.showCustomToast(context, errorMessage);
        if (e != null) {
            Log.e(TAG, errorMessage, e);
        }
    }

    /**
     * Shows a toast message for user-friendly errors
     */
    public static void showUserError(Context context, String message) {
        ToastUtils.showCustomToast(context, message);
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
