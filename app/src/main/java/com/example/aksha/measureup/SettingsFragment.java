package com.example.aksha.measureup;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {

    Button buttonPassword, buttonQuestion;
    private String secure;
    private String question;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Spinner spinner = (Spinner) view.findViewById(R.id.spinner2);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(),
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.pages)) {
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

        // creating the position of the language in spinner from arraylist


        spinner.setSelection(2);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long row_id) {
                if (parent.getItemAtPosition(position).toString().equals("Home")) {
                    // TODO navigate to record screen fragment
                } else if (parent.getItemAtPosition(position).toString().equals("Gallery")) {
                    // TODO navigate to gallery fragment
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }

        });

        buttonPassword = (Button) view.findViewById(R.id.button2);
        buttonQuestion = (Button) view.findViewById(R.id.button10);

        SharedPreferences settings = this.getActivity().getSharedPreferences("PREFS", 0);
        secure = settings.getString("secure", "");
        question = settings.getString("question", "");


        buttonPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (secure.equals("yes")) {
                    // TODO navigate to change password fragment
                } else {
                    // TODO navigate to create password fragment
                }
            }
        });

        buttonQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (question.equals("")) {
                    Toast.makeText(SettingsFragment.this.getActivity(), "Need to create password first!", Toast.LENGTH_SHORT).show();
                } else {
                    // TODO navigate to change security question fragment
                }
            }
        });
    }
}
