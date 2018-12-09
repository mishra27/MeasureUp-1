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

public class ForgotPasswordFragment extends Fragment {

    EditText editText;
    Button button;
    String question, answer;
    TextView textView;
    NavController navController;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        navController = Navigation.findNavController(this.getActivity(), R.id.fragment);
        return inflater.inflate(R.layout.fragment_forgot_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final SharedPreferences settings = this.getActivity().getSharedPreferences("PREFS", 0);
        question = settings.getString("question", "");
        answer = settings.getString("answer", "");

        editText = (EditText) view.findViewById(R.id.editText7);
        button = (Button) view.findViewById(R.id.button5);
        textView = (TextView) view.findViewById(R.id.textView7);

        textView.setText(question);

        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String text = editText.getText().toString().toLowerCase();

                if (text.equals(answer)) {
                    settings.edit().remove("password");
                    navController.navigate(R.id.action_forgotPasswordFragment_to_createPasswordFragment);

                    // TODO navigate to create password fragment
                } else {
                    Toast.makeText(ForgotPasswordFragment.this.getActivity(), "Invalid answer!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
