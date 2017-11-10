package com.zsp.filedownloader;

/**
 * Created by zsp on 2017/10/30.
 */

public interface DownLoadState {
    int DOWNLOAD_STATE_WAIT = 0;
    int DOWNLOAD_STATE_CONNECT = 1;
    int DOWNLOAD_STATE_DOWNLOADING = 2;
    int DOWNLOAD_STATE_STOP = 3;
    int DOWNLOAD_STATE_FINISH = 5;
    int DOWNLOAD_STATE_ERROR = 6;
    int DOWNLOAD_STATE_CANCEL = 7;
}
