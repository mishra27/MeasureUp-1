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
    private MatOfByte status1_;
    private MatOfByte status2_;
    private MatOfFloat err_;


    private int numOfFrame_; // run frameGrab first
    MediaMetadataRetriever mmr_;

    public VideoProcessor(File videoFile) {

        videoFile_ = videoFile;

        firstFrame_ = new Mat();
        lastFrame_ = new Mat();
        firstPoint_ = new Point();
        firstPoint_.x = 441;
        firstPoint_.y = 1266;
        secondPoint_ = new Point();
        initPts1_ = new MatOfPoint2f();
        initPts2_ = new MatOfPoint2f();
        firstOutPoint_ = new Point();
        secondOutPoint_ = new Point();
        status1_ = new MatOfByte();
        status2_ = new MatOfByte();
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
        findInitFeatures(firstFrame_, secondCorners_, secondPoint_, initPts2_);
        Mat prevImg = new Mat();
        Mat nextImg = new Mat();
        MatOfPoint2f prevPts1;
        MatOfPoint2f nextPts1;
        MatOfPoint2f prevPts2;
        MatOfPoint2f nextPts2;
        prevPts1 = initPts1_;
        nextPts1 = new MatOfPoint2f();
        List<Point> initPF1 = initPts1_.toList();
        prevPts2 = initPts2_;
        nextPts2 = new MatOfPoint2f();
        List<Point> initPF2 = initPts1_.toList();
        double aveX1 = 0.0;
        double aveY1 = 0.0;
        double aveX2 = 0.0;
        double aveY2 = 0.0;

        for (int j = 0; j < initPF1.size(); j++) {
            aveX1 = initPF1.get(j).x + aveX1;
            aveY1 = initPF1.get(j).y + aveY1;
        }
        aveX1 = aveX1 / initPF1.size();
        aveY1 = aveY1 / initPF1.size();
        firstOutPoint_.x = aveX1;
        firstOutPoint_.y = aveY1;
        Log.d("THATX video : ", String.valueOf(aveX1));
        Log.d("THATY video : ", String.valueOf(aveY1));

        for (int j = 0; j < initPF2.size(); j++) {
            aveX2 = initPF2.get(j).x + aveX2;
            aveY2 = initPF2.get(j).y + aveY2;
        }
        aveX2 = aveX2 / initPF2.size();
        aveY2 = aveY2 / initPF2.size();
        secondOutPoint_.x = aveX2;
        secondOutPoint_.y = aveY2;
        Log.d("THATX video 2: ", String.valueOf(aveX2));
        Log.d("THATY video 2: ", String.valueOf(aveY2));


        for (int i = 0; i < numOfFrame_ - 1; i++) {
            prevImg = frames_.get(i);
            nextImg = frames_.get(i + 1);
            Video.calcOpticalFlowPyrLK(prevImg, nextImg, prevPts1, nextPts1, status1_, err_, winSize_, maxLevel_);
            Video.calcOpticalFlowPyrLK(prevImg, nextImg, prevPts2, nextPts2, status2_, err_, winSize_, maxLevel_);
            List<Point> outPtsList1 = new ArrayList<>(nextPts1.toList());
            outPtsList1.clear();
            getGoodNextPoints(outPtsList1, nextPts1, prevPts1, firstOutPoint_);
            List<Point> outPtsList2 = new ArrayList<>(nextPts2.toList());
            outPtsList2.clear();
            getGoodNextPoints(outPtsList2, nextPts2, prevPts2, secondOutPoint_);

            Mat prev = frames_.get(i);
            Imgproc.circle(prev, new Point(firstOutPoint_.x, firstOutPoint_.y), 20, new Scalar(0, 0, 255), 5);
            Imgproc.circle(prev, new Point(secondOutPoint_.x, secondOutPoint_.y), 20, new Scalar(0, 0, 255), 5);
            if (i % 10 == 0) {
                saveFrame(i + 100, prev);
            }
        }
    }

        public double[] getGoodNextPoints(List<Point> outPtsList1, MatOfPoint2f nextPts1, MatOfPoint2f prevPts1, Point outputPoint) {
            double[] xy = {0, 0};
            outPtsList1 = new ArrayList<>(nextPts1.toList());
            outPtsList1.clear();
            // select good points
            int numOfOutPoints1 = status1_.toList().size();
            MatOfPoint2f goodNew1 = new MatOfPoint2f();
            for (int k = 0; k < numOfOutPoints1; k++) {
                if (status1_.toList().get(k) == 1) {
                    outPtsList1.add(nextPts1.toList().get(k));
                }
            }
            goodNew1.fromList(outPtsList1);
            prevPts1 = goodNew1;
            xy[0] = 0.0;
            xy[1] = 0.0;

            for (int j = 0; j < outPtsList1.size(); j++) {
                xy[0] = outPtsList1.get(j).x + xy[0];
                xy[1] = outPtsList1.get(j).y + xy[1];
            }
            xy[0] = xy[0] / outPtsList1.size();
            xy[1] = xy[1] / outPtsList1.size();
            outputPoint.x = xy[0];
            outputPoint.y = xy[1];
            Log.d("THATX video : ", String.valueOf(xy[0]));
            Log.d("THATY video : ", String.valueOf(xy[1]));
            return xy;
        }
//        List<Point> outPtsList = nextPts1.toList();
//
//        double aveX1 = 0.0;
//        double aveY1 = 0.0;
//        for (int i=0; i<outPtsList.size()-1; i++) {
//            aveX1 = outPtsList.get(i).x + aveX1;
//            aveY1 = outPtsList.get(i).y + aveY1;
//        }
//        aveX1 = aveX1/outPtsList.size();
//        aveY1 = aveY1/outPtsList.size();
//        firstOutPoint_.x = aveX1;
//        firstOutPoint_.y = aveY1;

    public  double measurement (double opticalFocalM, double ccdHeigthM, double distanceM, ArrayList<Point> first2, ArrayList<Point> last2) {
        double intrinsicFocal = intrinsicFocal(opticalFocalM, ccdHeigthM, frameHeight_);
        Mat tranlationMatrix = new Mat(4, 3, CvType.CV_64F);
        translationMatrix(tranlationMatrix, distanceM);
        ArrayList<Point> outputPoint = new ArrayList<Point>();
        double[] world1 = {0, 0, 0};
        double[] world2 = {0, 0, 0};
        measureRealXYZ(intrinsicFocal, tranlationMatrix, first2.get(0), first2.get(1), world1);
        measureRealXYZ(intrinsicFocal, tranlationMatrix, last2.get(0), last2.get(1), world2);
        return Math.abs(world1[2] - world2[2]);
    }

    public void grabFrames() {
        frameGrabber(50000, frames_, videoFile_);
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

        for (long tl=0; tl<(totalLength - 34000); tl=tl+step) {
            Mat newFrame = new Mat(height, width, CvType.CV_8UC1);
            grabFrameAsMat(tl, newFrame);
            frames.add(newFrame);
            numOfFrame_++;
        }
        firstFrame_ = frames.get(0);
        lastFrame_ = frames.get(frames.size()-1);
        Log.d("HERE size : ", String.valueOf(frames.size()-1));
        saveFrame(0, firstFrame_);



//        Mat gray = new Mat(height, width, CvType.CV_32S);
//        gray.put(0, 0, graycale);
//        String path = Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_PICTURES) + "/MeasureUp/";
//        Imgcodecs.imwrite(path + "/gray.jpg", gray);
    }

    public void grabFrameAsMat (long step, Mat frame) {

        Bitmap currentFrame = mmr_.getFrameAtTime(step, OPTION_CLOSEST);
        int width = currentFrame.getWidth();
        int height = currentFrame.getHeight();
        frameHeight_ = height;
        frameWidth_ = width;
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
        Mat U32gray = new Mat(height, width, CvType.CV_32S);
        U32gray.put(0, 0, graycale);
        U32gray.convertTo(frame, CvType.CV_8UC1);

    }

    public void findInitFeatures(Mat inputFrame, MatOfPoint corners, Point point, MatOfPoint2f initPts) {
        double x = point.x;
        double y = point.y;
        Mat mask = new Mat(inputFrame.rows(), inputFrame.cols(), CvType.CV_8UC1, Scalar.all(0));
        Imgproc.circle(mask, point, 50, new Scalar( 255, 255, 255), -1, 8, 0);
        Imgproc.goodFeaturesToTrack(inputFrame, corners, 10, 0.3, 7.0, mask, 7);
        Mat cropped = new Mat();
        inputFrame.copyTo(cropped, mask);
        saveFrame(999, cropped);
        initPts.fromList(corners.toList());
    }

    public void translationMatrix (Mat matrix64F, double distanceM) {
        Mat tm = new Mat(4, 3, CvType.CV_64F);
        tm = matrix64F;
        double[] row0 = {1.0, 0, 0, distanceM};
        double[] row1 = {0, 1, 0, 0};
        double[] row2 = {0, 0, 1, 0};
        tm.put(0, 0, row0);
        tm.put(1, 0, row1);
        tm.put(2, 0, row2);
    }

    public double intrinsicFocal (double opticalFocalM, double ccdHeightM, double imgHeight) {
        return opticalFocalM * imgHeight / ccdHeightM;
    }

    public void measureRealXYZ(double intrinsicFocalM, Mat translationMatrix, Point imgXYL, Point imgXYR, double[] worldXYZ) {
        double z =
                intrinsicFocalM * (intrinsicFocalM * translationMatrix.get(0, 3)[0])/
                        (imgXYR.x *
                                (translationMatrix.get(2, 0)[0] * imgXYL.x +
                                translationMatrix.get(2, 1)[0] * imgXYL.y +
                                translationMatrix.get(2, 2)[0] * intrinsicFocalM) -
                        intrinsicFocalM *
                                (translationMatrix.get(0, 0)[0] * imgXYL.x +
                                translationMatrix.get(0, 1)[0] * imgXYL.y) +
                                translationMatrix.get(0, 2)[0] * intrinsicFocalM);
        double x = z * imgXYL.x / intrinsicFocalM;
        double y = z * imgXYL.y / intrinsicFocalM;
        worldXYZ[0] = x;
        worldXYZ[1] = y;
        worldXYZ[2] = z;
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

    public void saveFrame(int index, Mat frame) {
        String fileIndex = String.valueOf(index);
        String currentFileName = "VideoObject-" + Long.toHexString(System.currentTimeMillis());
        String path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES) + "/MeasureUp/" + "video";
        Imgcodecs.imwrite(path + fileIndex + ".jpg", frame);
    }

}
