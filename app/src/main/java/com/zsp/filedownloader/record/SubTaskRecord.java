package com.zsp.filedownloader.record;

/**
 * Created by zsp on 2017/11/2.
 */

public class SubTaskRecord {

    public long id;
    public long taskID;
    public long start;
    public long end;
    public long finshed;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTaskID() {
        return taskID;
    }

    public void setTaskID(long taskID) {
        this.taskID = taskID;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public long getFinshed() {
        return finshed;
    }

    public void setFinshed(long finshed) {
        this.finshed = finshed;
    }

}
