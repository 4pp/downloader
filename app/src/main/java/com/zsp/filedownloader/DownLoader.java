package com.zsp.filedownloader;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.zsp.filedownloader.record.RecordManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by zsp on 2017/10/27.
 * 下载器使用入口
 */

public class DownLoader {

    private static Config defConfig;
    private static DownLoader instance;

    public static DownLoader getInstance() {
        if (instance == null) {
            synchronized (DownLoader.class) {
                if (instance == null) {
                    instance = new DownLoader();
                }
            }
        }
        return instance;
    }


    Dispatcher dispatcher;
    List<DownLoadListener> listeners;
    Handler handler;
    public Config config;
    public RecordManager recordManager;



    public DownLoader() {
        this(defConfig);
    }

    public DownLoader(Config cfg) {
        config = cfg;
        dispatcher = new Dispatcher(this);
        listeners = new ArrayList<>();
        handler = new Handler(Looper.getMainLooper());
        recordManager = new RecordManager();
    }

    public void registerListener(DownLoadListener listener){
        listeners.add(listener);
    }

    public void unregisterListener(DownLoadListener listener){
        listeners.remove(listener);
    }

    public static void init(Context context,Config config) {
        defConfig = config;
        RecordManager.initialize(context);
    }

    public Config getConfig() {
        return config;
    }

    public RecordManager getRecordManager() {
        return recordManager;
    }

    public Dispatcher dispatcher() {
        return dispatcher;
    }

    public String add(String url){
       return add(url,null);
    }

    public String add(String url,String fileName){
        Task task = new Task(this,url,fileName);
        dispatcher.enqueue(task);
        onAddTask(task);
        return task.getId();
    }

    public void cancel(String id){
        Task task = dispatcher.cancel(id);
        task.setState(Const.DOWNLOAD_STATE_CANCEL);
        onCancelTask(task);
    }

    public List<Task> getTasks(){
        List<Task> list = dispatcher().getTasks();
        Collections.sort(list, new Comparator<Task>() {
            @Override
            public int compare(Task o1, Task o2) {
                return o2.getSortLevel() - o1.getSortLevel();
            }
        });
        return list;
    }

    public void cancelAll(){
        dispatcher.cancelAll();
    }

    public void onAddTask(final Task task){
        handler.post(new Runnable() {
            @Override
            public void run() {
                for (DownLoadListener listener:listeners){
                    listener.onAddTask(task);
                }
            }
        });
    }

    public void onCancelTask(final Task task){
       handler.post(new Runnable() {
           @Override
           public void run() {
               for (DownLoadListener listener:listeners){
                   listener.onCancelTask(task);
               }
           }
       });
    }

    public void onTaskConnect(final Task task){
        task.setState(Const.DOWNLOAD_STATE_CONNECT);
        handler.post(new Runnable() {
            @Override
            public void run() {
                for (DownLoadListener listener:listeners){
                    listener.onTaskConnect(task);
                }
            }
        });
    }

    public void onTaskStart(final Task task){
        task.setState(Const.DOWNLOAD_STATE_DOWNLOADING);
       handler.post(new Runnable() {
           @Override
           public void run() {
               for (DownLoadListener listener:listeners){
                   listener.onTaskStart(task);
               }
           }
       });
    }

    public void onTaskProcess(final Task task){
        handler.post(new Runnable() {
            @Override
            public void run() {
                for (DownLoadListener listener:listeners){
                    listener.onTaskProcess(task);
                }
            }
        });

    }

    public void onTaskStop(final Task task){
        handler.post(new Runnable() {
            @Override
            public void run() {
                for (DownLoadListener listener:listeners){
                    listener.onTaskStop(task);
                }
            }
        });
    }

    public void onTaskError(final Task task, final String msg){
        task.setState(Const.DOWNLOAD_STATE_ERROR);
        handler.post(new Runnable() {
            @Override
            public void run() {
                for (DownLoadListener listener:listeners){
                    listener.onTaskError(task,msg);
                }
            }
        });
    }


    public void onTaskFinished(final Task task){
        task.setState(Const.DOWNLOAD_STATE_FINISH);
        handler.post(new Runnable() {
            @Override
            public void run() {
                for (DownLoadListener listener:listeners){
                    listener.onTaskFinished(task);
                }
            }
        });
    }
}
