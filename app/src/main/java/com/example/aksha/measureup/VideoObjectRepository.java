package com.example.aksha.measureup;

import android.app.Application;
import android.os.AsyncTask;

import com.example.aksha.DataBase.AppDatabase;
import com.example.aksha.DataBase.VideoObjectDao;
import com.example.aksha.DataBase.VideoObject;

import java.util.List;

import androidx.lifecycle.LiveData;

public class VideoObjectRepository {
    private VideoObjectDao videoObjectDao;
    private LiveData<List<VideoObject>> allVideoObjects;

    private static class VideoObjectAsyncTask extends AsyncTask<VideoObject, Void, Void> {
        VideoObjectDao videoObjectDao;

        VideoObjectAsyncTask(VideoObjectDao videoObjectDao) {
            this.videoObjectDao = videoObjectDao;
        }

        @Override
        protected Void doInBackground(VideoObject... videoObjects) {
            videoObjectDao.insert(videoObjects[0]);
            return null;
        }
    }

    VideoObjectRepository(Application application) {
        AppDatabase db = AppDatabase.getAppDatabase(application);

        videoObjectDao = db.videoObjectDao();
        allVideoObjects = videoObjectDao.getAll();
    }

    LiveData<List<VideoObject>> getAllVideoObjects() {
        return allVideoObjects;
    }

    public void insert(VideoObject videoObject) {
        new VideoObjectAsyncTask(videoObjectDao).execute(videoObject);
    }
}
