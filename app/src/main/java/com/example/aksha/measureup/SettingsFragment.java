package com.example.aksha.measureup;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
        ((AppCompatActivity) this.getActivity()).getSupportActionBar().show();

        super.onViewCreated(view, savedInstanceState);

        buttonPassword = view.findViewById(R.id.button2);
        buttonQuestion = view.findViewById(R.id.button10);

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
