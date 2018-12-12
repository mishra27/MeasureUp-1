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

import com.example.aksha.db.models.Measurement;
import com.example.aksha.db.viewmodels.MeasurementViewModel;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;

public class MeasurementSaveDialog extends Dialog {
    View view;
    FrameLayout dialogLayout;
    LinearLayout buttonsLayout;

    Measurement measurement;
    MeasurementViewModel measurementViewModel;
    NavController navController;

    public MeasurementSaveDialog(@NonNull Context context, Measurement measurement, MeasurementViewModel measurementViewModel, NavController navController) {
        this(context);
        this.measurement = measurement;
        this.measurementViewModel = measurementViewModel;
        this.navController = navController;

        init(context);
    }

    public MeasurementSaveDialog(@NonNull Context context) {
        super(context);
    }

    private void init(@NonNull Context context) {
        this.setCanceledOnTouchOutside(false);
        this.setCancelable(false);

        LayoutInflater inflater = this.getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_object_save, null);
        dialogLayout = view.findViewById(R.id.dialogLayout);
        buttonsLayout = view.findViewById(R.id.buttonsLayout);

        ((TextView) view.findViewById(R.id.textView10)).setText("Measurement: " + measurement.getLength() + " m");

        Button discard = new Button(context);
        Button save = new Button(context);

        LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, 1);

        final EditText measurementName = new EditText(context);
        measurementName.setInputType(InputType.TYPE_CLASS_TEXT);
        measurementName.setMaxLines(1);
        measurementName.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
        measurementName.setHint("Enter a name for the measurement");

        discard.setText("Discard");
        discard.setLayoutParams(new LinearLayout.LayoutParams(buttonLayoutParams));
        discard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MeasurementSaveDialog.this.cancel();
            }
        });

        save.setText("Save");
        save.setLayoutParams(new LinearLayout.LayoutParams(buttonLayoutParams));
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = measurementName.getText().toString();

                if (name.isEmpty()) return;

                measurement.setName(name);
                measurementViewModel.insert(measurement);

                MeasurementSaveDialog.this.dismiss();
                navController.navigate(R.id.action_pointSelectionFragment_to_objectDetailsFragment);
            }
        });

        dialogLayout.addView(measurementName);

        buttonsLayout.addView(discard);
        buttonsLayout.addView(save);

        this.setContentView(view);

        // have dialog take up as much width as possible
        this.getWindow().getAttributes().width = WindowManager.LayoutParams.MATCH_PARENT;
    }
}
