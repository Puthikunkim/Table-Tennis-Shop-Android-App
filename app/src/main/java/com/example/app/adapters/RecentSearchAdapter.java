package com.example.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.R;

import java.util.ArrayList;
import java.util.List;

public class RecentSearchAdapter extends RecyclerView.Adapter<RecentSearchAdapter.ViewHolder> {
    private List<String> searches;
    private OnSearchClickListener listener;

    public interface OnSearchClickListener {
        void onSearchClick(String search);
        void onSearchRemove(String search);
    }

    public RecentSearchAdapter(OnSearchClickListener listener) {
        this.searches = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_search, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String search = searches.get(position);
        holder.searchText.setText(search);
        
        holder.itemView.setOnClickListener(v -> listener.onSearchClick(search));
        holder.removeButton.setOnClickListener(v -> {
            listener.onSearchRemove(search);
            searches.remove(position);
            notifyItemRemoved(position);
        });
    }

    @Override
    public int getItemCount() {
        return searches.size();
    }

    public void setSearches(List<String> searches) {
        this.searches = searches;
        notifyDataSetChanged();
    }

    public List<String> getSearches() {
        return new ArrayList<>(searches);
    }

    public void addSearch(String search) {
        // Remove if already exists
        searches.remove(search);
        // Add to beginning
        searches.add(0, search);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView searchText;
        ImageButton removeButton;

        ViewHolder(View itemView) {
            super(itemView);
            searchText = itemView.findViewById(R.id.searchText);
            removeButton = itemView.findViewById(R.id.removeButton);
        }
    }
} 