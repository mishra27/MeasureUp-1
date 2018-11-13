package com.example.aksha.measureup;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class VideoProcessor {
    //
    private File videoFile_;

    // params for ShiTomasi corner detection
    private Map<String, Double> feature_params = new HashMap<String, Double>();

    // params for lucas kanade optical flow
    private Map<String, String> lk_params = new HashMap<String, String>();

    // opencv files
    private double frameHeight_;
    private double frameWidth_;
    private ArrayList<Mat> frames_;
    private Mat firstFrame_;
    private Mat lastFrame_;

    private int numOfFrame_;
    MediaMetadataRetriever mmr_;

    public VideoProcessor(File videoFile) {

        videoFile_ = videoFile;

        mmr_ = new MediaMetadataRetriever();
        mmr_.setDataSource(videoFile.getAbsolutePath());

        // params for ShiTomasi corner detection
        feature_params.put("maxCorners", 100.0);
        feature_params.put("qualityLevel", 0.3);
        feature_params.put("minDistance", 7.0);
        feature_params.put("blockSize", 7.0);

    }

    public void grabFrames() {
        frameGrabber(200000, frames_, videoFile_);
    }

    public void frameGrabber(long timeLapseUs, ArrayList<Mat> frames, File videoFile) {

        // grab the first frame info to construct Mat
        Bitmap firstFrame = mmr_.getFrameAtTime(0);
        int width = firstFrame.getWidth();
        int height = firstFrame.getHeight();
        long videoLengthUs = videoFile.length(); // TODO need to test the unit
        numOfFrame_ = 0;

        // loop over to grab frame every timeLapseUs
        for (long tl=0; tl<videoLengthUs; tl=tl+timeLapseUs) {
            Mat newFrame = new Mat(height, width, CvType.CV_32S);
            grabFrameAsMat(tl, newFrame, videoFile);
            frames.add(newFrame);
            numOfFrame_++;
        }
        // setFirstFrame
        firstFrame_ = frames_.get(0);
        saveFirstFrame();
        // setLastFrame
        lastFrame_ =  frames_.get(frames.size()-1);
        saveLastFrame();


//        Mat gray = new Mat(height, width, CvType.CV_32S);
//        gray.put(0, 0, graycale);
//        String path = Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_PICTURES) + "/MeasureUp/";
//        Imgcodecs.imwrite(path + "/gray.jpg", gray);
    }

    public void grabFrameAsMat (long timeStamp, Mat frame, File videoFile) {

        Bitmap currentFrame = mmr_.getFrameAtTime(timeStamp);
        int width = currentFrame.getWidth();
        int height = currentFrame.getHeight();
        int[] rawPixels = new int[width*height];
        currentFrame.getPixels(rawPixels, 0, width, 0, 0, width, height);
        int[] R = new int[rawPixels.length];
        int[] G = new int[rawPixels.length];
        int[] B = new int[rawPixels.length];
        int[] graycale = new int[rawPixels.length];
        for (int i=0; i<rawPixels.length; i++) {
            R[i] = (rawPixels[i] >> 16) & 0xff;
            G[i] = (rawPixels[i] >> 8) & 0xff;
            B[i] = rawPixels[i] & 0xff;
            graycale[i] = (R[i] + G[i] + B[i])/3;
        }
        frame.put(0, 0, graycale);

    }

    public void findCorners(Mat inputFrame, MatOfPoint corners) {

    }

    // get properties
    public ArrayList<Mat> getFrames() {
        return frames_;
    }

    public int getNumOfFrame() {
        return numOfFrame_;
    }

    public Mat getFirstFrame() {
        return firstFrame_;
    }

    public Mat getLastFrame() {
        return  lastFrame_;
    }

    public void saveFirstFrame() {
        String path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES) + "/MeasureUp/";
        Imgcodecs.imwrite(path + "/first.jpg", firstFrame_);
    }

    public void saveLastFrame() {
        String path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES) + "/MeasureUp/";
        Imgcodecs.imwrite(path + "/last.jpg", lastFrame_);
    }

}
