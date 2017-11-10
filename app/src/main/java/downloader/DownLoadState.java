package downloader;

/**
 * Created by zsp on 2017/10/30.
 */

public interface DownLoadState {
    // 任务在没有进入下载执行前的初始状态,或超过最大同时下载数限制时进入等待状态
    int DOWNLOAD_STATE_WAIT = 0;
    // 网络连接中
    int DOWNLOAD_STATE_CONNECT = 1;
    // 下载中状态
    int DOWNLOAD_STATE_DOWNLOADING = 2;
    // 暂停的停止状态
    int DOWNLOAD_STATE_STOP = 3;
    // 已下载完成
    int DOWNLOAD_STATE_FINISH = 5;
    // 下载失败的停止状态
    int DOWNLOAD_STATE_ERROR = 6;
    // 被取消的状态,任务取消后会删除,这个状态也只会瞬间存在
    int DOWNLOAD_STATE_CANCEL = 7;
}
