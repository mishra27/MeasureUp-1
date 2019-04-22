package com.example.aksha.measureup;

import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.example.aksha.db.viewmodels.VideoObjectViewModel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

/**
 * This is a simple example that shows how to create an augmented reality (AR) application using the
 * ARCore API. The application will display any detected planes and will allow the user to tap on a
 * plane to place a 3d model of the Android robot.
 */
public class MainActivity extends AppCompatActivity {
    NavController navController;

    VideoObjectViewModel videoObjectViewModel;

    // Load OpenCv native library
    static {System.loadLibrary("opencv_java3");


        }



        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        videoObjectViewModel = ViewModelProviders.of(this).get(VideoObjectViewModel.class);


        navController = Navigation.findNavController(this, R.id.fragment);
        NavigationUI.setupActionBarWithNavController(this, navController);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // https://developer.android.com/training/system-ui/immersive.html#sticky
//            Window w = this.getWindow();
//            w.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//            this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            w.getDecorView().setSystemUiVisibility(  View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
//

            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
       }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
    public interface OnFragmentInteractionListener{
        public void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onBackPressed() {
        //this is only needed if you have specific things
        //that you want to do when the user presses the back button.
        /* your specific things...*/

        if(getSupportActionBar().getTitle().equals("Details"))
            navController.navigate(R.id.action_objectDetailsFragment_to_galleryFragment);

        else if(getSupportActionBar().getTitle().equals("Gallery"))
            navController.navigate(R.id.action_galleryFragment_to_recordScreenFragment);

        else if(getSupportActionBar().getTitle().equals("Main")){}

        else if(getSupportActionBar().getTitle().equals("Password"))
            navController.navigate(R.id.action_changePasswordFragment_to_passwordSettingsFragment);

        else if(getSupportActionBar().getTitle().equals("Security Question"))
            navController.navigate(R.id.action_changeSecurityQuestionFragment_to_settingsFragment);

        else if(getSupportActionBar().getTitle().equals("Settings"))
            navController.navigate(R.id.action_settingsFragment_to_recordScreenFragment);

        else if(getSupportActionBar().getTitle().equals("Password Settings"))
            navController.navigate(R.id.action_passwordSettingdFragment_to_settingsFragments);

        else
            super.onBackPressed();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(getSupportActionBar().getTitle().equals("Details"))
                    navController.navigate(R.id.action_objectDetailsFragment_to_galleryFragment);

                else if(getSupportActionBar().getTitle().equals("Gallery"))
                    navController.navigate(R.id.action_galleryFragment_to_recordScreenFragment);

                else if(getSupportActionBar().getTitle().equals("Password"))
                    navController.navigate(R.id.action_changePasswordFragment_to_passwordSettingsFragment);

                else if(getSupportActionBar().getTitle().equals("Security Question"))
                    navController.navigate(R.id.action_changeSecurityQuestionFragment_to_settingsFragment);

                else if(getSupportActionBar().getTitle().equals("Settings"))
                    navController.navigate(R.id.action_settingsFragment_to_recordScreenFragment);

                else if(getSupportActionBar().getTitle().equals("Password Settings"))
                    navController.navigate(R.id.action_passwordSettingdFragment_to_settingsFragments);

                break;
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
           RecordScreenFragment.myOnKeyDown(keyCode);

           return true;
            //and so on...
        }
        return super.onKeyDown(keyCode, event);
    }
}







