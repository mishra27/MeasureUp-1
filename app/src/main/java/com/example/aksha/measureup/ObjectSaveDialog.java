package com.example.aksha.measureup;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.aksha.DataBase.AppDatabase;
import com.example.aksha.DataBase.VideoObjects;

import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.AndroidUtil;
import org.jcodec.common.model.Picture;

import java.io.File;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.room.Database;

import static com.example.aksha.DataBase.AppDatabase.getAppDatabase;

public class ObjectSaveDialog extends Dialog {
    View view;
    FrameLayout dialogLayout;
    LinearLayout buttonsLayout;

    AppDatabase db;
    private NavController navController;

    private Button discardButton;
    private Button saveButton;
    //private VideoObjects videoObject;
    private String path;
    private double dist;

    public ObjectSaveDialog(@NonNull Context context, NavController navController, String path, double dist) {
        this(context);
        this.navController = navController;
      //  this.videoObject = videoObject;
        this.path = path;
        this.dist = dist;
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
        final EditText objectName = new EditText(context);
        objectName.setInputType(InputType.TYPE_CLASS_TEXT);
        objectName.setMaxLines(1);
        objectName.setHint("Enter a title for the object");
        objectName.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));

        dialogLayout.addView(objectName);


        // set up buttons within the buttonsLayout
        discardButton = new Button(context);
        saveButton = new Button(context);

        LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, 1);

        discardButton.setLayoutParams(new LinearLayout.LayoutParams(buttonLayoutParams));
        discardButton.setText("Discard");
        discardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ObjectSaveDialog.this.cancel();

                // TODO remove recorded object from memory
            }
        });

        saveButton.setLayoutParams(new LinearLayout.LayoutParams(buttonLayoutParams));
        saveButton.setText("Save");
        saveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final String newFilename = objectName.getText().toString();
                db = getAppDatabase(getContext());

                new Thread(new Runnable() {
                                        @Override
                    public void run() {
                                            Bitmap img = null;
                                            try {
                                                Picture thumbnail = FrameGrab.getFrameFromFile(new File(path), 1);
                                                img = AndroidUtil.toBitmap(thumbnail);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            } catch (JCodecException e) {
                                                e.printStackTrace();
                                            }




                       VideoObjects obj = new VideoObjects();
                        obj.setVideoName(newFilename);
                        obj.setMoveDistance(dist);
                        obj.setVideoPath(path);
                        obj.setVideoThumbnail(img);
                        db.videoObjectDao().insertAll(obj);
                    }
                }) .start();
                showOtherDialog();

                // TODO save recorded object to gallery
            }
        });

        buttonsLayout.addView(discardButton);
        buttonsLayout.addView(saveButton);

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
        discardButton.setText("No");
        discardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ObjectSaveDialog.this.cancel();
            }
        });

        saveButton.setText("Yes");
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ObjectSaveDialog.this.dismiss();

                // navigate to point selection screen
                navController.navigate(R.id.action_recordScreenFragment_to_pointSelectionFragment);
            }
        });
    }
}
