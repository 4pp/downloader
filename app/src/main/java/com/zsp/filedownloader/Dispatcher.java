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

    private int maxRequests = 10;

    private final Deque<DownLoadTask> readyAsyncCalls = new ArrayDeque<>();

    private final Deque<DownLoadTask> runningAsyncCalls = new ArrayDeque<>();

    private final Map<String, DownLoadTask> allAsyncCalls = new HashMap<>();

    private ExecutorService executorService;

    public synchronized ExecutorService executorService() {
        if (executorService == null) {
            executorService = Executors.newCachedThreadPool();
        }
        return executorService;
    }

    synchronized void enqueue(DownLoadTask task) {
        if (runningAsyncCalls.size() < maxRequests) {
            runningAsyncCalls.add(task);
            executorService().execute(task);
        } else {
            readyAsyncCalls.add(task);
        }
        allAsyncCalls.put(task.id, task);

    }

    synchronized void finished(DownLoadTask call) {
        if (!runningAsyncCalls.remove(call)) throw new AssertionError("AsyncCall wasn't running!");
        allAsyncCalls.remove(call.id);
        promoteCalls();
    }

    private void promoteCalls() {
        if (runningAsyncCalls.size() >= maxRequests) return; // Already running max capacity.
        if (readyAsyncCalls.isEmpty()) return; // No ready calls to promote.

        for (Iterator<DownLoadTask> i = readyAsyncCalls.iterator(); i.hasNext(); ) {
            DownLoadTask call = i.next();
            i.remove();

            runningAsyncCalls.add(call);
            executorService().execute(call);

            if (runningAsyncCalls.size() >= maxRequests) return; // Reached max capacity.
        }
    }

    public synchronized DownLoadTask cancel(String id) {
        DownLoadTask task = allAsyncCalls.get(id);

        if (runningAsyncCalls.contains(task)) {
            task.cancel();
        }

        if (readyAsyncCalls.contains(task)) {
            readyAsyncCalls.remove(task);
            allAsyncCalls.remove(id);
        }

        return task;
    }

    public synchronized void cancelAll() {

        for (DownLoadTask task : readyAsyncCalls) {
            task.cancel();
            readyAsyncCalls.remove(task);
            allAsyncCalls.remove(task.id);
        }

        for (DownLoadTask call : runningAsyncCalls) {
            call.cancel();
        }
    }
}
