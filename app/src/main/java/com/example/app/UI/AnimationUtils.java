package com.example.app.UI;

import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Utility class for common animations used across activities
 */
public class AnimationUtils {
    
    private static final long DEFAULT_DURATION = 120L;
    private static final float DEFAULT_SCALE = 1.15f;
    
    /**
     * Applies a scale animation to a view and executes a callback after completion
     */
    public static void animateButton(View view, Runnable afterAnimation) {
        animateButton(view, DEFAULT_SCALE, DEFAULT_DURATION, afterAnimation);
    }
    
    /**
     * Applies a scale animation to a view with custom parameters
     */
    public static void animateButton(View view, float scale, long duration, Runnable afterAnimation) {
        view.animate()
            .scaleX(scale)
            .scaleY(scale)
            .setDuration(duration)
            .setInterpolator(new AccelerateDecelerateInterpolator())
            .withEndAction(() -> {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(duration)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .withEndAction(afterAnimation)
                    .start();
            })
            .start();
    }
    
    /**
     * Applies a fade animation to a view
     */
    public static void fadeIn(View view, long duration) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(new AccelerateDecelerateInterpolator())
            .start();
    }
    
    /**
     * Applies a fade out animation to a view
     */
    public static void fadeOut(View view, long duration) {
        view.animate()
            .alpha(0f)
            .setDuration(duration)
            .setInterpolator(new AccelerateDecelerateInterpolator())
            .withEndAction(() -> view.setVisibility(View.GONE))
            .start();
    }
} 