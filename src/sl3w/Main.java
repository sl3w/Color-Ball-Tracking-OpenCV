package sl3w;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import javax.swing.*;
import java.util.ArrayList;

public class Main {
    //Switching Debug Mode
    private static final boolean IS_DEBUGGING = true;
    //Counter to track count of dots changing
    private static short counterNotChanching = 0;
    //Counter of bad sigma
    private static short counterOfBadSigma = 0;

    //Frames
    //Main frame - always to show
    private static final JFrame frame1 = new JFrame("Camera");
    //HSV frame - only in debug mode show
    private static final JFrame frame2 = new JFrame("HSV");
    //Mask frame - only in debug mode show
    private static final JFrame frame3 = new JFrame("Mask");

    //Panel for frames above
    private static final Panel panel1 = new Panel();
    private static final Panel panel2 = new Panel();
    private static final Panel panel3 = new Panel();

    //Output "images" for panels above
    private static Mat outputImage1;
    private static Mat outputImage2;
    private static Mat outputImage3;

    //Arrays of HSV ranges: min and max
    private static final ArrayList<Scalar> hsv_minAr = new ArrayList<>();
    private static final ArrayList<Scalar> hsv_maxAr = new ArrayList<>();

    //Arrays for tracked objects
    private static final ArrayList<Integer> trackedX = new ArrayList<>();
    private static final ArrayList<Integer> trackedY = new ArrayList<>();

    //Check all contours or maximum size contour
    private static final boolean IS_MAX_CONTOUR_ONLY = false;

    //Dots on screen to track history
    private static final int COUNT_DOTS_TRACK_HISTORY = 20;

    public static void main(String[] args) {
        // Load the native library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        initFrames();
        VideoCapture videoCapture = new VideoCapture(0);
        initVideoCapture(videoCapture);
        initHsvRanges();

        if (videoCapture.isOpened()) {
            trackObject(videoCapture);
        }
    }

    //Frames init
    private static void initFrames() {
        frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame1.setSize(640, 480);
        frame1.setBounds(0, 0, frame1.getWidth(), frame1.getHeight());
        frame1.setContentPane(panel1);
        frame1.setVisible(true);

        frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame2.setSize(640, 480);
        frame2.setBounds(300, 100, frame2.getWidth() + 300, 100 + frame2.getHeight());

        frame3.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame3.setSize(640, 480);
        frame3.setBounds(900, 300, frame2.getWidth() + 900, 300 + frame2.getHeight());

        if (IS_DEBUGGING) {
            frame2.setContentPane(panel2);
            frame2.setVisible(true);
            frame3.setContentPane(panel3);
            frame3.setVisible(true);
        }
    }

    //VideoCapture init
    private static void initVideoCapture(VideoCapture videoCapture) {
        videoCapture.set(3, 1366);
        videoCapture.set(4, 768);
        videoCapture.set(15, -2);

        Mat webcam_image = new Mat();
        videoCapture.read(webcam_image);
        frame1.setSize(webcam_image.width() + 40, webcam_image.height() + 60);
        if (IS_DEBUGGING) {
            frame2.setSize(webcam_image.width() + 40, webcam_image.height() + 60);
            frame3.setSize(webcam_image.width() + 40, webcam_image.height() + 60);
        }
    }

    //HSV ranges init
    private static void initHsvRanges() {
        hsv_minAr.add(new Scalar(8, 185, 120));
        hsv_maxAr.add(new Scalar(13, 225, 170));

        hsv_minAr.add(new Scalar(6, 210, 210));
        hsv_maxAr.add(new Scalar(8, 220, 220));

        hsv_minAr.add(new Scalar(6, 160, 110));
        hsv_maxAr.add(new Scalar(7, 180, 130));

        hsv_minAr.add(new Scalar(6, 160, 50));
        hsv_maxAr.add(new Scalar(9, 210, 120));

        hsv_minAr.add(new Scalar(4, 170, 80));
        hsv_maxAr.add(new Scalar(9, 190, 120));

        hsv_minAr.add(new Scalar(0, 60, 40));
        hsv_maxAr.add(new Scalar(4, 100, 80));

        hsv_minAr.add(new Scalar(16, 160, 240));
        hsv_maxAr.add(new Scalar(18, 200, 255));

        hsv_minAr.add(new Scalar(15, 200, 230));
        hsv_maxAr.add(new Scalar(17, 255, 255));

        hsv_minAr.add(new Scalar(11, 190, 160));
        hsv_maxAr.add(new Scalar(15, 240, 255));

        hsv_minAr.add(new Scalar(5, 130, 60));
        hsv_maxAr.add(new Scalar(8, 170, 130));

        hsv_minAr.add(new Scalar(8, 170, 80));
        hsv_maxAr.add(new Scalar(10, 220, 140));
    }

    //Checking i contour to round
    private static void checkToRound(int i, ArrayList<MatOfPoint> contours, Mat circles,
                                        ArrayList<MatOfPoint> neededContours, Mat webcam_image) {
        Point center = new Point();
        float[] radius = new float[1];
        Imgproc.minEnclosingCircle(
                new MatOfPoint2f(contours.get(i).toArray()),
                center, radius);

        for (int j = 0; j < circles.cols(); j++) {
            double[] circle = circles.get(0, j);
            Point center1 = new Point(Math.round(circle[0]), Math.round(circle[1]));
            double radius1 = Math.round(circle[2]);

            if (center.x > center1.x - radius1 && center.x < center1.x + radius1 &&
                    center.y > center1.y - radius1 && center.y < center1.y + radius1) {
                neededContours.add(contours.get(i));

                if (IS_DEBUGGING) {
                    Imgproc.circle(webcam_image, center, (int) radius[0],
                            new Scalar(255, 0, 0));
                }

                trackedX.add((int) center1.x);
                trackedY.add((int) center1.y);

                counterNotChanching = 0;
                break;
            }
        }
    }

    //Main method to track objects
    private static void trackObject(VideoCapture videoCapture) {
        Mat webcam_image, hsv_image, mask, maskCopy, circles;

        while (true) {
            counterNotChanching++;
            videoCapture.read(webcam_image = new Mat());
            if (!webcam_image.empty()) {

                Imgproc.cvtColor(webcam_image, hsv_image = new Mat(), Imgproc.COLOR_BGR2HSV);

                Core.inRange(hsv_image, hsv_minAr.get(0), hsv_maxAr.get(0), mask = new Mat());
                Core.inRange(hsv_image, hsv_minAr.get(0), hsv_maxAr.get(0), maskCopy = new Mat());


                Imgproc.erode(mask, mask, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(15, 15)));
                Imgproc.dilate(mask, mask, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(15, 15)));

                Imgproc.GaussianBlur(mask, mask, new Size(11, 11), 0, 0);
                Imgproc.GaussianBlur(maskCopy, maskCopy, new Size(11, 11), 0, 0);

                for (int i = 1; i < hsv_maxAr.size(); i++) {
                    Mat curMask = new Mat();
                    Core.inRange(hsv_image, hsv_minAr.get(i), hsv_maxAr.get(i), curMask);
                    Imgproc.erode(curMask, curMask, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(15, 15)));
                    Imgproc.dilate(curMask, curMask, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(15, 15)));
                    Imgproc.GaussianBlur(curMask, curMask, new Size(11, 11), 0, 0);

                    Core.bitwise_or(mask, curMask, mask);
                    Core.bitwise_or(maskCopy, curMask, maskCopy);
                }

                ArrayList<MatOfPoint> contours = new ArrayList<>();

                Imgproc.findContours(mask, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
                Imgproc.drawContours(mask, contours, -1, new Scalar(255, 0, 0), 2);

                circles = new Mat();
                circles.release();
                Imgproc.HoughCircles(mask, circles, Imgproc.CV_HOUGH_GRADIENT, 2, mask.height() / 8, 200, 100, 10, 5000);

                if (circles.dataAddr() > 0) {
                    ArrayList<MatOfPoint> neededContours = new ArrayList<>();
                    if (IS_MAX_CONTOUR_ONLY) {
                        int maxI = 0;
                        double maxS = Imgproc.contourArea(contours.get(0));
                        for (int i = 1; i < contours.size(); i++) {
                            double s = Imgproc.contourArea(contours.get(i));
                            if (s > maxS) {
                                maxI = i;
                            }
                        }
                        checkToRound(maxI, contours, circles, neededContours, webcam_image);
                    } else {
                        for (int i = 0; i < contours.size(); i++) {
                            checkToRound(i, contours, circles, neededContours, webcam_image);
                        }
                    }
                    contours = neededContours;
                }

                if (IS_DEBUGGING) {
                    Imgproc.drawContours(webcam_image, contours, -1, new Scalar(0, 0, 255), 2);
                    Imgproc.drawContours(maskCopy, contours, -1, new Scalar(255, 0, 0), 2);
                }

                int toRis = trackedX.size() - COUNT_DOTS_TRACK_HISTORY;
                if (trackedX.size() < COUNT_DOTS_TRACK_HISTORY)
                    toRis = 0;
                for (int i = trackedX.size() - 1; i > toRis; i--) {
                    double sigma = Math.abs(Math.pow(trackedX.get(i) - trackedX.get(i - 1), 2) + Math.pow(trackedY.get(i) - trackedY.get(i - 1), 2));

                    if (sigma < 3000 || trackedX.size() < 3) {
                        Imgproc.circle(webcam_image, new Point(trackedX.get(i), trackedY.get(i)), 10, new Scalar(0, 255, 255), Core.FILLED);
                    } else {
                        counterOfBadSigma++;
                        trackedX.remove(i);
                        trackedY.remove(i);
                    }
                }

                if (trackedX.size() > COUNT_DOTS_TRACK_HISTORY) {
                    trackedX.remove(0);
                    trackedY.remove(0);
                }
                if (counterNotChanching >= 50 || counterOfBadSigma > 10) {
                    counterOfBadSigma = 0;
                    trackedX.clear();
                    trackedY.clear();
                }


                double[] data;
                if (IS_DEBUGGING) {
                    Imgproc.line(webcam_image, new Point(150, 50), new Point(202, 200), new Scalar(100, 10, 10)/*CV_BGR(100,10,10)*/, 3);
                    Imgproc.circle(webcam_image, new Point(210, 210), 10, new Scalar(100, 10, 10), 3);
                    data = webcam_image.get(210, 210);
                    String text = "(" + data[0] + "," + data[1] + "," + data[2] + ")";
                    Imgproc.putText(webcam_image, text, new Point(30, 30), Core.FONT_HERSHEY_COMPLEX,
                            1.0, new Scalar(100, 10, 10, 255), 3);
                }
                outputImage1 = webcam_image;

                if (IS_DEBUGGING) {
                    Imgproc.line(hsv_image, new Point(150, 50), new Point(202, 200), new Scalar(100, 10, 10)/*CV_BGR(100,10,10)*/, 3);
                    Imgproc.circle(hsv_image, new Point(210, 210), 10, new Scalar(100, 10, 10), 3);
                    data = hsv_image.get(210, 210);
                    if (data.length > 1) {
                        String text = "(" + data[0] + "," + data[1] + "," + data[2] + ")";
                        Imgproc.putText(hsv_image, text, new Point(30, 30), Core.FONT_HERSHEY_COMPLEX,
                                1.0, new Scalar(100, 10, 10, 255), 3);
                    }
                    outputImage2 = hsv_image;
                }

                if (IS_DEBUGGING && !maskCopy.empty()) {
                    Imgproc.line(maskCopy, new Point(150, 50), new Point(202, 200), new Scalar(100, 10, 10)/*CV_BGR(100,10,10)*/, 3);
                    Imgproc.circle(maskCopy, new Point(210, 210), 10, new Scalar(100, 10, 10), 3);
                    data = maskCopy.get(210, 210);
                    if (data.length > 1) {
                        String text = "(" + data[0] + "," + data[1] + "," + data[2] + ")";
                        Imgproc.putText(maskCopy, text, new Point(30, 30), Core.FONT_HERSHEY_COMPLEX,
                                1.0, new Scalar(100, 10, 10, 255), 3);
                    }
                    outputImage3 = maskCopy;
                }

                panel1.setimagewithMat(outputImage1);
                frame1.repaint();

                if (IS_DEBUGGING) {
                    panel2.setimagewithMat(outputImage2);
                    frame2.repaint();

                    panel3.setimagewithMat(outputImage3);
                    frame3.repaint();
                }
            } else {
                System.out.println("Error: No captured frame! Stop.");
                break;
            }
        }
    }
}

