package com.zsp.filedownloader;

import android.util.Log;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by zsp on 2017/10/28.
 */

public class SubTask implements Runnable {
    private static final String TAG = "SubTask";

    public DownLoadTask pTask;
    public String id;
    public String downloadUrl;
    public String savePath;
    public long startPosition;
    public long endPosition;
    public long finishedLength;
    public long blockSize;

    public int state = Const.DOWNLOAD_STATE_WAIT;

    InputStream inputStream;
    RandomAccessFile file;
    HttpURLConnection conn;

    public SubTask(DownLoadTask downLoadTask, long start, long end) {
        pTask = downLoadTask;
        id = "SubTask-" + pTask.subTaskArray.size();
        downloadUrl = pTask.downloadUrl;
        savePath = pTask.savePath;
        startPosition = start;
        endPosition = end;
        blockSize = end - start;

    }

    @Override
    public void run() {
        try {
            state = Const.DOWNLOAD_STATE_DOWNLOADING;
            URL url = new URL(downloadUrl);
            conn = (HttpURLConnection) url.openConnection();
            Log.d(TAG, "run: 子线程下载范围" + id + " " + startPosition + "-" + (endPosition - 1));
            conn.setRequestProperty("Range", "bytes=" + startPosition + "-" + (endPosition - 1));
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setReadTimeout(30 * 1000);
            inputStream = conn.getInputStream();
            file = new RandomAccessFile(savePath, "rwd");
            file.seek(startPosition); // 指定开始写文件的位置
            byte[] buffer = new byte[4096];
            int len;
            while (state == Const.DOWNLOAD_STATE_DOWNLOADING && (len = inputStream.read(buffer)) != -1) {
                file.write(buffer, 0, len);
                finishedLength += len;
                Log.d(TAG, "run: 子线程:" + id + " 已下载 " + finishedLength + "/" + blockSize);
                pTask.appendFinished(len);
            }
            Log.d(TAG, "run: 子线程:" + id + "下载完成");
            state = Const.DOWNLOAD_STATE_FINISH;
        } catch (Exception e) {
            e.printStackTrace();
            state = Const.DOWNLOAD_STATE_STOP;
        } finally {
            pTask.countDownLatch.countDown();
            closeIO();
        }
    }

    public void stop() {
        if (state == Const.DOWNLOAD_STATE_DOWNLOADING) {
            state = Const.DOWNLOAD_STATE_STOP;
            closeIO();
        }
    }

    private void closeIO() {

        try {
            if (file != null) {
                file.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

//        try {
//            if (conn != null) {
//                conn.disconnect();
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
