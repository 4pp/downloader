package downloader;

import downloader.record.SubTaskRecord;

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

    private Task pTask;
    private SubTaskRecord record;

    public SubTaskRecord getRecord() {
        return record;
    }

    public void setRecord(SubTaskRecord record) {
        this.record = record;
    }

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
        savePath = pTask.getFilePath() + pTask.getFileName();
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
            conn.setConnectTimeout(pTask.getDownLoader().getConfig().getConnectTimeout());
            conn.setReadTimeout(pTask.getDownLoader().getConfig().getReadTimeout());
            conn.setRequestProperty("Range", "bytes=" + (record.getStart() + record.getFinished()) + "-" + (record.getEnd() - 1));
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setReadTimeout(30 * 1000);
            inputStream = conn.getInputStream();
            file = new RandomAccessFile(savePath, "rwd");
            file.seek(record.getStart() + record.getFinished()); // 指定开始写文件的位置
            byte[] buffer = new byte[4096];
            int len;
            while (pTask.getState() == DownLoadState.DOWNLOAD_STATE_DOWNLOADING && (len = inputStream.read(buffer)) != -1) {
                file.write(buffer, 0, len);
                pTask.updateProcess(len,this);
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        SubTask other = (SubTask) obj;
        if (record.getId() != other.record.getId()) return false;
        return true;
    }

    @Override
    public int hashCode() {
        long id = record.getId();
        return (int) (id ^ (id >>> 32));
    }
}
