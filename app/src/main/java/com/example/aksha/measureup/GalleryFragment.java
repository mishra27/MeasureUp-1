package com.example.aksha.measureup;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.example.aksha.DataBase.VideoObject;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

public class GalleryFragment extends Fragment {
    private VideoObjectViewModel videoObjectViewModel;

    private GridView gridView;
    private GridAdapter gridAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_gallery, container, false);

        this.gridView = rootView.findViewById(R.id.gridView);
        this.gridAdapter = new GridAdapter(getActivity(), null);

        gridView.setAdapter(gridAdapter);

        videoObjectViewModel = ViewModelProviders.of(this).get(VideoObjectViewModel.class);
        videoObjectViewModel.getAllVideoObjects().observe(this, new Observer<List<VideoObject>>() {
            @Override
            public void onChanged(List<VideoObject> videoObjects) {
                gridAdapter.setVideoObjects(videoObjects);

                int size = 0;
                if (videoObjects != null) {
                    size = videoObjects.size();
                }
                Log.i("Gallery VideoObjects", Integer.toString(size));
            }
        });

        return rootView;
    }
}
