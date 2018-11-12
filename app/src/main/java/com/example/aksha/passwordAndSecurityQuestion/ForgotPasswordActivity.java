package com.example.aksha.passwordAndSecurityQuestion;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aksha.measureup.R;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordActivity extends AppCompatActivity {

    EditText editText;
    Button button;
    String question, answer;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        setContentView(R.layout.activity_forgot_password);

        final SharedPreferences settings = getSharedPreferences("PREFS", 0);
        question = settings.getString("question", "");
        answer = settings.getString("answer", "");

        editText = (EditText) findViewById(R.id.editText7);
        button = (Button) findViewById(R.id.button5);
        textView = (TextView) findViewById(R.id.textView7);

        textView.setText(question);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = editText.getText().toString().toLowerCase();

                if(text.equals(answer)){
                    settings.edit().remove("password");
                    Intent intent = new Intent(getApplicationContext(), CreatePasswordActivity.class);
                    startActivity(intent);
                    finish();
                } else{
                    Toast.makeText(ForgotPasswordActivity.this,"Invalid answer!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Do Here what ever you want do on back press;
    }
}
