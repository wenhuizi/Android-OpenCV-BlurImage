package com.example.opencvdemo.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;

public class CameraUtil {
    private static final String TAG = "CameraUtil";
    public static final int INT_OPEN_CAMERA = 1001;
    //检查是否有摄像头
    private static boolean isCamera = false;

    public static File file;

    /**
     * 打开相机
     *
     * @param from
     */
    @SuppressLint("QueryPermissionsNeeded")
    public static void openCamera(Activity from) {
        Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intentCamera.resolveActivity(from.getPackageManager()) != null) {
            file = new File(from.getApplicationContext().getCacheDir(), "temp.png");
            if (file.isFile()) file.delete(); // 删除之前拍照
            Uri photoURI = FileProvider.getUriForFile(from, "com.example.opencvdemo.fileprovider", file);
            intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            from.startActivityForResult(intentCamera, INT_OPEN_CAMERA);
        } else {
            Log.d(TAG, "onClick:打开失败");
        }
    }
}
