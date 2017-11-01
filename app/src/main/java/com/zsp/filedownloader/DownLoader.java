package com.zsp.filedownloader;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zsp on 2017/10/27.
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

    public void addListener(DownLoadListener listener){
        listeners.add(listener);
    }

    public void removeListener(DownLoadListener listener){
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
        onCancelTask(task);
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

    public void onTaskFinished(final DownLoadTask task){
        handler.post(new Runnable() {
            @Override
            public void run() {
                for (DownLoadListener listener:listeners){
                    listener.onTaskFinished(task);
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

    public void onTaskStart(final DownLoadTask task){
       handler.post(new Runnable() {
           @Override
           public void run() {
               for (DownLoadListener listener:listeners){
                   listener.onTaskStart(task);
               }
           }
       });
    }

    public void onTaskError(final DownLoadTask task){
        handler.post(new Runnable() {
            @Override
            public void run() {
                for (DownLoadListener listener:listeners){
                    listener.onTaskError(task);
                }
            }
        });
    }

}
