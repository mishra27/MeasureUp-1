package com.example.aksha.password;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aksha.measureup.R;
import com.example.aksha.measureup.SettingsActivity;

public class ChangeSecurityQuestionActivity extends AppCompatActivity {

    EditText editText1, editText2;
    Button button;
    ImageView imageView;
    TextView textView;
    String password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        Window w = this.getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        setContentView(R.layout.activity_change_security_question);

        imageView = (ImageView) findViewById(R.id.imageView2);
        editText1 = (EditText) findViewById(R.id.editText4);
        editText2 = (EditText) findViewById(R.id.editText11);
        textView = (TextView) findViewById(R.id.editText8);
        button = (Button) findViewById(R.id.button11);

        SharedPreferences settings = getSharedPreferences("PREFS", 0);
        password = settings.getString("password", "");


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = editText1.getText().toString();


                if(text.equals(password)){
                    String text1 = textView.getText().toString();
                    String text2 = editText2.getText().toString().toLowerCase();

                    SharedPreferences settings =  getSharedPreferences("PREFS", 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.remove("question");
                    editor.putString("question", text1);
                    editor.remove("answer");
                    editor.putString("answer", text2);
                    editor.apply();

                    Toast.makeText(ChangeSecurityQuestionActivity.this,"Security question successfully updated!", Toast.LENGTH_SHORT).show();
                } else{
                    Toast.makeText(ChangeSecurityQuestionActivity.this,"Invalid password!", Toast.LENGTH_SHORT).show();
                }


            }
        });


        imageView.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Do Here what ever you want do on back press;
    }
}
