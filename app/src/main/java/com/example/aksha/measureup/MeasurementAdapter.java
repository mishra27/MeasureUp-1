package com.example.aksha.measureup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.aksha.db.models.Measurement;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MeasurementAdapter extends RecyclerView.Adapter<MeasurementAdapter.MeasurementHolder> {
    class MeasurementHolder extends RecyclerView.ViewHolder {
        public MeasurementHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    private LayoutInflater inflater;
    private List<Measurement> measurements;

    MeasurementAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public MeasurementHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View measurementView = inflater.inflate(R.layout.measurement_layout, parent, false);
        return new MeasurementHolder(measurementView);
    }

    @Override
    public void onBindViewHolder(@NonNull MeasurementHolder holder, int position) {
        if (measurements != null) {
            Measurement measurement = measurements.get(position);

            TextView name = holder.itemView.findViewById(R.id.name);
            TextView length = holder.itemView.findViewById(R.id.length);

            name.setText(measurement.getName());
            length.setText(measurement.getLength() + " m");
        } else {

        }
    }

    public void setMeasurements(List<Measurement> measurements) {
        this.measurements = measurements;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return measurements != null ? measurements.size() : 0;
    }
}
