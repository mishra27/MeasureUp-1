package com.example.aksha.measureup;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        final View rootView = inflater.inflate(R.layout.fragment_object_details, container, false);


        videoObjectViewModel = ViewModelProviders.of(getActivity()).get(VideoObjectViewModel.class);

        videoObjectViewModel.getCurrentVideoObject().observe(getActivity(), new Observer<VideoObject>() {
            @Override
            public void onChanged(VideoObject videoObject) {
                TextView text = rootView.findViewById(R.id.name);
                ImageView thumbnail = rootView.findViewById(R.id.thumbnail);
//                ListView measurements = rootView.findViewById(R.id.measurementList);

                text.setText(videoObject.getVideoName());
                thumbnail.setImageBitmap(BitmapFactory.decodeFile(videoObject.getThumbnailPath()));

            }
        });
        return rootView;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
}
