package com.example.aksha.measureup;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

public class SettingsFragment extends Fragment {

    Button buttonPassword, buttonQuestion;
    private String secure;
    private String question;
    NavController navController;
    Switch optical;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        navController = Navigation.findNavController(this.getActivity(), R.id.fragment);

        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ((AppCompatActivity) this.getActivity()).getSupportActionBar().show();

        super.onViewCreated(view, savedInstanceState);

        buttonPassword = view.findViewById(R.id.buttonPass);
        buttonQuestion = view.findViewById(R.id.buttonSec);
        optical = view.findViewById(R.id.optical);

        SharedPreferences settings = this.getActivity().getSharedPreferences("PREFS", 0);
        secure = settings.getString("secure", "");
        question = settings.getString("question", "");


        optical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences settings = SettingsFragment.this.getActivity().getSharedPreferences("PREFS", 0);
                SharedPreferences.Editor editor = settings.edit();

                if(optical.isChecked()) {

                    editor.remove("optical");
                    editor.putString("optical", "yes");
                    editor.apply();
                }

                else{
                    editor.remove("optical");
                    editor.putString("optical", "no");
                    editor.apply();
                }


            }
        });

        buttonPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                navController.navigate(R.id.action_settingsFragment_to_PasswordSettingsFragment);

//                if (secure.equals("yes")) {
//                    navController.navigate(R.id.action_settingsFragment_to_changePasswordFragment);
//
//                    // TODO navigate to change password fragment
//                } else {
//                    navController.navigate(R.id.action_settingsFragment_to_createPasswordFragment);
//
//                    // TODO navigate to create password fragment
//                }
            }
        });

        buttonQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (question.equals("")) {
                    Toast.makeText(SettingsFragment.this.getActivity(), "Need to create password first!", Toast.LENGTH_SHORT).show();
                } else {
                    navController.navigate(R.id.action_settingsFragment_to_changeSecurityQuestionFragment);

                    // TODO navigate to change security question fragment
                }
            }
        });
    }
}
