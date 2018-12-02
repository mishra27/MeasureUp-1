package com.example.aksha.measureup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.AndroidUtil;
import org.jcodec.common.model.Picture;

import com.example.aksha.DataBase.VideoObject;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class GridAdapter extends BaseAdapter {
    private Context context;
    private List<VideoObject> videoObjects;
    private LayoutInflater inflater;

    public GridAdapter(Context context, List<VideoObject> videoObjects) {
        this.context = context;
        this.inflater = LayoutInflater.from(this.context);
        this.setVideoObjects(videoObjects);
    }

    public void setVideoObjects(List<VideoObject> videoObjects) {
        this.videoObjects = videoObjects;
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        if (videoObjects != null) {
            return videoObjects.size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        return videoObjects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View objectView;
        if (convertView == null) {
            objectView = inflater.inflate(R.layout.gallery_object_layout, null);

            TextView text = objectView.findViewById(R.id.textView);
            text.setText(videoObjects.get(position).getVideoName());

            ImageView img = objectView.findViewById(R.id.imageView);

            try {
                Picture thumbnail = FrameGrab.getFrameFromFile(new File(videoObjects.get(position).getVideoPath()), 1);
                img.setImageBitmap(AndroidUtil.toBitmap(thumbnail));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JCodecException e) {
                e.printStackTrace();
            }
        } else {
            objectView = (View) convertView;
        }

        return objectView;
    }

}

