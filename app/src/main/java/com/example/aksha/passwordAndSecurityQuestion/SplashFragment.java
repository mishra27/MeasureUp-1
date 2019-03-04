package com.example.aksha.passwordAndSecurityQuestion;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.aksha.measureup.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

public class SplashFragment extends Fragment {

    NavController navController;
    String password, secure;
    private String answer;
    private String optical;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        navController = Navigation.findNavController(this.getActivity(), R.id.fragment);
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences settings = this.getActivity().getSharedPreferences("PREFS", 0);
        password = settings.getString("password", "");
        secure = settings.getString("secure", "");
        answer = settings.getString("answer", "");
        optical = settings.getString("optical","");

        if(optical.length() == 0){
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("optical", "no");
            editor.apply();
        }


        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(secure.equals("later") || secure.equals("") || (answer.equals("") && !secure.equals("no") ) ){
                    // if password has not been initialized yet
                    navController.navigate(R.id.action_splashFragment_to_setupFragment);

                    // TODO navigate to setup fragment
                }

                else if(secure.equals("no")){
                    // if password has not been initialized yet
                    navController.navigate(R.id.action_splashFragment_to_recordScreenFragment);

                    // TODO navigate to record screen fragment
                }

                else if( password.equals("") ){
                    navController.navigate(R.id.action_splashFragment_to_createPasswordFragment);
                    // if password has not been initialized yet
                    // TODO navigate to create password fragment
                }
                else{

                    navController.navigate(R.id.action_splashFragment_to_enterPasswordFragment);
                    // TODO navigate to enter password fragment
                }

            }
        }, 2000);
    }
}

