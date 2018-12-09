package com.example.aksha.passwordAndSecurityQuestion;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aksha.measureup.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

public class ChangeSecurityQuestionFragment extends Fragment {

    EditText editText1, editText2;
    Button button;
    TextView textView;
    String password;
    NavController navController;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        navController = Navigation.findNavController(this.getActivity(), R.id.fragment);

        return inflater.inflate(R.layout.fragment_change_security_question, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        editText1 = (EditText) view.findViewById(R.id.editText4);
        editText2 = (EditText) view.findViewById(R.id.editText11);
        textView = (TextView) view.findViewById(R.id.editText8);
        button = (Button) view.findViewById(R.id.button11);

        SharedPreferences settings = this.getActivity().getSharedPreferences("PREFS", 0);
        password = settings.getString("password", "");


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = editText1.getText().toString();


                if (text.equals(password)) {
                    String text1 = textView.getText().toString();
                    String text2 = editText2.getText().toString().toLowerCase();

                    SharedPreferences settings = ChangeSecurityQuestionFragment.this.getActivity().getSharedPreferences("PREFS", 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.remove("question");
                    editor.putString("question", text1);
                    editor.remove("answer");
                    editor.putString("answer", text2);
                    editor.apply();

                    Toast.makeText(ChangeSecurityQuestionFragment.this.getActivity(), "Security question successfully updated!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ChangeSecurityQuestionFragment.this.getActivity(), "Invalid password!", Toast.LENGTH_SHORT).show();
                }
            }
        });





    }
}
