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
                .build();
        DownLoader.init(this,cfg);

    }
}
