package com.zsp.filedownloader;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by zsp on 2017/10/27.
 * 下载器使用入口
 */

public class DownLoader {

    private static DownLoader instance;
    Dispatcher dispatcher;

    List<DownLoadListener> listeners;

    Handler handler;

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

    private static Config defConfig;

    public Config config;

    public Config getConfig() {
        return config;
    }

    public DownLoader() {
        this(defConfig);
    }

    public DownLoader(Config cfg) {
        config = cfg;
        dispatcher = new Dispatcher(this);
        listeners = new ArrayList<>();
        handler = new Handler(Looper.getMainLooper());

    }

    public void registerListener(DownLoadListener listener){
        listeners.add(listener);
    }

    public void unregisterListener(DownLoadListener listener){
        listeners.remove(listener);
    }

    public static void init(Config config) {
        defConfig = config;
    }

    public Dispatcher dispatcher() {
        return dispatcher;
    }

    public String add(String url){
       return add(url,null);
    }

    public String add(String url,String fileName){
        DownLoadTask task = new DownLoadTask(this,url,fileName);
        dispatcher.enqueue(task);
        onAddTask(task);
        return task.getId();
    }

    public void cancel(String id){
        DownLoadTask task = dispatcher.cancel(id);
        task.setState(Const.DOWNLOAD_STATE_CANCEL);
        onCancelTask(task);
    }

    public List<DownLoadTask> getTasks(){
        List<DownLoadTask> list = dispatcher().getTasks();
        Collections.sort(list, new Comparator<DownLoadTask>() {
            @Override
            public int compare(DownLoadTask o1, DownLoadTask o2) {
                return o2.getSortLevel() - o1.getSortLevel();
            }
        });
        return list;
    }

    public void cancelAll(){
        dispatcher.cancelAll();
    }

    public void onAddTask(final DownLoadTask task){
        handler.post(new Runnable() {
            @Override
            public void run() {
                for (DownLoadListener listener:listeners){
                    listener.onAddTask(task);
                }
            }
        });
    }

    public void onCancelTask(final DownLoadTask task){
       handler.post(new Runnable() {
           @Override
           public void run() {
               for (DownLoadListener listener:listeners){
                   listener.onCancelTask(task);
               }
           }
       });
    }

    public void onTaskConnect(final DownLoadTask task){
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

    public void onTaskStart(final DownLoadTask task){
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

    public void onTaskProcess(final DownLoadTask task){
        handler.post(new Runnable() {
            @Override
            public void run() {
                for (DownLoadListener listener:listeners){
                    listener.onTaskProcess(task);
                }
            }
        });

    }

    public void onTaskStop(final DownLoadTask task){
        handler.post(new Runnable() {
            @Override
            public void run() {
                for (DownLoadListener listener:listeners){
                    listener.onTaskStop(task);
                }
            }
        });
    }

    public void onTaskError(final DownLoadTask task,final String msg){
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


    public void onTaskFinished(final DownLoadTask task){
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
