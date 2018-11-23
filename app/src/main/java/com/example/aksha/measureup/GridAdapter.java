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
    private int[] images;
    private Context context;
    private String[] items;
    LayoutInflater inflater;

    public GridAdapter(Context context, String[] items, int[] array) {
        this.context = context;
        this.items = items;
        this.images = array;
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

        View gridView;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {

            gridView = new View(context);
            gridView = inflater.inflate(R.layout.gallery_object_layout, null);
            ImageView img = new ImageView(context);
            TextView text = new TextView(context);
            text = (TextView) gridView.findViewById(R.id.textView);
            text.setText(items[position]);
            img = (ImageView) gridView.findViewById(R.id.imageView);
            //.setText(items[position]);
//            img.setLayoutParams(new GridView.LayoutParams(85, 85));
//            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
//            img.setPadding(8, 8, 8, 8);
            img.setImageResource(images[position]);

        }
        else {
            gridView = (View) convertView;
        }



        return gridView;




    }

}

