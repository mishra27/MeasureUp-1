package com.example.aksha.measureup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.AndroidUtil;
import org.jcodec.common.model.Picture;

import com.example.aksha.DataBase.VideoObjects;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class GridAdapter extends BaseAdapter {

   //private TextView text1;
    private Context context;
    List<VideoObjects> db;
    LayoutInflater inflater;

    public GridAdapter(Context context, List<VideoObjects> db) {
        this.context = context;
        this.db = db;
        inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount() {
        return db.size();
    }

    @Override
    public Object getItem(int position) {
        return db.get(position);
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
            text.setText(db.get(position).getVideoName());
            img = (ImageView) gridView.findViewById(R.id.imageView);
            //.setText(items[position]);
//            img.setLayoutParams(new GridView.LayoutParams(85, 85));
//            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
//            img.setPadding(8, 8, 8, 8);
            img.setImageBitmap(db.get(position).getVideoThumbnail());



        }
        else {
            gridView = (View) convertView;
        }



        return gridView;




    }

}

