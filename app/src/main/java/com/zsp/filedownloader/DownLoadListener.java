package com.zsp.filedownloader;

/**
 * Created by zsp on 2017/10/31.
 */

public interface DownLoadListener {

    void onAddTask(DownLoadTask task);

    void onCancelTask(DownLoadTask task);

    void onTaskStart(DownLoadTask task);

    void onTaskProcess(DownLoadTask task);

    void onTaskFinished(DownLoadTask task);
}
