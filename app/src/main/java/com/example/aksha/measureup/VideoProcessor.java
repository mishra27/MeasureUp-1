package com.example.aksha.measureup;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class VideoProcessor {
    static {System.loadLibrary("opencv_java3");}

    // params for ShiTomasi corner detection
    private Map<String, Double> feature_params = new HashMap<String, Double>();

    // params for lucas kanade optical flow
    private Map<String, String> lk_params = new HashMap<String, String>();

    // opencv files
    private VideoCapture video_;
    private double numOfFrames_;
    private double frameHeight_;
    private double frameWidth_;
    private Mat sampleImg_;
    private ArrayList<Mat> frames_;
    private Mat firstFrame_;
    private Mat lastFrame_;

    public VideoProcessor() {
        // params for ShiTomasi corner detection
        feature_params.put("maxCorners", 100.0);
        feature_params.put("qualityLevel", 0.3);
        feature_params.put("minDistance", 7.0);
        feature_params.put("blockSize", 7.0);



        video_ = new VideoCapture(); // path
        numOfFrames_ = video_.get(Videoio.CAP_PROP_FRAME_COUNT);
        frameHeight_ = video_.get(Videoio.CAP_PROP_FRAME_HEIGHT);
        frameWidth_ = video_.get(Videoio.CAP_PROP_FRAME_WIDTH);

        firstFrame_ = new Mat((int)frameHeight_, (int)frameWidth_, CvType.CV_8UC3); // check img type
        lastFrame_ = new Mat((int)frameHeight_, (int)frameWidth_, CvType.CV_8UC3);

        video_.set(Videoio.CAP_PROP_POS_FRAMES, 1);
        video_.read(firstFrame_);
        video_.set(Videoio.CAP_PROP_POS_FRAMES, numOfFrames_);
        video_.read(lastFrame_);
    }

    public void findCorners(Mat inputframe, MatOfPoint corners) {
        Mat gray = new Mat((int)frameHeight_, (int)frameWidth_, CvType.CV_8U);
        Imgproc.cvtColor(inputframe, gray, Imgproc.COLOR_RGB2GRAY);

    }
}
