package com.example.aksha.passwordAndSecurityQuestion;

import android.app.ActionBar;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.aksha.measureup.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SecurityQuestionFragment extends Fragment {

    EditText editText1, editText2;
    Button button;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_security_question, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editText1 = (EditText) view.findViewById(R.id.editText5);
        editText2 = (EditText) view.findViewById(R.id.editText6);
        button = (Button) view.findViewById(R.id.button4);



        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text1 = editText1.getText().toString();
                String text2 = editText2.getText().toString().toLowerCase();

                SharedPreferences settings =  SecurityQuestionFragment.this.getActivity().getSharedPreferences("PREFS", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("question", text1);
                editor.putString("answer", text2);
                editor.apply();

                // TODO navigate to record screen fragment
            }
        });
    }
}
