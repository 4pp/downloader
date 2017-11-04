package com.zsp.filedownloader;

import android.text.TextUtils;
import android.util.Log;

import com.zsp.filedownloader.record.SubTaskRecord;
import com.zsp.filedownloader.record.TaskRecord;

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

    private TaskRecord record;
    private DownLoader downLoader;

    public long getId() {
        return record.getId();
    }

    public String getFilePath() {
        return record.getFilePath();
    }

    public String getDownloadUrl() {
        return record.getDownloadUrl();
    }

    public String getFileName() {
        return record.getFileName();
    }

    public long getContentLength() {
        return record.getContentLength();
    }
//
    public long getFinishedLength() {
        return record.getFinishedLength();
    }
//
    public int getSortLevel() {
        return sortLevel;
    }
//
    public long getCreateAt() {
        return record.getCreateAt();
    }
//
    public int getSubTaskCount() {
        return subTaskArray == null ? 0 : subTaskArray.size();
    }

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


    public Task(DownLoader downLoader, TaskRecord record) {
        this.downLoader = downLoader;
        this.maxThreads = downLoader.getConfig().getMaxThreads();
        this.record = record;
    }

    @Override
    public void run() {
        try {
            downLoader.onTaskConnect(this);
            URL url = new URL(record.getDownloadUrl());
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5 * 1000);
            conn.setRequestMethod("GET");
            int code = conn.getResponseCode();
            if (code == 200) {
                downLoader.onTaskStart(this);
                int contentLength = conn.getContentLength();
                record.setContentLength(contentLength);
                try {
                    String savePath = record.getFilePath() + record.getFileName();
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

                if (record.isFinished()) {
                    Log.d(TAG, "下载完成" + record.getFinishedLength() + "/" + contentLength);
                    downLoader.onTaskFinished(this);
                    downLoader.dispatcher().finished(this);
                } else if (state == Const.DOWNLOAD_STATE_STOP){
                    Log.d(TAG, "下载停止" + record.getFinishedLength() + "/" + contentLength);
                    downLoader.onTaskStop(this);
                    downLoader.dispatcher().stoped(this);
                }else if(state == Const.DOWNLOAD_STATE_CANCEL){
                    Log.d(TAG, "下载取消" + record.getFinishedLength() + "/" + contentLength);
                    downLoader.onCancelTask(this);
                    downLoader.dispatcher().finished(this);
                }
            } else {
                downLoader.onTaskError(this, "响应码:" + code);
            }

        } catch (Exception e) {
            e.printStackTrace();
            downLoader.onTaskError(this, e.toString());
        } finally {
            closeIO();
        }
    }

    private void multipleTaskDownloading() throws Exception {
        executorService = Executors.newCachedThreadPool();
        subTaskArray = new ArrayList(maxThreads);
        countDownLatch = new CountDownLatch(maxThreads);

        int blockSize = (int) (record.getContentLength() / maxThreads);
        List<Future<Boolean>> list = new ArrayList<>(maxThreads);
        for (int i = 0; i < maxThreads; i++) {
            long start = i * blockSize;
            long end = (i + 1) * blockSize;
            if (i == maxThreads - 1) {
                // subTask.endLocation  +=  (length % blockSize);
                end = record.getContentLength();
            }

            SubTaskRecord subRecord = new SubTaskRecord();
            downLoader.recordManager.subTask().add(subRecord);
            subRecord.setTaskID(record.getId());
            subRecord.setStart(start);
            subRecord.setEnd(end);
            SubTask subTask = new SubTask(this,subRecord);
            subTaskArray.add(subTask);
            Log.d(TAG, "run: 启动子线程" + i);
            Future<Boolean> future = executorService.submit(subTask);
            list.add(future);
        }

        try {
            countDownLatch.await();
            for (int i = 0; i < list.size(); i++) {
                Future<Boolean> future = list.get(i);
                if (!future.get().booleanValue()) {
                    throw new Exception("子线程执行失败");
                }
            }
        } catch (ExecutionException e) {
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
            long finished = record.getFinishedLength() + len;
            record.setFinishedLength(finished);
            updateProcess();
        }
    }

    public synchronized void appendFinished(long len,long subTaskId,long subFinished) {
        long finished = record.getFinishedLength() + len;
        record.setFinishedLength(finished);
        updateProcess();
    }

    public void updateProcess() {
        long curTime = System.currentTimeMillis();
        long interval = curTime - lastTime;
        delayTime += interval;
        if (delayTime > downLoader.getConfig().getUpdateInterval()
                || record.isFinished()) {
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
        if (state == Const.DOWNLOAD_STATE_DOWNLOADING) {
            state = Const.DOWNLOAD_STATE_CANCEL;
            closeIO();
        }
        File file = new File(record.getFilePath()+record+getFileName());
        if (file.exists()) {
            file.delete();
        }
        Log.d(TAG, "cancel: 取消了任务:" + file.getAbsolutePath());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Task other = (Task) obj;
        if (record.getId() != other.record.getId()) return false;
        return true;
    }

    @Override
    public int hashCode() {
        long id = record.getId();
        return (int) (id ^ (id >>> 32));
    }

    public void subTaskFinished() {
        countDownLatch.countDown();
    }


}
