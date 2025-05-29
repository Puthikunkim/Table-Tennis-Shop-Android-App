package com.example.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.Model.TableTennisProduct;
import com.example.app.R;

import java.util.List;

public class TopPicksAdapter extends RecyclerView.Adapter<TopPicksAdapter.ViewHolder> {
    private final List<TableTennisProduct> products;
    private final Context context;

    public TopPicksAdapter(Context context, List<TableTennisProduct> products) {
        this.context = context;
        this.products = products;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_top_pick, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TableTennisProduct product = products.get(position);
        holder.name.setText(product.getName());
        holder.views.setText(product.getViews() + " views");
        // TODO: load image into holder.image

    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, views;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.topPickImage);
            name = itemView.findViewById(R.id.topPickName);
            views = itemView.findViewById(R.id.topPickViews);
        }
    }
}