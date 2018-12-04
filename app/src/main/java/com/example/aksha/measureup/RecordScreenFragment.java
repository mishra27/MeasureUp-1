package com.example.aksha.measureup;

import android.graphics.Bitmap;
import android.opengl.EGL14;
import android.opengl.EGLDisplay;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aksha.DataBase.VideoObject;
import com.example.aksha.videoRecorder.RecordButtonView;
import com.example.aksha.videoRecorder.VideoRecorder;
import com.example.common.helpers.CameraPermissionHelper;
import com.example.common.helpers.DisplayRotationHelper;
import com.example.common.helpers.SnackbarHelper;
import com.example.common.helpers.TapHelper;
import com.example.common.rendering.BackgroundRenderer;
import com.example.common.rendering.PlaneRenderer;
import com.example.common.rendering.PointCloudRenderer;
import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Frame;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.IntBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.opengles.GL10;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

public class RecordScreenFragment extends Fragment implements GLSurfaceView.Renderer {
    private static final String TAG = MainActivity.class.getSimpleName();


    // Rendering. The Renderers are created here, and initialized when the GL surface is created.
    private GLSurfaceView surfaceView;
    private VideoRecorder mRecorder;
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

    // Temporary matrix allocated here to reduce number of allocations for each frame.
    private final float[] anchorMatrix = new float[16];
    private static final float[] DEFAULT_COLOR = new float[]{0f, 0f, 0f, 0f};
    private boolean firstTime = false;
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
        return inflater.inflate(R.layout.fragment_record_screen, container, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        //((AppCompatActivity) this.getActivity()).getSupportActionBar().show();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        videoObjectViewModel = ViewModelProviders.of(getActivity()).get(VideoObjectViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        surfaceView = view.findViewById(R.id.surfaceview);
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        if (!CameraPermissionHelper.hasCameraPermission(this.getActivity())) {
            Toast.makeText(this.getContext(), "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                    .show();
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this.getActivity())) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(this.getActivity());
            }
            this.getActivity().finish();
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

    public static String saveThumbnail(Mat frame, String parent) {
        String path = parent + "/thumbnail.jpg";
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
        // GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (session == null) {
            return;
        }
        // Notify ARCore session that the view size changed so that
        // the perspective matrix and
        // the video background can be properly adjusted.
        displayRotationHelper.updateSessionIfNeeded(session);

        try {

            //GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

            session.setCameraTextureName(backgroundRenderer.getTextureId());

            // Obtain the current frame from ARSession. When the configuration is set to
            // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
            // camera framerate.
            Frame frame = session.update();
            Camera camera = frame.getCamera();

            // Handle one tap per frame.
            handleTap(frame, camera);

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

            // Get camera matrix and draw.
            float[] viewmtx = new float[16];
            camera.getViewMatrix(viewmtx, 0);

            // Compute lighting from average intensity of the image.
            final float lightIntensity = frame.getLightEstimate().getPixelIntensity();

            // Visualize tracked points.
            PointCloud pointCloud = frame.acquirePointCloud();
            pointCloudRenderer.update(pointCloud);


            draw(frame, camera.getTrackingState() == TrackingState.PAUSED,
                    viewmtx, projmtx, camera.getDisplayOrientedPose(), lightIntensity);

            if (mRecorder != null && mRecorder.isRecording()) {
                VideoRecorder.CaptureContext ctx = mRecorder.startCapture();
                if (ctx != null) {
                    // draw again
                    draw(frame, camera.getTrackingState() == TrackingState.PAUSED,
                            viewmtx, projmtx, camera.getDisplayOrientedPose(), lightIntensity);

                    // restore the context
                    mRecorder.stopCapture(ctx, frame.getTimestamp());

                }

                if (firstTime) {
                    // save thumbnail
                    Bitmap mBitmap = savePixels(0, 0, surfaceView.getWidth(), surfaceView.getHeight(), gl);
                    Mat newframe = getMat(mBitmap);
                    currentThumbnailPath = saveThumbnail(newframe, Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES) + "/MeasureUp/" + currentFileName);

                    initial = getDistance(camera);
                    firstTime = false;
                }
            } else if (mRecorder != null && !mRecorder.isRecording() && last) {
                double distance = Math.abs(getDistance(camera) - initial);
                result.setGravity(Gravity.CENTER);
                result.setText("Distance Moved " + Double.toString(distance) + " cm");
                last = false;

                File distanceFile = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES) + "/MeasureUp/" + "distance.txt");

                try {
                    PrintWriter out = new PrintWriter(distanceFile);
                    out.write(Double.toString(distance));
                    out.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                //Log.d(TAG, "getDistance(camera) "+ getDistance(camera));
                currentVideoDistance = Math.abs(getDistance(camera) - initial);
                result.setGravity(Gravity.CENTER);
                result.setText("Distance Moved " + Double.toString(currentVideoDistance) + " cm");

                currentVideoObject = new VideoObject(currentFileName);
                currentVideoObject.setVideoPath(currentVideoPath);
                currentVideoObject.setThumbnailPath(currentThumbnailPath);
                currentVideoObject.setMoveDistance(currentVideoDistance);
                last = false;

                Message msg = handler.obtainMessage(0, currentVideoObject);
                msg.sendToTarget();


//                File distanceFile = new File(Environment.getExternalStoragePublicDirectory(
//                        Environment.DIRECTORY_PICTURES) + "/MeasureUp/" + currentFileName,currentFileName + "_distance.txt");
//
//
//
//
//                try {
//                    PrintWriter out = new PrintWriter(distanceFile);
//                    out.write(Double.toString(currentVideoDistance));
//                    out.close();
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }

                // session.update();
            }

            // Application is responsible for releasing the point cloud resources after
            // using it.
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

    private void draw(Frame frame, boolean paused,
                      float[] viewMatrix, float[] projectionMatrix,
                      Pose displayOrientedPose, float lightIntensity) {

        // Clear screen to notify driver it should not load
        // any pixels from previous frame.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Draw background.
        backgroundRenderer.draw(frame);

        // If not tracking, don't draw 3d objects.
        if (paused) {
            return;
        }

        pointCloudRenderer.draw(viewMatrix, projectionMatrix);
    }

    // Handle only one tap per frame, as taps are usually low frequency compared to frame rate.
    private void handleTap(Frame frame, Camera camera) {

    }

    public void clickToggleRecording(View view) {
        Log.d(TAG, "clickToggleRecording");
        // mRecorder = null;
        if (mRecorder == null || !mRecorder.isRecording()) {
            Log.d(TAG, "HERE");
            currentFileName = "Object-" + Long.toHexString(System.currentTimeMillis());
            File videoFile = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES) + "/MeasureUp/" + currentFileName, currentFileName + "_video.mp4");
            File dir = videoFile.getParentFile();
            currentVideoPath = videoFile.getPath();

            try (PrintWriter out = new PrintWriter(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES) + "/MeasureUp/filePath.txt")) {
                out.println(currentVideoPath);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            if (!dir.exists()) {
                dir.mkdirs();
            }

            try {
                mRecorder = new VideoRecorder(surfaceView.getWidth(),
                        surfaceView.getHeight(),
                        VideoRecorder.DEFAULT_BITRATE, videoFile);
                mRecorder.setEglConfig(mAndroidEGLConfig);
            } catch (IOException e) {
                Log.e(TAG, "Exception starting recording", e);
            }
        }

        mRecorder.toggleRecording();
        updateControls();
    }

    private void onVideoCaptured() {
        videoObjectViewModel.setCurrentVideoObject(currentVideoObject);

        new ObjectSaveDialog(this.getContext(), currentVideoObject, videoObjectViewModel,
                Navigation.findNavController(this.getActivity(), R.id.fragment)).show();
    }

    private void updateControls() {
        boolean recording = mRecorder != null && mRecorder.isRecording();

        RecordButtonView recordButtonView = this.getView().findViewById(R.id.recordButtonView);
        recordButtonView.setRecording(recording);
        result = this.getView().findViewById(R.id.textView);
        result.setText("");

        if (!recording) {
            last = true;
        } else {
            firstTime = true;
        }
    }


}
