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

public class ChangePasswordFragment extends Fragment {

    EditText editText1, editText2, editText3;
    Button button;
    String password;
    NavController navController;

//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        navController = Navigation.findNavController(this.getActivity(), R.id.fragment);

        return inflater.inflate(R.layout.fragment_change_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editText1 = (EditText) view.findViewById(R.id.editText4);
        editText2 = (EditText) view.findViewById(R.id.editText10);
        editText3 = (EditText) view.findViewById(R.id.editText11);
        button = (Button) view.findViewById(R.id.button11);

        SharedPreferences settings = this.getActivity().getSharedPreferences("PREFS", 0);
        password = settings.getString("password", "");


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = editText1.getText().toString();

                if(text.equals(password)){

                    String text1 = editText2.getText().toString();
                    String text2 = editText3.getText().toString();

                    if(text1.equals("") || text2.equals("")){
                        Toast.makeText(ChangePasswordFragment.this.getActivity(), "No new password entered!", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        if(text1.equals(text2)){
                            SharedPreferences settings =  ChangePasswordFragment.this.getActivity().getSharedPreferences("PREFS", 0);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.remove("password");
                            editor.putString("password", text1);
                            editor.apply();
                            Toast.makeText(ChangePasswordFragment.this.getActivity(),"Passwords successfully changed!", Toast.LENGTH_SHORT).show();
                        }

                        else{
                            Toast.makeText(ChangePasswordFragment.this.getActivity(),"New passwords did not match", Toast.LENGTH_SHORT).show();
                        }
                    }

                } else{
                    Toast.makeText(ChangePasswordFragment.this.getActivity(),"Invalid password!", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }
}
