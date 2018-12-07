package com.example.aksha.db.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "measurements")
public class Measurement {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    @ForeignKey(entity = VideoObject.class, parentColumns = "id", childColumns = "id", onDelete = CASCADE)
    @ColumnInfo(name = "object_id")
    private int objectId;

    @ColumnInfo(name = "name")
    @NonNull
    private String name;

    @ColumnInfo(name = "length")
    private Double length;

    @ColumnInfo(name = "x1")
    @NonNull
    private Double x1;

    @ColumnInfo(name = "y1")
    @NonNull
    private Double y1;

    @ColumnInfo(name = "x2")
    @NonNull
    private Double x2;

    @ColumnInfo(name = "y2")
    @NonNull
    private Double y2;

    @Ignore
    private VideoObject videoObject;

    public Measurement() {
        name = "";
    }

    public void setId(int id) {
        this.id = id;
    }
    public int getId() {
        return this.id;
    }

    public int getObjectId() {
        return objectId;
    }

    public void setObjectId(int objectId) {
        this.objectId = objectId;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public Double getLength() {
        return length;
    }

    public void setLength(Double length) {
        this.length = length;
    }

    @NonNull
    public Double getX1() {
        return x1;
    }

    public void setX1(@NonNull Double x1) {
        this.x1 = x1;
    }

    @NonNull
    public Double getY1() {
        return y1;
    }

    public void setY1(@NonNull Double y1) {
        this.y1 = y1;
    }

    @NonNull
    public Double getX2() {
        return x2;
    }

    public void setX2(@NonNull Double x2) {
        this.x2 = x2;
    }

    @NonNull
    public Double getY2() {
        return y2;
    }

    public void setY2(@NonNull Double y2) {
        this.y2 = y2;
    }
}
