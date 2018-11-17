package com.example.aksha.measureup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class GalleryFragment extends Fragment {
    GridView gridView;
    public String[] items ={"Hello","I'm","a","cool"};
    private Integer[] images = {R.drawable.image1, R.drawable.image2, R.drawable.image3, R.drawable.image4};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_gallery, container, false);
        gridView = (GridView)rootView.findViewById(R.id.gallery_grid_dynamic);
        gridView.setAdapter(new GridAdapter(getActivity(), items, images));



        return rootView;
    }



//    public void itemClicked(int position) {
//
//        if(position == 0){
//            mp = MediaPlayer.create(getActivity(), R.raw.sound1);
//            mp.start();
//        }else if(position == 1){
//            mp = MediaPlayer.create(getActivity(), R.raw.sound2);
//            mp.start();
//
//        }else if(position == 2) {
//            mp = MediaPlayer.create(getActivity(), R.raw.sound3);
//            mp.start();
//        }
//        else if(position == 3) {
//            mp = MediaPlayer.create(getActivity(), R.raw.sound4);
//            mp.start();
//        }
//        else if(position == 4) {
//            mp = MediaPlayer.create(getActivity(), R.raw.sound5);
//            mp.start();
//        }
//        else if(position == 5) {
//            mp = MediaPlayer.create(getActivity(), R.raw.sound6);
//            mp.start();
//        }

//    }

}
