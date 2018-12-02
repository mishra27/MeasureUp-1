package com.example.aksha.DataBase;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
/*
this class is used to create the structure of the table. Do not update without fully understanding
dataBase.

MAKE SURE TO ASK MICHAEL BEFORE MAKING CHANGES!!!!!
 */

@Entity(tableName = "video_objects")
public class VideoObject {
    @PrimaryKey
    @ColumnInfo(name = "video_name")
    @NonNull
    private String videoName;

    @ColumnInfo(name = "move_distance")
    private Double moveDistance;

    @ColumnInfo(name = "video_path")
    private String videoPath;

    public VideoObject(String videoName) {
        this.setVideoName(videoName);
    }

//    @ColumnInfo(name = "video_thumbnail")
//    public Bitmap videoThumbnail;
//
//
//    public void setVideoThumbnail(Bitmap Thumbnail){
//        this.videoThumbnail = Thumbnail;
//    }
//    public Bitmap getVideoThumbnail(){
//
//        return videoThumbnail;
//    }
    public void setVideoName(String videoName){
        this.videoName = videoName;
    }
    public String getVideoName(){
        return videoName;
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
