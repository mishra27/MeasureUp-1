package com.example.aksha.measureup;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import com.example.aksha.DataBase.videoObjectDao;

import com.example.aksha.DataBase.AppDatabase;
import com.example.aksha.DataBase.VideoObjects;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static com.example.aksha.DataBase.AppDatabase.getAppDatabase;

public class GalleryFragment extends Fragment {
    GridView gridView;
    AppDatabase db;
    String[] items ={"Hello","plz", "nextLine"};
    int[] images = {R.drawable.image1, R.drawable.image2, R.drawable.image3};
    List<VideoObjects> dataBase;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_gallery, container, false);
        db = getAppDatabase(getContext());
        gridView = (GridView)rootView.findViewById(R.id.gridView);
        //gridView.setAdapter(new testImageAdapter(getContext()));
        new Thread(new Runnable() {
            @Override
            public void run() {
                dataBase = db.videoObjectDao().getAll();
                gridView.setAdapter(new GridAdapter(getActivity(), dataBase));
            }
        }) .start();





        return rootView;
    }
}
