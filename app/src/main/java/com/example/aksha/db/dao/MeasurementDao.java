package com.example.aksha.db.dao;

import com.example.aksha.db.models.Measurement;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface MeasurementDao {
    @Query("select * from measurements where object_id=:objectId")
    LiveData<List<Measurement>> getMeasurements(Integer objectId);

    @Insert
    void insert(Measurement measurement);

    @Delete
    void delete(Measurement measurement);
}
