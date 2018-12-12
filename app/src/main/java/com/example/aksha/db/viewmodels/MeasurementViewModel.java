package com.example.aksha.db.viewmodels;

import android.app.Application;

import com.example.aksha.db.models.Measurement;
import com.example.aksha.db.models.VideoObject;
import com.example.aksha.db.repositories.MeasurementRepository;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class MeasurementViewModel extends AndroidViewModel {
    private final MutableLiveData<Measurement> currentMeasurement = new MutableLiveData<>();
    private MeasurementRepository measurementRepository;

    public MeasurementViewModel(@NonNull Application application) {
        super(application);

        measurementRepository = new MeasurementRepository(application);
    }

    public void setCurrentMeasurement(Measurement measurement) {
        currentMeasurement.setValue(measurement);
    }

    public LiveData<Measurement> getCurrentMeasurement() {
        return currentMeasurement;
    }

    public LiveData<List<Measurement>> getMeasurements(VideoObject videoObject) {
        return measurementRepository.getMeasurements(videoObject);
    }

    public LiveData<List<Measurement>> getAllMeasurements() {
        return measurementRepository.getAllMeasurments();
    }

    public void insert(Measurement measurement) {
        measurementRepository.insert(measurement);
    }
}
