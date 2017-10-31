package com.zsp.filedownloader;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zsp on 2017/10/27.
 */

public class DownLoadTask implements Runnable {
    private static final String TAG = "DownLoadTask";

    DownLoader downLoader;
    public String id;
    public String downloadUrl;
    public String fileName;
    public long contentLength;
    public long finishedLength;

    public long createAt;
    public String savePath;
    ExecutorService executorService;
    ArrayList<SubTask> subTaskArray;
    CountDownLatch countDownLatch;

    public int state = Const.DOWNLOAD_STATE_WAIT;

    int subMax = 3;

    InputStream inputStream;
    RandomAccessFile file;
    HttpURLConnection conn;

    public DownLoadTask(DownLoader downLoader, String url) {
        this.downLoader = downLoader;
        id = System.currentTimeMillis() + "|" + url;
        createAt = System.currentTimeMillis();
        downloadUrl = url;
        fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
        savePath = downLoader.getConfig().saveDir + fileName;
    }

    @Override
    public void run() {
        state = Const.DOWNLOAD_STATE_DOWNLOADING;
        downLoader.onTaskStart(this);
        try {
            URL url = new URL(downloadUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(10 * 1000);
            conn.setRequestMethod("GET");
            int code = conn.getResponseCode();

            if (code == 200) {
                contentLength = conn.getContentLength();
                try {
                    file = new RandomAccessFile(savePath, "rwd");
                    file.setLength(contentLength);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                long size = contentLength / 1024;

                Log.d(TAG, "run: contentLength:" + size + "kb" + " len:" + contentLength);
                if (size > 100) {
                    multipleTaskDownloading();
                } else {
                    singleTaskDownloading();
                }

                Log.d(TAG, "run: 下载文件完成" + finishedLength + "/" + contentLength);
            } else {

            }
        } catch (Exception e) {
            e.printStackTrace();
            state = Const.DOWNLOAD_STATE_STOP;
        } finally {
            downLoader.onTaskFinished(this);
            downLoader.dispatcher().finished(this);
            closeIO();
        }
    }

    private void multipleTaskDownloading() {
        executorService = Executors.newCachedThreadPool();
        subTaskArray = new ArrayList(subMax);
        countDownLatch = new CountDownLatch(subMax);

        int blockSize = (int) (contentLength / subMax);
        for (int i = 0; i < subMax; i++) {
            long start = i * blockSize;
            long end = (i + 1) * blockSize;
            if (i == subMax - 1) {
                // subTask.endLocation  +=  (length % blockSize);
                end = contentLength;
            }
            SubTask subTask = new SubTask(this, start, end);
            subTaskArray.add(subTask);
            Log.d(TAG, "run: 启动子线程" + i);
            executorService.submit(subTask);
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void singleTaskDownloading() {
        try {
            inputStream = conn.getInputStream();
            byte[] buf = new byte[1024];
            int len = 0;
            while (state == Const.DOWNLOAD_STATE_DOWNLOADING && (len = inputStream.read(buf)) != -1) {
                file.write(buf, 0, len);
                finishedLength += len;
                downLoader.onTaskProcess(this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void appendFinished(long len) {
        finishedLength += len;
        downLoader.onTaskProcess(this);
    }

    public void stop() {
        if (state == Const.DOWNLOAD_STATE_DOWNLOADING) {
            state = Const.DOWNLOAD_STATE_STOP;

            closeIO();

            for (int i = 0; i < subTaskArray.size(); i++) {
                SubTask subTask = subTaskArray.get(i);
                subTask.stop();
            }
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

    public void cancel() {
        stop();
        File file = new File(savePath);
        if (file.exists()) {
            file.delete();
        }
        Log.d(TAG, "cancel: 取消了任务:" + savePath);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        DownLoadTask other = (DownLoadTask) obj;
        if (!id.equals(other.id)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
