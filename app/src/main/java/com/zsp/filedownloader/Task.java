package com.zsp.filedownloader;

import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by zsp on 2017/10/30.
 * 下载任务
 */

public class Task implements Runnable {
    private static final String TAG = "DownLoadTask";

    private long lastTime;
    private long delayTime;
    private int sortLevel;

    DownLoader downLoader;
    private String id;
    private String downloadUrl;
    private String fileName;
    private long contentLength;
    private long finishedLength;

    public String getId() {
        return id;
    }

    public String getSavePath() {
        return savePath;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public long getContentLength() {
        return contentLength;
    }

    public long getFinishedLength() {
        return finishedLength;
    }

    public int getSortLevel() {
        return sortLevel;
    }

    public long getCreateAt() {
        return createAt;
    }

    public int getSubTaskCount() {
        return subTaskArray == null ? 0 : subTaskArray.size();
    }

    private long createAt;
    private String savePath;
    private ExecutorService executorService;
    private ArrayList<SubTask> subTaskArray;
    private CountDownLatch countDownLatch;

    private int maxThreads;
    private InputStream inputStream;
    private RandomAccessFile file;
    private HttpURLConnection conn;

    private int state = Const.DOWNLOAD_STATE_WAIT;

    public void setState(int state) {
        if (state == Const.DOWNLOAD_STATE_DOWNLOADING) {
            sortLevel = 1;
        } else {
            sortLevel = 0;
        }
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public Task(DownLoader downLoader, String url) {
        this(downLoader, url, null);
    }

    public Task(DownLoader downLoader, String url, String saveFileName) {
        this.downLoader = downLoader;
        this.maxThreads = downLoader.getConfig().getMaxThreads();
        id = System.currentTimeMillis() + "|" + url;
        createAt = System.currentTimeMillis();
        downloadUrl = url;

        if (TextUtils.isEmpty(saveFileName)) {
            fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
            if (TextUtils.isEmpty(fileName)) {
                fileName = "download-" + System.currentTimeMillis();
            }
        } else {
            String suffix = downloadUrl.substring(downloadUrl.lastIndexOf("."));
            if (TextUtils.isEmpty(suffix)) {
                fileName = saveFileName;
            } else {
                fileName = saveFileName + suffix;
            }
        }

        savePath = downLoader.getConfig().getSaveDir() + fileName;


    }

    @Override
    public void run() {

        try {
            downLoader.onTaskConnect(this);
            URL url = new URL(downloadUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5 * 1000);
            conn.setRequestMethod("GET");
            int code = conn.getResponseCode();
            if (code == 200) {
                downLoader.onTaskStart(this);
                contentLength = conn.getContentLength();
                try {
                    file = new RandomAccessFile(savePath, "rwd");
                    file.setLength(contentLength);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                long size = contentLength / 1024;

                Log.d(TAG, "下载内容 contentLength:" + size + "kb" + " len:" + contentLength);
                if (size > downLoader.getConfig().getSingleTaskThreshold()) {
                    multipleTaskDownloading();
                } else {
                    singleTaskDownloading();
                }

                if (contentLength == finishedLength){
                    Log.d(TAG, "下载完成" + finishedLength + "/" + contentLength);
                    downLoader.onTaskFinished(this);
                }else{
                    Log.d(TAG, "下载停止" + finishedLength + "/" + contentLength);
                    downLoader.onTaskStop(this);
                    downLoader.dispatcher().stoped(this);
                }

                //取消和完成
                if (state != Const.DOWNLOAD_STATE_STOP){
                    downLoader.dispatcher().finished(this);
                }
            } else {
                downLoader.onTaskError(this,"响应码:"+code);
            }

        } catch (Exception e) {
            e.printStackTrace();
            downLoader.onTaskError(this,e.toString());
        } finally {
            closeIO();
        }
    }

    private void multipleTaskDownloading() throws Exception {
        executorService = Executors.newCachedThreadPool();
        subTaskArray = new ArrayList(maxThreads);
        countDownLatch = new CountDownLatch(maxThreads);

        int blockSize = (int) (contentLength / maxThreads);
        List<Future<Boolean>> list = new ArrayList<>(maxThreads);
        for (int i = 0; i < maxThreads; i++) {
            long start = i * blockSize;
            long end = (i + 1) * blockSize;
            if (i == maxThreads - 1) {
                // subTask.endLocation  +=  (length % blockSize);
                end = contentLength;
            }
            SubTask subTask = new SubTask(this, start, end);
            subTaskArray.add(subTask);
            Log.d(TAG, "run: 启动子线程" + i);
            Future<Boolean> future = executorService.submit(subTask);
            list.add(future);
        }

        try{
            countDownLatch.await();
            for (int i = 0; i < list.size(); i++) {
                Future<Boolean> future = list.get(i);
                if (!future.get().booleanValue()) {
                    throw new Exception("子线程执行失败");
                }
            }
        }catch (ExecutionException e){
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void singleTaskDownloading() throws IOException {
        inputStream = conn.getInputStream();
        byte[] buf = new byte[1024];
        int len = 0;
        while (state == Const.DOWNLOAD_STATE_DOWNLOADING && (len = inputStream.read(buf)) != -1) {
            file.write(buf, 0, len);
            finishedLength += len;
            updateProcess();
        }
    }

    public synchronized void appendFinished(long len) {
        finishedLength += len;
        updateProcess();
    }

    public void updateProcess() {
        long curTime = System.currentTimeMillis();
        long interval = curTime - lastTime;
        delayTime += interval;
        if (delayTime > downLoader.getConfig().getUpdateInterval()
                || finishedLength == contentLength) {
            downLoader.onTaskProcess(this);
            delayTime = 0;
        }

        lastTime = System.currentTimeMillis();
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

        try {
            if (conn != null) {
                conn.disconnect();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
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
        Task other = (Task) obj;
        if (!id.equals(other.id)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public void subTaskFinished() {
        countDownLatch.countDown();
    }


}
