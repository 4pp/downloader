package com.zsp.filedownloader;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

/**
 * Created by zsp on 2017/10/31.
 * 分块下载子任务
 */

public class SubTask implements Callable {
    private static final String TAG = "SubTask";

    public Task pTask;
    public String id;
    public String downloadUrl;
    public String savePath;
    public long startPosition;
    public long endPosition;
    public long finishedLength;
    public long blockSize;

    InputStream inputStream;
    RandomAccessFile file;
    HttpURLConnection conn;

    public SubTask(Task downLoadTask, long start, long end) {
        pTask = downLoadTask;
        id = "SubTask-" + pTask.getSubTaskCount();
        downloadUrl = pTask.getDownloadUrl();
        savePath = pTask.getSavePath();
        startPosition = start;
        endPosition = end;
        blockSize = end - start;

    }

//    public void stop() {
//        closeIO();
//    }

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

    @Override
    public Object call() throws Exception {
        try {
            URL url = new URL(downloadUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Range", "bytes=" + startPosition + "-" + (endPosition - 1));
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setReadTimeout(30 * 1000);
            inputStream = conn.getInputStream();
            file = new RandomAccessFile(savePath, "rwd");
            file.seek(startPosition); // 指定开始写文件的位置
            byte[] buffer = new byte[4096];
            int len;
            while (pTask.getState() == Const.DOWNLOAD_STATE_DOWNLOADING && (len = inputStream.read(buffer)) != -1) {
                file.write(buffer, 0, len);
                finishedLength += len;
                pTask.appendFinished(len);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            pTask.subTaskFinished();
            closeIO();
        }
        return true;
    }
}
