package com.example.aksha.measureup;


import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.util.Log;
import android.util.SizeF;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;

import com.example.aksha.db.models.Measurement;
import com.example.aksha.db.models.VideoObject;
import com.example.aksha.db.viewmodels.MeasurementViewModel;
import com.example.aksha.db.viewmodels.VideoObjectViewModel;

import org.opencv.core.Point;

import java.io.File;
import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

public class PointSelectionFragment extends Fragment {
    private PointSelectorView point1;
    private PointSelectorView point2;
    private Button measureButton;
    private ImageView imageView;

    private VideoObjectViewModel videoObjectViewModel;
    private MeasurementViewModel measurementViewModel;

    private File img = null;
    private VideoProcessor vp = null;

    double refDistance = 0;

    public PointSelectionFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((AppCompatActivity) this.getActivity()).getSupportActionBar().hide();

        videoObjectViewModel = ViewModelProviders.of(getActivity()).get(VideoObjectViewModel.class);
        measurementViewModel = ViewModelProviders.of(getActivity()).get(MeasurementViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_point_selection, container, false);

        videoObjectViewModel.getCurrentVideoObject().observe(this, new Observer<VideoObject>() {
            @Override
            public void onChanged(VideoObject videoObject) {
                String videoPath = videoObject.getVideoPath();
                refDistance = videoObject.getMoveDistance() / 100;

                Log.d("TESTm ", videoPath);

                File video = new File(videoPath);

                vp = new VideoProcessor(video);

                imageView = getView().findViewById(R.id.imageView4);
                Bitmap thumbnail = BitmapFactory.decodeFile(videoObject.getThumbnailPath());
                imageView.setImageBitmap(thumbnail);
            }
        });

        point1 = view.findViewById(R.id.pointSelectorView);
        point2 = view.findViewById(R.id.pointSelectorView2);
        measureButton = view.findViewById(R.id.button12);

        measureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickProcessor();

                // Navigation.findNavController(PointSelectionFragment.this.getView()).navigateUp();
            }
        });

        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                View view = PointSelectionFragment.this.getView();

                point1.setX(view.getWidth() / 4 - point1.getWidth() / 2);
                point1.setY(view.getHeight() / 2 - point1.getHeight() / 2);

                point2.setX(view.getWidth() * 3 / 4 - point1.getWidth() / 2);
                point2.setY(view.getHeight() / 2 - point1.getHeight() / 2);

                point1.invalidate();
                point2.invalidate();

                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        return view;
    }

    public ArrayList<Point> getMeasurePoints() {
        ArrayList<Point> points = new ArrayList<>();

        points.add(point1.getPoint());
        points.add(point2.getPoint());

        return points;
    }

    private SizeF getCameraResolution(int camNum)
    {
        SizeF size = new SizeF(0,0);
        CameraManager manager = (CameraManager) this.getActivity().getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] cameraIds = manager.getCameraIdList();
            if (cameraIds.length > camNum) {
                CameraCharacteristics character = manager.getCameraCharacteristics(cameraIds[camNum]);
                size = character.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
            }
        }
        catch (CameraAccessException e)
        {
            Log.e("YourLogString", e.getMessage(), e);
        }
        return size;
    }

    private float getFocalLength(int camNum) {
        CameraManager manager = (CameraManager) this.getActivity().getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] cameraIds = manager.getCameraIdList();
            if (cameraIds.length > camNum) {
                CameraCharacteristics character = manager.getCameraCharacteristics(cameraIds[camNum]);
                return character.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)[camNum];
            }
        }
        catch (CameraAccessException e)
        {
            Log.e("YourLogString", e.getMessage(), e);
        }

        return 0;
    }

    public void onClickProcessor() {
        ArrayList<Point> iniPoints = getMeasurePoints();
        Point p1 = iniPoints.get(0);
        Point p2 = iniPoints.get(1);

        /*
        vp.grabFrames(false);
        vp.setInitPoints(p1, p2);
        vp.trackOpticalFlow();
        ArrayList<Point> finalPoints = vp.getFinalPoints();
        SizeF sizeF = getCameraResolution(0);
        double oFM = getFocalLength(0) / 1000;
        double ccdH = getCameraResolution(0).getWidth() / 1000;
        double results = vp.measurement(oFM, ccdH, refDistance, iniPoints, finalPoints);

        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());

        builder.setTitle("Results");
        builder.setMessage(results + " m");
        builder.setCancelable(true);

        builder.create().show();
        */

        VideoObject videoObject = videoObjectViewModel.getCurrentVideoObject().getValue();

        if (videoObject != null) {
            View parentView = (View) point1.getParent();
            int width = parentView.getWidth();
            int height = parentView.getHeight();

            Measurement measurement = new Measurement();
            measurement.setName("Measurement");
            measurement.setObjectId(videoObject.getId());
//            measurement.setLength(results);
            measurement.setLength(1d);
            measurement.setX1(p1.x / width);
            measurement.setX2(p2.x / width);
            measurement.setY1(p1.y / height);
            measurement.setY2(p2.y / height);

            measurementViewModel.insert(measurement);
        } else {
            Log.e("onClickProcessor", "videoObject is null!!!!");
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        //((AppCompatActivity) this.getActivity()).getSupportActionBar().show();
    }
}