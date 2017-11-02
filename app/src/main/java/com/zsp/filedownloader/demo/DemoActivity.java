package com.zsp.filedownloader.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.zsp.filedownloader.DownLoadListener;
import com.zsp.filedownloader.DownLoadTask;
import com.zsp.filedownloader.DownLoader;
import com.zsp.filedownloader.R;

import java.util.List;

public class DemoActivity extends AppCompatActivity implements DownLoadListener{

    private static final String TAG = "DemoActivity";

    DownLoadAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

//        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "ddd" + File.separator;
//        File file = new File(path);
//
//        ActivityCompat.requestPermissions(this, new String[]{android
//                .Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
//
//        if (file.exists()) {
//            Config cfg = new Config.Builder()
//                    .setSaveDir(path)
//                    .build();
//            DownLoader.init(cfg);
//        }
        findViewById(R.id.btn_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DownLoader.getInstance().add("http://res9.d.cn/android/yxzx.apk",System.currentTimeMillis()+"-yxzx");
            }
        });

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        adapter = new DownLoadAdapter(this);
        adapter.setListener(new DownLoadAdapter.Listener() {
            @Override
            public void onCancel(View view, int position) {
                Log.d(TAG, "onClick: 删除:"+position);
                DownLoadTask task = (DownLoadTask) adapter.getItem(position);
                DownLoader.getInstance().cancel(task.getId());
            }
        });

        ListView listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(adapter);

        List<DownLoadTask> list = DownLoader.getInstance().getTasks();
        adapter.setDataSource(list);
        DownLoader.getInstance().registerListener(this);
    }

    @Override
    protected void onDestroy() {
        DownLoader.getInstance().unregisterListener(this);
        super.onDestroy();
    }

    @Override
    public void onAddTask(DownLoadTask task) {
        Log.d(TAG, "onAddTask: 添加任务 "+task.getFileName());
        adapter.add(task);
    }

    @Override
    public void onCancelTask(DownLoadTask task) {
        Log.d(TAG, "onCancelTask: 取消任务 "+task.getFileName());
        adapter.remove(task);
    }

    @Override
    public void onTaskConnect(DownLoadTask task) {
        Log.d(TAG, "onTaskConnect: 连接中"+task.getFileName());
    }

    @Override
    public void onTaskStart(DownLoadTask task) {
        Log.d(TAG, "onTaskStart: 启动任务 "+task.getFileName());
    }

    @Override
    public void onTaskStop(DownLoadTask task) {
        Log.d(TAG, "onTaskStop: 停止任务 "+task.getFileName());
    }

    @Override
    public void onTaskProcess(DownLoadTask task) {
        Log.d(TAG, "onTaskProcess: 执行任务 "+task.getFileName()+" 进度:"+task.getFinishedLength()+"/"+task.getContentLength());
        if (!adapter.isTouching){
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onTaskFinished(DownLoadTask task) {
        Log.d(TAG, "onTaskFinished: 下载完成:"+task.getFileName());
        adapter.remove(task);
    }

    @Override
    public void onTaskError(DownLoadTask task,String msg) {
        Log.d(TAG, "onTaskError: 下载失败:"+msg);
        adapter.notifyDataSetChanged();
    }

//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
//                                           @NonNull int[] grantResults) {
//        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        switch (requestCode) {
//            case 1:
//                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    //创建文件夹
//                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//                        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "ddd" + File.separator;
//                        File file = new File(path);
//
//                        if (!file.exists()) {
//                            if (!file.exists()) {
//                                boolean b = file.mkdirs();
//                                Log.d(TAG, "onCreate: 创建目录" + path + "===" + b);
//                            } else {
//                                Log.d(TAG, "onCreate: 已有目录" + path);
//                            }
//
//                            Config cfg = new Config.Builder()
//                                    .setSaveDir(path)
//                                    .build();
//                            DownLoader.init(cfg);
//
//                        }
//                    }
//                    break;
//                }
//        }
//    }
}
