package com.example.aksha.db;


import android.content.Context;

import com.example.aksha.db.dao.MeasurementDao;
import com.example.aksha.db.dao.VideoObjectDao;
import com.example.aksha.db.models.Measurement;
import com.example.aksha.db.models.VideoObject;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {VideoObject.class, Measurement.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase INSTANCE;

    public abstract VideoObjectDao videoObjectDao();
    public abstract MeasurementDao measurementDao();

    public static AppDatabase getAppDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "measureup").build();
            }
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}
