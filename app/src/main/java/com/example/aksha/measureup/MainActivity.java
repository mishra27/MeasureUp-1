package com.example.aksha.measureup;

import android.app.ActionBar;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.example.common.helpers.TransparentNavigationHelper;

import androidx.appcompat.app.AppCompatActivity;

/**
 * This is a simple example that shows how to create an augmented reality (AR) application using the
 * ARCore API. The application will display any detected planes and will allow the user to tap on a
 * plane to place a 3d model of the Android robot.
 */
public class MainActivity extends AppCompatActivity implements RecordScreenFragment.OnFragmentInteractionListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        TransparentNavigationHelper.setFullScreenOnWindowFocusChanged(this, hasFocus);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}






