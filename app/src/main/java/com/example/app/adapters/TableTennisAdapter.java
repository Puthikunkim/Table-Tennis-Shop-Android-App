package com.example.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.app.Model.TableTennisProduct;
import com.example.app.R;

import java.util.List;

public class TableTennisAdapter extends ArrayAdapter<TableTennisProduct> {

    private int mResource;
    private Context mContext;
    private List<TableTennisProduct> mProducts;

    public TableTennisAdapter(Context context, int resource, List<TableTennisProduct> objects) {
        super(context, resource, objects);
        mResource = resource;
        mContext = context;
        mProducts = objects;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemView = convertView;
        if (itemView == null) {
            itemView = LayoutInflater.from(mContext).inflate(mResource, parent, false);
        }

        // Get the current product
        TableTennisProduct product = mProducts.get(position);

        // Bind views
        TextView nameTextView = itemView.findViewById(R.id.textViewProductName);
        TextView descTextView = itemView.findViewById(R.id.textViewProductDescription);
        TextView priceTextView = itemView.findViewById(R.id.textViewProductPrice);
        ImageView imageView = itemView.findViewById(R.id.imageViewProduct);

        // Set text
        nameTextView.setText(product.getName());
        descTextView.setText(product.getDescription());
        priceTextView.setText(String.format("$%.2f", product.getPrice()));

        // Load image (first one from imageUrls) using Picasso
        imageView.setImageResource(0); // sets image to blank

        return itemView;
    }

}
