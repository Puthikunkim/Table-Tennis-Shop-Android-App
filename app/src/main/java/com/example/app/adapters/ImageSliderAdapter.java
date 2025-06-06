package com.example.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.R;
import com.example.app.utils.ImageLoader;

import java.util.List;

public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.ImageViewHolder> {
    private final List<String> imageUrls;
    private OnImageClickListener listener;

    public interface OnImageClickListener {
        void onImageClick(int position);
    }

    public ImageSliderAdapter(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public void setOnImageClickListener(OnImageClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate a simple layout with just an ImageView (match_parent width/height)
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_slider, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String url = imageUrls.get(position);
        ImageLoader.loadImage(holder.itemView.getContext(), holder.imageView, url);
        
        if (listener != null) {
            holder.itemView.setOnClickListener(v -> listener.onImageClick(position));
        }
    }

    @Override
    public int getItemCount() {
        return imageUrls != null ? imageUrls.size() : 0;
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        final ImageView imageView;
        
        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.sliderImageView);
        }
    }
}
