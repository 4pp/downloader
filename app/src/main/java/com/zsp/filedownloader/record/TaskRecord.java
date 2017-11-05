package com.zsp.filedownloader.record;

import android.text.TextUtils;

/**
 * Created by zsp on 2017/11/2.
 */

public class TaskRecord {

    private long id;
    private String downloadUrl;
    private String filePath;
    private String fileName;
    private String mimeType;
    private String eTag;
    private String disposition;
    private long contentLength;
    private volatile long finishedLength;
    private int state;
    private long createAt;
    private int[] tasks;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String geteTag() {
        return eTag;
    }

    public void seteTag(String eTag) {
        this.eTag = eTag;
    }

    public String getDisposition() {
        return disposition;
    }

    public void setDisposition(String disposition) {
        this.disposition = disposition;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public synchronized long getFinishedLength() {
        return finishedLength;
    }

    public synchronized void setFinishedLength(long finishedLength) {
        this.finishedLength = finishedLength;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public long getCreateAt() {
        return createAt;
    }

    public void setCreateAt(long createAt) {
        this.createAt = createAt;
    }

    public int[] getTasks() {
        return tasks;
    }

    public void setTasks(int[] tasks) {
        this.tasks = tasks;
    }

    public String getTasksToString(){
        StringBuffer sb = new StringBuffer();
        if (tasks != null){
            for (int i=0;i<tasks.length;i++){
                sb.append(tasks[i]);
                if (i<tasks.length-1){
                    sb.append(",");
                }
            }
        }
        return sb.toString();
    }

    public void setTasksToArray(String str){
        if (TextUtils.isEmpty(str)){
            return;
        }
        String[] ids = str.split(",");
        tasks = new int[ids.length];
        for (int i=0;i<ids.length;i++){
            tasks[i] = Integer.parseInt(ids[i]);
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("\r\n");
        sb.append(">>>====================================");
        sb.append("\r\n");
        sb.append("id:"+id);
        sb.append("\r\n");
        sb.append("downloadUrl:"+downloadUrl);
        sb.append("\r\n");
        sb.append("filePath:"+filePath);
        sb.append("\r\n");
        sb.append("fileName:"+fileName);
        sb.append("\r\n");
        sb.append("mimeType:"+mimeType);
        sb.append("\r\n");
        sb.append("eTag:"+eTag);
        sb.append("\r\n");
        sb.append("disposition:"+disposition);
        sb.append("\r\n");
        sb.append("contentLength:"+contentLength);
        sb.append("\r\n");
        sb.append("finishedLength:"+finishedLength);
        sb.append("\r\n");
        sb.append("state:"+state);
        sb.append("\r\n");
        sb.append("createAt:"+createAt);
        sb.append("\r\n");
        sb.append("tasks:"+getTasksToString());
        sb.append("\r\n");
        sb.append("<<<====================================");
        sb.append("\r\n");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        TaskRecord other = (TaskRecord) obj;
        if (id != other.getId()) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return (int)(id ^ (id >>> 32));
    }

    public boolean isFinished(){
        return finishedLength == contentLength;
    }

    public boolean isNew(){
       return getContentLength() == 0 && getFinishedLength() == 0;
    }
}
