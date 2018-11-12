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

public class EnterPasswordActivity extends AppCompatActivity {

    EditText editText;
    Button button, button2;

    String password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        setContentView(R.layout.activity_enter_password);

        SharedPreferences settings = getSharedPreferences("PREFS", 0);
        password = settings.getString("password", "");


        editText = (EditText) findViewById(R.id.editText3);
        button = (Button) findViewById(R.id.button3);
        button2 = (Button) findViewById(R.id.button6);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = editText.getText().toString();

                if(text.equals(password)){
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                } else{
                    Toast.makeText(EnterPasswordActivity.this,"Invalid password!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    Intent intent = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
                    startActivity(intent);
                    finish();

            }
        });
    }
}
