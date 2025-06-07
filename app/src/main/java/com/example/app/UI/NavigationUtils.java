package com.example.app.UI;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Utility class for handling common navigation patterns across activities
 */
public class NavigationUtils {
    
    /**
     * Navigates to an activity with standard flags
     */
    public static void navigateToActivity(Context context, Class<?> targetActivity) {
        Intent intent = new Intent(context, targetActivity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }
    
    /**
     * Navigates to an activity with extras
     */
    public static void navigateToActivity(Context context, Class<?> targetActivity, Bundle extras) {
        Intent intent = new Intent(context, targetActivity);
        intent.putExtras(extras);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }
    
    /**
     * Navigates to an activity with a single extra
     */
    public static void navigateToActivity(Context context, Class<?> targetActivity, String key, String value) {
        Intent intent = new Intent(context, targetActivity);
        intent.putExtra(key, value);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }
    
    /**
     * Navigates to an activity with fade transition
     */
    public static void navigateWithFade(Context context, Class<?> targetActivity) {
        Intent intent = new Intent(context, targetActivity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
        if (context instanceof BaseActivity) {
            ((BaseActivity<?>) context).overridePendingTransition(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            );
        }
    }
    
    /**
     * Navigates to an activity with no transition
     */
    public static void navigateWithNoTransition(Context context, Class<?> targetActivity) {
        Intent intent = new Intent(context, targetActivity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
        if (context instanceof BaseActivity) {
            ((BaseActivity<?>) context).overridePendingTransition(0, 0);
        }
    }
} 