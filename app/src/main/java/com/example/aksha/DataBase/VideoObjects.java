package com.example.aksha.DataBase;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
/*
this class is used to create the structure of the table. Do not update without fully understanding
dataBase.

MAKE SURE TO ASK MICHAEL BEFORE MAKING CHANGES!!!!!
 */

@Entity(tableName = "objectGallery")
public class VideoObjects {
    @PrimaryKey
    @ColumnInfo(name = "video_name")
    @NonNull
    public String videoName;

    @ColumnInfo(name = "move_distance")
    public Double moveDistance;

    @ColumnInfo(name = "video_path")
    public String videoPath;

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
    public String getVideoFile(){
        return videoPath;
    }
    public void setVideoPath(String videoPath){
        this.videoPath = videoPath;
    }
}
