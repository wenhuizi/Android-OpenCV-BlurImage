package com.example.opencvdemo.activity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.example.opencvdemo.IConstract;
import com.example.opencvdemo.R;
import com.example.opencvdemo.databinding.ActivityMainBinding;
import com.example.opencvdemo.detect.Blur2Detect;
import com.example.opencvdemo.util.CameraUtil;
import com.example.opencvdemo.util.OpenCvUtil;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    ActivityMainBinding binding;
    private Mat imageMat;
    //声明OpenCV加载完成监听器
    private BaseLoaderCallback mloaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    //这里加载成功才可以调用OpenCV的相关方法
                    Log.i("OpenCV", "OpenCV loaded successfully");
                    imageMat = new Mat();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };
    Button b_open;
    ImageView allimageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
//        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
//        }
        b_open = findViewById(R.id.open);
//        allimageView=findViewById(R.id.open_imageview);
//
        b_open.setOnClickListener(this::open_selectimg);
        setBtnListener();

//        setContentView(R.layout.activity_main);

//        boolean initDebug = OpenCVLoader.initDebug();
//        Log.e("OpenCV", "onCreate: " + initDebug );
    }

    private void setBtnListener() {
        //拍照
        binding.btnOpenCamera.setOnClickListener(v -> {
            if (XXPermissions.isGranted(this, Permission.CAMERA)) {
                CameraUtil.openCamera(MainActivity.this);
            } else {
                XXPermissions.with(this)
                        .permission(Permission.CAMERA)
                        .request(new OnPermissionCallback() {
                            @Override
                            public void onGranted(List<String> permissions, boolean allGranted) {
                                binding.btnOpenCamera.performClick(); // 同意权限了，模拟用户点击按钮
                            }

                            @Override
                            public void onDenied(List<String> permissions, boolean doNotAskAgain) {
                                Toast.makeText(MainActivity.this, "权限被拒绝", Toast.LENGTH_SHORT).show();
                                XXPermissions.startPermissionActivity(MainActivity.this, permissions);
                            }
                        });
            }
        });
    }

    public void open_selectimg(View v) {
        Intent intent = new Intent(MainActivity.this, SelectImg.class);
        startActivity(intent);
    }

    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found.Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mloaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package.Using it!");
            mloaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult:requestCode" + requestCode);
        Log.d(TAG, "onActivityResult:resultCode=" + resultCode);
        Log.d(TAG, "file " + CameraUtil.file);
        Log.d(TAG, "file size " + CameraUtil.file.length());
        if (requestCode == CameraUtil.INT_OPEN_CAMERA && CameraUtil.file.isFile()) {
//            Bitmap bitmap = BitmapFactory.decodeFile(CameraUtil.file.getPath());
            //加载位图
            Glide.with(this)
                    .load(CameraUtil.file)
                    .into(binding.ibImageShow);
//            BmpUtil.loadBitmap(bitmap, binding.ibImageShow);
            //检测模糊度
            detectBlur(CameraUtil.file);
        }
        //handleImageOnKitKat(data);

    }

    //检测模糊度-方差
    public void detectBlur(File file) {
        //模糊度检测子对象
        Blur2Detect blurDetect = new Blur2Detect(file);
        //耗时操作在子线程中执行，避免ANR(Application Not Responding)
        new Thread(new Runnable() {
            @Override
            public void run() {
                //开始执行检测逻辑
                blurDetect.detecting(new IConstract.ICommon() {
                    //重载onDetected方法，检测结束后执行
                    @Override
                    public void onDetected() {
                        //在UI线程中更新控件
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                double sqaDev = blurDetect.sqDev;
                                String strDes = blurDetect.strBlurDescribe;
                                Log.d(TAG, "run:sq=" + sqaDev);
                                Log.d(TAG, "run:des=" + strDes);
                                //声明空间弹窗
                                LayoutInflater inflater = getLayoutInflater();
                                View view = inflater.inflate(R.layout.dialog_blur, null);
                                //根据方差判断图像是否清晰
                                TextView textView = view.findViewById(R.id.tv_blur);
//                                textView.setText(
//                                        OpenCvUtil.getBlurBySquDev(sqaDev, MainActivity.this)
//                                );
                                OpenCvUtil.judgeBlurBySquDev(sqaDev, textView, getApplicationContext());
                                //定义弹窗
                                //AlertDialog alertDialog2 = new AlertDialog.Builder(MainActivity.this).setView(view).create();
                                AlertDialog alertDialog2 = new AlertDialog.Builder(MainActivity.this)
                                        .setView(view)
                                        .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Log.d(TAG, "onClick:开始保存");
                                                saveImage(file);
                                            }
                                        })
                                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Log.d(TAG, "onClick:取消保存");
                                            }
                                        }).create();

                                alertDialog2.show();
                            }
                        });
                    }

                    @Override
                    public void onDoSomething() {

                    }
                });
            }
        }).start();
    }


    /**
     * 检查权限  （ 多个
     */
    private Boolean checkSelfPermissions(String... permissions) {
        boolean isGranted = true;
        for (String permission : permissions) {
            isGranted = isGranted &&
                    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
        }
        return isGranted;
    }

    /**
     * 保存图片
     *
     * @param file 图片文件
     */
    private void saveImage(File file) {
        // 小于 android 10 需要权限
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                !checkSelfPermissions("android.permission.WRITE_EXTERNAL_STORAGE")
        ) { // 小于android 10 且没有同意权限 ，申请权限
            requestWritePermissionLauncher.launch("android.permission.WRITE_EXTERNAL_STORAGE");
        } else { // 有权限了，下载
            //开始一个新的进程执行保存图片的操作
            ContentResolver contentResolver = getContentResolver();
            if (contentResolver == null) return;
            Uri insertUri = contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues()
            );
            OutputStream outputStream = null;
            FileInputStream inputStream = null;
            BufferedInputStream bufferedInputStream = null;
            try {
                outputStream = contentResolver.openOutputStream(insertUri, "rw");
                inputStream = new FileInputStream(file);
                int bufferSize = 1024 * 8;
                byte[] buffer = new byte[bufferSize];
                bufferedInputStream = new BufferedInputStream(inputStream, bufferSize);
                int readLength = 0;
                while (readLength != -1) {
                    readLength = bufferedInputStream.read(buffer);
                    outputStream.write(buffer);
                }
                Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (bufferedInputStream != null) bufferedInputStream.close();
                    if (inputStream != null) inputStream.close();
                    if (outputStream != null) outputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }

    /**
     * 请求权限 - 储存卡
     */
    private ActivityResultLauncher<String> requestWritePermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    result -> {
                        if (result.equals(true)) { //权限获取到之后的动作
                            saveImage(CameraUtil.file);
                        } else { //权限没有获取到的动作
                            Toast.makeText(this, "请同意权限", Toast.LENGTH_SHORT).show();
                            XXPermissions.startPermissionActivity(this);
                        }
                    });


}