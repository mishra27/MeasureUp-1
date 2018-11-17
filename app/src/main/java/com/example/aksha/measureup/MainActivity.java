package com.example.aksha.measureup;

import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.common.helpers.TransparentNavigationHelper;
import com.example.common.helpers.CameraPermissionHelper;
import com.example.common.helpers.DisplayRotationHelper;
import com.example.common.helpers.FullScreenHelper;
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

import org.opencv.core.Point;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

/**
 * This is a simple example that shows how to create an augmented reality (AR) application using the
 * ARCore API. The application will display any detected planes and will allow the user to tap on a
 * plane to place a 3d model of the Android robot.
 */
public class MainActivity extends AppCompatActivity {
    NavController navController;
    private RelativeLayout mContainer;
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

    private File videoFile_;


    //Load OpenCv native library
    static {System.loadLibrary("opencv_java3");}

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navController = Navigation.findNavController(this, R.id.fragment);
        NavigationUI.setupActionBarWithNavController(this, navController);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    public void onClickProcessor(View view) {
        File videoFile = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES) + "/MeasureUp/" + "video", "video.mp4");
        VideoProcessor vp = new VideoProcessor(videoFile);
        // will save first and last frame and grab all frames in ArrayList<Mat>
        vp.grabFrames();
        vp.trackOpticalFlow();
        ArrayList<Point> iniPoints = vp.getInitPoints();
        ArrayList<Point> finalPoints = vp.getFinalPoints();
        double oFM = 3.57 / 1000;
        double ccdH = 5.23 / 1000;
        double refDistance = 65.0 / 100;
        double results = vp.measurement(oFM, ccdH, refDistance, iniPoints, finalPoints);
        Log.d("results: ", String.valueOf(results));
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}






