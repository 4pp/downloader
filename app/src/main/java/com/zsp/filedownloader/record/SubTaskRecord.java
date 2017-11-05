package com.zsp.filedownloader.record;

/**
 * Created by zsp on 2017/11/2.
 */

public class SubTaskRecord {

    private long id;
    private long taskID;
    private long start;
    private long end;
    private long finished;

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

    public long getFinished() {
        return finished;
    }

    public void setFinished(long finished) {
        this.finished = finished;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("\r\n");
        sb.append(">>>====================================");
        sb.append("\r\n");
        sb.append("id:"+id);
        sb.append("\r\n");
        sb.append("taskId:"+taskID);
        sb.append("\r\n");
        sb.append("start:"+start);
        sb.append("\r\n");
        sb.append("end:"+end);
        sb.append("\r\n");
        sb.append("finished:"+finished);
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
        SubTaskRecord other = (SubTaskRecord) obj;
        if (id != other.getId()) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return (int)(id ^ (id >>> 32));
    }

}
