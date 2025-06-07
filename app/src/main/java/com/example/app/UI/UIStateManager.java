package com.example.app.UI;

import android.view.View;
import android.view.ViewGroup;

/**
 * Utility class to manage UI state transitions and visibility across activities.
 */
public class UIStateManager {
    
    /**
     * Shows one view and hides all others in a ViewGroup
     */
    public static void showViewAndHideOthers(ViewGroup container, View viewToShow) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            child.setVisibility(child == viewToShow ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Shows multiple views and hides all others in a ViewGroup
     */
    public static void showViewsAndHideOthers(ViewGroup container, View... viewsToShow) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            boolean shouldShow = false;
            for (View view : viewsToShow) {
                if (child == view) {
                    shouldShow = true;
                    break;
                }
            }
            child.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Shows a view with animation
     */
    public static void showViewWithAnimation(View view, long duration) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
            .alpha(1f)
            .setDuration(duration)
            .start();
    }

    /**
     * Hides a view with animation
     */
    public static void hideViewWithAnimation(View view, long duration) {
        view.animate()
            .alpha(0f)
            .setDuration(duration)
            .withEndAction(() -> view.setVisibility(View.GONE))
            .start();
    }
} 