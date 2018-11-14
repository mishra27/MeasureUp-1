package com.example.aksha.passwordAndSecurityQuestion;

import android.app.ActionBar;
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

public class EnterPasswordFragment extends Fragment {

    EditText editText;
    Button button, button2;
    String password;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_enter_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences settings = this.getActivity().getSharedPreferences("PREFS", 0);
        password = settings.getString("password", "");


        editText = (EditText) view.findViewById(R.id.editText3);
        button = (Button) view.findViewById(R.id.button3);
        button2 = (Button) view.findViewById(R.id.button6);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = editText.getText().toString();

                if(text.equals(password)){
                    // TODO navigate to record screen fragment
                } else{
                    Toast.makeText(EnterPasswordFragment.this.getActivity(),"Invalid password!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // TODO button2 navigate to forgot password fragment
    }
}
