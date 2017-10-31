package com.zsp.filedownloader.demo;

import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.zsp.filedownloader.Config;
import com.zsp.filedownloader.DownLoadListener;
import com.zsp.filedownloader.DownLoadTask;
import com.zsp.filedownloader.DownLoader;
import com.zsp.filedownloader.R;

import java.io.File;

public class DemoActivity extends AppCompatActivity implements DownLoadListener{

    private static final String TAG = "DemoActivity";

    DownLoadAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "ddd" + File.separator;
        File file = new File(path);

        ActivityCompat.requestPermissions(this, new String[]{android
                .Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        if (file.exists()) {
            Config cfg = new Config.Builder()
                    .setSaveDir(path)
                    .build();
            DownLoader.init(cfg);
        }

        DownLoader.getInstance().addListener(this);

        findViewById(R.id.btn_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DownLoader.getInstance().add("http://res9.d.cn/android/yxzx.apk");
            }
        });

        adapter = new DownLoadAdapter(this);
        ListView listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(adapter);

    }

    @Override
    protected void onDestroy() {
        DownLoader.getInstance().removeListener(this);

        super.onDestroy();
    }

    @Override
    public void onAddTask(DownLoadTask task) {
        Log.d(TAG, "onAddTask: 添加任务 "+task.fileName);
        adapter.add(task);
    }

    @Override
    public void onCancelTask(DownLoadTask task) {
        Log.d(TAG, "onAddTask: 取消任务 "+task.fileName);
    }

    @Override
    public void onTaskStart(DownLoadTask task) {
        Log.d(TAG, "onTaskStart: 启动任务 "+task.fileName);
    }

    @Override
    public void onTaskProcess(DownLoadTask task) {
        Log.d(TAG, "onTaskProcess: 执行任务 "+task.fileName+" 进度:"+task.finishedLength+"/"+task.contentLength);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onTaskFinished(DownLoadTask task) {
        Log.d(TAG, "onTaskFinished: 下载完成:"+task.fileName);
        adapter.remove(task);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //创建文件夹
                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "ddd" + File.separator;
                        File file = new File(path);

                        if (!file.exists()) {
                            if (!file.exists()) {
                                boolean b = file.mkdirs();
                                Log.d(TAG, "onCreate: 创建目录" + path + "===" + b);
                            } else {
                                Log.d(TAG, "onCreate: 已有目录" + path);
                            }

                            Config cfg = new Config.Builder()
                                    .setSaveDir(path)
                                    .build();
                            DownLoader.init(cfg);

                        }
                    }
                    break;
                }
        }
    }
}
