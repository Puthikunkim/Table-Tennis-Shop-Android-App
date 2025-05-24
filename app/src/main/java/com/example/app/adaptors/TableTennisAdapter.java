package com.example.app.adaptors;

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

}
