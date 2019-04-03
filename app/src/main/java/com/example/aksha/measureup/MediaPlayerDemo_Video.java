package com.example.aksha.measureup;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;

import java.io.File;

import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import com.example.aksha.measureup.CustomVideoView;

public class MediaPlayerDemo_Video extends AppCompatActivity {

    private CustomVideoView videoView = null;
    private RelativeLayout relativeLayout = null;
    private int width;
    private int height;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getSupportActionBar().hide();

        setContentView(R.layout.activity_media_player_demo__video);

        relativeLayout = (RelativeLayout) findViewById(R.id.main_relative_layout);
       // getScreenResolution(this);

        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        width = metrics.widthPixels;
        height = metrics.heightPixels;
        System.out.println(" HEIGHT "+ height + " X "+ width);



        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                width, height);

        videoView = new CustomVideoView(this,Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES) + "/.MeasureUp/video.mp4", height, width);

        relativeLayout.addView(videoView, params);

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // https://developer.android.com/training/system-ui/immersive.html#sticky
            Window w = this.getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            w.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }

}
