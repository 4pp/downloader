package downloader.demo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import downloader.DownLoadListener;
import downloader.DownLoader;
import com.zsp.filedownloader.R;
import downloader.Task;

import java.util.List;


/**
 * Created by zsp on 2017/11/7.
 */

public class DownloadFragment extends Fragment implements DownLoadListener {

    private static final String TAG = "DownloadFragment";

    private static final String ARG_PARAM1 = "param1";
    private static final int TYPE_FINNISHED = 1; //已完成的
    private static final int TYPE_UNFINNISHED = 0;//未完成的,下载中、暂停、等待任务

    ListView listView;
    DownLoadAdapter adapter;

    private int type;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View rootView = inflater.inflate(getLayoutId(), container, false);
        initView(rootView,savedInstanceState);
        return  rootView;
    }

    protected int getLayoutId() {
        return R.layout.fragment_list_download;
    }

    protected void initView(View rootView, Bundle savedInstanceState) {

        if (getArguments() != null) {
            setType(getArguments().getInt(ARG_PARAM1));
        }
        adapter = new DownLoadAdapter(getContext());
        adapter.setListener(new DownLoadAdapter.Listener() {
            @Override
            public void onCancel(View view, int position) {
                Log.d(TAG, "onClick: 删除:" + position);
                Task task = (Task) adapter.getItem(position);
                DownLoader.getInstance().cancel(task.getId());
            }

            @Override
            public void onStop(View view, int position) {
                Log.d(TAG, "onClick: 停止:" + position);
                Task task = (Task) adapter.getItem(position);
                DownLoader.getInstance().stop(task.getId());
            }

            @Override
            public void onRestart(View view, int position) {
                Log.d(TAG, "onClick: 重启:" + position);
                Task task = (Task) adapter.getItem(position);
                DownLoader.getInstance().restart(task.getId());
            }

            @Override
            public void onOpen(View view, int position) {

            }
        });

        listView = (ListView) rootView.findViewById(R.id.listview);
        listView.setAdapter(adapter);

        Log.d(TAG, "initView: 注册");
        DownLoader.getInstance().registerListener(this);
    }

    @Override
    public void onResume() {

        Log.d(TAG, "onResume: =======" + type);
        if (type == TYPE_UNFINNISHED) {
            List<Task> list = DownLoader.getInstance().getRunningTasks();
            adapter.setDataSource(list);
        } else if (type == TYPE_FINNISHED) {
            List<Task> list = DownLoader.getInstance().getFinishedTasks();
            adapter.setDataSource(list);
        }

        super.onResume();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "initView: 销毁");
        DownLoader.getInstance().unregisterListener(this);
        super.onDestroy();
    }

    public static DownloadFragment newInstance(int type) {
        DownloadFragment fragment = new DownloadFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAddTask(Task task) {
        if (type == TYPE_UNFINNISHED) {
            Log.d(TAG, "onAddTask: 添加任务 " + task.getFileName());
            adapter.add(task);
        }
    }

    @Override
    public void onCancelTask(Task task) {
        //if (type == TYPE_UNFINNISHED)
        {
            Log.d(TAG, type+"onCancelTask: 取消任务 " + task.getFileName());
            adapter.remove(task);
        }
    }

    @Override
    public void onTaskConnect(Task task) {
        if (type == TYPE_UNFINNISHED) {
            Log.d(TAG, "onTaskConnect: 连接中" + task.getFileName());
            adapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onTaskStart(Task task) {
        if (type == TYPE_UNFINNISHED) {
            Log.d(TAG, "onTaskStart: 启动任务 " + task.getFileName());
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onTaskStop(Task task) {
        if (type == TYPE_UNFINNISHED)
        {
            Log.d(TAG,"onTaskStop: 停止任务 " + task.getFileName());
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onTaskProcess(Task task) {
        if (type == TYPE_UNFINNISHED) {
            //Log.d(TAG, "onTaskProcess: 执行任务 " + task.getFileName() + " 进度:" + task.getFinishedLength() + "/" + task.getContentLength());
            if (!adapter.isTouching) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onTaskFinished(Task task) {
        if (type == TYPE_UNFINNISHED) {
            Log.d(TAG, "onTaskFinished: 下载完成:" + task.getFileName());
            adapter.remove(task);
        } else {
            adapter.insert(task);
        }
    }

    @Override
    public void onTaskError(Task task, String msg) {
        if (type == TYPE_UNFINNISHED) {
            Log.d(TAG, "onTaskError: 下载失败:" + msg);
            adapter.notifyDataSetChanged();
        }
    }
}
