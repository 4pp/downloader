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

    private CountDownLatch countDownLatch;
    private int maxThreads;
    private RandomAccessFile file;
    private HttpURLConnection conn;

    //private int state = Const.DOWNLOAD_STATE_WAIT;

    public void setState(int state) {
        if (state == Const.DOWNLOAD_STATE_DOWNLOADING) {
            sortLevel = 1;
        } else {
            sortLevel = 0;
        }
        record.setState(state);
    }

    public int getState() {
        return record.getState();
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
                long contentLength = conn.getContentLength();
                //新任务
                if (record.isNew()) {
                    record.setContentLength(contentLength);
                    try {
                        String savePath = record.getFilePath() + record.getFileName();
                        file = new RandomAccessFile(savePath, "rwd");
                        file.setLength(contentLength);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                downLoader.onTaskStart(this);
                downLoader.recordManager.task().update(record);
                long size = contentLength / 1024;
                Debug.log("下载内容 contentLength:" + size + "kb" + " len:" + contentLength);

//                if (size > downLoader.getConfig().getSingleTaskThreshold()) {
//                    multipleTaskDownloading();
//                } else {
//                    singleTaskDownloading();
//                }

                ExecutorService executorService = Executors.newCachedThreadPool();
                List<SubTaskRecord> recordList = downLoader.getRecordManager().subTask().queryByTaskId(record.getId());
                int threadCount = maxThreads;
                if (recordList != null) {
                    threadCount = recordList.size();
                } else {
                    if (size < downLoader.getConfig().getSingleTaskThreshold()) {
                        threadCount = 1;
                    }
                }

                countDownLatch = new CountDownLatch(threadCount);
                int blockSize = (int) (record.getContentLength() / threadCount);
                List<Future<Boolean>> list = new ArrayList<>(threadCount);

                for (int i = 0; i < threadCount; i++) {
                    SubTaskRecord subRecord = null;
                    if (recordList == null){
                        long start = i * blockSize;
                        long end = (i + 1) * blockSize;
                        if (i == threadCount - 1) {
                            end = record.getContentLength(); // subTask.endLocation  +=  (length % blockSize);
                        }
                        subRecord = new SubTaskRecord();
                        subRecord.setTaskID(record.getId());
                        subRecord.setStart(start);
                        subRecord.setEnd(end);
                        downLoader.recordManager.subTask().add(subRecord);
                    }else{
                        subRecord = recordList.get(i);
                    }
                    SubTask subTask = new SubTask(this, subRecord);
                    Log.d(TAG, "run: 启动子线程" + i);
                    Future<Boolean> future = executorService.submit(subTask);
                    list.add(future);
                }

                executorService.shutdown();
                countDownLatch.await();
                for (int i = 0; i < list.size(); i++) {
                    Future<Boolean> future = list.get(i);
                    if (!future.get().booleanValue()) {
                        throw new Exception("子线程执行失败");
                    }
                }

                if (record.isFinished()) {
                    Debug.log("下载完成 " + record.getFinishedLength() + "/" + contentLength);
                    downLoader.onTaskFinished(this);
                    downLoader.dispatcher().finished(this);
                    downLoader.recordManager.task().update(record);
                    downLoader.recordManager.subTask().deleteByTaskId(record.getId());
                } else if (getState() == Const.DOWNLOAD_STATE_STOP) {
                    Debug.log("下载停止 " + record.getFinishedLength() + "/" + contentLength);
                    downLoader.onTaskStop(this);
                    downLoader.dispatcher().stoped(this);
                    downLoader.recordManager.task().update(record);
                } else if (getState() == Const.DOWNLOAD_STATE_CANCEL) {
                    Debug.log("下载取消 " + record.getFinishedLength() + "/" + contentLength);
                    downLoader.onCancelTask(this);
                    downLoader.dispatcher().finished(this);
                    downLoader.recordManager.task().delete(record.getId());
                    downLoader.recordManager.subTask().deleteByTaskId(record.getId());
                }
            } else {
                Debug.log("下载失败 " + record.getFinishedLength());
                downLoader.onTaskError(this, "响应码:" + code);
                downLoader.dispatcher().stoped(this);
                downLoader.recordManager.task().update(record);
            }

        } catch (Exception e) {
            e.printStackTrace();
            downLoader.onTaskError(this, e.toString());
            downLoader.dispatcher().stoped(this);
            downLoader.recordManager.task().update(record);
        } finally {
            closeIO();
        }
    }

    public synchronized void appendFinished(long len, SubTask subTask) {
        long finished = record.getFinishedLength() + len;
        record.setFinishedLength(finished);
        updateProcess(subTask);
    }

    public void updateProcess(SubTask subTask) {
        long curTime = System.currentTimeMillis();
        long interval = curTime - lastTime;
        delayTime += interval;
        if (delayTime > downLoader.getConfig().getUpdateInterval()
                || record.isFinished()) {
            downLoader.recordManager.task().update(record);
            downLoader.recordManager.subTask().update(subTask.record);
            downLoader.onTaskProcess(this);
            delayTime = 0;
        }

        lastTime = System.currentTimeMillis();
    }

    public void stop() {
        if (getState() == Const.DOWNLOAD_STATE_DOWNLOADING) {
            setState(Const.DOWNLOAD_STATE_STOP);
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
            if (conn != null) {
                conn.disconnect();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cancel() {
        if (getState() == Const.DOWNLOAD_STATE_DOWNLOADING) {
            setState(Const.DOWNLOAD_STATE_CANCEL);
            closeIO();
        }
        File file = new File(record.getFilePath() + record + getFileName());
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
