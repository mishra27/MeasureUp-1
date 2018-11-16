package com.example.aksha.measureup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class GridAdapter extends BaseAdapter {

   //private TextView text1;
    private ImageView img;
    private TextView text2;
    private Integer[] images = {};
    private Context context;
    private String[] items;
    LayoutInflater inflater;

    public GridAdapter(Context context, String[] items, Integer[] array) {
        this.context = context;
        this.items = items;
        images = array;
        inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public Object getItem(int position) {
        return items[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View gridView;
        if (convertView == null) {

            gridView = new View(context);
            gridView = inflater.inflate(R.layout.gallery_object_layout, null);

        }
        else {
            gridView = (View) convertView;
        }
        img.setImageResource(images[position]);
        text2 = (TextView) gridView
                .findViewById(R.id.Test2);
        //.setText(items[position]);


        return gridView;




    }

}

