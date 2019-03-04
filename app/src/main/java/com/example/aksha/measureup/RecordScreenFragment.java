package com.example.aksha.measureup;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.opengl.EGL14;
import android.opengl.EGLDisplay;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.aksha.db.models.VideoObject;
import com.example.aksha.db.viewmodels.VideoObjectViewModel;
import com.example.common.helpers.CameraPermissionHelper;
import com.example.common.helpers.DisplayRotationHelper;
import com.example.common.helpers.SnackbarHelper;
import com.example.common.helpers.TapHelper;
import com.example.common.rendering.BackgroundRenderer;
import com.example.common.rendering.PlaneRenderer;
import com.example.common.rendering.PointCloudRenderer;
import com.google.android.material.snackbar.Snackbar;
import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.CameraIntrinsics;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.IntBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.opengles.GL10;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import static android.app.Activity.RESULT_OK;

public class RecordScreenFragment extends Fragment implements GLSurfaceView.Renderer  {
    private static final String TAG = MainActivity.class.getSimpleName();


    // Rendering. The Renderers are created here, and initialized when the GL surface is created.
    private GLSurfaceView surfaceView;
    private VideoProcessor videoProcessor;
    private android.opengl.EGLConfig mAndroidEGLConfig;

    private boolean installRequested;

    private Session session;
    private final SnackbarHelper messageSnackbarHelper = new SnackbarHelper();
    private DisplayRotationHelper displayRotationHelper;
    private TapHelper tapHelper;

    private final BackgroundRenderer backgroundRenderer = new BackgroundRenderer();

    private final PlaneRenderer planeRenderer = new PlaneRenderer();
    private final PointCloudRenderer pointCloudRenderer = new PointCloudRenderer();

    private TextView result;
    private TextView liveResult;



    // Temporary matrix allocated here to reduce number of allocations for each frame.
    private final float[] anchorMatrix = new float[16];
    private static final float[] DEFAULT_COLOR = new float[]{0f, 0f, 0f, 0f};
    private boolean firstTime = true;
    private boolean last = false;
    private double initial;

    private Handler handler;
    private VideoObjectViewModel videoObjectViewModel;

    // data associated with the captured video object
    private String currentFileName;
    private VideoObject currentVideoObject;
    private String currentVideoPath;
    private String currentThumbnailPath;
    private double currentVideoDistance;
    private static final int REQUEST_CODE = 1000;
    private int mScreenDensity;
    private MediaProjectionManager mProjectionManage;
    private static int DISPLAY_WIDTH = 720;
    private static int DISPLAY_HEIGHT = 1280;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjectionCallback mMediaProjectionCallback;
    private static ToggleButton mToggleButton;
    private static Switch focusSwitch;
    private static CheckBox mCheckBox;
    private static ImageButton galleryOption;
    private static ImageButton settingsOption;
    private MediaRecorder mMediaRecorder;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final int REQUEST_PERMISSIONS = 10;


    double initialCameraX = 0.0;
    double initialCameraY = 0.0;
    double initialCameraZ = 0.0;


    double finalCameraX = 0.0;
    double finalCameraY = 0.0;
    double finalCameraZ = 0.0;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private MediaProjectionManager mProjectionManager;
    private double finalDist;
    private float[] camera1;
    private float[] camera2;
    private ArrayList<Bitmap> listMap;

    // Anchors created from taps used for object placing with a given color.
    private static class ColoredAnchor {
        public final Anchor anchor;
        public final float[] color;

        public ColoredAnchor(Anchor a, float[] color4f) {
            this.anchor = a;
            this.color = color4f;
        }
    }

    private final ArrayList<ColoredAnchor> anchors = new ArrayList<>();

    public RecordScreenFragment() {
        // Required empty public constructor
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                onVideoCaptured();
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ((AppCompatActivity) this.getActivity()).getSupportActionBar().hide();
        //((AppCompatActivity) this.getActivity()).get.hide();

        View view = inflater.inflate(R.layout.fragment_record_screen, container, false);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        //((AppCompatActivity) this.getActivity()).getSupportActionBar().show();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



//        t.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        videoObjectViewModel = ViewModelProviders.of(getActivity()).get(VideoObjectViewModel.class);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        surfaceView = view.findViewById(R.id.surfaceview);
        result = view.findViewById(R.id.dist);
        liveResult = view.findViewById(R.id.liveResult);
        displayRotationHelper = new DisplayRotationHelper(/*context=*/ view.getContext());

        // Set up tap listener.
        tapHelper = new TapHelper(/*context=*/ view.getContext());
        surfaceView.setOnTouchListener(tapHelper);

        // Set up renderer.
        surfaceView.setPreserveEGLContextOnPause(true);
        surfaceView.setEGLContextClientVersion(2);
        surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0); // Alpha used for plane blending.
        surfaceView.setRenderer(this);
        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        view.findViewById(R.id.recordButtonView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickToggleRecording(v);
            }
        });

        view.findViewById(R.id.imageButton).setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_recordScreenFragment_to_settingsFragment, null));
        view.findViewById(R.id.imageButton2).setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_recordScreenFragment_to_galleryFragment, null));
        installRequested = false;

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;

        mMediaRecorder = new MediaRecorder();

        mProjectionManager = (MediaProjectionManager) getActivity().getSystemService
                (getContext().MEDIA_PROJECTION_SERVICE);

        mToggleButton = (ToggleButton) getView().findViewById(R.id.ToggleButton);
        mToggleButton.setVisibility(View.INVISIBLE);

        focusSwitch = (Switch) getView().findViewById(R.id.focusSwitch);

        mCheckBox = (CheckBox) getView().findViewById(R.id.checkBox);
        galleryOption = getView().findViewById(R.id.imageButton);
        settingsOption = getView().findViewById(R.id.imageButton2);
        mToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) + ContextCompat
                        .checkSelfPermission(getContext(),
                                Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale
                            (getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                            ActivityCompat.shouldShowRequestPermissionRationale
                                    (getActivity(), Manifest.permission.RECORD_AUDIO)) {
                        mToggleButton.setChecked(false);
                        Snackbar.make(getView().findViewById(android.R.id.content),R.string.loading ,
                                Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ActivityCompat.requestPermissions(getActivity(),
                                                new String[]{Manifest.permission
                                                        .WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                                                REQUEST_PERMISSIONS);
                                        mToggleButton.setChecked(false);
                                    }
                                }).show();
                    } else {
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission
                                        .WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                                REQUEST_PERMISSIONS);
                    }
                } else {
                    if(!mCheckBox.isChecked()) {
                        Toast.makeText(getActivity(),
                                "Please check the box first!", Toast.LENGTH_SHORT).show();
                        mToggleButton.setChecked(false);
                        return;
                    }
                    onToggleScreenShare(v);
                }
            }
        });

        mCheckBox.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                result.setText("");
                if(!mCheckBox.isChecked()){
                    mMediaRecorder.reset();
                    stopScreenSharing();

                    String objectFolder = currentVideoPath.substring(0,58);
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
                }

                else {
                    currentFileName = "Object-" + Long.toHexString(System.currentTimeMillis());
                    File videoFile = new File(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES) + "/.MeasureUp/" + currentFileName, currentFileName + "_video.mp4");
                    File dir = videoFile.getParentFile();
                    currentVideoPath = videoFile.getPath();



                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    initRecorder(currentVideoPath);
                    shareScreen();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if (session == null) {
            Exception exception = null;
            String message = null;
            try {
                switch (ArCoreApk.getInstance().requestInstall(this.getActivity(), !installRequested)) {
                    case INSTALL_REQUESTED:
                        installRequested = true;
                        return;
                    case INSTALLED:
                        break;
                }

                // ARCore requires camera permissions to operate. If we did not yet obtain runtime
                // permission on Android M and above, now is a good time to ask the user for it.
                if (!CameraPermissionHelper.hasCameraPermission(this.getActivity())) {
                    CameraPermissionHelper.requestCameraPermission(this.getActivity());
                    return;
                }

                // Create the session.
                session = new Session(/* context= */ this.getContext());
                focusSwitch.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View view) {

                            //session = new Session(/* context= */ this.getContext());
                            Config config = new Config(session);
                            if (focusSwitch.isChecked()) {
                                config.setFocusMode(Config.FocusMode.AUTO);
                            } else {
                                config.setFocusMode(Config.FocusMode.FIXED);
                            }
                            session.configure(config);
                    }
                });



            } catch (UnavailableArcoreNotInstalledException
                    | UnavailableUserDeclinedInstallationException e) {
                message = "Please install ARCore";
                exception = e;
            } catch (UnavailableApkTooOldException e) {
                message = "Please update ARCore";
                exception = e;
            } catch (UnavailableSdkTooOldException e) {
                message = "Please update this app";
                exception = e;
            } catch (UnavailableDeviceNotCompatibleException e) {
                message = "This device does not support AR";
                exception = e;
            } catch (Exception e) {
                message = "Failed to create AR session";
                exception = e;
            }

            if (message != null) {
                messageSnackbarHelper.showError(this.getActivity(), message);
                Log.e(TAG, "Exception creating session", exception);
                return;
            }
        }

        // Note that order matters - see the note in onPause(), the reverse applies here.
        try {
            session.resume();
        } catch (CameraNotAvailableException e) {
            // In some cases (such as another camera app launching) the camera may be given to
            // a different app instead. Handle this properly by showing a message and recreate the
            // session at the next iteration.
            messageSnackbarHelper.showError(this.getActivity(), "Camera not available. Please restart the app.");
            session = null;
            return;
        }



        surfaceView.onResume();
        displayRotationHelper.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (session != null) {
            // Note that the order matters - GLSurfaceView is paused first so that it does not try
            // to query the session. If Session is paused before GLSurfaceView, GLSurfaceView may
            // still call session.update() and get a SessionPausedException.
            displayRotationHelper.onPause();
            surfaceView.onPause();
            session.pause();
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

        EGL10 egl10 = (EGL10) EGLContext.getEGL();
        javax.microedition.khronos.egl.EGLDisplay display = egl10.eglGetCurrentDisplay();
        int v[] = new int[2];
        egl10.eglGetConfigAttrib(display, config, EGL10.EGL_CONFIG_ID, v);

        EGLDisplay androidDisplay = EGL14.eglGetCurrentDisplay();
        int attribs[] = {EGL14.EGL_CONFIG_ID, v[0], EGL14.EGL_NONE};
        android.opengl.EGLConfig myConfig[] = new android.opengl.EGLConfig[1];
        EGL14.eglChooseConfig(androidDisplay, attribs, 0, myConfig, 0, 1, v, 1);
        this.mAndroidEGLConfig = myConfig[0];

        // Prepare the rendering objects. This involves reading shaders, so may throw an IOException.
        try {
            // Create the texture and pass it to ARCore session to be filled during update().
            backgroundRenderer.createOnGlThread(/*context=*/ this.getContext());
            planeRenderer.createOnGlThread(/*context=*/ this.getContext(), "models/trigrid.png");
            pointCloudRenderer.createOnGlThread(/*context=*/ this.getContext());


        } catch (IOException e) {
            Log.e(TAG, "Failed to read an asset file", e);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        displayRotationHelper.onSurfaceChanged(width, height);
        GLES20.glViewport(0, 0, width, height);
    }

    public static String saveThumbnail(Mat frame, String parent, String name) {
        String path = parent + "/" + name +".jpg";
        Log.d("FILE NAME ", path);
        Imgcodecs.imwrite(path, frame);

        return path;
    }

    private Mat getMat(Bitmap mBitmap) {
        int w  = surfaceView.getWidth();
        int h = surfaceView.getHeight();
        Mat rgb = new Mat(h, w, CvType.CV_8UC4);
        Utils.bitmapToMat(mBitmap, rgb);
        Imgproc.cvtColor(rgb, rgb, Imgproc.COLOR_BGR2RGB);

        return rgb;
    }

    public static Bitmap savePixels(int x, int y, int w, int h, GL10 gl)
    {
        int b[]=new int[w*h];
        int bt[]=new int[w*h];
        IntBuffer ib=IntBuffer.wrap(b);
        ib.position(0);
        gl.glReadPixels(x, y, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, ib);

        /*  remember, that OpenGL bitmap is incompatible with
        Android bitmap and so, some correction need.
        */

        for(int i=0; i<h; i++)
        {
            for(int j=0; j<w; j++)
            {
                int pix=b[i*w+j];
                int pb=(pix>>16)&0xff;
                int pr=(pix<<16)&0x00ff0000;
                int pix1=(pix&0xff00ff00) | pr | pb;
                bt[(h-i-1)*w+j]=pix1;
            }
        }

        Bitmap sb = Bitmap.createBitmap(bt, w, h,  Bitmap.Config.ARGB_8888);
        return sb;

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // draw(gl);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (session == null) {
            return;
        }
        // Notify ARCore session that the view size changed so that
        // the perspective matrix and
        // the video background can be properly adjusted.
        displayRotationHelper.updateSessionIfNeeded(session);

        try {

            session.setCameraTextureName(backgroundRenderer.getTextureId());

            // Obtain the current frame from ARSession. When the configuration is set to
            // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
            // camera framerate.
            Frame frame = session.update();
            Camera camera = frame.getCamera();

            // Handle one tap per frame.
           // handleTap(frame, camera);

            // Draw background.
            backgroundRenderer.draw(frame);

            // Obtain the current frame from ARSession. When the
            //configuration is set to
            // UpdateMode.BLOCKING (it is by default), this will
            // throttle the rendering to the camera framerate.
//                Frame frame = session.update();
//
//                Camera camera = frame.getCamera();

            // Handle taps. Handling only one tap per frame, as taps are
            // usually low frequency compared to frame rate.


            // Get projection matrix.
            float[] projmtx = new float[16];
            camera.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f);
              //          Log.d("Projection ", Arrays.toString(projmtx));




            // Get camera matrix and draw.
            float[] viewmtx = new float[16];
            camera.getViewMatrix(viewmtx, 0);

            //Log.d("Projection ", Arrays.toString(viewmtx));


            // Compute lighting from average intensity of the image.
            final float lightIntensity = frame.getLightEstimate().getPixelIntensity();

            // Visualize tracked points.
            PointCloud pointCloud = frame.acquirePointCloud();



//            draw(frame, camera.getTrackingState() == TrackingState.PAUSED,
//                    viewmtx, projmtx, camera.getDisplayOrientedPose(), lightIntensity);
           

            if ( mToggleButton.isChecked()) {


                if (firstTime && mToggleButton.isChecked()) {
                    Log.d("asda", "popopop ");
                    firstTime =  false;

                    initialCameraX = camera.getPose().tx();
                    initialCameraY = camera.getPose().ty();
                    initialCameraZ = camera.getPose().tz();
                    // save thumbnail


                    Bitmap mBitmap = savePixels(0, 0, surfaceView.getWidth(), surfaceView.getHeight(), gl);
                    Mat newframe = getMat(mBitmap);
                    currentThumbnailPath = saveThumbnail(newframe, Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES) + "/.MeasureUp/" + currentFileName, "thumbnail");


                    initial = getDistance(camera);
                    camera1 = new float[16];
                    camera.getDisplayOrientedPose().inverse().toMatrix(camera1, 0);



//                    CameraIntrinsics cameraIntrinsics = camera.getImageIntrinsics();
//                    float[] f = cameraIntrinsics.getFocalLength();
//                    int[] p = cameraIntrinsics.getImageDimensions();
//                     for(int i = 0; i< f.length; i++){
//                         System.out.println("FOCAL" + f[i]);
//                     }
//
//                    for(int i = 0; i< p.length; i++){
//                        System.out.println("Princ " + p[i]);
//                    }

                    //camera.getViewMatrix(camera1, 0);


                }
            } else if (last) {
                finalCameraX = camera.getPose().tx();
                finalCameraY = camera.getPose().ty();
                finalCameraZ = camera.getPose().tz();

                double distance = Math.sqrt(Math.pow((initialCameraX-finalCameraX), 2) +
                        Math.pow((initialCameraY-finalCameraY), 2) +
                        Math.pow((initialCameraZ-finalCameraZ), 2));

                finalDist = Math.round(distance * 100.0) / 100.0;
                //double distance = Math.abs(getDistance(camera) - initial);

                result.setText("Distance Moved " + Double.toString(distance*100) + " cm");
                last = false;
                firstTime = true;
                initial = 0;

               // finalDist = distance;
                camera2 = new float[16];
                camera.getDisplayOrientedPose().inverse().toMatrix(camera2, 0);
                //camera.getViewMatrix(camera2, 0);

                currentVideoObject = new VideoObject(currentFileName);
                currentVideoObject.setVideoPath(currentVideoPath);
                currentVideoObject.setThumbnailPath(currentThumbnailPath);
                currentVideoObject.setMoveDistance(finalDist*100);

//                Bitmap mBitmap = savePixels(0, 0, surfaceView.getWidth(), surfaceView.getHeight(), gl);
//                Mat newframe = getMat(mBitmap);
//                currentThumbnailPath = saveThumbnail(newframe, Environment.getExternalStoragePublicDirectory(
//                        Environment.DIRECTORY_PICTURES) + "/.MeasureUp/" + currentFileName, "lastFrame");

                try (PrintWriter out = new PrintWriter(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES) + "/.MeasureUp/" + currentFileName+ "/matrix.txt")) {
                    //out.println(currentVideoPath);
                    out.println(Arrays.toString(camera1));
                    out.println(Arrays.toString(camera2));

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                Bitmap mBitmap = savePixels(0, 0, surfaceView.getWidth(), surfaceView.getHeight(), gl);
                Mat newframe = getMat(mBitmap);
                saveThumbnail(newframe, Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES) + "/.MeasureUp/" + currentFileName, "last");

                //last = false;

                Log.d("Distances ", Double.toString(finalDist) );


                Message msg = handler.obtainMessage(0, currentVideoObject);
                msg.sendToTarget();
                // session.update();
            }

            double d = Math.abs(getDistance(camera) - initial);
            //  result.setGravity(Gravity.CENTER);
            Log.d("DIST", String.valueOf(d));

//            if(mCheckBox.isChecked()) {
//                liveResult.setVisibility(View.VISIBLE);
//                liveResult.setText(String.valueOf(d));
//
//            }
//
//            else
//                liveResult.setVisibility(View.INVISIBLE);
//

            // Application is responsible for releasing the point cloud resources after
            // using it.
            pointCloudRenderer.update(pointCloud);
            pointCloudRenderer.draw(viewmtx, projmtx);
            pointCloud.release();

        } catch (Throwable t) {
            // Avoid crashing the application due to unhandled exceptions.
            Log.e(TAG, "Exception on the OpenGL thread", t);
        }
    }

    private double getDistance(Camera camera) {
        float[] translation = camera.getPose().getTranslation();
        return (Math.round((Math.sqrt(Math.pow((translation[0]), 2) +
                Math.pow((translation[1]), 2) +
                Math.pow((translation[2]), 2))) * 100.0) / 100.0) * 100;
    }

//    private void draw(Frame frame, boolean paused,
//                      float[] viewMatrix, float[] projectionMatrix,
//                      Pose displayOrientedPose, float lightIntensity) {
//
//        // Clear screen to notify driver it should not load
//        // any pixels from previous frame.
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
//
//        // Draw background.
//        backgroundRenderer.draw(frame);
//
//        // If not tracking, don't draw 3d objects.
//        if (paused) {
//            return;
//        }
//
//        pointCloudRenderer.draw(viewMatrix, projectionMatrix);
//    }



    public void clickToggleRecording(View view) {
        Log.d(TAG, "clickToggleRecording");
        // mRecorder = null;
        //updateControls();
    }

    public void onToggleScreenShare(View view) {
        if (((ToggleButton) view).isChecked()) {
            mMediaRecorder.start();
        } else {

            last = true;


            mMediaRecorder.reset();
            stopScreenSharing();
            Log.v(TAG, "Stopping Recording");
            mCheckBox.setChecked(false);
            //onVideoCaptured();
        }
    }

    private void onVideoCaptured() {
        videoObjectViewModel.setCurrentVideoObject(currentVideoObject);

        new ObjectSaveDialog(this.getContext(), currentVideoObject, videoObjectViewModel,
                Navigation.findNavController(this.getActivity(), R.id.fragment)).show();
//        result = this.getView().findViewById(R.id.textView10);

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_CODE) {
            Log.e(TAG, "Unknown request code: " + requestCode);
            return;
        }
        if (resultCode != RESULT_OK) {
            Toast.makeText(getActivity(),
                    "Screen Cast Permission Denied", Toast.LENGTH_SHORT).show();
            mToggleButton.setChecked(false);

            return;
        }
        mMediaProjectionCallback = new MediaProjectionCallback();
        mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
        mMediaProjection.registerCallback(mMediaProjectionCallback, null);
        mVirtualDisplay = createVirtualDisplay();

        if(mToggleButton.isChecked())
        mMediaRecorder.start();
    }



    private void shareScreen() {
        if (mMediaProjection == null) {
            startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
            return;
        }
        mVirtualDisplay = createVirtualDisplay();
//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            public void run() {
//                mMediaRecorder.start();
//            }
//        }, 1000);
    }

    private VirtualDisplay createVirtualDisplay() {
        return mMediaProjection.createVirtualDisplay("MainActivity",
                DISPLAY_WIDTH, DISPLAY_HEIGHT, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mMediaRecorder.getSurface(), null /*Callbacks*/, null
                /*Handler*/);
    }

    private void initRecorder(String currentVideoPath) {
        String time = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        DISPLAY_WIDTH = surfaceView.getWidth();
        DISPLAY_HEIGHT = surfaceView.getHeight();
        try {
//            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setOutputFile(currentVideoPath);
            mMediaRecorder.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
//            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mMediaRecorder.setVideoEncodingBitRate(15000000);
            mMediaRecorder.setVideoFrameRate(30);
            int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
            int orientation = ORIENTATIONS.get(rotation + 90);
            mMediaRecorder.setOrientationHint(orientation);
            mMediaRecorder.prepare();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class MediaProjectionCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            if (mToggleButton.isChecked()) {
                mToggleButton.setChecked(false);
                mMediaRecorder.stop();
                mMediaRecorder.reset();
                Log.v(TAG, "Recording Stopped");
            }
            mMediaProjection = null;
            stopScreenSharing();
        }
    }

    private void stopScreenSharing() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        //mMediaRecorder.release(); //If used: mMediaRecorder object cannot
        // be reused again
        destroyMediaProjection();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyMediaProjection();
    }

    private void destroyMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.unregisterCallback(mMediaProjectionCallback);
            mMediaProjection.stop();
            mMediaProjection = null;
        }
        Log.i(TAG, "MediaProjection Stopped");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {

        if (!CameraPermissionHelper.hasCameraPermission(getActivity())) {
            Toast.makeText(this.getContext(), "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                    .show();
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this.getActivity())) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(getActivity());
            }
            this.getActivity().finish();
        }
        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                if ((grantResults.length > 0) && (grantResults[0] +
                        grantResults[1]) == PackageManager.PERMISSION_GRANTED) {
                    onToggleScreenShare(mToggleButton);
                } else {
                    mToggleButton.setChecked(false);
                    Snackbar.make(getView().findViewById(android.R.id.content), R.string.loading,
                            Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                                    intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                    startActivity(intent);
                                }
                            }).show();
                }
                return;
            }
        }
    }

    public static void myOnKeyDown(int key_code){
        if(key_code == KeyEvent.KEYCODE_VOLUME_DOWN && mToggleButton.isChecked()){
            mToggleButton.performClick();
            mCheckBox.setVisibility(View.VISIBLE);
            galleryOption.setVisibility(View.VISIBLE);
            settingsOption.setVisibility(View.VISIBLE);
            focusSwitch.setVisibility(View.VISIBLE);

        }
        else if(key_code == KeyEvent.KEYCODE_VOLUME_DOWN && mCheckBox.isChecked()){
            mCheckBox.setVisibility(View.INVISIBLE);
            galleryOption.setVisibility(View.INVISIBLE);
            settingsOption.setVisibility(View.INVISIBLE);
            focusSwitch.setVisibility(View.INVISIBLE);
            if(mCheckBox.getVisibility() == View.INVISIBLE)
            mToggleButton.performClick();
        }


        //do whatever you want here
    }

}
