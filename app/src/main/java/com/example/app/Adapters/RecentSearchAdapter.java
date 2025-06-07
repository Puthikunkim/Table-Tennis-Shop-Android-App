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

/**
 * Adapter for displaying a list of recent search queries below a search bar.
 * Includes functionality to re-run or delete past searches, and caps the list at 10 items.
 */
public class RecentSearchAdapter extends RecyclerView.Adapter<RecentSearchAdapter.ViewHolder> {
    private final List<String> searches; // The list of recent search terms
    private final OnSearchClickListener listener; // Listener to handle user interactions
    private static final int MAX_SEARCHES = 10; // Limit to prevent unlimited growth

    // Interface for notifying parent components when a search is clicked or removed
    public interface OnSearchClickListener {
        void onSearchClick(String search);    // User tapped a recent search term
        void onSearchRemove(String search);   // User deleted a search term
    }

    public RecentSearchAdapter(OnSearchClickListener listener) {
        this.searches = new ArrayList<>();
        this.listener = listener;
    }

    /**
     * Inflates the layout for each recent search row.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_search, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds a search string to its TextView and sets up click/delete actions.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String search = searches.get(position);
        holder.searchText.setText(search);

        // When user taps on a search, notify listener to re-run it
        holder.itemView.setOnClickListener(v -> listener.onSearchClick(search));

        // When user taps the 'X' icon, remove that search
        holder.removeButton.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                String toRemove = searches.get(adapterPosition);
                listener.onSearchRemove(toRemove); // Notify caller first
                removeSearch(adapterPosition);     // Then remove locally
            }
        });
    }

    @Override
    public int getItemCount() {
        return searches.size();
    }

    /**
     * Replaces the current list of searches with a new one.
     */
    public void setSearches(List<String> newSearches) {
        this.searches.clear();
        this.searches.addAll(newSearches);
        notifyDataSetChanged();
    }

    /**
     * Returns a copy of the current searches (useful for saving/restoring state).
     */
    public List<String> getSearches() {
        return new ArrayList<>(searches);
    }

    /**
     * Adds a new search term to the top of the list.
     * Moves it to the top if it already exists, and trims the list if it exceeds max size.
     */
    public void addSearch(String search) {
        if (search == null || search.trim().isEmpty()) {
            return;
        }

        String trimmedSearch = search.trim();
        int existingIndex = searches.indexOf(trimmedSearch);

        // If the search is already in the list, remove the old instance
        if (existingIndex != -1) {
            searches.remove(existingIndex);
        }

        // Add to the top of the list
        searches.add(0, trimmedSearch);

        // If we've exceeded the maximum allowed, remove the oldest
        if (searches.size() > MAX_SEARCHES) {
            searches.remove(searches.size() - 1);
            notifyItemRemoved(searches.size()); // Notify removal of the last item
        }

        notifyItemInserted(0); // Notify UI that a new item was added at the top
    }

    /**
     * Removes a search item at a specific position.
     */
    public void removeSearch(int position) {
        if (position >= 0 && position < searches.size()) {
            searches.remove(position);
            notifyItemRemoved(position);
        }
    }

    /**
     * ViewHolder for holding references to search term text and delete button.
     */
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
