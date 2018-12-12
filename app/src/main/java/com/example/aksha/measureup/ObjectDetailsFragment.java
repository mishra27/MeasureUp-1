package com.example.aksha.measureup;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aksha.db.models.Measurement;
import com.example.aksha.db.models.VideoObject;
import com.example.aksha.db.viewmodels.MeasurementViewModel;
import com.example.aksha.db.viewmodels.VideoObjectViewModel;

import java.io.File;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ObjectDetailsFragment extends Fragment  {
    private VideoObjectViewModel videoObjectViewModel;
    private MeasurementViewModel measurementViewModel;
    private NavController navController;

    public ObjectDetailsFragment() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        ((AppCompatActivity) this.getActivity()).getSupportActionBar().show();
        ((AppCompatActivity) this.getActivity()).getSupportActionBar().setTitle("Details");

        videoObjectViewModel = ViewModelProviders.of(getActivity()).get(VideoObjectViewModel.class);
        measurementViewModel = ViewModelProviders.of(this).get(MeasurementViewModel.class);

        final View rootView = inflater.inflate(R.layout.fragment_object_details, container, false);

        final TextView text = rootView.findViewById(R.id.name);
        final ThumbnailMeasurementView thumbnail = rootView.findViewById(R.id.thumbnail);
        final RecyclerView measurementsView = rootView.findViewById(R.id.measurementList);

        final MeasurementAdapter measurementAdapter = new MeasurementAdapter(getContext(), measurementViewModel);

        measurementsView.setAdapter(measurementAdapter);
        measurementsView.setLayoutManager(new LinearLayoutManager(getContext()));

        measurementViewModel.getCurrentMeasurement().observe(this, new Observer<Measurement>() {
            @Override
            public void onChanged(Measurement measurement) {
                if (measurement != null) {
                    thumbnail.setPoints(measurement.getX1(), measurement.getY1(), measurement.getX2(), measurement.getY2());
                    thumbnail.showMeasurement();
                } else {
                    thumbnail.hideMeasurement();
                }
            }
        });

        videoObjectViewModel.getCurrentVideoObject().observe(this, new Observer<VideoObject>() {
            @Override
            public void onChanged(final VideoObject videoObject) {
                text.setText(videoObject.getName());
                thumbnail.setImageBitmap(BitmapFactory.decodeFile(videoObject.getThumbnailPath()));

                measurementViewModel.getMeasurements(videoObject).observe(ObjectDetailsFragment.this, new Observer<List<Measurement>>() {
                    @Override
                    public void onChanged(List<Measurement> measurements) {
                        measurementAdapter.setMeasurements(measurements);
                    }
                });
            }
        });

        rootView.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                measurementNew(v);
            }
        });

        rootView.findViewById(R.id.imageButton3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteObject(v);
            }
        });

        return rootView;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        navController = Navigation.findNavController(this.getActivity(), R.id.fragment);
        super.onCreate(savedInstanceState);

    }

    public void measurementNew(View view) {
        // navigate to point selection screen
        navController.navigate(R.id.action_objectDetailsFragment_to_pointSelectionFragment);
    }

    public void deleteObject(View view) {
        // navigate to point selection screen
        videoObjectViewModel.getCurrentVideoObject().observe(this, new Observer<VideoObject>() {
            @Override
            public void onChanged(VideoObject videoObject) {
                String objectFolder = videoObject.getVideoPath().substring(0,58);
              Log.d("PATH IS", objectFolder);

                File dir = new File(objectFolder);
                if (dir.isDirectory())
                {
                    String[] children = dir.list();
                    for (int i = 0; i < children.length; i++)
                    {
                        new File(dir, children[i]).delete();
                    }
                }

                dir.delete();
                videoObjectViewModel.delete(videoObject);
            }
        });

        Toast.makeText(getActivity(), "Object deleted", Toast.LENGTH_SHORT).show();
        navController.navigate(R.id.action_objectDetailsFragment_to_galleryFragment);


    }

}
