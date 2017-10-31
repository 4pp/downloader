package com.zsp.filedownloader;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by zsp on 2017/10/28.
 */

public class DownLoadService extends Service {

    public DownLoadBinder binder;

    private static final String TAG = "DownLoadService";

    @Override
    public void onCreate() {
        super.onCreate();
        binder = new DownLoadBinder();
        Log.d(TAG, "onCreate: 创建服务");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: 绑定服务");
        return binder;
    }

    public class DownLoadBinder extends Binder{

        public DownLoadService getService(){
            return DownLoadService.this;
        }
    }

}
