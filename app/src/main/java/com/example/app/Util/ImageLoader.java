package com.example.app.Util;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.app.R;

/**
 * Utility class for loading images into ImageView components using the Glide library.
 * This class provides a centralised way to handle image loading, including placeholders
 * and error drawables.
 */
public class ImageLoader {
    public static void loadImage(Context context, ImageView imageView, String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.loading)
                    .error(R.drawable.loading)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.loading);
        }
    }

    public static void loadProductImage(Context context, ImageView imageView, String imageUrl) {
        loadImage(context, imageView, imageUrl);
    }
} 