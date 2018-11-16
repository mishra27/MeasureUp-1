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
    private  MatOfByte status_;
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
        secondPoint_ = new Point();

        firstPoint_.x = 441;
        firstPoint_.y = 1266;
        secondPoint_.x = 435;
        secondPoint_.y = 684;
        initPts1_ = new MatOfPoint2f();
        initPts2_ = new MatOfPoint2f();
        firstOutPoint_ = new Point();
        secondOutPoint_ = new Point();
        status_ = new MatOfByte();
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

    private void findNextGoodPoint(MatOfPoint2f prevPts, MatOfPoint2f nextPts, MatOfByte status_, Point outputPoint) {
        Mat prevImg;
        Mat nextImg;
        double aveX;
        double aveY;
        for (int i = 0; i<numOfFrame_-1; i++) {
            prevImg = frames_.get(i);
            nextImg = frames_.get(i+1);
            Video.calcOpticalFlowPyrLK(prevImg, nextImg, prevPts, nextPts, status_, err_, winSize_, maxLevel_);
            List<Point> outPtsList = new ArrayList<>(nextPts.toList());
            outPtsList.clear();
            // select good points
            int numOfOutPoints = status_.toList().size();
            MatOfPoint2f goodNew = new MatOfPoint2f();
            for (int k=0; k<numOfOutPoints; k++) {
                if (status_.toList().get(k) == 1) {
                    outPtsList.add(nextPts.toList().get(k));
                }
            }
            goodNew.fromList(outPtsList);
            prevPts = goodNew;
            aveX = 0.0;
            aveY = 0.0;

            for (int j=0; j<outPtsList.size(); j++) {
                aveX = outPtsList.get(j).x + aveX;
                aveY = outPtsList.get(j).y + aveY;
            }
            aveX = aveX/outPtsList.size();
            aveY = aveY/outPtsList.size();
            outputPoint.x = aveX;
            outputPoint.y = aveY;
            Log.d("THATX video : ", String.valueOf(aveX));
            Log.d("THATY video : ", String.valueOf(aveY));
            Mat prev = frames_.get(i);
            Imgproc.circle(prev, new Point(aveX, aveY), 20, new Scalar(0, 0, 255), 5);
            if (i%10 == 0) {
                saveFrame(i+100, prev );
            }

        }
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
        List<Point> initPF2 = initPts2_.toList();
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

        findNextGoodPoint(prevPts1, nextPts1, status1_, firstOutPoint_);
        findNextGoodPoint(prevPts2, nextPts2, status2_, secondOutPoint_);

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

    public  double measurement (double opticalFocalM, double ccdHeigthM, double distanceM, ArrayList<Point> first2, ArrayList<Point> last2) {
        double intrinsicFocal = intrinsicFocal(opticalFocalM, ccdHeigthM, frameHeight_);
        Mat translationMatrix = new Mat(3, 1, CvType.CV_64F);
        translationMatrix(translationMatrix, distanceM);
        ArrayList<Point> outputPoint = new ArrayList<Point>();
        double[] world1 = new double[3];
        double[] world2 = new double[3];
        measureRealXYZ(intrinsicFocal, distanceM, first2.get(0), last2.get(0), world1);
        measureRealXYZ(intrinsicFocal, distanceM, first2.get(1), last2.get(1), world2);
        return Math.sqrt(Math.pow(world1[0]-world2[0],2) + Math.pow(world1[1] - world2[1], 2));
    }

    public void translationMatrix (Mat matrix64F, double distanceM) {
        double[] row0 = new double[]{1, 0, 0, distanceM};
        double[] row1 = new double[]{0, 1, 0, 0};
        double[] row2 = new double[]{0, 0, 1, 0};
        matrix64F.put(0, 0, row0);
        matrix64F.put(1, 0, row1);
        matrix64F.put(2, 0, row2);
    }

    public double intrinsicFocal (double opticalFocalM, double ccdHeightM, double imgHeight) {
        return opticalFocalM * imgHeight / ccdHeightM;
    }

    public void measureRealXYZ(double intrinsicFocalM, double distanceM, Point imgXYL, Point imgXYR, double[] worldXYZ) {
        double z =
                intrinsicFocalM * intrinsicFocalM * distanceM/
                        (imgXYR.x *
                                (0 * imgXYL.x +
                                0 * imgXYL.y +
                                1 * intrinsicFocalM) -
                        intrinsicFocalM *
                                (1 * imgXYL.x +
                                0 * imgXYL.y +
                                0 * intrinsicFocalM));
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

    public ArrayList<Point> getInitPoints() {
        ArrayList<Point> result = new ArrayList<Point>();
        result.add(firstPoint_);
        result.add(secondPoint_);
        return result;
    }

    public ArrayList<Point> getFinalPoints() {
        ArrayList<Point> result = new ArrayList<Point>();
        result.add(firstOutPoint_);
        result.add(secondOutPoint_);
        return result;
    }

    public void saveFrame(int index, Mat frame) {
        String fileIndex = String.valueOf(index);
        String currentFileName = "VideoObject-" + Long.toHexString(System.currentTimeMillis());
        String path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES) + "/MeasureUp/" + "video";
        Imgcodecs.imwrite(path + fileIndex + ".jpg", frame);
    }

}
