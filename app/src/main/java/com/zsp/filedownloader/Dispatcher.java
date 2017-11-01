package com.zsp.filedownloader;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zsp on 2017/10/31.
 */

public class Dispatcher {

    private DownLoader downLoader;
    private int maxTasks;

    public Dispatcher(DownLoader downLoader){
        this.downLoader = downLoader;
        maxTasks = this.downLoader.getConfig().getMaxTasks();
    }

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

    synchronized void finished(DownLoadTask call) {
        if (!runningTasks.remove(call)) throw new AssertionError("Task wasn't running!");
        allTasks.remove(call.getId());
        promoteCalls();
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

        if (runningTasks.contains(task)) {
            task.cancel();
        }

        if (readyTasks.contains(task)) {
            readyTasks.remove(task);
            allTasks.remove(id);
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
}
