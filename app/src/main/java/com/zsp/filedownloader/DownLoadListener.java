package com.zsp.filedownloader;

/**
 * Created by zsp on 2017/10/31.
 */

public interface DownLoadListener {

    // 添加任务后
    void onAddTask(DownLoadTask task);
    // 取消任务后
    void onCancelTask(DownLoadTask task);
    // 连接下载资源中
    void onTaskConnect(DownLoadTask task);
    // 任务开始下载
    void onTaskStart(DownLoadTask task);
    // 停止 / 暂停 后
    void onTaskStop(DownLoadTask task);
    // 下载中 更新进度
    void onTaskProcess(DownLoadTask task);
    // 下载成功完成后
    void onTaskFinished(DownLoadTask task);
    // 下载错误停止后
    void onTaskError(DownLoadTask task,String msg);
}
