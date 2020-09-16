import org.opencv.core.Point;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.opencv.videoio.VideoCapture;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

class Panel extends JPanel {
    private static final long serialVersionUID = 1L;
    private BufferedImage image;


    // Create a constructor method
    public Panel() {
        super();
    }

    private BufferedImage getimage() {
        return image;
    }

    public void setimage(BufferedImage newimage) {
        image = newimage;
        return;
    }

    public void setimagewithMat(Mat newimage) {
        image = this.matToBufferedImage(newimage);
        return;
    }

    /**
     * Converts/writes a Mat into a BufferedImage.
     *
     * @param matrix Mat of type CV_8UC3 or CV_8UC1
     * @return BufferedImage of type TYPE_3BYTE_BGR or TYPE_BYTE_GRAY
     */
    public BufferedImage matToBufferedImage(Mat matrix) {
        int cols = matrix.cols();
        int rows = matrix.rows();
        int elemSize = (int) matrix.elemSize();
        byte[] data = new byte[cols * rows * elemSize];
        int type;
        matrix.get(0, 0, data);
        switch (matrix.channels()) {
            case 1:
                type = BufferedImage.TYPE_BYTE_GRAY;
                break;
            case 3:
                type = BufferedImage.TYPE_3BYTE_BGR;
                // bgr to rgb
                byte b;
                for (int i = 0; i < data.length; i = i + 3) {
                    b = data[i];
                    data[i] = data[i + 2];
                    data[i + 2] = b;
                }
                break;
            default:
                return null;
        }
        BufferedImage image2 = new BufferedImage(cols, rows, type);
        image2.getRaster().setDataElements(0, 0, cols, rows, data);
        return image2;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        //BufferedImage temp=new BufferedImage(640, 480, BufferedImage.TYPE_3BYTE_BGR);
        BufferedImage temp = getimage();
        //Graphics2D g2 = (Graphics2D)g;
        if (temp != null)
            g.drawImage(temp, 10, 10, temp.getWidth(), temp.getHeight(), this);
    }
}

public class ledObjectTrack {

    static ArrayList<Integer> momx = new ArrayList<>();
    static ArrayList<Integer> momy = new ArrayList<>();
    static int c = 0;
    static boolean tryKrugi = true;
    static Mat output1;
    static Mat output2;
    static Mat output3;
    static Mat output4;

    static boolean myDebug = false;

    public static void main(String arg[]) {
        // Load the native library.
        //int line = 0;
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        // It is better to group all frames together so cut and paste to
        // create more frames is easier
        try {
            trackGreen();
        } catch (AWTException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return;
    }

    private static void trackGreen() throws AWTException {

        short counterNotChanching = 0;
//		Mouse mouse = new Mouse();
        JFrame frame1 = new JFrame("Camera");
        frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame1.setSize(640, 480);
        frame1.setBounds(0, 0, frame1.getWidth(), frame1.getHeight());
        Panel panel1 = new Panel();
        frame1.setContentPane(panel1);
        frame1.setVisible(true);

        JFrame frame2 = new JFrame("HSV");
        frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame2.setSize(640, 480);
        frame2.setBounds(300, 100, frame2.getWidth() + 300, 100 + frame2.getHeight());
        Panel panel2 = new Panel();
        if (myDebug) {
            frame2.setContentPane(panel2);
            frame2.setVisible(true);
        }
		/*JFrame frame3 = new JFrame("S,V Distance");
                      frame3.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                      frame3.setSize(640,480);
                      frame3.setBounds(600,200, frame3.getWidth()+600, 200+frame3.getHeight());  
                      Panel panel3 = new Panel();  
                      frame3.setContentPane(panel3);  
                      frame3.setVisible(true);*/
        JFrame frame4 = new JFrame("Threshold");
        frame4.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame4.setSize(640, 480);
        frame4.setBounds(900, 300, frame2.getWidth() + 900, 300 + frame2.getHeight());
        Panel panel4 = new Panel();
        if (myDebug) {
            frame4.setContentPane(panel4);
            frame4.setVisible(true);
        }
        //-- 2. Read the video stream
        VideoCapture capture = new VideoCapture(0);
        //capture.set(10, 0);
        capture.set(3, 1366);
        capture.set(4, 768);
        capture.set(15, -2);
        Mat webcam_image = new Mat();
        Mat hsv_image = new Mat();
        Mat hsv_image2 = new Mat();
        Mat thresholded = new Mat();
        Mat thresholded2 = new Mat();
        Mat thresholded3 = new Mat();
        Mat thresholded4 = new Mat();
        ArrayList<Mat> thresholdedAr = new ArrayList<>();
        capture.read(webcam_image);
        frame1.setSize(webcam_image.width() + 40, webcam_image.height() + 60);
        frame2.setSize(webcam_image.width() + 40, webcam_image.height() + 60);
        //frame3.setSize(webcam_image.width()+40,webcam_image.height()+60);
        frame4.setSize(webcam_image.width() + 40, webcam_image.height() + 60);
        Mat array255 = new Mat(webcam_image.height(), webcam_image.width(), CvType.CV_8UC1);
        array255.setTo(new Scalar(255));
		/*Mat S=new Mat();  
                      S.ones(new Size(hsv_image.width(),hsv_image.height()),CvType.CV_8UC1);  
                      Mat V=new Mat();  
                      V.ones(new Size(hsv_image.width(),hsv_image.height()),CvType.CV_8UC1);  
                          Mat H=new Mat();  
                      H.ones(new Size(hsv_image.width(),hsv_image.height()),CvType.CV_8UC1);*/
        Mat distance = new Mat(webcam_image.height(), webcam_image.width(), CvType.CV_8UC1);
        //new Mat();//new Size(webcam_image.width(),webcam_image.height()),CvType.CV_8UC1);
        List<Mat> lhsv = new ArrayList<Mat>(3);
        Mat circles = new Mat(); // No need (and don't know how) to initialize it.
        // The function later will do it... (to a 1*N*CV_32FC3)
//        Scalar hsv_min = new Scalar(8, 165, 90, 0);
//        Scalar hsv_max = new Scalar(13, 225, 230, 0);
        Scalar hsv_min = new Scalar(8, 185, 120, 0);
        Scalar hsv_max = new Scalar(13, 225, 170, 0);

//        Scalar hsv_min9 = new Scalar(7, 160, 75, 0);
//        Scalar hsv_max9 = new Scalar(10, 220, 110, 0);

        Scalar hsv_min9 = new Scalar(6, 210, 210, 0);
        Scalar hsv_max9 = new Scalar(8, 220, 220, 0);

        Scalar hsv_min6 = new Scalar(6, 160, 110, 0);
        Scalar hsv_max6 = new Scalar(7, 180, 130, 0);

        Scalar hsv_min7 = new Scalar(0, 90, 50, 0);
        Scalar hsv_max7 = new Scalar(7, 170, 100, 0);

        Scalar hsv_min8 = new Scalar(6, 160, 50, 0);
        Scalar hsv_max8 = new Scalar(9, 210, 120, 0);

        Scalar hsv_min10 = new Scalar(4, 170, 80, 0);
        Scalar hsv_max10 = new Scalar(9, 190, 120, 0);

        Scalar hsv_min11 = new Scalar(0, 60, 40, 0);
        Scalar hsv_max11 = new Scalar(4, 100, 80, 0);

        Scalar hsv_min2 = new Scalar(16, 160, 240, 0);
        Scalar hsv_max2 = new Scalar(18, 200, 255, 0);
//
        Scalar hsv_min3 = new Scalar(15, 200, 230, 0);
        Scalar hsv_max3 = new Scalar(17, 255, 255, 0);

        Scalar hsv_min4 = new Scalar(12, 190, 160, 0);
        Scalar hsv_max4 = new Scalar(15, 240, 255, 0);

        Scalar hsv_min5 = new Scalar(5, 130, 60, 0);
        Scalar hsv_max5 = new Scalar(8, 170, 130, 0);

        Scalar hsv_min12 = new Scalar(8, 170, 80, 0);
        Scalar hsv_max12 = new Scalar(10, 220, 130, 0);

//        Scalar hsv_min13 = new Scalar(5, 30, 10, 0);
//        Scalar hsv_max13 = new Scalar(20, 90, 40, 0);

        ArrayList<Scalar> hsv_minAr = new ArrayList<>();
        ArrayList<Scalar> hsv_maxAr = new ArrayList<>();

        hsv_minAr.add(hsv_min);
        hsv_minAr.add(hsv_min2);
        hsv_minAr.add(hsv_min3);
        hsv_minAr.add(hsv_min4);
        hsv_minAr.add(hsv_min5);
        hsv_minAr.add(hsv_min6);
//        hsv_minAr.add(hsv_min7);
        hsv_minAr.add(hsv_min8);
        hsv_minAr.add(hsv_min9);
        hsv_minAr.add(hsv_min10);
        hsv_minAr.add(hsv_min11);
        hsv_minAr.add(hsv_min12);
//        hsv_minAr.add(hsv_min13);

        hsv_maxAr.add(hsv_max);
        hsv_maxAr.add(hsv_max2);
        hsv_maxAr.add(hsv_max3);
        hsv_maxAr.add(hsv_max4);
        hsv_maxAr.add(hsv_max5);
        hsv_maxAr.add(hsv_max6);
//        hsv_maxAr.add(hsv_max7);
        hsv_maxAr.add(hsv_max8);
        hsv_maxAr.add(hsv_max9);
        hsv_maxAr.add(hsv_max10);
        hsv_maxAr.add(hsv_max11);
        hsv_maxAr.add(hsv_max12);
//        hsv_maxAr.add(hsv_max13);

        //Scalar hsv_min2 = new Scalar(0,0,125, 0);
        //Scalar hsv_max2 = new Scalar(0,0, 255, 0);
        double[] data = new double[3];
        if (capture.isOpened()) {
            while (true) {
                counterNotChanching++;
                capture.read(webcam_image);
                if (!webcam_image.empty()) {
                    thresholdedAr = new ArrayList<>();
                    // One way to select a range of colors by Hue
                    Imgproc.cvtColor(webcam_image, hsv_image, Imgproc.COLOR_BGR2HSV);
//					System.out.println(hsv_image);
                    Core.inRange(hsv_image, hsv_min, hsv_max, thresholded);
                    Core.inRange(hsv_image, hsv_min, hsv_max, thresholded3);

                    for (int i = 1; i < hsv_maxAr.size(); i++) {
                        thresholded4 = new Mat();
                        Core.inRange(hsv_image, hsv_minAr.get(i), hsv_maxAr.get(i), thresholded4);
                        Imgproc.erode(thresholded4, thresholded4, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(15, 15)));
                        Imgproc.dilate(thresholded4, thresholded4, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(15, 15)));
                        Imgproc.GaussianBlur(thresholded4, thresholded4, new Size(11, 11), 0, 0);

                        thresholdedAr.add(thresholded4);
//                        thresholded4.release();
                    }
//                    Core.inRange(hsv_image, hsv_min2, hsv_max2, thresholded3);
//                    Core.inRange(hsv_image, hsv_min3, hsv_max3, thresholded4);
                    //Core.inRange(hsv_image, hsv_min2, hsv_max2, thresholded2);
                    //Core.bitwise_or(thresholded, thresholded2, thresholded);
                    Imgproc.erode(thresholded, thresholded, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(15, 15)));
//                    Imgproc.erode(thresholded3, thresholded3, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(15, 15)));
//                    Imgproc.erode(thresholded4, thresholded4, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(15, 15)));
                    Imgproc.dilate(thresholded, thresholded, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(15, 15)));
//                    Imgproc.dilate(thresholded3, thresholded3, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(15, 15)));
//                    Imgproc.dilate(thresholded4, thresholded4, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(15, 15)));
                    // Notice that the thresholds don't really work as a "distance"
                    // Ideally we would like to cut the image by hue and then pick just
                    // the area where S combined V are largest.
                    // Strictly speaking, this would be something like sqrt((255-S)^2+(255-V)^2)>Range
                    // But if we want to be "faster" we can do just (255-S)+(255-V)>Range
                    // Or otherwise 510-S-V>Range
                    // Anyhow, we do the following... Will see how fast it goes...
//					Core.split(hsv_image, lhsv); // We get 3 2D one channel Mats
//					Mat S = lhsv.get(1);
//					Mat V = lhsv.get(2);
//					Core.subtract(array255, S, S);
//					Core.subtract(array255, V, V);
//					S.convertTo(S, CvType.CV_32F);
//					V.convertTo(V, CvType.CV_32F);
//					Core.magnitude(S, V, distance);
//					Core.inRange(distance,new Scalar(0.0), new Scalar(200.0), thresholded2);
//					Core.bitwise_and(thresholded, thresholded2, thresholded);
                    // Apply the Hough Transform to find the circles
                    Imgproc.GaussianBlur(thresholded, thresholded, new Size(11, 11), 0, 0);
                    Imgproc.GaussianBlur(thresholded3, thresholded3, new Size(11, 11), 0, 0);
//                    Imgproc.GaussianBlur(thresholded4, thresholded4, new Size(11, 11), 0, 0);

//                    thresholded3 = thresholdedAr.get(0);

//                    System.out.println(thresholdedAr.size());
                    for (int i = 0; i < thresholdedAr.size(); i++) {
                        Core.bitwise_or(thresholded, thresholdedAr.get(i), thresholded);
                        Core.bitwise_or(thresholded3, thresholdedAr.get(i), thresholded3);
                    }

//                    thresholded = thresholded3;

//                    Core.bitwise_or(thresholded, thresholded3, thresholded);
//                    Core.bitwise_or(thresholded, thresholded4, thresholded);
                    List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
                    List<MatOfPoint> contours2 = new ArrayList<MatOfPoint>();
                    List<MatOfPoint> contours3 = new ArrayList<MatOfPoint>();

//                    Imgproc.HoughCircles(thresholded, circles, Imgproc.CV_HOUGH_GRADIENT, 2, thresholded.height() / 8, 200, 100, 0, 5000);
//                    Imgproc.findContours(thresholded, circles, thresholded2, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

                    Imgproc.findContours(thresholded, contours, thresholded2, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
//                    Imgproc.drawContours(webcam_image, contours, -1, new Scalar(255, 0, 0), 2);
                    Imgproc.drawContours(thresholded, contours, -1, new Scalar(255, 0, 0), 2);
                    circles.release();
                    Imgproc.HoughCircles(thresholded, circles, Imgproc.CV_HOUGH_GRADIENT, 2, thresholded.height() / 8, 200, 100, 10, 5000);


//                    for (int i = 0; i < contours.size(); i++) {
//                        Point center = new Point();
//                        float[] radius = new float[1];
//                        Imgproc.minEnclosingCircle(
//                                new MatOfPoint2f(contours.get(i).toArray()),
//                                center, radius);
//                        if (radius[0] > 10)
//                        Imgproc.circle(webcam_image, center, (int) radius[0],
//                                new Scalar(255,0,0));
//                    }

//region aaaa
                    long dataAddr = circles.dataAddr();

//                    System.out.println(circles);

                    if (dataAddr > 0) {
//                        int maxI = 0;
//                        double maxS = Imgproc.contourArea(contours.get(0));
//                        for (int i = 1; i < contours.size(); i++) {
////						MatOfPoint2f approx = new MatOfPoint2f();
////						Imgproc.approxPolyDP(new MatOfPoint2f(contours.get(i).toArray()), approx, 0.01 * Imgproc.arcLength(new MatOfPoint2f(contours.get(i).toArray()), true), true);
//                            double s = Imgproc.contourArea(contours.get(i));
//                            if (s > maxS) {
//                                maxI = i;
//                            }
////						if (approx.elemSize() > 8 && s > 50000 && s < 10000)
////							contours2.add(contours.get(i));
//                        }

                        for (int i = 0; i < contours.size(); i++) {
                        Point center = new Point();
                        float[] radius = new float[1];
                        Imgproc.minEnclosingCircle(
                                new MatOfPoint2f(contours.get(i).toArray()),
                                center, radius);
//                        System.out.println(radius[0]);
//                            System.out.println(center);
//                            if(radius[0] > 20)
                        for (int x = 0; x < circles.cols(); x++) {
                            double[] c = circles.get(0, x);
                            Point center1 = new Point(Math.round(c[0]), Math.round(c[1]));
                            double radius1 = Math.round(c[2]);
//                            Imgproc.circle(thresholded, center1, 30, new Scalar(0, 0, 0), Core.FILLED);


                            if (center.x > center1.x - radius1 && center.x < center1.x + radius1 &&
                                    center.y > center1.y - radius1 && center.y < center1.y + radius1/* && radius[0] > 20*/) {
                                contours3.add(contours.get(i));
//                                    System.out.println(radius1);
//                                    System.out.println(radius[0]);

                                Imgproc.drawContours(thresholded3, contours3, -1, new Scalar(255, 0, 0), 2);

                                if (myDebug) {
                                    Imgproc.circle(webcam_image, center, (int) radius[0],
                                            new Scalar(255, 0, 0));
                                }

//                                    Moments moments = Imgproc.moments(thresholded3);
//
//                                    int centerX = (int) (moments.get_m10() / moments.get_m00());
//                                    int centerY = (int) (moments.get_m01() / moments.get_m00());

//                                    momx.add(centerX);
//                                    momy.add(centerY);
                                momx.add((int) center1.x);
                                momy.add((int) center1.y);

//                                    dotsPlused++;
                            }
                        }
                    }
                    }

//                    endregion aaaa
                    contours = contours3;
//                                        Imgproc.drawContours(webcam_image, contours, -1, new Scalar(255, 0, 0), 2);
                    Imgproc.drawContours(thresholded3, contours, -1, new Scalar(255, 0, 0), 2);

                    //region trykrugi
//if (tryKrugi) {
//    Mat fake = webcam_image.clone();
//    Mat gray = new Mat();
//    Imgproc.cvtColor(fake, gray, Imgproc.COLOR_BGR2GRAY);
//    Imgproc.medianBlur(gray, gray, 5);
//    Imgproc.HoughCircles(gray, circles, Imgproc.HOUGH_GRADIENT, 1.0,
//            (double) gray.rows() / 5, // change this value to detect circles with different distances to each other
//            50.0, 30.0, 0, 100); // change the last two parameters
//    // (min_radius & max_radius) to detect larger circles
//
//
////                    Mat fake = webcam_image.clone();
//
//    for (int x = 0; x < circles.cols(); x++) {
//        double[] c = circles.get(0, x);
//        Point center1 = new Point(Math.round(c[0]), Math.round(c[1]));
//        Imgproc.circle(fake, center1, 30, new Scalar(0, 0, 0), Core.FILLED);
//    }
//
//    Mat hsv_fake_image = new Mat();
//
//    Imgproc.cvtColor(fake, hsv_fake_image, Imgproc.COLOR_BGR2HSV);
//
//    Core.inRange(hsv_fake_image, new Scalar(0, 0, 0, 0), new Scalar(0, 0, 0, 0), thresholded3);
//    Imgproc.findContours(thresholded3, contours2, thresholded4, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
//    Imgproc.drawContours(fake, contours2, -1, new Scalar(255, 0, 0), 2);
//
////                    thresholded3 = fake;
//
////                    Core.bitwise_xor(webcam_image, fake, webcam_image);
//
//
////                    webcam_image = fake;
//
//
//    if (contours.size() > 0 && contours2.size() > 0)
//        for (int x = 0; x < contours.size(); x++) {
//            Point center1 = new Point();
//            float[] radius1 = new float[1];
//            Imgproc.minEnclosingCircle(
//                    new MatOfPoint2f(contours.get(x).toArray()),
//                    center1, radius1);
//
////                        double[] c = circles.get(0, x);
////
////                        System.out.println(c);
////                        Point center = new Point(Math.round(c[0]), Math.round(c[1]));
//            // circle center
////                        Imgproc.circle(webcam_image, center, 1, new Scalar(0,100,100), 3, 8, 0 );
//            // circle outline
////                        int radius = (int) Math.round(c[2]);
////                        Imgproc.circle(webcam_image, center, radius, new Scalar(255,0,255), 3, 8, 0 );
//
//            for (int i = 0; i < contours2.size(); i++) {
//                Point center = new Point();
//                float[] radius = new float[1];
//                Imgproc.minEnclosingCircle(
//                        new MatOfPoint2f(contours2.get(i).toArray()),
//                        center, radius);
////                            System.out.println(radius[0]);
////                            System.out.println(center);
////                            if(radius[0] > 20)
//                if (center1.x > center.x - radius1[0] && center1.x < center.x + radius1[0] &&
//                        center1.y > center.y - radius1[0] && center1.y < center.y + radius1[0]) {
//                    contours3.add(contours.get(x));
//                    momx.add((int) center1.x);
//                    momy.add((int) center1.y);
//
//                    System.out.println(c++);
//                }
//            }
//        }
//
//
////                    if (!circles.empty())
////                    Core.bitwise_xor(thresholded, circles, thresholded);
//    //					System.out.println(circles);
////					Mat result = new Mat(img.size(), CvType.CV_8UC3, CvUtils.COLOR_WHITE);
////                    for (int i = 0, r = circles.rows(); i < r; i++) {
////                        for (int j = 0, c = circles.cols(); j < c; j++) {
//////                            double [] data1 = new double[2];
//////                            double[] data1;
////                            double[] circle = circles.get(i, j);
////							Imgproc.circle(result, new Point(circle[0], circle[1]),
////									(int) circle[2], CvUtils.COLOR_BLACK);
//////                            System.out.println(circle);
////                        }
////                    }
//
//					int maxI = 0;
//					double maxS = Imgproc.contourArea(contours.get(0));
//					for (int i = 1; i < contours.size(); i++) {
////						MatOfPoint2f approx = new MatOfPoint2f();
////						Imgproc.approxPolyDP(new MatOfPoint2f(contours.get(i).toArray()), approx, 0.01 * Imgproc.arcLength(new MatOfPoint2f(contours.get(i).toArray()), true), true);
//						double s = Imgproc.contourArea(contours.get(i));
//						if (s > maxS) {
//							maxI = i;
//						}
////						if (approx.elemSize() > 8 && s > 50000 && s < 10000)
////							contours2.add(contours.get(i));
//					}
////
////					contours2.add(contours.get(maxI));
////                    Imgproc.findContours(thresholded, contours, thresholded2, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
//
//
////					for (int i = 0; i < contours.size(); i++) {
////						Point center = new Point();
////						float[] radius = new float[1];
////						Imgproc.minEnclosingCircle(
////								new MatOfPoint2f(contours.get(i).toArray()),
////								center, radius);
////						System.out.println(radius[0]);
////						System.out.println(center);
////						if(radius[0] > 20)
////							contours2.add(contours.get(i));
////					}
//    contours = contours3;
//    panel4.setimagewithMat(fake);
//
//}

                    //endregion
//                    Imgproc.drawContours(webcam_image, contours, -1, new Scalar(255, 0, 0), 2);

//
//                        for (int x = 0; x < circles.cols(); x++) {
//                            double[] c = circles.get(0, x);
//                            Point center1 = new Point(Math.round(c[0]), Math.round(c[1]));
//                            Imgproc.circle(thresholded, center1, 30, new Scalar(0, 0, 0), Core.FILLED);
//                        }


//                    output2 = thresholded;

//                    if (!tryKrugi) {
//                        Moments moments = Imgproc.moments(thresholded);
//
////                        System.out.println(moments);
//                        int centerX = (int) (moments.get_m10() / moments.get_m00());
//                        int centerY = (int) (moments.get_m01() / moments.get_m00());
//
//                        momx.add(centerX);
//                        momy.add(centerY);
//                    }
                    int toRis = momx.size() - 20;
                    if (momx.size() < 20)
                        toRis = 0;
                    for (int i = momx.size() - 1; i > toRis; i--) {
//                        if (momx.size() > 10)
                        double s = Math.abs(Math.pow(momx.get(i) - momx.get(i - 1), 2) + Math.pow(momy.get(i) - momy.get(i - 1), 2));
//
//

//                        if (s > 9000) {
//                            if (i - 1 > 0) {
//                                momx.remove(i - 1);
//                                momy.remove(i - 1);
//                            }
//                        } else
//                            System.out.println(s);
                            if (s < 3000 || momx.size() < 3) {

    Imgproc.circle(webcam_image, new Point(momx.get(i), momy.get(i)), 10, new Scalar(0, 255, 255), Core.FILLED);

}

                    }

                    output1 = webcam_image;


//
                    if ((momx.size() > 10) || (counterNotChanching >= 2 && momx.size() > 0)) {
                        counterNotChanching = 0;
                        momx.remove(0);
                        momy.remove(0);
                    }

                    //System.out.println(contours.size());
                    //Imgproc.Canny(thresholded, thresholded, 500, 250);
                    //-- 4. Add some info to the image
                    if (myDebug) {
                        Imgproc.line(webcam_image, new Point(150, 50), new Point(202, 200), new Scalar(100, 10, 10)/*CV_BGR(100,10,10)*/, 3);
                        Imgproc.circle(webcam_image, new Point(210, 210), 10, new Scalar(100, 10, 10), 3);
                        data = webcam_image.get(210, 210);
                        Imgproc.putText(webcam_image, String.format("(" + String.valueOf(data[0]) + "," + String.valueOf(data[1]) + "," + String.valueOf(data[2]) + ")"), new Point(30, 30), 3 //FONT_HERSHEY_SCRIPT_SIMPLEX
                                , 1.0, new Scalar(100, 10, 10, 255), 3);
                    }
//                    int thickness = 2;
//                    int lineType = 8;
//                    Point start = new Point(0, 0);
//                    Point end = new Point(0, 0);
//                    Scalar black = new Scalar(100, 10, 10);
                    //getCoordinates(thresholded);

					/*try {
                                              // These coordinates are screen coordinates
                                              int xCoord = (int) data[0];
                                              int yCoord = (int) data[1];

                                              // Move the cursor
                                              Robot robot = new Robot();
                                              robot.mouseMove(xCoord, yCoord);
                                          } catch (AWTException e) {
                                          }*/
                    //int cols = circles.cols();
                    int rows = circles.rows();
                    int elemSize = (int) circles.elemSize(); // Returns 12 (3 * 4bytes in a float)
                    float[] data2 = new float[rows * elemSize / 4];
					/*if (data2.length>0){  
                              circles.get(0, 0, data2); // Points to the first element and reads the whole thing  
                                            // into data2  
                             /* for(int i=0; i<data2.length; i=i+3) {  
                                Point center= new Point(data2[i], data2[i+1]);  
                                //Core.ellipse( this, center, new Size( rect.width*0.5, rect.height*0.5), 0, 0, 360, new Scalar( 255, 0, 255 ), 4, 8, 0 );  
                                try {
                                                // These coordinates are screen coordinates
                                                int xCoord = (int) ((int) data2[i]);
                                                int yCoord = (int) ((int) data2[i+1]);

                                                System.out.println("X: "+xCoord);
                                              System.out.println("Y: "+yCoord);
                                                // Move the cursor
                                                Robot robot = new Robot();
                                              robot.mouseMove(xCoord, yCoord);
                                                //  robot.mousePress(InputEvent.BUTTON3_MASK);
                                               // robot.mouseRelease(InputEvent.BUTTON3_MASK);
                                            } catch (AWTException e) {
                                            }

                                Core.ellipse( webcam_image, center, new Size((double)data2[i+2], (double)data2[i+2]), 0, 0, 360, new Scalar( 255, 0, 255 ), 4, 8, 0 );  

                              }  
                            }  */

					/*List<Moments> mu = new ArrayList<Moments>(contours.size());
                            for (int i = 0; i < contours.size(); i++) {
                                mu.add(i, Imgproc.moments(contours.get(i), false));
                                Moments p = mu.get(i);
                                int x = (int) (p.get_m10() / p.get_m00());
                                int y = (int) (p.get_m01() / p.get_m00());
                                System.out.println("X = "+ x +" y = " +y );
                                try {

                                    Robot robot = new Robot();
                                    robot.mouseMove(x, y);
                                } catch (AWTException e) {
                                }
                                Core.circle(webcam_image, new Point(x, y), 4, new Scalar(255,49,0,255), 4);
                            }*/
                    //if(contours.size()==1){
//						int x = mouse.getX(contours);
//						int y = mouse.getY(contours);
                    //moves mouse to the specific x,y coordinate calculated in the Mouse class
//						mouse.moveMouse(x, y);
//						System.out.println(x);
//						System.out.println(y);
//					Imgproc.circle(webcam_image, new Point(x, y), 4, new Scalar(255,49,0,255), 4);
					/*}else if(contours.size()==2){
						mouse.rightClick();
					}*/

                    if (!thresholded3.empty()) {
                        Imgproc.line(thresholded3, new Point(150, 50), new Point(202, 200), new Scalar(100, 10, 10)/*CV_BGR(100,10,10)*/, 3);
                        Imgproc.circle(thresholded3, new Point(210, 210), 10, new Scalar(100, 10, 10), 3);
                        data = thresholded3.get(210, 210);
                        if (data.length > 1) {
                            Imgproc.putText(thresholded3, String.format("(" + String.valueOf(data[0]) + "," + String.valueOf(data[1]) + "," + String.valueOf(data[2]) + ")"), new Point(30, 30), 3 //FONT_HERSHEY_SCRIPT_SIMPLEX
                                    , 1.0, new Scalar(100, 10, 10, 255), 3);
                        }
                    }
                    output2 = thresholded3;

                    Imgproc.line(hsv_image, new Point(150, 50), new Point(202, 200), new Scalar(100, 10, 10)/*CV_BGR(100,10,10)*/, 3);
                    Imgproc.circle(hsv_image, new Point(210, 210), 10, new Scalar(100, 10, 10), 3);
                    data = hsv_image.get(210, 210);
                    Imgproc.putText(hsv_image, String.format("(" + String.valueOf(data[0]) + "," + String.valueOf(data[1]) + "," + String.valueOf(data[2]) + ")"), new Point(30, 30), 3 //FONT_HERSHEY_SCRIPT_SIMPLEX
                            , 1.0, new Scalar(100, 10, 10, 255), 3);

                    output4 = hsv_image;
//                    distance.convertTo(distance, CvType.CV_8UC1);
//                    Imgproc.line(distance, new Point(150, 50), new Point(202, 200), new Scalar(100)/*CV_BGR(100,10,10)*/, 3);
//                    Imgproc.circle(distance, new Point(210, 210), 10, new Scalar(100), 3);
//                    data = (double[]) distance.get(210, 210);
//                    //getCoordinates(thresholded);
//                    Imgproc.putText(distance, String.format("(" + String.valueOf(data[0]) + ")"), new Point(30, 30), 3 //FONT_HERSHEY_SCRIPT_SIMPLEX
//                            , 1.0, new Scalar(100), 3);
                    //-- 5. Display the image

                    //System.out.println(data[1]);
                    panel1.setimagewithMat(output1);
                    frame1.repaint();

                    if (myDebug) {
                        panel2.setimagewithMat(output2);
                        //panel2.setimagewithMat(S);
                        //distance.convertTo(distance, CvType.CV_8UC1);
                        //panel3.setimagewithMat(distance);
                        panel4.setimagewithMat(hsv_image);
//                    if (!circles.empty())


                        frame2.repaint();
                        // frame3.repaint();
                        frame4.repaint();
                    }

                } else {
                    System.out.println(" --(!) No captured frame -- Break!");
                    break;
                }
            }
        }

    }
} 
