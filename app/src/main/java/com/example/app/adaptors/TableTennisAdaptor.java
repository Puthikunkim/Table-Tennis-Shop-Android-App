package com.example.app.UI;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.app.Model.TableTennisProduct;
import com.example.app.R;

import java.util.List;

public class ProductAdapter extends ArrayAdapter<TableTennisProduct> {

    private int mResource;
    private Context mContext;
    private List<TableTennisProduct> mProducts;

    public ProductAdapter(Context context, int resource, List<TableTennisProduct> objects) {
        super(context, resource, objects);
        mResource = resource;
        mContext = context;
        mProducts = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View item = convertView;
        if (item == null) {
            item = LayoutInflater.from(mContext).inflate(mResource, parent, false);
        }

        TableTennisProduct product = mProducts.get(position);

        ImageView imageView = item.findViewById(R.id.image_view);
        TextView nameView = item.findViewById(R.id.name_text_view);
        TextView descriptionView = item.findViewById(R.id.description_text_view);
        TextView priceView = item.findViewById(R.id.price_text_view);

        nameView.setText(product.getName());
        descriptionView.setText(product.getDescription());
        priceView.setText("$" + String.format("%.2f", product.getPrice()));

        // Load first image if exists
        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            Glide.with(mContext)
                    .load(product.getImageUrls().get(0))
                    .placeholder(R.drawable.placeholder) // add a placeholder in drawable
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.placeholder);
        }

        return item;
    }
}
