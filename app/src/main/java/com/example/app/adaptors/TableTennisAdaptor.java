package com.example.app.adaptors;

import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

public class TableTennisAdaptor extends ArrayAdapter {

    public TableTennisAdaptor(@NonNull Context context, int resource, @NonNull String[] objects) {
        super(context, resource, objects);

    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull android.view.ViewGroup parent) {
        return super.getView(position, convertView, parent);
    }
}

