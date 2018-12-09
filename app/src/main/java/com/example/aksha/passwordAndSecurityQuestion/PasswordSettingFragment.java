package com.example.aksha.passwordAndSecurityQuestion;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
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


public class PasswordSettingFragment extends Fragment {
    NavController navController;
    private Button buttonEnableDisable;
    private Button changePassword;
    private String password;
    private String secure;
    SharedPreferences settings;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        settings = this.getActivity().getSharedPreferences("PREFS", 0);
        navController = Navigation.findNavController(this.getActivity(), R.id.fragment);
        return inflater.inflate(R.layout.fragment_password_setting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        buttonEnableDisable = (Button) view.findViewById(R.id.button2);
        changePassword = (Button) view.findViewById(R.id.button10);


        password = settings.getString("password", "");
        secure = settings.getString("secure", "");


        if(!secure.equals("yes")){
            buttonEnableDisable.setText("Enable Password");
            changePassword.setVisibility(View.GONE);
        }

        else{

            buttonEnableDisable.setText("Disable Password");
            changePassword.setVisibility(View.VISIBLE);
        }


        //

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                navController.navigate(R.id.action_passwordSettingdFragment_to_changePasswordFragments);


            }
        });

        buttonEnableDisable.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                if(buttonEnableDisable.getText().equals("Enable Password")) {

                    if(password.equals(""))
                        navController.navigate(R.id.action_passwordSettingdFragment_to_createPasswordFragments);

                    else{
                        SharedPreferences.Editor editor = settings.edit();
                        editor.remove("secure");
                        editor.putString("secure", "yes");
                        editor.apply();
                        Toast.makeText(PasswordSettingFragment.this.getActivity(), "Old password enabled!", Toast.LENGTH_SHORT).show();
                        changePassword.setVisibility(View.VISIBLE);
                   }


                    buttonEnableDisable.setText("Disable Password");
                }

                else if(buttonEnableDisable.getText().equals("Disable Password")) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Verify Password");

                    // Set up the input
                    final EditText input = new EditText(getContext());

                    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    builder.setView(input);

                    // Set up the buttons
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String pass = input.getText().toString();
                            if(password.equals(pass)){
                                SharedPreferences.Editor editor = settings.edit();
                                editor.remove("secure");
                                editor.putString("secure", "no");
                                editor.apply();
                                Toast.makeText(PasswordSettingFragment.this.getActivity(), "Password disabled!", Toast.LENGTH_SHORT).show();
                                buttonEnableDisable.setText("Enable Password");
                                changePassword.setVisibility(View.GONE);
                            }

                            else{
                                Toast.makeText(PasswordSettingFragment.this.getActivity(), "Invalid password! Try again", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();
                }
            }
        });


    }
}
