package com.zsp.filedownloader.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.zsp.filedownloader.DownLoadListener;
import com.zsp.filedownloader.Task;
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

        findViewById(R.id.btn_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // DownLoader.getInstance().add("http://res9.d.cn/android/yxzx.apk",System.currentTimeMillis()+"-yxzx");
                DownLoader.getInstance().add("http://192.168.3.6:8080/pdf.zip");
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
                Task task = (Task) adapter.getItem(position);
                DownLoader.getInstance().cancel(task.getId());
            }

            @Override
            public void onStop(View view, int position) {
                Log.d(TAG, "onClick: 停止:"+position);
                Task task = (Task) adapter.getItem(position);
                DownLoader.getInstance().stop(task.getId());
            }

            @Override
            public void onRestart(View view, int position) {
                Log.d(TAG, "onClick: 重启:"+position);
                Task task = (Task) adapter.getItem(position);
                DownLoader.getInstance().restart(task.getId());
            }
        });

        ListView listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(adapter);

        List<Task> list = DownLoader.getInstance().getTasks();
        adapter.setDataSource(list);
        DownLoader.getInstance().registerListener(this);
    }

    @Override
    protected void onDestroy() {
        DownLoader.getInstance().unregisterListener(this);
        super.onDestroy();
    }

    @Override
    public void onAddTask(Task task) {
        Log.d(TAG, "onAddTask: 添加任务 "+task.getFileName());
        adapter.add(task);
    }

    @Override
    public void onCancelTask(Task task) {
        Log.d(TAG, "onCancelTask: 取消任务 "+task.getFileName());
        adapter.remove(task);
    }

    @Override
    public void onTaskConnect(Task task) {
        Log.d(TAG, "onTaskConnect: 连接中"+task.getFileName());
    }

    @Override
    public void onTaskStart(Task task) {
        Log.d(TAG, "onTaskStart: 启动任务 "+task.getFileName());
    }

    @Override
    public void onTaskStop(Task task) {
        Log.d(TAG, "onTaskStop: 停止任务 "+task.getFileName());
    }

    @Override
    public void onTaskProcess(Task task) {
        Log.d(TAG, "onTaskProcess: 执行任务 "+task.getFileName()+" 进度:"+task.getFinishedLength()+"/"+task.getContentLength());
        if (!adapter.isTouching){
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onTaskFinished(Task task) {
        Log.d(TAG, "onTaskFinished: 下载完成:"+task.getFileName());
        adapter.remove(task);
    }

    @Override
    public void onTaskError(Task task, String msg) {
        Log.d(TAG, "onTaskError: 下载失败:"+msg);
        adapter.notifyDataSetChanged();
    }
}
