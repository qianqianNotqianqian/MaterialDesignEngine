package mapleleaf.materialdesign.engine.asynctask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import mapleleaf.materialdesign.engine.utils.TopLevelFuncationKt;

public class ImageSaver {
    @SuppressLint("StaticFieldLeak")
    private final Context context;
    private final ExecutorService executorService;
    private volatile boolean isRunning = false;

    public ImageSaver(Context context) {
        this.context = context;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void saveImage(String imageUrl) {
        if (!isRunning) {
            isRunning = true;
            executorService.submit(() -> {
                String result = "";
                try {
                    String sdcard = Environment.getExternalStorageDirectory().toString();
                    File directory = new File(sdcard + "/Pictures/MDEngine/");
                    if (!directory.exists()) {
                        directory.mkdirs();
                    }
                    int idx = imageUrl.lastIndexOf(".");
                    String ext = imageUrl.substring(idx);

                    File file = new File(directory, new Date().getTime() + ext);

                    downloadImage(imageUrl, file);

                    if (isImageFile(file)) {
                        result = "图片已保存至：" + file.getAbsolutePath();
                        refreshGallery(file);
                    } else {
                        file.delete();
                        result = "保存失败！无效的图像文件";
                    }
                } catch (Exception e) {
                    result = "保存失败！" + e.getLocalizedMessage();
                } finally {
                    isRunning = false;
                    String finalResult = result;
                    new Handler(Looper.getMainLooper()).post(() -> TopLevelFuncationKt.toast(finalResult));
                }
            });
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    private void downloadImage(String imageUrl, File file) throws IOException {
        HttpURLConnection conn = null;
        InputStream inputStream = null;
        FileOutputStream outStream = null;
        try {
            URL url = new URL(imageUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(20000);

            if (conn.getResponseCode() == 200) {
                inputStream = conn.getInputStream();
                outStream = new FileOutputStream(file);

                byte[] buffer = new byte[4096];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    outStream.write(buffer, 0, len);
                }
            }
        } finally {
            if (outStream != null) {
                outStream.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private boolean isImageFile(File file) {
        return file.exists() && file.length() > 0;
    }

    private void refreshGallery(File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(file));
        context.sendBroadcast(mediaScanIntent);
    }
}