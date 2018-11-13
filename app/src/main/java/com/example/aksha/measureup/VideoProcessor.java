package com.example.aksha.measureup;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.media.MediaMetadataRetriever.OPTION_CLOSEST;

public class VideoProcessor {
    //
    private File videoFile_;

    // params for ShiTomasi corner detection
    private Map<String, Double> feature_params = new HashMap<String, Double>();

    // params for lucas kanade optical flow
    private Map<String, String> lk_params = new HashMap<String, String>();
    private TermCriteria tc_ = new TermCriteria(TermCriteria.COUNT+ TermCriteria.EPS, 10, 0.03);
    private Size winSize_;
    private int maxLevel_;

    // opencv files
    private double frameHeight_;
    private double frameWidth_;
    private ArrayList<Mat> frames_ = new ArrayList<Mat>() ;
    private Mat firstFrame_;
    private Mat lastFrame_;
    private Point firstPoint_;
    private Point secondPoint_;
    private MatOfPoint firstCorners_;
    private MatOfPoint secondCorners_;
    private MatOfPoint2f initPts1_;
    private MatOfPoint2f initPts2_;
    private Point firstOutPoint_;
    private Point secondOutPoint_;
    private MatOfByte status_;
    private MatOfFloat err_;


    private int numOfFrame_; // run frameGrab first
    MediaMetadataRetriever mmr_;

    public VideoProcessor(File videoFile) {

        videoFile_ = videoFile;

        firstFrame_ = new Mat();
        lastFrame_ = new Mat();
        firstPoint_ = new Point();
        secondPoint_ = new Point();
        initPts1_ = new MatOfPoint2f();
        initPts2_ = new MatOfPoint2f();
        firstOutPoint_ = new Point();
        secondOutPoint_ = new Point();
        status_ = new MatOfByte();
        err_ = new MatOfFloat();
        firstCorners_ = new MatOfPoint();
        secondCorners_ = new MatOfPoint();


        mmr_ = new MediaMetadataRetriever();
        mmr_.setDataSource(videoFile.getAbsolutePath());

        // params for ShiTomasi corner detection
        feature_params.put("maxCorners", 10.0);
        feature_params.put("qualityLevel", 0.3);
        feature_params.put("minDistance", 7.0);
        feature_params.put("blockSize", 7.0);

        // params for lucas kanade optical flow
        tc_.epsilon = 0.03;
        tc_.maxCount = 10;
        winSize_ = new Size(15, 15);
        maxLevel_ = 2;


    }

    public void trackOpticalFlow() {
        findInitFeatures(firstFrame_, firstCorners_, firstPoint_, initPts1_);
        Mat prevImg = new Mat();
        Mat nextImg = new Mat();
        MatOfPoint2f prevPts;
        MatOfPoint2f nextPts;
        prevPts = initPts1_;
        nextPts = new MatOfPoint2f();
        for (int i=0; i<numOfFrame_-1; i++) {
            prevImg = frames_.get(i);
            nextImg = frames_.get(i+1);
            Video.calcOpticalFlowPyrLK(prevImg, nextImg, prevPts, nextPts, status_, err_, winSize_, maxLevel_);
        }
        List<Point> outPtsList = nextPts.toList();

        double aveX = 0.0;
        double aveY = 0.0;
        for (int i=0; i<outPtsList.size()-1; i++) {
            aveX = outPtsList.get(i).x + aveX;
            aveY = outPtsList.get(i).y + aveY;
        }
        aveX = aveX/outPtsList.size();
        aveY = aveY/outPtsList.size();
        firstOutPoint_.x = aveX;
        firstOutPoint_.y = aveY;
    }

    public void grabFrames() {
        frameGrabber(200000, frames_, videoFile_);
    }

    public void frameGrabber(long step, ArrayList<Mat> frames, File videoFile) {

        // grab the first frame info to construct Mat
        Bitmap firstFrame = mmr_.getFrameAtTime(0);
        int width = firstFrame.getWidth();
        int height = firstFrame.getHeight();

        String videoLength = mmr_.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

        long totalLength = Integer.valueOf(videoLength) * 1000;

        //long videoLengthUs = videoFile.length(); // TODO need to test the unit
        numOfFrame_ = 0;

        Log.d("HERE video : ", String.valueOf(totalLength));

        // loop over to grab frame every timeLapseUs

        for (long tl=0; tl<totalLength; tl=tl+step) {
            Mat newFrame = new Mat(height, width, CvType.CV_32S);
            grabFrameAsMat(tl, newFrame, videoFile);
            frames.add(newFrame);
            numOfFrame_++;
        }
        // setFirstFrame
        firstFrame_ = frames.get(0);
        saveFirstFrame();
        // setLastFrame
        Log.d("HERE size : ", String.valueOf(frames.size()-1));
        lastFrame_ =  frames.get(frames.size()-1);
        saveLastFrame();


//        Mat gray = new Mat(height, width, CvType.CV_32S);
//        gray.put(0, 0, graycale);
//        String path = Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_PICTURES) + "/MeasureUp/";
//        Imgcodecs.imwrite(path + "/gray.jpg", gray);
    }

    public void grabFrameAsMat (long step, Mat frame, File videoFile) {

        Bitmap currentFrame = mmr_.getFrameAtTime(step, OPTION_CLOSEST);
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

    public void findInitFeatures(Mat inputFrame, MatOfPoint corners, Point point, MatOfPoint2f initPts) {
        double x = point.x;
        double y = point.y;
        Mat mask = new Mat(inputFrame.rows(), inputFrame.cols(), CvType.CV_32S);
        Imgproc.circle(mask, point, 50, new Scalar( 255, 255, 255));
        Imgproc.goodFeaturesToTrack(inputFrame, corners, 10, 0.3, 7.0, mask);
        initPts.fromList(corners.toList());
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
