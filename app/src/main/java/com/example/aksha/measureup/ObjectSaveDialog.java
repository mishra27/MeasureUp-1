package com.example.aksha.measureup;

import android.app.Dialog;
import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.aksha.DataBase.VideoObject;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;

public class ObjectSaveDialog extends Dialog {
    View view;
    FrameLayout dialogLayout;
    LinearLayout buttonsLayout;

    private VideoObject videoObject;
    private VideoObjectViewModel videoObjectViewModel;

    private NavController navController;

    private EditText objectName;
    private Button cancelButton;
    private Button dismissButton;

    public ObjectSaveDialog(@NonNull Context context, VideoObject videoObject, VideoObjectViewModel videoObjectViewModel, NavController navController) {
        this(context);
        this.videoObject = videoObject;
        this.videoObjectViewModel = videoObjectViewModel;
        this.navController = navController;
    }

    public ObjectSaveDialog(@NonNull Context context) {
        super(context);

        this.setCanceledOnTouchOutside(false);
        this.setCancelable(false);

        LayoutInflater inflater = this.getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_object_save, null);
        dialogLayout = view.findViewById(R.id.dialogLayout);
        buttonsLayout = view.findViewById(R.id.buttonsLayout);

        // set up contents of the dialog
        objectName = new EditText(context);
        objectName.setInputType(InputType.TYPE_CLASS_TEXT);
        objectName.setMaxLines(1);
        objectName.setHint("Enter a title for the object");
        objectName.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));

        dialogLayout.addView(objectName);


        // set up buttons within the buttonsLayout
        cancelButton = new Button(context);
        dismissButton = new Button(context);

        LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, 1);

        cancelButton.setLayoutParams(new LinearLayout.LayoutParams(buttonLayoutParams));
        cancelButton.setText("Discard");
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ObjectSaveDialog.this.cancel();

                // TODO remove recorded object from memory
            }
        });

        dismissButton.setLayoutParams(new LinearLayout.LayoutParams(buttonLayoutParams));
        dismissButton.setText("Save");
        dismissButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!objectName.getText().toString().isEmpty()) {
                    showOtherDialog();
                    // TODO save recorded object to gallery
                    videoObject.setVideoName(objectName.getText().toString());
                    videoObjectViewModel.insert(videoObject);
                }
            }
        });

        buttonsLayout.addView(cancelButton);
        buttonsLayout.addView(dismissButton);

        this.setContentView(view);

        // have dialog take up as much width as possible
        this.getWindow().getAttributes().width = WindowManager.LayoutParams.MATCH_PARENT;
    }

    private void showOtherDialog() {
        // modify dialog layout
        dialogLayout.removeAllViewsInLayout();

        TextView textView = new TextView(getContext());
        textView.setText("Your object has been saved in the gallery!\nWould you like to measure it?");
        textView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));

        dialogLayout.addView(textView);

        // modify buttons
        cancelButton.setText("No");
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ObjectSaveDialog.this.cancel();
            }
        });

        dismissButton.setText("Yes");
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ObjectSaveDialog.this.dismiss();

                // navigate to point selection screen
                navController.navigate(R.id.action_recordScreenFragment_to_pointSelectionFragment);
            }
        });
    }
}
