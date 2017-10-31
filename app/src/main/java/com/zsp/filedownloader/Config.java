package com.zsp.filedownloader;

/**
 * Created by zsp on 2017/10/27.
 */

public class Config {

    // 最大线程下载数
    int maxThreadCount = 10; //每个下载任务的最大线程数
    int maxTaskCount = 10; //下载最大任务数

    //保存位置
    public String saveDir;

    public Config(Builder builder){
        saveDir = builder.saveDir;
    }

    public static class Builder{

        String saveDir;

        public Builder setSaveDir(String path){
            saveDir = path;
            return this;
        }

        public Config build(){
            Config config = new Config(this);
            return config;
        }
    }

}
