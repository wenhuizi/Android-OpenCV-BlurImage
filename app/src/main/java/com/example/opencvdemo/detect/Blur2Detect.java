package com.example.opencvdemo.detect;

import android.util.Log;

import com.example.opencvdemo.IConstract;
import com.example.opencvdemo.util.OpenCvUtil;

import java.io.File;

public class Blur2Detect {

    private static final String TAG = "BlurDetect";
    public double sqDev = 0.0;
    public String strBlurDescribe = "";

    private File file;

    public Blur2Detect(final File file) {
        this.file = file;
    }

    protected boolean doCheckValid() {
        return file != null;
    }

    public final void detecting(IConstract.ICommon doAfter) {
        if (doCheckValid()) {
            Log.e(TAG, "detecting: 开始检测");
            doDetect();
            doAfter.onDoSomething();
        } else {
            Log.e(TAG, "detecting: 未开始检测");
        }
        doAfter.onDetected();
    }


    protected void doDetect() {
        //获取标准差
        sqDev = OpenCvUtil.getSquareDeviation(file);
        Log.e(TAG, "doDetect: " + "-------------------------------");
        //保留两位小数
//        sqDev=NumberUtil.reserveDecimal2(sqDev);
        sqDev = (int) (sqDev * 100) / 100.0;
        strBlurDescribe = "模糊度：" + sqDev;
        Log.d(TAG, "计算结束：" + strBlurDescribe);
    }
}
