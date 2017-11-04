package com.zsp.filedownloader;

/**
 * Created by zsp on 2017/10/30.
 */

public class Config {

    private int maxThreads;
    private int maxTasks;
    private int updateInterval;
    private int singleTaskThreshold;
    private String saveDir;

    public String getSaveDir() {
        return saveDir;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public int getMaxTasks() {
        return maxTasks;
    }

    public int getSingleTaskThreshold() {
        return singleTaskThreshold;
    }

    public int getUpdateInterval() {
        return updateInterval;
    }

    public Config(Builder builder) {
        saveDir = builder.saveDir;
        maxTasks = builder.maxTasks;
        maxThreads = builder.maxThreads;
        updateInterval = builder.updateInterval;
        singleTaskThreshold = builder.singleTaskThreshold;
        Debug.enable = builder.debug;

    }

    public static class Builder {

        //保存路径
        private String saveDir;
        //每个下载任务的最大线程数量
        private int maxThreads = 3;
        //最大并发下载任务数量
        private int maxTasks = 2;
        //下载进度更新回调间隔时间,单位毫秒
        private int updateInterval = 50;
        //启用任务内多线程下载的数据量的临界值,单位KB
        private int singleTaskThreshold = 100;
        private boolean debug = true;


        public Builder setSaveDir(String path) {
            saveDir = path;
            return this;
        }

        public Builder setMaxThreads(int i) {
            maxThreads = i;
            return this;
        }

        public Builder setMaxTasks(int i) {
            maxTasks = i;
            return this;
        }

        public Builder setUpdateInterval(int i) {
            updateInterval = i;
            return this;
        }

        public Builder setSingleTaskThreshold(int i) {
            singleTaskThreshold = i;
            return this;
        }

        public Builder debug(boolean enable){
            debug = enable;
            return this;
        }

        public Config build() {
            Config config = new Config(this);
            return config;
        }

    }

}
