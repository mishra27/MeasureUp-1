package com.example.aksha.measureup;

import android.app.Application;

import com.example.aksha.DataBase.VideoObject;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class VideoObjectViewModel extends AndroidViewModel {
    private VideoObjectRepository repository;
    private LiveData<List<VideoObject>> videoObjects;

    public VideoObjectViewModel(@NonNull Application application) {
        super(application);

        repository = new VideoObjectRepository(application);
        videoObjects = repository.getAllVideoObjects();
    }

    LiveData<List<VideoObject>> getAllVideoObjects() {
        return videoObjects;
    }

    public void insert(VideoObject videoObject) {
        repository.insert(videoObject);
    }
}
