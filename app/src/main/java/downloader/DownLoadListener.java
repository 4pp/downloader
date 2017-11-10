package downloader;

/**
 * Created by zsp on 2017/10/31.
 */

public interface DownLoadListener {

    // 添加任务后
    void onAddTask(Task task);
    // 取消任务后
    void onCancelTask(Task task);
    // 连接下载资源中
    void onTaskConnect(Task task);
    // 任务开始下载
    void onTaskStart(Task task);
    // 停止 / 暂停 后
    void onTaskStop(Task task);
    // 下载中 更新进度
    void onTaskProcess(Task task);
    // 下载成功完成后
    void onTaskFinished(Task task);
    // 下载错误停止后
    void onTaskError(Task task, String msg);
}
