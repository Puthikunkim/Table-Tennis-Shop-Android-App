package com.example.app.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.R;
import com.example.app.Util.ImageLoader;

import java.util.List;

/**
 * Adapter for displaying a list of images in a horizontal image slider (e.g., ViewPager2).
 * Each item is just a single image, and tapping on it can trigger a callback.
 */
public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.ImageViewHolder> {

    // List of image URLs to load into the slider
    private final List<String> imageUrls;

    // Listener for handling image click events
    private OnImageClickListener listener;

    // Interface to allow external classes to respond to image clicks
    public interface OnImageClickListener {
        void onImageClick(int position);
    }

    // Basic constructor takes in the list of image URLs to display
    public ImageSliderAdapter(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    // Allows parent components (e.g. Activities) to set a click listener
    public void setOnImageClickListener(OnImageClickListener listener) {
        this.listener = listener;
    }

    // Called when RecyclerView needs a new ViewHolder (i.e., a new image view to display)
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each image, usually just a full-size ImageView
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_slider, parent, false);
        return new ImageViewHolder(view);
    }

    // Binds the image URL to the ImageView using the ImageLoader utility
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String url = imageUrls.get(position);

        // Load the image from the URL into the ImageView
        ImageLoader.loadImage(holder.itemView.getContext(), holder.imageView, url);

        // If an image click listener is set, trigger it on tap
        if (listener != null) {
            holder.itemView.setOnClickListener(v -> listener.onImageClick(position));
        }
    }

    // Return the total number of images in the list
    @Override
    public int getItemCount() {
        return imageUrls != null ? imageUrls.size() : 0;
    }

    // ViewHolder to hold references to the view components (in this case, just one ImageView)
    static class ImageViewHolder extends RecyclerView.ViewHolder {
        final ImageView imageView;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.sliderImageView);
        }
    }
}
