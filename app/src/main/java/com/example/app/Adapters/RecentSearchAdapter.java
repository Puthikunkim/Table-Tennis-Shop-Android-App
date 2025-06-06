package com.example.app.Adapters;

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
    private final List<String> searches;
    private final OnSearchClickListener listener;
    private static final int MAX_SEARCHES = 10;

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
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                String toRemove = searches.get(adapterPosition);
                listener.onSearchRemove(toRemove);
                removeSearch(adapterPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return searches.size();
    }

    public void setSearches(List<String> newSearches) {
        this.searches.clear();
        this.searches.addAll(newSearches);
        notifyDataSetChanged();
    }

    public List<String> getSearches() {
        return new ArrayList<>(searches);
    }

    public void addSearch(String search) {
        if (search == null || search.trim().isEmpty()) {
            return;
        }

        String trimmedSearch = search.trim();
        int existingIndex = searches.indexOf(trimmedSearch);
        
        if (existingIndex != -1) {
            searches.remove(existingIndex);
        }
        
        searches.add(0, trimmedSearch);
        
        if (searches.size() > MAX_SEARCHES) {
            searches.remove(searches.size() - 1);
            notifyItemRemoved(searches.size());
        }
        
        notifyItemInserted(0);
    }

    public void removeSearch(int position) {
        if (position >= 0 && position < searches.size()) {
            searches.remove(position);
            notifyItemRemoved(position);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView searchText;
        final ImageButton removeButton;

        ViewHolder(View itemView) {
            super(itemView);
            searchText = itemView.findViewById(R.id.searchText);
            removeButton = itemView.findViewById(R.id.removeButton);
        }
    }
} 