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

    private final Deque<Task> pauseTasks = new ArrayDeque<>();

    private final Deque<Task> readyTasks = new ArrayDeque<>();

    private final Deque<Task> runningTasks = new ArrayDeque<>();

    private final Map<String, Task> allTasks = new HashMap<>();

    private ExecutorService executorService;

    public synchronized ExecutorService executorService() {
        if (executorService == null) {
            executorService = Executors.newCachedThreadPool();
        }
        return executorService;
    }

    synchronized void enqueue(Task task) {
        if (runningTasks.size() < maxTasks) {
            runningTasks.add(task);
            executorService().execute(task);
        } else {
            readyTasks.add(task);
        }
        allTasks.put(task.getId(), task);
    }


    synchronized void finished(Task task) {
        if (!runningTasks.remove(task)) throw new AssertionError("Task wasn't running!");
        allTasks.remove(task.getId());
        promoteCalls();
    }

    synchronized void stoped(Task task){
        if (!runningTasks.remove(task)) throw new AssertionError("Task wasn't running!");
        pauseTasks.add(task);
        promoteCalls();
    }

    synchronized void restart(String id){
        Task task = allTasks.get(id);
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

        for (Iterator<Task> i = readyTasks.iterator(); i.hasNext(); ) {
            Task call = i.next();
            i.remove();

            runningTasks.add(call);
            executorService().execute(call);

            if (runningTasks.size() >= maxTasks) return;
        }
    }

    public synchronized Task cancel(String id) {
        Task task = allTasks.get(id);
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

    public synchronized Task stop(String id) {
        Task task = allTasks.get(id);
        if (task == null){
            return null;
        }

        if (runningTasks.contains(task)) {
            task.stop();
        }

        return task;
    }

    public synchronized void cancelAll() {

        for (Task task : readyTasks) {
            task.cancel();
            readyTasks.remove(task);
            allTasks.remove(task.getId());
        }

        for (Task call : runningTasks) {
            call.cancel();
        }
    }

    public List<Task> getTasks() {
        List<Task> list = new LinkedList<>();
        for (String key : allTasks.keySet()) {
            Task task = allTasks.get(key);
            list.add(task);
        }
        return list;
    }
}
