package com.example.aksha.db.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "video_objects")
public class VideoObject {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "name")
    @NonNull
    private String name;

    @ColumnInfo(name = "thumbnail_path")
    private String thumbnailPath;

    @ColumnInfo(name = "move_distance")
    private Double moveDistance;

    @ColumnInfo(name = "video_path")
    private String videoPath;

    public VideoObject(String name) {
        this.setName(name);
    }

    public void setId(int id) {
        this.id = id;
    }
    public int getId() {
        return this.id;
    }
    public void setName(String name){
        this.name = name;
    }
    public String getName(){
        return name;
    }
    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }
    public String getThumbnailPath() {
        return this.thumbnailPath;
    }
    public Double getMoveDistance(){
        return moveDistance;
    }
    public void setMoveDistance(Double moveDistance){
        this.moveDistance = moveDistance;
    }
    public void setVideoPath(String videoPath){
        this.videoPath = videoPath;
    }
    public String getVideoPath() {
        return this.videoPath;
    }
}
