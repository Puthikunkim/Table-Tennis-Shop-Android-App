package com.example.app.utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.app.R;

public class ImageLoader {
    public static void loadImage(Context context, ImageView imageView, String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_background)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.ic_launcher_background);
        }
    }

    public static void loadProductImage(Context context, ImageView imageView, String imageUrl) {
        loadImage(context, imageView, imageUrl);
    }
} 