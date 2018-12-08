package com.example.aksha.db.dao;

import com.example.aksha.db.models.Measurement;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface MeasurementDao {
    @Query("select * from measurements where object_id=:objectId")
    LiveData<List<Measurement>> getMeasurements(int objectId);

    @Query("select * from measurements")
    LiveData<List<Measurement>> getAllMeasurements();

    @Insert
    void insert(Measurement... measurement);


}
