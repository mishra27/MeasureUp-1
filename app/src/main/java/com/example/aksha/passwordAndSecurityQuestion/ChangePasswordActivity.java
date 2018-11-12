package com.example.aksha.passwordAndSecurityQuestion;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.aksha.measureup.R;
import com.example.aksha.measureup.SettingsActivity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class ChangePasswordActivity extends AppCompatActivity {

    EditText editText1, editText2, editText3;
    Button button;
    ImageView imageView;
    String password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        Window w = this.getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        setContentView(R.layout.activity_change_password);

        editText1 = (EditText) findViewById(R.id.editText4);
        editText2 = (EditText) findViewById(R.id.editText10);
        editText3 = (EditText) findViewById(R.id.editText11);
        button = (Button) findViewById(R.id.button11);
        imageView = (ImageView) findViewById(R.id.imageView);

        SharedPreferences settings = getSharedPreferences("PREFS", 0);
        password = settings.getString("password", "");


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = editText1.getText().toString();

                if(text.equals(password)){

                    String text1 = editText2.getText().toString();
                    String text2 = editText3.getText().toString();

                    if(text1.equals("") || text2.equals("")){
                        Toast.makeText(ChangePasswordActivity.this,"No new password entered!", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        if(text1.equals(text2)){
                            SharedPreferences settings =  getSharedPreferences("PREFS", 0);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.remove("password");
                            editor.putString("password", text1);
                            editor.apply();
                            Toast.makeText(ChangePasswordActivity.this,"Passwords successfully changed!", Toast.LENGTH_SHORT).show();
                        }

                        else{
                            Toast.makeText(ChangePasswordActivity.this,"New passwords did not match", Toast.LENGTH_SHORT).show();

                        }
                    }

                } else{
                    Toast.makeText(ChangePasswordActivity.this,"Invalid password!", Toast.LENGTH_SHORT).show();
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
