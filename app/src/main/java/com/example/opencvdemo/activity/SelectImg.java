package com.example.opencvdemo.activity;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
import static com.example.opencvdemo.util.OpenCvUtil.BLUR_THRESHOLD15;
import static com.example.opencvdemo.util.OpenCvUtil.BLUR_THRESHOLD300;
import static com.example.opencvdemo.util.OpenCvUtil.BLUR_THRESHOLD50;
import static com.example.opencvdemo.util.OpenCvUtil.BLUR_THRESHOLD500;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.opencvdemo.IConstract;
import com.example.opencvdemo.IDetectCallback;
import com.example.opencvdemo.R;
import com.example.opencvdemo.detect.Blur2Detect;
import com.example.opencvdemo.util.GlideEngine;
import com.luck.picture.lib.basic.PictureSelectionModel;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnResultCallbackListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectImg extends AppCompatActivity {
    Button finish, selectimg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_img);

        finish = (Button) findViewById(R.id.finish);
        finish.setOnClickListener(this::out);
        selectimg = findViewById(R.id.selectimg);
        selectimg.setOnClickListener(v -> {
            // 打开图片选择
            PictureSelectionModel imageEngine = PictureSelector.create(this)
                    .openGallery(SelectMimeType.ofImage())
                    .setMaxSelectNum(10)// 最大图片选择数量
                    .setMinSelectNum(1)// 最小选择数量
                    .setImageSpanCount(4)// 每行显示个数
                    .setImageEngine(GlideEngine.createGlideEngine());
            imageEngine.forResult(new OnResultCallbackListener<LocalMedia>() {
                @Override
                public void onResult(ArrayList<LocalMedia> result) {
                    Map<Uri, Double> sqaDevList = new HashMap<>();
                    for (LocalMedia localMedia : result) {
                        String path = localMedia.getPath();
                        Uri uri = Uri.parse(path);
                        detectBlur(uri2File(uri), data -> {
                            sqaDevList.put(uri, data);
                            // 全部计算结束，开始判断结果
                            if (sqaDevList.size() == result.size()) calculation(sqaDevList);
                        });
                    }
                }

                @Override
                public void onCancel() {

                }
            });
        });
    }

    /**
     * 计算结果
     */
    private void calculation(Map<Uri, Double> sqaDevList) {
        int a = 0, b = 0, c = 0, d = 0, e = 0; // 清晰度
        // 判断清晰度
        for (Uri uri : sqaDevList.keySet()) {
            Double sqa = sqaDevList.get(uri);
            if (sqa > BLUR_THRESHOLD500) a++;  // 清晰
            else if (sqa > BLUR_THRESHOLD300) b++; // 不清晰
            else if (sqa > BLUR_THRESHOLD50) c++; // 很不清晰
            else if (sqa > BLUR_THRESHOLD15) d++; // 非常不清晰
            else e++; // 完全看不清
        }
        StringBuilder sb = new StringBuilder("您所选择的图片包含：");
        if (a > 0) sb.append(a + "张清晰，");
        if (b > 0) sb.append(b + "张不清晰，");
        if (c > 0) sb.append(c + "张很不清晰，");
        if (d > 0) sb.append(d + "张非常不清晰，");
        if (e > 0) sb.append(e + "张完全看不清，");

        List<Uri> deleteUri = new ArrayList<>();
        for (Uri uri : sqaDevList.keySet()) {
            Double sqa = sqaDevList.get(uri);
            if (sqa > BLUR_THRESHOLD50) continue; // 很不清晰
            deleteUri.add(uri);
        }
        if (deleteUri.size() > 0) sb.append("需要帮您删除非常不清晰和完全看不清的照片吗？");


        //声明空间弹窗
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_blur, null);
        //根据方差判断图像是否清晰
        TextView textView = view.findViewById(R.id.tv_blur);
        textView.setText(sb.toString());
        //定义弹窗
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this).setView(view);
        if (deleteUri.size() > 0) {
            dialogBuilder.setPositiveButton("删除", (dialog, which) -> {
                // 执行删除 uri
                Log.d(TAG, "onClick:开始删除");
                if (deleteUri.size() > 0) deleteImage(deleteUri);
            }).setNegativeButton("取消", null);
        } else {
            dialogBuilder.setPositiveButton("确定", null);
        }
        dialogBuilder.create().show();
    }


    //检测模糊度-方差
    public void detectBlur(File file, IDetectCallback callback) {
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
                                callback.onDetectCallback(sqaDev);
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


    public void out(View v) {
        Intent intent = new Intent();
        intent.setClass(SelectImg.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);//设置不要刷新将要跳到的界面
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//它可以关掉所要到的界面中间的activity
        startActivity(intent);
    }


    /**
     * uri 转 file
     */
    private File uri2File(Uri uri) {
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            // 获取文件后缀
            String mimeType = getContentResolver().getType(uri);
            String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
            if (TextUtils.isEmpty(extension)) extension = "png";
            // 创建文件
            String displayName = System.currentTimeMillis() + "." + extension;
            File imageDir = new File(getCacheDir() + File.pathSeparator + "file");
            if (!imageDir.exists()) imageDir.mkdirs(); // 如果不存在目录，则创建
            File imageFile = new File(imageDir, displayName);
            // 写入文件
            inputStream = getContentResolver().openInputStream(uri);
            fileOutputStream = new FileOutputStream(imageFile);
            byte[] bytes = new byte[1024];
            while (inputStream.read(bytes) != -1) {
                fileOutputStream.write(bytes);
            }
            return imageFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (inputStream != null) inputStream.close();
                if (fileOutputStream != null) fileOutputStream.close();
            } catch (IOException e) {
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x02303) {
            String str = resultCode == Activity.RESULT_OK ? "删除成功" : "删除失败";
            Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
        }
    }


    private boolean deleteImage(List<Uri> imageUri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            PendingIntent deleteRequest = MediaStore.createDeleteRequest(getContentResolver(), imageUri);
            try {
                startIntentSenderForResult(deleteRequest.getIntentSender(),
                        0x02303, null, 0, 0, 0, null);
                return true;
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else try {
            for (Uri uri : imageUri) {
                this.getContentResolver().delete(uri, null, null);
            }
            Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
            return true;
        } catch (Exception e) {
            return false;
        }

//        try {
//            return this.getContentResolver().delete(image, null, null) > 0;
//        } catch (SecurityException exception) {
//            // 若启用了分区存储，上面代码delete将会报错，显示没有权限。
//            // 需要捕获这个异常，并用下面代码，使用startIntentSenderForResult弹出弹窗向用户请求修改当前图片的权限
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && exception instanceof RecoverableSecurityException) {
//                RecoverableSecurityException securityException = (RecoverableSecurityException) exception;
//                IntentSender intentSender = securityException.getUserAction().getActionIntent().getIntentSender();
//
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return false;
    }


}