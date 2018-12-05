package com.example.aksha.measureup;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.aksha.db.models.VideoObject;
import com.example.aksha.db.viewmodels.VideoObjectViewModel;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

public class GalleryFragment extends Fragment {
    private VideoObjectViewModel videoObjectViewModel;

    private GridView gridView;
    private GridAdapter gridAdapter;
    private NavController navController;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        navController = Navigation.findNavController(this.getActivity(), R.id.fragment);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_gallery, container, false);

        this.gridView = rootView.findViewById(R.id.gridView);
        this.gridAdapter = new GridAdapter(getActivity(), null);

        gridView.setAdapter(gridAdapter);

        videoObjectViewModel = ViewModelProviders.of(getActivity()).get(VideoObjectViewModel.class);
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
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                videoObjectViewModel.setCurrentVideoObject(videoObjectViewModel.getAllVideoObjects().getValue().get(position));

                navController.navigate(R.id.action_galleryFragment_to_objectDetailsFragment);
            }
        });
        return rootView;
    }
}
