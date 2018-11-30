package com.example.aksha.DataBase;


import java.util.List;

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
public interface videoObjectDao {

    @Query("SELECT * FROM objectGallery")
    List<VideoObjects> getAll();

    @Query("SELECT video_Name, video_path FROM objectGallery")
    List<VideoObjects> getAllG();

    @Query("SELECT * FROM objectGallery where video_Name LIKE  :videoName")
    VideoObjects findByName(String videoName);

    @Query("SELECT COUNT(*) from objectGallery")
    int countVideos();

    @Insert
    void insertAll(VideoObjects... users);

    @Delete
    void delete(VideoObjects user);
}
