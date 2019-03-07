package com.example.aksha.measureup;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.util.SizeF;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.aksha.db.models.Measurement;
import com.example.aksha.db.models.VideoObject;
import com.example.aksha.db.viewmodels.MeasurementViewModel;
import com.example.aksha.db.viewmodels.VideoObjectViewModel;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

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
    private ArrayList<Point> points = new ArrayList<>();
    private String path;
    private int count = 0;
    private CustomVideoView videoView = null;
    private RelativeLayout relativeLayout = null;
    private String videoPath;

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

        relativeLayout = (RelativeLayout) view.findViewById(R.id.main_relative_layout);








        videoObjectViewModel.getCurrentVideoObject().observe(this, new Observer<VideoObject>() {
            @Override
            public void onChanged(VideoObject videoObject) {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        1080, 1920);
                videoView = new CustomVideoView(getContext(), videoObject.getVideoPath(), 1920, 1080);
                relativeLayout.addView(videoView, params);


                videoPath = videoObject.getVideoPath();
                refDistance = videoObject.getMoveDistance() / 100;

                Log.d("TESTm ", videoPath);

                File video = new File(videoPath);

                vp = new VideoProcessor(video);

                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                imageView = getView().findViewById(R.id.imageView4);
                Bitmap thumbnail = BitmapFactory.decodeFile(videoObject.getThumbnailPath());
                imageView.setImageBitmap(thumbnail);

//                imageView = getView().findViewById(R.id.imageView4);
//                // Bitmap thumbnail = BitmapFactory.decodeFile(videoObject.getThumbnailPath());
//                Mat f = videoView.getMats().get(2);
//                Bitmap bmp = Bitmap.createBitmap(f.cols(), f.rows(), Bitmap.Config.ARGB_8888);
//                Utils.matToBitmap(f, bmp);
//                imageView.setImageBitmap(bmp);



            }
        });




        point1 = view.findViewById(R.id.pointSelectorView);
        point2 = view.findViewById(R.id.pointSelectorView2);
        measureButton = view.findViewById(R.id.button12);


        measureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences settings = PointSelectionFragment.this.getActivity().getSharedPreferences("PREFS", 0);
                String optical = settings.getString("optical", "");
                VideoObject videoObject = videoObjectViewModel.getCurrentVideoObject().getValue();
                path = videoObject.getThumbnailPath();




//                int s = videoView.getMats().size();
//                Log.d("size ", String.valueOf(s));s

                if(optical.equals("yes"))
                onClickProcessor();

                else if(count == 0){
                    getMeasurePoints();

                    // Bitmap thumbnail = BitmapFactory.decodeFile(videoObject.getThumbnailPath());
                    Mat f = videoView.getMats().get(videoView.getMats().size()-3);
                    Bitmap bmp = Bitmap.createBitmap(f.cols(), f.rows(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(f, bmp);
                    imageView.setImageBitmap(bmp);
//                    Bitmap lastFrame = BitmapFactory.decodeFile(path.replaceAll("thumbnail", "last"));
//                    imageView.setImageBitmap(lastFrame);
                    count++;
                }

                else{
                    count = 0;
                    onClickProcessor2();
                }


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

        points.add(point1.getPoint());
        points.add(point2.getPoint());

        return points;
    }

    private SizeF getCameraResolution(int camNum) {
        SizeF size = new SizeF(0, 0);
        CameraManager manager = (CameraManager) this.getActivity().getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] cameraIds = manager.getCameraIdList();
            if (cameraIds.length > camNum) {
                CameraCharacteristics character = manager.getCameraCharacteristics(cameraIds[camNum]);
                size = character.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
            }
        } catch (CameraAccessException e) {
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
        } catch (CameraAccessException e) {
            Log.e("YourLogString", e.getMessage(), e);
        }

        return 0;
    }

    public void onClickProcessor() {
        ArrayList<Point> iniPoints = getMeasurePoints();
        Point p1 = iniPoints.get(0);
        Point p2 = iniPoints.get(1);

        VideoObject videoObject = videoObjectViewModel.getCurrentVideoObject().getValue();

        path = videoObject.getThumbnailPath();
        Bitmap thumbnail = BitmapFactory.decodeFile(path);

        int w  = thumbnail.getWidth();
        int h = thumbnail.getHeight();
        Mat rgb = new Mat(h, w, CvType.CV_8UC4);
        Utils.bitmapToMat(thumbnail, rgb);
        Imgproc.cvtColor(rgb, rgb, Imgproc.COLOR_BGR2RGB);

        Imgproc.circle(rgb, p1, 1, new Scalar(0, 0, 255), 1);
        Imgproc.circle(rgb, p2, 1, new Scalar(0, 0, 255), 1);



        saveThumbnail(rgb, Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES) + "/.MeasureUp/", "lol");




//        /*
       // vp.grabFrames(false);
        vp.initializeFrames(videoView.getMats());
        vp.setInitPoints(p1, p2);
        vp.trackOpticalFlow();
        ArrayList<Point> finalPoints = vp.getFinalPoints();
        SizeF sizeF = getCameraResolution(0);
        double oFM = getFocalLength(0) / 1000;
        double ccdH = getCameraResolution(0).getWidth() / 1000;
        double[] results = vp.measurement(oFM, ccdH, refDistance, iniPoints, finalPoints, path);
//        */


        if (videoObject != null) {
            View parentView = (View) point1.getParent();
            int width = parentView.getWidth();
            int height = parentView.getHeight();

            Measurement measurement = new Measurement();
            measurement.setName("Measurement");
            measurement.setObjectId(videoObject.getId());
            measurement.setLength(results[0]);
            measurement.setX1(p1.x / width);
            measurement.setX2(p2.x / width);
            measurement.setY1(p1.y / height);
            measurement.setY2(p2.y / height);

            final Toast mToast =Toast.makeText(getActivity(),
                    String.valueOf(results[1]) + " m", Toast.LENGTH_LONG);
            int toastDuration = 15000; // in MilliSeconds
            //Toast mToast = Toast.makeText(this, "My text", Toast.LENGTH_LONG);
            CountDownTimer countDownTimer;
            countDownTimer = new CountDownTimer(toastDuration, 1000) {
                public void onTick(long millisUntilFinished) {
                    mToast.show();
                }

                public void onFinish() {
                    mToast.cancel();
                }
            };

           // mToast.show();
            countDownTimer.start();


            new MeasurementSaveDialog(this.getContext(), measurement, measurementViewModel,
                    Navigation.findNavController(this.getActivity(), R.id.fragment)).show();
        } else {
            Log.e("onClickProcessor", "videoObject is null!!!!");
        }

    }


    public void onClickProcessor2() {
        ArrayList<Point> iniPoints = getMeasurePoints();
        ArrayList<Point> finalPoints = new ArrayList<>();
        finalPoints.add(iniPoints.remove(2));
        finalPoints.add(iniPoints.remove(2));
        Point p1 = iniPoints.get(0);
        Point p2 = iniPoints.get(1);
        Point p3 = finalPoints.get(0);
        Point p4 = finalPoints.get(1);

        vp.initializeFrames(videoView.getMats());
        VideoObject videoObject = videoObjectViewModel.getCurrentVideoObject().getValue();

        Bitmap thumbnail = BitmapFactory.decodeFile(videoObject.getThumbnailPath());
//        Bitmap last = BitmapFactory.decodeFile(videoObject.getThumbnailPath().replaceAll("thumbnail", "last"));


        int w  = thumbnail.getWidth();
        int h = thumbnail.getHeight();
//        Mat rgb = new Mat(h, w, CvType.CV_8UC4);
//        Utils.bitmapToMat(thumbnail, rgb);
//        Imgproc.cvtColor(rgb, rgb, Imgproc.COLOR_BGR2RGB);
//
//        Imgproc.circle(rgb, p1, 5, new Scalar(123, 0, 255), 2);
//        Imgproc.circle(rgb, p2, 5, new Scalar(0, 0, 255), 2);
//
//        Mat rgb2 = new Mat(h, w, CvType.CV_8UC4);
//        Utils.bitmapToMat(last, rgb2);
//        Imgproc.cvtColor(rgb2, rgb2, Imgproc.COLOR_BGR2RGB);
//
//        Imgproc.circle(rgb2, p3, 5, new Scalar(123, 0, 255), 2);
//        Imgproc.circle(rgb2, p4, 5, new Scalar(0, 0, 255), 2);
//
//
//
//        saveThumbnail(rgb, Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_PICTURES) + "/.MeasureUp/", "lol");
//        saveThumbnail(rgb2, Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_PICTURES) + "/.MeasureUp/", "lol2");

        videoObject = videoObjectViewModel.getCurrentVideoObject().getValue();

        path = videoObject.getThumbnailPath();


        SizeF sizeF = getCameraResolution(0);
        double oFM = getFocalLength(0) / 1000;
        double ccdH = getCameraResolution(0).getWidth() / 1000;
        double[] results = vp.measurementNoOptical(oFM, ccdH, iniPoints, finalPoints, h, w,path, refDistance);

    //    System.out.println("dimen " + rgb.height() + " " +rgb.width() + " " + oFM + "");
//        */


        if (videoObject != null) {
            View parentView = (View) point1.getParent();
            int width = parentView.getWidth();
            int height = parentView.getHeight();

            Measurement measurement = new Measurement();
            measurement.setName("Measurement");
            measurement.setObjectId(videoObject.getId());
            measurement.setLength(results[0]);
            measurement.setX1(p1.x / width);
            measurement.setX2(p2.x / width);
            measurement.setY1(p1.y / height);
            measurement.setY2(p2.y / height);

            new MeasurementSaveDialog(this.getContext(), measurement, measurementViewModel,
                    Navigation.findNavController(this.getActivity(), R.id.fragment)).show();
        } else {
            Log.e("onClickProcessor", "videoObject is null!!!!");
        }

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        //((AppCompatActivity) this.getActivity()).getSupportActionBar().show();
    }

    public static String saveThumbnail(Mat frame, String parent, String name) {
        String path = parent + "/" + name +".jpg";
        Log.d("FILE NAME ", path);
        Imgcodecs.imwrite(path, frame);

        return path;
    }

    public static void showToastMethod(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

}