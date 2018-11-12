package com.example.aksha.passwordAndSecurityQuestion;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.aksha.measureup.MainActivity;
import com.example.aksha.measureup.R;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class CreatePasswordActivity extends AppCompatActivity {

    EditText editText1, editText2;
    Button button;
    String question;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();


        Window w = this.getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        setContentView(R.layout.activity_create_password);


        editText1 = (EditText) findViewById(R.id.editText);
        editText2 = (EditText) findViewById(R.id.editText2);
        button = (Button) findViewById(R.id.button);

        SharedPreferences settings = getSharedPreferences("PREFS", 0);
        question = settings.getString("question", "");


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text1 = editText1.getText().toString();
                String text2 = editText2.getText().toString();

                if(text1.equals("") || text2.equals("")){
                    Toast.makeText(CreatePasswordActivity.this,"No password entered!", Toast.LENGTH_SHORT).show();
                }
                else{
                    if(text1.equals(text2)){
                        SharedPreferences settings =  getSharedPreferences("PREFS", 0);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString("password", text1);
                        editor.putString("secure", "yes");

                        editor.apply();

                        //enter the app
                        if(question.equals("")){
                            // if password has not been initialized yet
                            Intent intent = new Intent(getApplicationContext(), SecurityQuestionActivity.class);
                            startActivity(intent);
                            finish();
                        }

                        else{
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }

                    else{
                        Toast.makeText(CreatePasswordActivity.this,"Passwords did not match", Toast.LENGTH_SHORT).show();

                    }
                }

            }
        });

    }

    @Override
    public void onBackPressed() {
        // Do Here what ever you want do on back press;
    }

}
