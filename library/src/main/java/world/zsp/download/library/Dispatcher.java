package world.zsp.download.library;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import world.zsp.download.library.record.TaskRecord;


/**
 * Created by zsp on 2017/10/31.
 * 下载任务分发,并发控制
 */

public class Dispatcher {

    private DownLoader downLoader;
    //可最大并行执行的任务数量
    private int maxTasks;

    //暂停/停止 队列
    private final BlockingQueue<Task> stopQueue = new LinkedBlockingQueue<>();
    //准备队列
    private final BlockingQueue<Task> readyQueue = new LinkedBlockingQueue();
    //下载队列
    private final BlockingQueue<Task> downloadQueue = new LinkedBlockingQueue();
    //运行中未完成的全部队列
    private final ConcurrentMap<Long, Task> runningQueue = new ConcurrentHashMap<>();
    //已完成任务
    private final BlockingQueue<Task> finishedQueue = new LinkedBlockingQueue<>();

    private ExecutorService executorService;

    public Dispatcher(DownLoader downLoader) {
        this.downLoader = downLoader;
        maxTasks = this.downLoader.getConfig().getMaxTasks();
    }

    /**
     * 加载任务到内存中,分别放入队列
     */
    public void loadTask() {
        executorService().submit(new Runnable() {
            @Override
            public void run() {
                List<TaskRecord> list = downLoader.recordManager.task().queryAll();
                for (Iterator<TaskRecord> itr = list.iterator(); itr.hasNext(); ) {
                    TaskRecord record = itr.next();
                    Task task = new Task(downLoader, record);
                    if (record.getState() == DownLoadState.DOWNLOAD_STATE_FINISH) {
                        finishedQueue.add(task);
                    } else if (record.getState() == DownLoadState.DOWNLOAD_STATE_WAIT) {
                        readyQueue.add(task);
                        runningQueue.put(task.getId(), task);
                    } else {
                        record.setState(DownLoadState.DOWNLOAD_STATE_STOP);
                        stopQueue.add(task);
                        runningQueue.put(task.getId(), task);
                    }
                }
            }
        });
    }


    public synchronized ExecutorService executorService() {
        if (executorService == null) {
            executorService = Executors.newCachedThreadPool();
        }
        return executorService;
    }

    synchronized void enqueue(Task task) {
        if (downloadQueue.size() < maxTasks) {
            downloadQueue.add(task);
            Debug.log(task.getId() + " 可立即执行,放入下载队列");
            executorService().execute(task);
        } else {
            readyQueue.add(task);
            Debug.log(task.getId() + " 任务已满,放入等待队列");
        }
        runningQueue.put(task.getId(), task);
    }


    synchronized void removeRunningQueue(Task task) {
        if (!downloadQueue.remove(task)) throw new AssertionError("Task wasn't running!");
        runningQueue.remove(task.getId());
        Debug.log(task.getId() + " 移除执行队列");
        promoteCalls();
    }

    synchronized void moveStopQueue(Task task) {
        if (!downloadQueue.remove(task)) throw new AssertionError("Task wasn't running!");
        stopQueue.add(task);
        Debug.log(task.getId() + " 转移到停止队列");
        promoteCalls();
    }

    synchronized void moveFinishedQueue(Task task) {
        if (!downloadQueue.remove(task)) throw new AssertionError("Task wasn't running!");
        runningQueue.remove(task.getId());
        finishedQueue.add(task);
        Debug.log(task.getId() + " 转移到完成队列");
        promoteCalls();
    }

    private void promoteCalls() {
        if (downloadQueue.size() >= maxTasks) return;
        if (readyQueue.isEmpty()) return;

        for (Iterator<Task> i = readyQueue.iterator(); i.hasNext(); ) {
            Task call = i.next();
            i.remove();

            downloadQueue.add(call);
            executorService().execute(call);

            if (downloadQueue.size() >= maxTasks) return;
        }
    }

    /**
     * 重新运行已停止的任务
     *
     * @param id 任务id
     * @return
     */
    synchronized Task restartTask(long id) {
        Task task = runningQueue.get(id);

        if (!stopQueue.remove(task)) {
            return null;
        }

        if (downloadQueue.size() < maxTasks) {
            downloadQueue.add(task);
            executorService().execute(task);
        } else {
            task.setState(DownLoadState.DOWNLOAD_STATE_WAIT);
            readyQueue.add(task);
        }
        return task;
    }

    /**
     * 取消任务,包括删除已完成的任务,同时会删除下载的文件
     * @param id
     * @return
     */
    synchronized Task cancelTask(long id) {
        Task task = runningQueue.get(id);
        if (task == null) {
            for (Iterator<Task> itr = finishedQueue.iterator(); itr.hasNext(); ) {
                task = itr.next();
                if (task.getId() == id) {
                    finishedQueue.remove(task);
                    task.cancel();
                    return task;
                }
            }
        }

        if (downloadQueue.contains(task)) {
            task.cancel();
        }

        if (readyQueue.contains(task)) {
            readyQueue.remove(task);
            runningQueue.remove(id);
            task.cancel();
        }

        if (stopQueue.contains(task)) {
            stopQueue.remove(task);
            runningQueue.remove(id);
            task.cancel();
        }

        return task;
    }

    /**
     * 停止任务
     * @param id
     * @return
     */
    synchronized Task stopTask(long id) {
        Task task = runningQueue.get(id);

        if (downloadQueue.contains(task)) {
            task.stop();
        }

        return task;
    }

    public List<Task> getRunningQueue() {
        List<Task> list = new LinkedList<>();
        for (Long key : runningQueue.keySet()) {
            Task task = runningQueue.get(key);
            list.add(task);
        }
        Debug.log("加载在执行任务 " + list.size());
        return list;
    }

    public List<Task> getFinishedQueue() {
        List<Task> list = new ArrayList<>();
        for (Iterator<Task> itr = finishedQueue.iterator(); itr.hasNext(); ) {
            Task task = itr.next();
            list.add(task);
        }
        Debug.log("加载已完成任务 " + list.size());
        return list;
    }
}
