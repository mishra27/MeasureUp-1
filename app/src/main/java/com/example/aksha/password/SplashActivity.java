package com.example.aksha.password;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.aksha.measureup.MainActivity;
import com.example.aksha.measureup.R;
import com.example.aksha.password.CreatePasswordActivity;
import com.example.aksha.password.EnterPasswordActivity;
import com.example.common.helpers.TransparentNavigationHelper;

public class SplashActivity extends AppCompatActivity {

    String password, secure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        setContentView(R.layout.activity_splash);

        SharedPreferences settings = getSharedPreferences("PREFS", 0);
        password = settings.getString("password", "");
        secure = settings.getString("secure", "");

        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(secure.equals("later") || secure.equals("")){
                    // if password has not been initialized yet
                    Intent intent = new Intent(getApplicationContext(), SetupActivity.class);
                    startActivity(intent);
                    finish();
                }

                else if(secure.equals("no")){
                    // if password has not been initialized yet
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }

                else if( password.equals("")){
                    // if password has not been initialized yet
                    Intent intent = new Intent(getApplicationContext(), CreatePasswordActivity.class);
                    startActivity(intent);
                    finish();
                }
                else{
                    Intent intent = new Intent(getApplicationContext(), EnterPasswordActivity.class);
                    startActivity(intent);
                    finish();
                }

            }
        }, 2000);
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        TransparentNavigationHelper.setFullScreenOnWindowFocusChanged(this, hasFocus);
    }
}

