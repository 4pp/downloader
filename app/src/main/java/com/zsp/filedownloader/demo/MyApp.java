package com.zsp.filedownloader.demo;

import android.app.Application;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;

import com.zsp.filedownloader.Config;
import com.zsp.filedownloader.DownLoader;

import java.io.File;

/**
 * Created by zsp on 2017/11/3.
 */

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "world" + File.separator;
        Config cfg = new Config.Builder()
                .setSaveDir(path)
                .build();
        DownLoader.init(this,cfg);

    }
}
