package com.example.aksha.password;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.aksha.measureup.MainActivity;
import com.example.aksha.measureup.R;
import com.example.common.helpers.TransparentNavigationHelper;

public class SecurityQuestionActivity extends AppCompatActivity {

    EditText editText1, editText2;
    Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        setContentView(R.layout.activity_security_question);

        editText1 = (EditText) findViewById(R.id.editText5);
        editText2 = (EditText) findViewById(R.id.editText6);
        button = (Button) findViewById(R.id.button4);



        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text1 = editText1.getText().toString();
                String text2 = editText2.getText().toString().toLowerCase();

                SharedPreferences settings =  getSharedPreferences("PREFS", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("question", text1);
                editor.putString("answer", text2);
                editor.apply();

                //enter the app

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

    @Override
    public void onBackPressed() {
        // Do Here what ever you want do on back press;
    }
}
