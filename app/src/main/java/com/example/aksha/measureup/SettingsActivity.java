package com.example.aksha.measureup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.aksha.password.ChangePasswordActivity;
import com.example.aksha.password.ChangeSecurityQuestionActivity;
import com.example.aksha.password.CreatePasswordActivity;
import com.example.aksha.password.EnterPasswordActivity;
import com.example.aksha.password.ForgotPasswordActivity;

public class SettingsActivity extends AppCompatActivity {

    Button buttonPassword, buttonQuestion;
    private String secure;
    private String question;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();



        Window w = this.getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        setContentView(R.layout.activity_settings);

        Spinner spinner = (Spinner) findViewById(R.id.spinner2);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(SettingsActivity.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.pages)){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // this part is needed for hiding the original view
                View view = super.getView(position, convertView, parent);
                view.setVisibility(View.GONE);

                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        Configuration conf = getResources().getConfiguration();

        //creating the position of the language in spinner from arraylist


        spinner.setSelection(2);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long row_id) {
                if(parent.getItemAtPosition(position).toString().equals("Home")) {
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(i);
                }

                else if(parent.getItemAtPosition(position).toString().equals("Gallery")) {
                    Intent i = new Intent(getApplicationContext(), GalleryViewActivity.class);
                    startActivity(i);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }

        });

        buttonPassword = (Button) findViewById(R.id.button2);
        buttonQuestion = (Button) findViewById(R.id.button10);

        SharedPreferences settings = getSharedPreferences("PREFS", 0);
        secure = settings.getString("secure", "");
        question = settings.getString("question", "");



        buttonPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(secure.equals("yes")){
                    Intent intent = new Intent(getApplicationContext(), ChangePasswordActivity.class);
                    startActivity(intent);
                    finish();
                } else{
                    Intent intent = new Intent(getApplicationContext(), CreatePasswordActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        buttonQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(question.equals("")){
                    Toast.makeText(SettingsActivity.this,"Need to create password first!", Toast.LENGTH_SHORT).show();
                } else{
                    Intent intent = new Intent(getApplicationContext(), ChangeSecurityQuestionActivity.class);
                    startActivity(intent);
                    finish();
                }


            }
        });



    }

    @Override
    public void onBackPressed() {
        // Do Here what ever you want do on back press;
    }


}
