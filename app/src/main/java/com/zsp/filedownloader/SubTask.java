package com.zsp.filedownloader;

import com.zsp.filedownloader.record.SubTaskRecord;

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

    public Task pTask;
    public SubTaskRecord record;

    public long id;
    public String downloadUrl;
    public String savePath;

    InputStream inputStream;
    RandomAccessFile file;
    HttpURLConnection conn;

    public SubTask(Task downLoadTask, SubTaskRecord record) {
        pTask = downLoadTask;
        this.record = record;
        id = record.getId();
        downloadUrl = pTask.getDownloadUrl();
        savePath = pTask.getFilePath()+pTask.getFileName();
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
            conn.setRequestProperty("Range", "bytes=" + record.getStart() + "-" + (record.getEnd() - 1));
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setReadTimeout(30 * 1000);
            inputStream = conn.getInputStream();
            file = new RandomAccessFile(savePath, "rwd");
            file.seek(record.getStart()); // 指定开始写文件的位置
            byte[] buffer = new byte[4096];
            int len;
            while (pTask.getState() == Const.DOWNLOAD_STATE_DOWNLOADING && (len = inputStream.read(buffer)) != -1) {
                file.write(buffer, 0, len);
                long finished = record.getFinshed() + len;
                pTask.appendFinished(len,record.getId(),finished);
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
