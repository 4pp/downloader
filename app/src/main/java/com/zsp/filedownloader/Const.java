package com.zsp.filedownloader;

/**
 * Created by zsp on 2017/10/30.
 */

public interface Const {
    boolean DEBUG = true;
    int DOWNLOAD_STATE_WAIT = 0;
    int DOWNLOAD_STATE_DOWNLOADING = 1;
    int DOWNLOAD_STATE_STOP = 2;
    int DOWNLOAD_STATE_FINISH = 3;
    int DOWNLOAD_STATE_ERROR = 4;
}
