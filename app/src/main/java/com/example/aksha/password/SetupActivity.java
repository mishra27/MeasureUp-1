package com.example.aksha.password;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.aksha.measureup.MainActivity;
import com.example.aksha.measureup.R;
import com.example.common.helpers.TransparentNavigationHelper;

public class SetupActivity extends AppCompatActivity {

    Button button, button2, button3;
    //String secure;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        setContentView(R.layout.activity_setup);

        button = (Button) findViewById(R.id.button7);
        button2 = (Button) findViewById(R.id.button8);
        button3 = (Button) findViewById(R.id.button9);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                        SharedPreferences settings =  getSharedPreferences("PREFS", 0);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString("secure", "yes");
                        editor.apply();

                        //enter the app

                            // if password has not been initialized yet
                            Intent intent = new Intent(getApplicationContext(), CreatePasswordActivity.class);
                            startActivity(intent);
                            finish();

                }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences settings =  getSharedPreferences("PREFS", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("secure", "no");
                editor.apply();

                //enter the app

                // if password has not been initialized yet
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();

            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences settings =  getSharedPreferences("PREFS", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("secure", "later");
                editor.apply();

                //enter the app
                // if password has not been initialized yet
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();

            }
        });



    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        TransparentNavigationHelper.setFullScreenOnWindowFocusChanged(this, hasFocus);
    }
}
