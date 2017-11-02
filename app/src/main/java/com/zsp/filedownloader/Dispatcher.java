package com.zsp.filedownloader;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zsp on 2017/10/31.
 * 下载任务分发,并发控制
 */

public class Dispatcher {

    private DownLoader downLoader;
    private int maxTasks;

    public Dispatcher(DownLoader downLoader) {
        this.downLoader = downLoader;
        maxTasks = this.downLoader.getConfig().getMaxTasks();
    }

    private final Deque<DownLoadTask> pauseTasks = new ArrayDeque<>();

    private final Deque<DownLoadTask> readyTasks = new ArrayDeque<>();

    private final Deque<DownLoadTask> runningTasks = new ArrayDeque<>();

    private final Map<String, DownLoadTask> allTasks = new HashMap<>();

    private ExecutorService executorService;

    public synchronized ExecutorService executorService() {
        if (executorService == null) {
            executorService = Executors.newCachedThreadPool();
        }
        return executorService;
    }

    synchronized void enqueue(DownLoadTask task) {
        if (runningTasks.size() < maxTasks) {
            runningTasks.add(task);
            executorService().execute(task);
        } else {
            readyTasks.add(task);
        }
        allTasks.put(task.getId(), task);
    }


    synchronized void finished(DownLoadTask task) {
        if (!runningTasks.remove(task)) throw new AssertionError("Task wasn't running!");
        allTasks.remove(task.getId());
        promoteCalls();
    }

    synchronized void stoped(DownLoadTask task){
        if (!runningTasks.remove(task)) throw new AssertionError("Task wasn't running!");
        pauseTasks.add(task);
        promoteCalls();
    }

    synchronized void restart(String id){
        DownLoadTask task = allTasks.get(id);
        if (!pauseTasks.remove(task)) throw new AssertionError("Task wasn't running!");

        if (runningTasks.size() < maxTasks) {
            runningTasks.add(task);
            executorService().execute(task);
        } else {
            task.setState(Const.DOWNLOAD_STATE_WAIT);
            readyTasks.add(task);
        }
    }

    private void promoteCalls() {
        if (runningTasks.size() >= maxTasks) return;
        if (readyTasks.isEmpty()) return;

        for (Iterator<DownLoadTask> i = readyTasks.iterator(); i.hasNext(); ) {
            DownLoadTask call = i.next();
            i.remove();

            runningTasks.add(call);
            executorService().execute(call);

            if (runningTasks.size() >= maxTasks) return;
        }
    }

    public synchronized DownLoadTask cancel(String id) {
        DownLoadTask task = allTasks.get(id);
        if (task == null){
            return null;
        }

        if (runningTasks.contains(task)) {
            task.cancel();
        }

        if (readyTasks.contains(task)) {
            readyTasks.remove(task);
            allTasks.remove(id);
        }

        return task;
    }

    public synchronized DownLoadTask stop(String id) {
        DownLoadTask task = allTasks.get(id);
        if (task == null){
            return null;
        }

        if (runningTasks.contains(task)) {
            task.stop();
        }

        return task;
    }

    public synchronized void cancelAll() {

        for (DownLoadTask task : readyTasks) {
            task.cancel();
            readyTasks.remove(task);
            allTasks.remove(task.getId());
        }

        for (DownLoadTask call : runningTasks) {
            call.cancel();
        }
    }

    public List<DownLoadTask> getTasks() {
        List<DownLoadTask> list = new LinkedList<>();
        for (String key : allTasks.keySet()) {
            DownLoadTask task = allTasks.get(key);
            list.add(task);
        }
        return list;
    }
}
