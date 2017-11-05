package com.zsp.filedownloader;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.zsp.filedownloader.record.RecordManager;
import com.zsp.filedownloader.record.TaskRecord;

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
        Debug.log("初始化 DownLoader");
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

    public long add(String url){
       return add(url,null);
    }

    public long add(String url,String fileName){
        TaskRecord record = new TaskRecord();
        record.setCreateAt(System.currentTimeMillis());
        record.setDownloadUrl(url);
        String saveFileName = getSaveFileName(url,fileName);
        record.setFilePath(getConfig().getSaveDir());
        record.setFileName(saveFileName);
        recordManager.task().add(record);

        Task task = new Task(this,record);
        dispatcher.enqueue(task);
        onAddTask(task);
        return record.getId();
    }

    public void cancel(long id){
        Task task = dispatcher.cancelTask(id);
        onCancelTask(task);
    }

    public void stop(long id){
        Task task = dispatcher.stopTask(id);
        onTaskStop(task);
    }

    public void restart(long id){
        Task task = dispatcher.restartTask(id);
        onTaskStop(task);
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

//    public void cancelAll(){
//        dispatcher.cancelAll();
//    }

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

    public String getSaveFileName(String url,String fileName){
        String saveName = fileName;
        if (TextUtils.isEmpty(fileName)) {
            saveName = url.substring(url.lastIndexOf("/"));
            if (TextUtils.isEmpty(saveName)) {
                saveName = "download-" + System.currentTimeMillis();
            }
        } else {
            if (saveName.indexOf(".") == -1){
                String suffix = url.substring(url.lastIndexOf("."));
                if (!TextUtils.isEmpty(suffix)) {
                    saveName = fileName + suffix;
                }
            }
        }
        return saveName;
    }
}
