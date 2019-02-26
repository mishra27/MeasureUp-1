package com.example.aksha.measureup;

import android.graphics.Bitmap;
import android.util.Log;

//import org.bytedeco.javacv.FrameGrabber;
import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.features2d.AKAZE;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.ORB;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;
import org.opencv.video.Video;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import wseemann.media.FFmpegMediaMetadataRetriever;

import static android.media.MediaMetadataRetriever.OPTION_CLOSEST;
import static org.opencv.core.CvType.CV_64F;
import static org.opencv.core.TermCriteria.COUNT;
import static org.opencv.core.TermCriteria.EPS;
import static org.opencv.imgproc.Imgproc.undistortPoints;
//import static org.opencv.imgcodecs.Imgcodecs.CV_LOAD_IMAGE_COLOR;

public class VideoProcessor {
    //
    private File videoFile_;

    // params for ShiTomasi corner detection
    private Map<String, Double> feature_params = new HashMap<String, Double>();

    // params for lucas kanade optical flow
    private Map<String, String> lk_params = new HashMap<String, String>();
    private TermCriteria tc_ = new TermCriteria(TermCriteria.COUNT+ EPS, 10, 0.03);
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
    FFmpegMediaMetadataRetriever mmr_;
   // FrameGrabber mm;
    private Mat fundamentalMatrix;
    private Mat essentialMatix;
    private Mat r = new Mat();
    private Mat t = new Mat();
    private MatOfKeyPoint inliers1;
    private MatOfKeyPoint inliers2;
    private Mat firstFrameNew;
    private Mat firstFramePoints;
    private Mat lastFramePoints;


    public VideoProcessor(File videoFile) {

        videoFile_ = videoFile;

        firstFrame_ = new Mat();
        lastFrame_ = new Mat();
        firstPoint_ = new Point();
        secondPoint_ = new Point();
//
//        firstPoint_.x = 441;
//        firstPoint_.y = 1266;
//        secondPoint_.x = 435;
//        secondPoint_.y = 684;
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


        mmr_ = new FFmpegMediaMetadataRetriever();
        mmr_.setDataSource(videoFile.getAbsolutePath());

//        try {
//            mm = FrameGrabber.createDefault(videoFile.getAbsolutePath());
//        } catch (FrameGrabber.Exception e) {
//            e.printStackTrace();
//        }

        // params for ShiTomasi corner detection
        feature_params.put("maxCorners", 100.0);
        feature_params.put("qualityLevel", 0.3);
        feature_params.put("minDistance", 7.0);
        feature_params.put("blockSize", 7.0);

        // params for lucas kanade optical flow
        tc_.epsilon = 0.03;
        tc_.maxCount = 10;
        winSize_ = new Size(15, 15);
        maxLevel_ = 2;


    }

    private Point findNextGoodPoint(MatOfPoint2f prevPts, MatOfPoint2f nextPts, MatOfByte status_) {


        Mat prevImg = new Mat();
        Mat nextImg = new Mat();
        double aveX;
        double aveY;
        for (int i = 0; i<numOfFrame_-1; i++) {


            prevImg = frames_.get(i);
            nextImg = frames_.get(i+1);
            Video.calcOpticalFlowPyrLK(prevImg, nextImg, prevPts, nextPts, status_, err_, winSize_, maxLevel_, new TermCriteria(EPS + COUNT, 10, 0.03));
            
            List<Point> nextList = new ArrayList<>(nextPts.toList());
            List<Point> goodNewList = new ArrayList<>();
            List<Byte> statusList = new ArrayList<>(status_.toList());

            // select good points
            int numOfOutPoints = statusList.size();
            MatOfPoint2f goodNew = new MatOfPoint2f();
            for (int k=0; k<numOfOutPoints; k++) {
                if (statusList.get(k) == 1) {
                    goodNewList.add(nextList.get(k));
                }
            }
            goodNew.fromList(goodNewList);
            prevPts = goodNew;

        }

        aveX = 0.0;
        aveY = 0.0;

        List<Point> outPtsList = prevPts.toList();
        for (int j = 0; j<outPtsList.size(); j++) {
            aveX = outPtsList.get(j).x + aveX;
            aveY = outPtsList.get(j).y + aveY;
        }
        aveX = aveX/outPtsList.size();
        aveY = aveY/outPtsList.size();

        Point p = new Point(aveX, aveY);

        Mat mask = new Mat(nextImg.rows(), nextImg.cols(), CvType.CV_8UC1, Scalar.all(0));
        Imgproc.circle(mask, p, 50, new Scalar( 255, 255, 255), -1, 8, 0);
        //Imgproc.goodFeaturesToTrack();
        Mat cropped = new Mat();
        nextImg.copyTo(cropped, mask);
        saveFrame(next, cropped);
        next++;

        return p;
    }


    public void trackOpticalFlow() {
        goodPoints(firstFrame_, firstCorners_, firstPoint_, initPts1_);
        goodPoints(firstFrame_, secondCorners_, secondPoint_, initPts2_);

        MatOfPoint2f prevPts1 = initPts1_;
        MatOfPoint2f nextPts1 = new MatOfPoint2f();

        MatOfPoint2f prevPts2 = initPts2_;
        MatOfPoint2f nextPts2 = new MatOfPoint2f();



        firstOutPoint_ =  findNextGoodPoint(prevPts1, nextPts1, status1_);
        secondOutPoint_ = findNextGoodPoint(prevPts2, nextPts2, status2_);
    }




    public void grabFrames(boolean first) {
        frameGrabber(50000, frames_, first);
    }

    public void frameGrabber(long step, ArrayList<Mat> frames, boolean first) {

        // grab the first frame info to construct Mat
       // mm.grabFrame()
        Bitmap firstFrame = mmr_.getFrameAtTime(0);
        int width = firstFrame.getWidth();
        int height = firstFrame.getHeight();

        String videoLength = mmr_.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION);

        long totalLength = Integer.valueOf(videoLength) * 1000;

        //long videoLengthUs = videoFile.length(); // TODO need to test the unit
        numOfFrame_ = 0;

        Log.d("HERE video : ", String.valueOf(totalLength));

        // loop over to grab frame every timeLapseUs

        if (first == true) {
            Mat newFrame = new Mat(height, width, CvType.CV_8UC1);
            grabFrameAsMat(0, newFrame);
            firstFrame_ = newFrame;
            saveFrame(0, newFrame);
            return;
        }

        for (long tl=0; tl<(totalLength - 15000); tl=tl+step) {
            Mat newFrame = new Mat(height, width, CvType.CV_8UC1);
            grabFrameAsMat(tl, newFrame);

            frames.add(newFrame);
            numOfFrame_++;
        }

        firstFrame_ = frames.get(0);

        firstFrameNew = new Mat(height, width, CvType.CV_8UC1);
        grabFrameAsMat(0, firstFrameNew);
        lastFrame_ = frames.get(frames.size()-1);
        Log.d("HERE size : ", String.valueOf(frames.size()-1));
        saveFrame(0, firstFrame_);

//        Mat gray = new Mat(height, width, CvType.CV_32S);
//        gray.put(0, 0, graycale);
//        String path = Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_PICTURES) + "/MeasureUp/";
//        Imgcodecs.imwrite(path + "/gray.jpg", gray);
    }

//    public void matching(){
//
//        Mat first = getFirstFrame();
//        Mat last = getLastFrame();
//
//        double hessianThreshold = 400;
//        int nOctaves = 4, nOctaveLayers = 3;
//        boolean extended = false, upright = false;
//        MatOfKeyPoint objectKeyPoints = new MatOfKeyPoint();
//        FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.AKAZE);
//        featureDetector.detect(first, objectKeyPoints);
//        KeyPoint[] keypoints = objectKeyPoints.toArray();
//
//        MatOfKeyPoint objectDescriptors = new MatOfKeyPoint();
//        DescriptorExtractor descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.AKAZE);
//        descriptorExtractor.compute(first, objectKeyPoints, objectDescriptors);
//
//
//        Mat outputImage = new Mat(first.rows(), first.cols(), CV_LOAD_IMAGE_COLOR);
//        Scalar newKeypointColor = new Scalar(255, 0, 0);
//
//        Features2d.drawKeypoints(first, objectKeyPoints, outputImage, newKeypointColor, 0);
//
//
//        // Match object image with the scene image
//        MatOfKeyPoint sceneKeyPoints = new MatOfKeyPoint();
//        MatOfKeyPoint sceneDescriptors = new MatOfKeyPoint();
//        System.out.println("Detecting key points in background image...");
//        featureDetector.detect(last, sceneKeyPoints);
//        System.out.println("Computing descriptors in background image...");
//        descriptorExtractor.compute(last, sceneKeyPoints, sceneDescriptors);
//
//        Mat matchoutput = new Mat(last.rows() * 2, last.cols() * 2, CV_LOAD_IMAGE_COLOR);
//        Scalar matchestColor = new Scalar(0, 255, 0);
//
//        List<MatOfDMatch> matches = new LinkedList<MatOfDMatch>();
//        DescriptorMatcher descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);
//        System.out.println("Matching object and scene images...");
//        descriptorMatcher.knnMatch(objectDescriptors, sceneDescriptors, matches, 2);
//
//        System.out.println("Calculating good match list...");
//        LinkedList<DMatch> goodMatchesList = new LinkedList<DMatch>();
//
//        float nndrRatio = 0.7f;
//
//        for (int i = 0; i < matches.size(); i++) {
//            MatOfDMatch matofDMatch = matches.get(i);
//            DMatch[] dmatcharray = matofDMatch.toArray();
//            DMatch m1 = dmatcharray[0];
//            DMatch m2 = dmatcharray[1];
//
//            if (m1.distance <= m2.distance * nndrRatio) {
//                goodMatchesList.addLast(m1);
//
//            }
//        }
//
//        if (goodMatchesList.size() >= 7) {
//            System.out.println("Object Found!!!");
//
//            List<KeyPoint> objKeypointlist = objectKeyPoints.toList();
//            List<KeyPoint> scnKeypointlist = sceneKeyPoints.toList();
//
//            LinkedList<Point> objectPoints = new LinkedList<>();
//            LinkedList<Point> scenePoints = new LinkedList<>();
//
//            for (int i = 0; i < goodMatchesList.size(); i++) {
//                objectPoints.addLast(objKeypointlist.get(goodMatchesList.get(i).queryIdx).pt);
//                scenePoints.addLast(scnKeypointlist.get(goodMatchesList.get(i).trainIdx).pt);
//            }
//
//            MatOfPoint2f objMatOfPoint2f = new MatOfPoint2f();
//            objMatOfPoint2f.fromList(objectPoints);
//            MatOfPoint2f scnMatOfPoint2f = new MatOfPoint2f();
//            scnMatOfPoint2f.fromList(scenePoints);
//
//            Mat homography = Calib3d.findHomography(objMatOfPoint2f, scnMatOfPoint2f, Calib3d.RANSAC, 3);
//
//            Mat obj_corners = new Mat(4, 1, CvType.CV_32FC2);
//            Mat scene_corners = new Mat(4, 1, CvType.CV_32FC2);
//
//            obj_corners.put(0, 0, new double[]{0, 0});
//            obj_corners.put(1, 0, new double[]{first.cols(), 0});
//            obj_corners.put(2, 0, new double[]{first.cols(), first.rows()});
//            obj_corners.put(3, 0, new double[]{0, first.rows()});
//
//            System.out.println("Transforming object corners to scene corners...");
//            Core.perspectiveTransform(obj_corners, scene_corners, homography);
//
//           // Mat img = Ip.imread(last, CV_LOAD_IMAGE_COLOR);
//
////            Core.line(img, new Point(scene_corners.get(0, 0)), new Point(scene_corners.get(1, 0)), new Scalar(0, 255, 0), 4);
////            Core.line(img, new Point(scene_corners.get(1, 0)), new Point(scene_corners.get(2, 0)), new Scalar(0, 255, 0), 4);
////            Core.line(img, new Point(scene_corners.get(2, 0)), new Point(scene_corners.get(3, 0)), new Scalar(0, 255, 0), 4);
////            Core.line(img, new Point(scene_corners.get(3, 0)), new Point(scene_corners.get(0, 0)), new Scalar(0, 255, 0), 4);
//
//            System.out.println("Drawing matches image...");
//            MatOfDMatch goodMatches = new MatOfDMatch();
//            goodMatches.fromList(goodMatchesList);
//
//            Features2d.drawMatches(first, objectKeyPoints, last, sceneKeyPoints, goodMatches, matchoutput, matchestColor, newKeypointColor, new MatOfByte(), 2);
//
//            saveFrame(999999999, outputImage);
//            saveFrame(99999999, matchoutput);
//            //saveFrame(9999999, img);
//
//        } else {
//            System.out.println("Object Not Found");
//        }
//
//    }


    public void grabFrameAsMat (long step, Mat frame) {

        Bitmap currentFrame = mmr_.getFrameAtTime(step, OPTION_CLOSEST);
        int width = currentFrame.getWidth();
        int height = currentFrame.getHeight();
        frameHeight_ = height;
        frameWidth_ = width;
//        int[] rawPixels = new int[width*height];
//        currentFrame.getPixels(rawPixels, 0, width, 0, 0, width, height);
//        int[] R = new int[rawPixels.length];
//        int[] G = new int[rawPixels.length];
//        int[] B = new int[rawPixels.length];
//        int[] graycale = new int[rawPixels.length];
//        for (int i=0; i<rawPixels.length; i++) {
//            R[i] = (rawPixels[i] >> 16) & 0xff;
//            G[i] = (rawPixels[i] >> 8) & 0xff;
//            B[i] = rawPixels[i] & 0xff;
//            graycale[i] = (R[i] + G[i] + B[i])/3;
//        }
//        Mat U32gray = new Mat(height, width, CvType.CV_32S);
//        U32gray.put(0, 0, graycale);
//        U32gray.convertTo(frame, CvType.CV_8UC1);
        Mat rgb = new Mat(height, width, CvType.CV_8UC4);
        Utils.bitmapToMat(currentFrame, rgb);
        Imgproc.cvtColor(rgb, frame, Imgproc.COLOR_RGB2GRAY);

    }

    int next = 998;
    public void findInitFeatures(Mat inputFrame, MatOfPoint corners, Point point, MatOfPoint2f initPts) {
        double x = point.x;
        double y = point.y;
        next++;
        Mat mask = new Mat(inputFrame.rows(), inputFrame.cols(), CvType.CV_8UC1, Scalar.all(0));
        Imgproc.circle(mask, point, 50, new Scalar( 255, 255, 255), -1, 8, 0);
        Imgproc.goodFeaturesToTrack(inputFrame, corners, 10, 0.3, 7.0, mask, 7);
        Mat cropped = new Mat();
        inputFrame.copyTo(cropped, mask);
        saveFrame(next, cropped);
        initPts.fromList(corners.toList());
    }

    public  double[] measurement (double opticalFocalM, double ccdWidthM, double distanceM, ArrayList<Point> first2, ArrayList<Point> last2) {
        double[] ansArr = new double[2];
        double intrinsicFocal = intrinsicFocal(opticalFocalM, ccdWidthM, frameHeight_);
        Mat translationMatrix = translationMatrix(distanceM);

        System.out.println(" param " + opticalFocalM + " " + ccdWidthM);
        getCameraParam(intrinsicFocal);
        ArrayList<Point> outputPoint = new ArrayList<Point>();
        double[] world1 = new double[3];
        double[] world2 = new double[3];
        double ans = measureRealXYZ(intrinsicFocal, distanceM, first2.get(0), last2.get(0), world1);
        measureRealXYZ(intrinsicFocal, distanceM, first2.get(1), last2.get(1), world2);

        featureMatching(firstFrame_, lastFrame_);
        ansArr[0] = ans;
        ansArr[1] = orbDescriptor(firstFrameNew, lastFrame_, intrinsicFocal, distanceM);

        System.out.println("ans old "+ String.valueOf(Math.sqrt(Math.pow(world1[0]-world2[0],2) + Math.pow(world1[1] - world2[1], 2) + Math.pow(world1[2]-world2[2],2))));


        return ansArr;
    }

    public Mat translationMatrix ( double distanceM) {
        Mat matrix64F = Mat.eye(3, 4, CV_64F);
        matrix64F.put(0, 3, distanceM);
        return matrix64F;

    }

    public double intrinsicFocal (double opticalFocalM, double ccdHeightM, double imgHeight) {
        return opticalFocalM * imgHeight / ccdHeightM;
    }

    public double measureRealXYZ(double intrinsicFocalM, double distanceM, Point imgXYL, Point imgXYR, double[] worldXYZ) {
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

        Mat input1 = new Mat(2, 2, CV_64F);

        input1.put(0,0, firstPoint_.x);
        input1.put(1,0, firstPoint_.y);

        input1.put(0,1,secondPoint_.x);
        input1.put(1,1,secondPoint_.y);

        Mat input2 = new Mat(2, 2, CV_64F);

        input2.put(0,0, firstOutPoint_.x);
        input2.put(1,0, firstOutPoint_.y);

        input2.put(0,1,secondOutPoint_.x);
        input2.put(1,1,secondOutPoint_.y);

//        input2.put(0,0, firstOutPoint_.x, secondOutPoint_.x);
//        input2.put(1,0, firstOutPoint_.y, secondOutPoint_.y);


        Mat output = new Mat(4, 2, CV_64F);

        int i = 1;
        Mat cameraMat1 =  new Mat(3, 4, CV_64F);
        Mat cameraMat2 =  new Mat(3, 4, CV_64F);

        try {
            File file =
                    new File(videoFile_.getParent()+ "/matrix.txt");

            Scanner sc = new Scanner(file);

            while (sc.hasNextLine()) {
                String camera = (sc.nextLine());
                camera = camera.replace("[", "");
                camera = camera.replace("]", "");
                camera = camera.replace("[", "");
                String[] array = camera.split(",");
                int l = 0;
                if(i == 1){
                    for(int j= 0; j <4; j++){
                        for(int k = 0; k<3; k++){
                            cameraMat1.put(k,j, Float.valueOf(array[l]) );
                            Log.d("valu ", array[l]);

                            l++;
                            if((l+1)%4 == 0)
                                l++;
                        }
                    }

                    l=0;
                    i++;
                }

                else{
                    for(int j= 0; j <4; j++){
                        for(int k = 0; k<3; k++){
                            cameraMat2.put(k, j, Float.valueOf(array[l]) );

                            l++;
                            if((l+1)%4 == 0)
                                l++;
                        }
                    }

                    l=0;
                    i=1;
                }
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Mat para = getCameraParam(intrinsicFocalM);
        Core.gemm(para, cameraMat1, 1, new Mat(), 0, cameraMat1 , 0);
        Core.gemm(para, cameraMat2, 1, new Mat(), 0, cameraMat2 , 0);


        Calib3d.triangulatePoints(cameraMat1, cameraMat2, input1, input2, output);

        //getPrjectionMatrix2(intrinsicFocalM, distanceM);

        double x1 = output.get(0, 0)[0]/output.get(3, 0)[0];
        double x2 = output.get(0, 1)[0]/output.get(3, 1)[0];

        double y1 = output.get(1, 0)[0]/output.get(3, 0)[0];
        double y2 = output.get(1, 1)[0]/output.get(3, 1)[0];

        double z1 = output.get(2, 0)[0]/output.get(3, 0)[0];
        double z2 = output.get(2, 1)[0]/output.get(3, 1)[0];

        Log.d("x1 " ,String.valueOf(output.get(0, 0)[0]/output.get(3, 0)[0]));
        Log.d("x2 " ,String.valueOf(output.get(0, 1)[0]/output.get(3, 1)[0]));

        Log.d("y1 " ,String.valueOf(output.get(1, 0)[0]/output.get(3, 0)[0]));
        Log.d("y2 " ,String.valueOf(output.get(1, 1)[0]/output.get(3, 1)[0]));

        Log.d("z1 " ,String.valueOf(output.get(2, 0)[0]/output.get(3, 0)[0]));
        Log.d("z2 " ,String.valueOf(output.get(2, 1)[0]/output.get(3, 1)[0]));

        double ans  = Math.sqrt(Math.pow(x1-x2,2) + Math.pow(y1 - y2, 2) + Math.pow(z1-z2,2));

        Log.d("ans newest", String.valueOf(ans));


        Log.d("dist ", String.valueOf(distanceM));

        return ans;

        //matching();
    }

    private void computeFundamentalMatrix() {
        MatOfPoint2f in = new MatOfPoint2f(firstPoint_, secondPoint_);
        MatOfPoint2f out = new MatOfPoint2f(firstOutPoint_, secondOutPoint_);

        fundamentalMatrix  = Calib3d.findFundamentalMat(in, out, Calib3d.FM_RANSAC, 3, 0.99);
    }

    private Mat computeEssentialMatrix(double intrinsicFocal) {
//        MatOfPoint2f in = new MatOfPoint2f(firstPoint_, secondPoint_);
//        MatOfPoint2f out = new MatOfPoint2f(firstOutPoint_, secondOutPoint_);

        Mat cameraMatrix = getCameraParam(intrinsicFocal);
        essentialMatix  = Calib3d.findEssentialMat(inliers1, inliers2, cameraMatrix );

        return essentialMatix;
    }

    private void recoverPose(double intrinsicFocal){

        MatOfPoint2f in = new MatOfPoint2f(firstPoint_, secondPoint_);
        MatOfPoint2f out = new MatOfPoint2f(firstOutPoint_, secondOutPoint_);
       // Calib3d.proje
        Calib3d.recoverPose(computeEssentialMatrix( intrinsicFocal), inliers1, inliers2,getCameraParam(intrinsicFocal), r, t);
    }

    private Mat getCameraParam(double intrinsicFocal) {

        Mat cameraParam =  Mat.zeros(3, 3, CV_64F);
        cameraParam.put(0,0, intrinsicFocal);
        cameraParam.put(0,2, frameWidth_/2);
        cameraParam.put(1,1, intrinsicFocal);
        cameraParam.put(1,2, frameHeight_/2);
        cameraParam.put(2,2, 1);


        System.out.println("test " + cameraParam.dump());

        //Log.d("outpput ", String.valueOf(cameraParam.get(0,1).to));
        return cameraParam;
    }

    private void getPrjectionMatrix2(double intrinsicFocal, double distanceM) {



       // Calib3d.recoverPose(computeEssentialMatrix( intrinsicFocal), inliers1, inliers2,getCameraParam(intrinsicFocal), r, t);




        //Calib3d.camera
//        Mat cameraParam = getCameraParam(intrinsicFocal);
//        Mat translationMatrix = translationMatrix(distanceM);
//        Mat matrix64F;
//        matrix64F= cameraParam.mul(translationMatrix);
         //return matrix64F;
    }

    private Mat getPrjectionMatrix1(double intrinsicFocal) {

        Mat cameraParam = getCameraParam(intrinsicFocal);
        Mat identityMatric = Mat.eye(3,4,CV_64F);
        Mat m1 = Mat.zeros(3, 4, CvType.CV_64F);

        Core.gemm(cameraParam, identityMatric, 1, new Mat(), 0, m1, 0);

        System.out.println( " look " + m1.dump());

        return m1;
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
        String path = videoFile_.getParent();
        String fileIndex = String.valueOf(index);
        Imgcodecs.imwrite(path + "/" + fileIndex + ".jpg", frame);
    }

    public Bitmap getFirstBitmap () {
        Bitmap bmp = null;

        try {
            //Imgproc.cvtColor(seedsImage, tmp, Imgproc.COLOR_RGB2BGRA);
            bmp = Bitmap.createBitmap(firstFrame_.cols(), firstFrame_.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(firstFrame_, bmp);
        }
        catch (CvException e){Log.d("Exception",e.getMessage());}
        return bmp;
    }

    public void setInitPoints(Point init1, Point init2) {
        firstPoint_.x = init1.x;
        firstPoint_.y = init1.y;
        secondPoint_.x = init2.x;
        secondPoint_.y = init2.y;
    }

    public void featureMatching(Mat img1, Mat img2){

        // We create AKAZE and detect and compute AKAZE keypoints and descriptors.
        // Since we don't need the mask parameter, noArray() is used.
        AKAZE akaze = AKAZE.create();
        MatOfKeyPoint kpts1 = new MatOfKeyPoint(), kpts2 = new MatOfKeyPoint();
        Mat desc1 = new Mat(), desc2 = new Mat();
        akaze.detectAndCompute(img1, new Mat(), kpts1, desc1);
        akaze.detectAndCompute(img2, new Mat(), kpts2, desc2);



        // Use brute-force matcher to find 2-nn matches
        // We use Hamming distance, because AKAZE uses binary descriptor by default.
        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
        List<MatOfDMatch> knnMatches = new ArrayList<>();
        matcher.knnMatch(desc1, desc2, knnMatches, 2);

        //Use 2-nn matches and ratio criterion to find correct keypoint matches
        float ratioThreshold = 0.8f; // Nearest neighbor matching ratio
        List<KeyPoint> listOfMatched1 = new ArrayList<>();
        List<KeyPoint> listOfMatched2 = new ArrayList<>();
        List<KeyPoint> listOfKeypoints1 = kpts1.toList();
        List<KeyPoint> listOfKeypoints2 = kpts2.toList();
        for (int i = 0; i < knnMatches.size(); i++) {
            DMatch[] matches = knnMatches.get(i).toArray();
            float dist1 = matches[0].distance;
            float dist2 = matches[1].distance;
            if (dist1 < ratioThreshold * dist2) {
                listOfMatched1.add(listOfKeypoints1.get(matches[0].queryIdx));
                listOfMatched2.add(listOfKeypoints2.get(matches[0].trainIdx));
            }
        }

        List<DMatch> listOfGoodMatches = new ArrayList<>();

        listOfGoodMatches.add(new DMatch(listOfMatched1.size(), listOfMatched2.size(), 10));

        Mat res = new Mat();
         inliers1 = new MatOfKeyPoint(listOfMatched1.toArray(new KeyPoint[listOfMatched1.size()]));
         inliers2 = new MatOfKeyPoint(listOfMatched2.toArray(new KeyPoint[listOfMatched2.size()]));
        MatOfDMatch goodMatches = new MatOfDMatch(listOfGoodMatches.toArray(new DMatch[listOfGoodMatches.size()]));
        //goodMatches.
        //Features2d.drawMatches

            Features2d.drawKeypoints(img1,inliers1, img1 );
        Features2d.drawKeypoints(img2,inliers2, img2 );


//        Features2d.drawMatches(img1, inliers1, img2, inliers2, goodMatches, res);
       //   Imgcodecs.imwrite(videoFile_.getParent()+"/akaze_result1.png", img1);
      //  Imgcodecs.imwrite(videoFile_.getParent()+"/akaze_result2.png", img2);

        double inlierRatio = listOfMatched1.size() / (double) listOfMatched1.size();
        System.out.println("A-KAZE Matching Results");
        System.out.println("*******************************");
        System.out.println("# Keypoints 1:                        \t" + listOfKeypoints1.size());
        System.out.println("# Keypoints 2:                        \t" + listOfKeypoints2.size());
        System.out.println("# Matches:                            \t" + listOfGoodMatches.size());
        System.out.println("# Inliers:                            \t" + listOfMatched1.size());
        System.out.println("# Inliers Ratio:                      \t" + listOfMatched2.size());
        System.out.println("# Inliers Ratio:                      \t" + listOfMatched2.size());
//        HighGui.imshow("result", res);
//        HighGui.waitKey();



    }



//
//    public void surfMatching(Mat img1, Mat img2){
//
//        double hessianThreshold = 400;
//        int nOctaves = 4, nOctaveLayers = 3;
//        boolean extended = false, upright = false;
//        SURF  = SURF.create();
//        SURF detector = SURF.create(hessianThreshold, nOctaves, nOctaveLayers, extended, upright);
//        MatOfKeyPoint keypoints1 = new MatOfKeyPoint(), keypoints2 = new MatOfKeyPoint();
//        Mat descriptors1 = new Mat(), descriptors2 = new Mat();
//        detector.detectAndCompute(img1, new Mat(), keypoints1, descriptors1);
//        detector.detectAndCompute(img2, new Mat(), keypoints2, descriptors2);
//
//        //-- Step 2: Matching descriptor vectors with a brute force matcher
//        // Since SURF is a floating-point descriptor NORM_L2 is used
//        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);
//        MatOfDMatch matches = new MatOfDMatch();
//        matcher.match(descriptors1, descriptors2, matches);
//
//        //-- Draw matches
//        Mat imgMatches = new Mat();
//        Features2d.drawMatches(img1, keypoints1, img2, keypoints2, matches, imgMatches);
//
//        HighGui.imshow("Matches", imgMatches);
//        HighGui.waitKey(0);
//
//
//    }

    public double orbDescriptor(Mat img1, Mat img2, double intrinsicFocal, double distanceM){


        //firstFrame_, , , initPts1_
        Mat mask = new Mat(img1.rows(), img1.cols(), CvType.CV_8UC1, Scalar.all(0));
        Imgproc.circle(mask, firstPoint_, 120, new Scalar( 255, 255, 255), -1, 8, 0);
        Imgproc.circle(mask, secondPoint_, 120, new Scalar( 255, 255, 255), -1, 8, 0);

//        Imgproc.goodFeaturesToTrack(img1, firstCorners_, 10, 0.3, 7.0, mask, 7);
        Mat cropped = new Mat();
        img1.copyTo(cropped, mask);

        Mat mask2 = new Mat(img2.rows(), img2.cols(), CvType.CV_8UC1, Scalar.all(0));
        Imgproc.circle(mask2, firstOutPoint_, 120, new Scalar( 255, 255, 255), -1, 8, 0);
        Imgproc.circle(mask2, secondOutPoint_, 120, new Scalar( 255, 255, 255), -1, 8, 0);

        Mat cropped2 = new Mat();
        img2.copyTo(cropped2, mask2);


        ORB orb = ORB.create();
        //orb.setMaxFeatures(100);
        MatOfKeyPoint kpts1 = new MatOfKeyPoint(), kpts2 = new MatOfKeyPoint();
        Mat desc1 = new Mat(), desc2 = new Mat();
        orb.detectAndCompute(cropped, new Mat(), kpts1, desc1);
        orb.detectAndCompute(cropped2, new Mat(), kpts2, desc2);

        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

        List<MatOfDMatch> matches = new ArrayList<MatOfDMatch>();
        matcher.knnMatch(desc1, desc2, matches, 2);
        // ratio test
        LinkedList<DMatch> good_matches = new LinkedList<DMatch>();
        for (Iterator<MatOfDMatch> iterator = matches.iterator(); iterator.hasNext();) {
            MatOfDMatch matOfDMatch = (MatOfDMatch) iterator.next();
            if (matOfDMatch.toArray()[0].distance / matOfDMatch.toArray()[1].distance < 0.9) {
                good_matches.add(matOfDMatch.toArray()[0]);
            }
        }

        // get keypoint coordinates of good matches to find homography and remove outliers using ransac
        List<Point> pts1 = new ArrayList<Point>();
        List<Point> pts2 = new ArrayList<Point>();
        for(int i = 0; i<good_matches.size(); i++){
            pts1.add(kpts1.toList().get(good_matches.get(i).queryIdx).pt);
            pts2.add(kpts2.toList().get(good_matches.get(i).trainIdx).pt);
        }

        // convertion of data types - there is maybe a more beautiful way
        Mat outputMask = new Mat();
        MatOfPoint2f pts1Mat = new MatOfPoint2f();
        pts1Mat.fromList(pts1);
        MatOfPoint2f pts2Mat = new MatOfPoint2f();
        pts2Mat.fromList(pts2);

        // Find homography - here just used to perform match filtering with RANSAC, but could be used to e.g. stitch images
        // the smaller the allowed reprojection error (here 15), the more matches are filtered
        Mat Homog = Calib3d.findHomography(pts1Mat, pts2Mat, Calib3d.RANSAC, 15, outputMask, 2000, 0.995);


        // outputMask contains zeros and ones indicating which matches are filtered
        LinkedList<DMatch> better_matches = new LinkedList<DMatch>();
        for (int i = 0; i < good_matches.size(); i++) {
            if (outputMask.get(i, 0)[0] != 0.0) {
                better_matches.add(good_matches.get(i));
            }
        }

        // DRAWING OUTPUT
        Mat outputImg = new Mat();
        // this will draw all matches, works fine
        MatOfDMatch better_matches_mat = new MatOfDMatch();
        better_matches_mat.fromList(better_matches);
        Features2d.drawMatches(img1, kpts1, img2, kpts2, better_matches_mat, outputImg);

       // better_matches_mat.toList().get(0)




//        BFMatcher bf = BFMatcher.create(NORM_HAMMING, true);
//        MatOfDMatch obtainedMatches = new MatOfDMatch();
//        bf.match(desc1,desc2, obtainedMatches);
//
//        Mat imgMatches = new Mat();
//        obtainedMatches.fromList((obtainedMatches.toList().subList(0,50).sort());
//        Features2d.drawMatches(cropped, kpts1, cropped2, kpts2,obtainedMatches , imgMatches);
//

//        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
//        MatOfDMatch matches = new MatOfDMatch();
//        matcher.match(desc1, desc2, matches);
//
//
//
//        Mat imgMatches = new Mat();
//
//
//         Features2d.drawMatches(cropped, kpts1, cropped2, kpts2, matches, imgMatches);
//
//
//        List<MatOfDMatch> knnMatches = new ArrayList<>();
//        matcher.knnMatch(desc1, desc2, knnMatches, 2);
//
//        float ratioThreshold = 0.8f; // Nearest neighbor matching ratio
//        List<KeyPoint> listOfMatched1 = new ArrayList<>();
//        List<KeyPoint> listOfMatched2 = new ArrayList<>();
//        List<KeyPoint> listOfKeypoints1 = kpts1.toList();
//        List<KeyPoint> listOfKeypoints2 = kpts2.toList();
////        for (int i = 0; i < knnMatches.size(); i++) {
////            DMatch[] matches2 = knnMatches.get(i).toArray();
////            float dist1 = matches2[0].distance;
////            float dist2 = matches2[1].distance;
////            if (dist1 < ratioThreshold * dist2) {
////                listOfMatched1.add(listOfKeypoints1.get(matches2[0].queryIdx));
////                listOfMatched2.add(listOfKeypoints2.get(matches2[0].trainIdx));
////            }
////        }
//
//
//
//        MatOfKeyPoint kpts01 = new MatOfKeyPoint(), kpts02 = new MatOfKeyPoint();
//        kpts01.fromList(listOfKeypoints1);
//        kpts02.fromList(listOfKeypoints2);
//
//        System.out.println("ANSWER " + listOfMatched1.size());
//        System.out.println("ANSWER " + listOfMatched2.size());
//
//
//        Features2d.drawKeypoints(cropped,kpts01, cropped );
//        Features2d.drawKeypoints(cropped2,kpts02, cropped2 );
//
//        Features2d.drawMatches(cropped, kpts01, cropped2, kpts02, matches, imgMatches);
//

        Imgcodecs.imwrite(videoFile_.getParent()+"/akaze_result1.png", cropped);
        Imgcodecs.imwrite(videoFile_.getParent()+"/akaze_result2.png", cropped2);






        List<KeyPoint> check = kpts1.toList();
        List<KeyPoint> check2 = kpts2.toList();
        List<Point> p1s = new ArrayList<>();
        List<Point> p2s = new ArrayList<>();



        double num_matches = better_matches.size();
        for(int i = 0; i < num_matches; i++){
            int idx1=better_matches.get(i).queryIdx;
            Imgproc.circle(outputImg, check.get(idx1).pt, 20, new Scalar(155, 0, 255), 5);
            p1s.add(check.get(idx1).pt);

            int idx2=better_matches.get(i).trainIdx;
            Imgproc.circle(img2, check2.get(idx2).pt, 20, new Scalar(155, 0, 255), 5);
            p2s.add(check2.get(idx2).pt);

            //int idx2=better_matches.get(i).queryIdx;

        }
      //  for(int i = 0; i < check.size(); i++){

      //  }

        Mat cameraP = getCameraParam(intrinsicFocal);


        Mat firstFramePoints = Converters.vector_Point_to_Mat(p1s);
        Mat lastFramePoints = Converters.vector_Point_to_Mat(p2s);


       // Mat pt1_norm = undistortPoints(firstFramePoints, cameraP, None);

//        Mat E = Calib3d.findEssentialMat(firstFramePoints, lastFramePoints, cameraP);
        Mat E =  Calib3d.findEssentialMat(firstFramePoints, lastFramePoints);


        Mat r = new Mat(3, 3, CV_64F);
        Mat t = new Mat(3, 1, CV_64F);

        Calib3d.recoverPose(E, firstFramePoints, lastFramePoints,  cameraP, r, t );

//        Mat R = new Mat(3,3,CvType.CV_64F);
//        System.out.println(" rold \t" +  r.dump());
//        System.out.println(" told \t" +  t.dump());
//
//        Calib3d.Rodrigues(r, R);
//        R = R.t();
//        Core.multiply(R, Scalar.all(-1), R);
//
//        Mat tt = new Mat(3,1,CvType.CV_64F);

        System.out.println(" Eessen \t" +  E.dump());
//
//
//        Core.gemm(R, t, 1, new Mat(), 0, tt, 0);
//        Calib3d.Rodrigues(R, r);
//
//        System.out.println(" rnew \t" +  r.dump());
//        System.out.println(" tnew \t" +  tt.dump());










//        # Normalize for Esential Matrix calaculation
//                pts_l_norm = cv2.undistortPoints(np.expand_dims(pts_l, axis=1), cameraMatrix=K_l, distCoeffs=None)
//        pts_r_norm = cv2.undistortPoints(np.expand_dims(pts_r, axis=1), cameraMatrix=K_r, distCoeffs=None)
//
//        E, mask = cv2.findEssentialMat(pts_l_norm, pts_r_norm, focal=1.0, pp=(0., 0.), method=cv2.RANSAC, prob=0.999, threshold=3.0)
//        points, R, t, mask = cv2.recoverPose(E, pts_l_norm, pts_r_norm)
//
//        M_r = np.hstack((R, t))
//        M_l = np.hstack((np.eye(3, 3), np.zeros((3, 1))))
//
//        P_l = np.dot(K_l,  M_l)
//        P_r = np.dot(K_r,  M_r)
//        point_4d_hom = cv2.triangulatePoints(P_l, P_r, np.expand_dims(pts_l, axis=1), np.expand_dims(pts_r, axis=1))
//        point_4d = point_4d_hom / np.tile(point_4d_hom[-1, :], (4, 1))
//        point_3d = point_4d[:3, :].T


        saveFrame(0004, outputImg);
        saveFrame(0003, img1);
        saveFrame(0002, img2);




        System.out.println( " rotate " + r.dump());
        System.out.println( " translate " + t.dump());


     List<Mat> add = new ArrayList<>();
     add.add(r);
     add.add(t);
        Core.hconcat(add,r);



        System.out.println( "rotate and translate " + r.dump());

        Mat p1 =  getPrjectionMatrix1(intrinsicFocal);
        Mat p2 = new Mat();


        Core.gemm(getCameraParam(intrinsicFocal), r, 1, new Mat(), 0, p2, 0);

        Mat input1 = new Mat(2, 2, CV_64F);


        input1.put(0,0, firstPoint_.x);
        input1.put(1,0, firstPoint_.y);

        input1.put(0,1,secondPoint_.x);
        input1.put(1,1,secondPoint_.y);

        Mat input2 = new Mat(2, 2, CV_64F);

        input2.put(0,0, firstOutPoint_.x);
        input2.put(1,0, firstOutPoint_.y);

        input2.put(0,1,secondOutPoint_.x);
        input2.put(1,1,secondOutPoint_.y);

//        input2.put(0,0, firstOutPoint_.x, secondOutPoint_.x);
//        input2.put(1,0, firstOutPoint_.y, secondOutPoint_.y);


        Mat output = new Mat(4, 2, CV_64F);

        Calib3d.triangulatePoints(p1, p2, input1, input2, output);

//        Mat pot = new Mat();
//        Calib3d.convertPointsFromHomogeneous(output, pot);
        System.out.println( "4d " + output.dump());
//        System.out.println( "3d " + pot.dump());





        double x1 = output.get(0, 0)[0]/output.get(3, 0)[0];
        double x2 = output.get(0, 1)[0]/output.get(3, 1)[0];

        double y1 = output.get(1, 0)[0]/output.get(3, 0)[0];
        double y2 = output.get(1, 1)[0]/output.get(3, 1)[0];

        double z1 = output.get(2, 0)[0]/output.get(3, 0)[0];
        double z2 = output.get(2, 1)[0]/output.get(3, 1)[0];

        Log.d("x1 " ,String.valueOf(output.get(0, 0)[0]/output.get(3, 0)[0]));
        Log.d("x2 " ,String.valueOf(output.get(0, 1)[0]/output.get(3, 1)[0]));

        Log.d("y1 " ,String.valueOf(output.get(1, 0)[0]/output.get(3, 0)[0]));
        Log.d("y2 " ,String.valueOf(output.get(1, 1)[0]/output.get(3, 1)[0]));

        Log.d("z1 " ,String.valueOf(output.get(2, 0)[0]/output.get(3, 0)[0]));
        Log.d("z2 " ,String.valueOf(output.get(2, 1)[0]/output.get(3, 1)[0]));

        double ans  = Math.sqrt(Math.pow(x1-x2,2) + Math.pow(y1 - y2, 2) + Math.pow(z1-z2,2))*distanceM;

        Log.d("ans ", String.valueOf(ans));


        Log.d("dist ", String.valueOf(distanceM));
        Log.d("focal ", String.valueOf(intrinsicFocal));

        return ans;
    }

    private double checkBlur(Mat img1) {
        int threshold = 110;
        Mat test = new Mat();
        Imgproc.Laplacian(img1, test, 3);
        MatOfDouble median = new MatOfDouble();
        MatOfDouble std= new MatOfDouble();
        Core.meanStdDev(test, median , std);

        double score = Math.pow(std.get(0,0)[0],2);

//        if(score > threshold){
//            return true;
//        }
        return score;
    }

    public void goodPoints(Mat inputFrame, MatOfPoint corners, Point point, MatOfPoint2f initPts) {

        Mat mask = new Mat(inputFrame.rows(), inputFrame.cols(), CvType.CV_8UC1, Scalar.all(0));
        Imgproc.circle(mask, point, 50, new Scalar( 255, 255, 255), -1, 8, 0);
        Imgproc.goodFeaturesToTrack(inputFrame, corners, 100, 0.1, 10.0, mask, 7, true, 0.04);
       //Imgproc.goodFeaturesToTrack();
        Mat cropped = new Mat();
        inputFrame.copyTo(cropped, mask);
        saveFrame(next, cropped);
        initPts.fromList(corners.toList());
        next++;


    }



}