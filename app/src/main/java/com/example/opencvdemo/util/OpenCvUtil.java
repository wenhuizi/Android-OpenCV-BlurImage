package com.example.opencvdemo.util;

import static org.opencv.core.Core.meanStdDev;
import static org.opencv.core.CvType.CV_64F;
import static org.opencv.imgproc.Imgproc.COLOR_BayerBG2GRAY;
import static org.opencv.imgproc.Imgproc.Laplacian;
import static org.opencv.imgproc.Imgproc.blur;
import static org.opencv.imgproc.Imgproc.cvtColor;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.TextView;

import com.example.opencvdemo.R;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;

/**
 * 自定义Opencv工具类
 * 包含：
 * Mat与Bitmap相互转换
 * 获取方差和标准差
 * 根据阈值判断模糊度
 */
public class OpenCvUtil {
    private static final String TAG = "OpenCvUtil";
    //模糊度阈值
    public static final int BLUR_THRESHOLD500 = 500;
    public static final int BLUR_THRESHOLD300 = 300;
    public static final int BLUR_THRESHOLD50 = 50;
    public static final int BLUR_THRESHOLD15 = 15;
    public static StringBuilder sb=null;

    /**
     * Mat转Bitmap
     *
     * @param mat
     * @return
     */
    public static Bitmap matToBitmap(Mat mat) {
        Bitmap result = null;
        if (mat != null) {
            result = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.RGB_565);
            if (result != null) {
                Utils.matToBitmap(mat, result);
            }
        }
        return result;
    }

    /**
     * Bitmap转Mat
     *
     * @param bitmap
     * @return Mat
     */
    public static Mat bitmapToMap(Bitmap bitmap) {
        Mat result = null;
        Bitmap bmp32 = bitmap.copy(Bitmap.Config.RGB_565, true);
        result = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC2, new Scalar(0));
        Utils.bitmapToMat(bmp32, result);
        return result;
    }

    public static double getStandarDeviation(Bitmap bitmap) {
        double result = 0.0;
        if (bitmap != null) {
            result = getStdDev(bitmap);
        }
        return result;
    }

    /**
     * 计算图像方差
     * @param bitmap
     * @return
     */
    public static double getSquareDeviation(Bitmap bitmap) {
        double result = 0.0;
        if (bitmap != null) {
            result = getStdDev(bitmap);
        }
        return result * result;
    }

    /**
     * 计算图像方差
     */
    public static double getSquareDeviation(File file) {
        double result = 0.0;
        if (file != null) {
            result = getStdDev(file.getPath());
        }
        return result * result;
    }

    /**
     * 输入标准差，判断图像是否清晰
     *
     * @param st
     * @param tv
     * @param context
     */
    public static void judgeBlurByStdDev(final double st, final TextView tv, final Context context) {
        judgeBlurBySquDev(st * st, tv, context);
    }

    /**
     * 通过方差判断
     *
     * @param sq
     * @param tv
     * @param context
     */
    public static void judgeBlurBySquDev(final double sq, final TextView tv, final Context context) {
        tv.post(new Runnable() {
            @Override
            public void run() {
                sb = new StringBuilder("模糊度：");
                double tempSt = sq;
                //标准差->方差
                sb.append(tempSt).append("\t");
                //颜色可以自行设置
                if (tempSt > BLUR_THRESHOLD500) {
                    sb.append("清晰");
                    tv.setTextColor(context.getColor(R.color.green));
                } else if (tempSt > BLUR_THRESHOLD300) {
                    sb.append("不清晰");
                    tv.setTextColor(context.getColor(R.color.red));
                } else if (tempSt > BLUR_THRESHOLD50) {
                    sb.append("很不清晰");
                    tv.setTextColor(context.getColor(R.color.red));
                } else if (tempSt > BLUR_THRESHOLD15) {
                    sb.append("非常不清晰");
                    tv.setTextColor(context.getColor(R.color.yellow));
                } else {
                    sb.append("完全看不清");
                    tv.setTextColor(context.getColor(R.color.red));
                }
                Log.d(TAG, "run: " + sb);
                tv.setText(sb);
            }
        });
    }

    public static String getsb(){
        return sb.toString();
    }

    /**
     * 获取模糊位图
     * @param srcBitmap
     * @return
     */
    public static Bitmap getBlurBitmap(final Bitmap srcBitmap) {
        Mat srcImage = bitmapToMap(srcBitmap);
        Mat blurImage = new Mat();
        blur(srcImage, blurImage, new Size(3, 3));
        return matToBitmap(blurImage);
    }

    /**
     * 获取标准差
     * param bitmap
     * @return double 标准差
     */
    private static double getStdDev(Bitmap bitmap) {

        Mat matSrc = bitmapToMap(bitmap);
        Mat mat = new Mat();
        int channel = matSrc.channels();
        System.out.println("getStdDev:channel=" + channel);
        //1表示图像是灰度图
        if (channel != 1) {
            cvtColor(matSrc, mat, COLOR_BayerBG2GRAY);
        } else {
            mat = matSrc;
        }
        Mat lap = new Mat();
        Laplacian(mat, lap, CV_64F);
        MatOfDouble s = new MatOfDouble();
        meanStdDev(lap, new MatOfDouble(), s);
        double st = s.get(0, 0)[0];
        System.out.println("getStdDev:st=" + st);
        return st;
    }

    private static double getStdDev(String filePath) {

        // 从文件加载 BGGR 格式的 Bayer 图像
        Mat matSrc = Imgcodecs.imread(filePath, Imgcodecs.IMREAD_GRAYSCALE);
        Mat mat = new Mat();
        int channel = matSrc.channels();
        System.out.println("getStdDev:channel=" + channel);
        //1表示图像是灰度图
        if (channel != 1) {
            cvtColor(matSrc, mat, COLOR_BayerBG2GRAY);
        } else {
            mat = matSrc;
        }
        Mat lap = new Mat();
        Laplacian(mat, lap, CV_64F);
        MatOfDouble s = new MatOfDouble();
        meanStdDev(lap, new MatOfDouble(), s);
        double st = s.get(0, 0)[0];
        System.out.println("getStdDev:st=" + st);
        return st;
    }
}
