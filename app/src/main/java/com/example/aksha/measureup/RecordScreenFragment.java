package com.example.aksha.measureup;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.opengl.EGL14;
import android.opengl.EGLDisplay;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.opengles.GL10;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RecordScreenFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RecordScreenFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecordScreenFragment extends Fragment implements GLSurfaceView.Renderer {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static final String TAG = MainActivity.class.getSimpleName();


    // Rendering. The Renderers are created here, and initialized when the GL surface is created.
    private GLSurfaceView surfaceView;
    private VideoRecorder mRecorder;
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
    private static final float[] DEFAULT_COLOR = new float[] {0f, 0f, 0f, 0f};
    private boolean firstTime = false;
    private boolean last = false;
    private String currentFileName;
    private String tempFileName;
    private double initial;

    private OnFragmentInteractionListener mListener;

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
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RecordScreenFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RecordScreenFragment newInstance(String param1, String param2) {
        RecordScreenFragment fragment = new RecordScreenFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_record_screen, container, false);
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

        //for drop down menu list
        Spinner spinner = (Spinner) view.findViewById(R.id.spinner1);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getContext(),
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.pages)){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // this part is needed for hiding the original view
                View view = super.getView(position, convertView, parent);
                view.setVisibility(View.GONE);

                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setSelection(0);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long row_id) {
                if(parent.getItemAtPosition(position).toString().equals("Gallery")) {
                    Intent i = new Intent(RecordScreenFragment.this.getContext(), MainActivity.class);
                    startActivity(i);
                }

                else if(parent.getItemAtPosition(position).toString().equals("Settings")) {
                    Intent i = new Intent(RecordScreenFragment.this.getContext(), SettingsActivity.class);
                    startActivity(i);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

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

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

        EGL10 egl10 =  (EGL10)EGLContext.getEGL();
        javax.microedition.khronos.egl.EGLDisplay display = egl10.eglGetCurrentDisplay();
        int v[] = new int[2];
        egl10.eglGetConfigAttrib(display,config, EGL10.EGL_CONFIG_ID, v);

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


            draw(frame,camera.getTrackingState() == TrackingState.PAUSED,
                    viewmtx, projmtx, camera.getDisplayOrientedPose(),lightIntensity);

            if (mRecorder!= null && mRecorder.isRecording()) {
                VideoRecorder.CaptureContext ctx = mRecorder.startCapture();
                if (ctx != null) {
                    // draw again
                    draw(frame, camera.getTrackingState() == TrackingState.PAUSED,
                            viewmtx, projmtx, camera.getDisplayOrientedPose(), lightIntensity);

                    // restore the context
                    mRecorder.stopCapture(ctx, frame.getTimestamp());

                }

                if(firstTime){
                    initial = getDistance(camera);
                    firstTime = false;
                }

            }

            else if (mRecorder!= null && !mRecorder.isRecording() && last){

                //Log.d(TAG, "getDistance(camera) "+ getDistance(camera));
                double distance = Math.abs(getDistance(camera) - initial);
                result.setGravity(Gravity.CENTER);
                result.setText("Distance Moved " + Double.toString(distance ) + " cm");
                last = false;

                File distanceFile = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES) + "/MeasureUp/" + currentFileName,currentFileName + "_distance.txt");

                try {
                    PrintWriter out = new PrintWriter(distanceFile);
                    out.write(Double.toString(distance));
                    out.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

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
                Math.pow((translation[2]), 2)))* 100.0) / 100.0)* 100;
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
                    Environment.DIRECTORY_PICTURES) + "/MeasureUp/" + currentFileName,currentFileName + "_video.mp4");
            File dir = videoFile.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }

            try {
                mRecorder = new VideoRecorder(surfaceView.getWidth(),
                        surfaceView.getHeight(),
                        VideoRecorder.DEFAULT_BITRATE, videoFile);
                mRecorder.setEglConfig(mAndroidEGLConfig);
            } catch (IOException e) {
                Log.e(TAG,"Exception starting recording", e);
            }
        }
        mRecorder.toggleRecording();
        updateControls();
    }

    private void updateControls() {
        boolean recording = mRecorder != null && mRecorder.isRecording();

        RecordButtonView recordButtonView = this.getView().findViewById(R.id.recordButtonView);
        recordButtonView.setRecording(recording);
        recordButtonView.invalidate(); // redraw record button with updated state
        result = this.getView().findViewById(R.id.textView);
        result.setText("");

        if (!recording) {
            last = true;
        }
        else
            firstTime = true;
    }

    public void OnClickGalleryButton(View view) {
        // TODO
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
