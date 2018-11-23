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
    String[] items ={"Hello","plz", "nextLine"};
    int[] images = {R.drawable.image1, R.drawable.image2, R.drawable.image3};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_gallery, container, false);
        gridView = (GridView)rootView.findViewById(R.id.gridView);
        //gridView.setAdapter(new testImageAdapter(getContext()));
        gridView.setAdapter(new GridAdapter(getActivity(), items, images));



        return rootView;
    }
}
