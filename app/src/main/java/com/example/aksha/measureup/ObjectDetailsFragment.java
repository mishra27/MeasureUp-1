package com.example.aksha.measureup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.aksha.DataBase.VideoObject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

public class ObjectDetailsFragment extends Fragment {
    private VideoObjectViewModel videoObjectViewModel;

    public ObjectDetailsFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        videoObjectViewModel = ViewModelProviders.of(getActivity()).get(VideoObjectViewModel.class);

        videoObjectViewModel.getCurrentVideoObject().observe(getActivity(), new Observer<VideoObject>() {
            @Override
            public void onChanged(VideoObject videoObject) {

            }
        });
    }
}
