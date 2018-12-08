package com.example.aksha.db.viewmodels;

import android.app.Application;

import com.example.aksha.db.models.VideoObject;
import com.example.aksha.db.repositories.VideoObjectRepository;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class VideoObjectViewModel extends AndroidViewModel {
    private final MutableLiveData<VideoObject> currentVideoObject = new MutableLiveData<>();
    private VideoObjectRepository repository;
    private LiveData<List<VideoObject>> videoObjects;

    public VideoObjectViewModel(@NonNull Application application) {
        super(application);

        repository = new VideoObjectRepository(application);
        videoObjects = repository.getAllVideoObjects();
    }

    public void setCurrentVideoObject(VideoObject currentVideoObject) {
        this.currentVideoObject.setValue(currentVideoObject);
    }

    public LiveData<VideoObject> getCurrentVideoObject() {
        return currentVideoObject;
    }

    public LiveData<List<VideoObject>> getAllVideoObjects() {
        return videoObjects;
    }

    public void insert(VideoObject videoObject) {
        repository.insert(videoObject);
    }
    public void delete(VideoObject videoObject) {
        repository.delete(videoObject);
    }
}
