package com.example.aksha.db.repositories;

import android.app.Application;
import android.os.AsyncTask;

import com.example.aksha.db.AppDatabase;
import com.example.aksha.db.dao.MeasurementDao;
import com.example.aksha.db.models.Measurement;
import com.example.aksha.db.models.VideoObject;

import java.util.List;

import androidx.lifecycle.LiveData;

public class MeasurementRepository {
    private MeasurementDao measurementDao;
    private LiveData<List<Measurement>> allMeasurments;

    private static class MeasurementAsyncTask extends AsyncTask<Measurement, Void, Void> {
        MeasurementDao measurementDao;

        MeasurementAsyncTask(MeasurementDao measurementDao) {
            this.measurementDao = measurementDao;
        }

        @Override
        protected Void doInBackground(Measurement... measurements) {
            measurementDao.insert(measurements);
            return null;
        }
    }

    public MeasurementRepository(Application application) {
        AppDatabase db = AppDatabase.getAppDatabase(application);

        measurementDao = db.measurementDao();
        allMeasurments = measurementDao.getAllMeasurements();
    }

    public LiveData<List<Measurement>> getMeasurements(VideoObject videoObject) {
        return measurementDao.getMeasurements(videoObject.getId());
    }

    public LiveData<List<Measurement>> getAllMeasurments() {
        return allMeasurments;
    }

    public void insert(Measurement... measurements) {
        new MeasurementAsyncTask(measurementDao).execute(measurements);
    }
}
