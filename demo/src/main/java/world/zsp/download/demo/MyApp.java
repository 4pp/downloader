package world.zsp.download.demo;

import android.app.Application;
import android.os.Environment;


import java.io.File;

import world.zsp.download.library.Config;
import world.zsp.download.library.DownLoader;

/**
 * Created by zsp on 2017/11/3.
 */

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //设置文件的下载目录
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() +
                File.separator + "mydownload" + File.separator;

        Config cfg = new Config.Builder()
                .setSaveDir(path)
                .setMaxTasks(5) //最大并发下载任务数量,默认5
                .setMaxThreads(3)//每个下载任务的最大线程数量默认3
                .setSingleTaskThreshold(100)//判断单或或线程下载的数据量,大于则多线程 ,小于则单个线程下载, 单位KB
                .build();
        DownLoader.init(this,cfg);

    }
}
