package com.example.aksha.db.dao;


import com.example.aksha.db.models.VideoObject;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;


// This Interface is used to abstract SQLite so it is easier to use and test. The three possible
// options are @query, @Update,@insert, and @delete. Documentation can be found online for each
// Uses for each:
// @Query: To access some data already within the database i.e. to show gallery
// @Update: To update a specific item within the dataBase i.e. to update the name of a video object,
//      To add a measurement to a video object.
// @delete: to delete a videoObject. i.e. user selects to remove video object or measurement
// @insert: to insert a new video Object. i.e. new recorded video
@Dao
public interface VideoObjectDao {

    @Query("SELECT * FROM video_objects")
    LiveData<List<VideoObject>> getAll();

    @Query("SELECT * FROM video_objects where name LIKE :videoName")
    VideoObject findByName(String videoName);

    @Query("SELECT COUNT(*) from video_objects")
    int countVideos();

    @Insert
    void insert(VideoObject videoObject);

    @Insert
    void insertAll(VideoObject... users);

    @Delete
    void delete(VideoObject user);
}
