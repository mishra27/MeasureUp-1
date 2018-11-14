package com.example.aksha.passwordAndSecurityQuestion;

import android.app.ActionBar;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.aksha.measureup.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SetupFragment extends Fragment {

    Button button, button2, button3;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_setup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        button = (Button) view.findViewById(R.id.button7);
        button2 = (Button) view.findViewById(R.id.button8);
        button3 = (Button) view.findViewById(R.id.button9);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences settings = SetupFragment.this.getActivity().getSharedPreferences("PREFS", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("secure", "yes");
                editor.apply();

                //enter the app

                // if password has not been initialized yet
                // TODO navigate to create password fragment

            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences settings =  SetupFragment.this.getActivity().getSharedPreferences("PREFS", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("secure", "no");
                editor.apply();

                //enter the app

                // if password has not been initialized yet
                // TODO navigate to record screen fragment

            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences settings =  SetupFragment.this.getActivity().getSharedPreferences("PREFS", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("secure", "later");
                editor.apply();

                //enter the app
                // if password has not been initialized yet
                // TODO navigate to record screen fragment

            }
        });
    }
}

