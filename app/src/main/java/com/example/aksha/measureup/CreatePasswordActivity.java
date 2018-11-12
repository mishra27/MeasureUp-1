package com.example.aksha.measureup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CreatePasswordActivity extends AppCompatActivity {

    EditText editText1, editText2;
    Button button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        setContentView(R.layout.activity_create_password);


        editText1 = (EditText) findViewById(R.id.editText);
        editText2 = (EditText) findViewById(R.id.editText2);
        button = (Button) findViewById(R.id.button);

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
                        editor.apply();

                        //enter the app

                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            finish();

                    }

                    else{
                        Toast.makeText(CreatePasswordActivity.this,"Passwords did not match", Toast.LENGTH_SHORT).show();

                    }
                }

            }
        });

    }
}
