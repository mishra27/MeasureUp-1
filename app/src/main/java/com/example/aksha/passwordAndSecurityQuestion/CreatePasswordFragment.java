package com.example.aksha.passwordAndSecurityQuestion;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.aksha.measureup.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

public class CreatePasswordFragment extends Fragment {

    EditText editText1, editText2;
    Button button;
    String question;
    NavController navController;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
        navController = Navigation.findNavController(this.getActivity(), R.id.fragment);
        return inflater.inflate(R.layout.fragment_create_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editText1 = (EditText) view.findViewById(R.id.editText);
        editText2 = (EditText) view.findViewById(R.id.editText2);
        button = (Button) view.findViewById(R.id.button);

        SharedPreferences settings = this.getActivity().getSharedPreferences("PREFS", 0);
        question = settings.getString("question", "");


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text1 = editText1.getText().toString();
                String text2 = editText2.getText().toString();

                if (text1.equals("") || text2.equals("")) {
                    Toast.makeText(CreatePasswordFragment.this.getActivity(), "No password entered!", Toast.LENGTH_SHORT).show();
                } else {
                    if (text1.equals(text2)) {
                        SharedPreferences settings = CreatePasswordFragment.this.getActivity().getSharedPreferences("PREFS", 0);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString("password", text1);
                        editor.putString("secure", "yes");

                        editor.apply();

                        //enter the app
                        if (question.equals("")) {
                            // if password has not been initialized yet
                            navController.navigate(R.id.action_createPasswordFragment_to_securityQuestionFragment);
                            // TODO navigate to security question fragment
                        } else {
                            navController.navigate(R.id.action_createPasswordFragment_to_recordScreenFragment);
                            // TODO navigate to record screen fragment
                        }
                    } else {
                        Toast.makeText(CreatePasswordFragment.this.getActivity(), "Passwords did not match", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}
