package com.zsp.filedownloader.record;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zsp.filedownloader.Debug;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by zsp on 2017/11/3.
 */

public class TaskRecordDAO {

    public static final String TABLE_NAME = SqlConst.TB_TASK;

    public long add(TaskRecord record) {
        Debug.log("添加记录\r\n"+record);
        SQLiteDatabase db = RecordManager.openDatabase();
        ContentValues values = contentValuesToRecord(record);
        long id = db.insert(TABLE_NAME, null, values);
        record.setId(id);
        return id;
    }


    public int update(TaskRecord record) {
        SQLiteDatabase db = RecordManager.openDatabase();
        ContentValues values = contentValuesToRecord(record);
        String whereClause = "id=?";
        String[] whereArgs = {String.valueOf(record.getId())};
        return db.update(TABLE_NAME, values, whereClause, whereArgs);
    }

    public int delete(long id) {
        SQLiteDatabase db = RecordManager.openDatabase();
        String whereClause = "id=?";
        String[] whereArgs = {String.valueOf(id)};
        return db.delete(TABLE_NAME, whereClause, whereArgs);
    }

    public TaskRecord querySingle(String whereClause,String[] whereArgs) {
        SQLiteDatabase db = RecordManager.openDatabase();
        Cursor cursor = db.rawQuery(TABLE_NAME,whereArgs);
        TaskRecord record = null;
        if (cursor.moveToFirst()) {
            record = cursorToRecord(cursor);
        }
        cursor.close();
        return record;
    }

    public List<TaskRecord> query(String whereClause, String[] whereArgs) {
        List list = null;
        SQLiteDatabase db = RecordManager.openDatabase();
        Cursor cursor = db.rawQuery(TABLE_NAME,whereArgs);
        if (cursor.getCount() > 0){
            list = new LinkedList();
            if (cursor.moveToFirst()) {
                for (int i = 0; i < cursor.getCount(); i++) {
                    TaskRecord record = cursorToRecord(cursor);
                    list.add(record);
                    cursor.moveToNext();
                }
            }
        }

        cursor.close();
        return list;
    }

    private ContentValues contentValuesToRecord(TaskRecord record){
        ContentValues values = new ContentValues();
        values.put(SqlConst.TB_TASK_URL, record.getDownloadUrl());
        values.put(SqlConst.TB_TASK_DIR_PATH, record.getFilePath());
        values.put(SqlConst.TB_TASK_FINISHED, record.getFinishedLength());
        values.put(SqlConst.TB_TASK_CONTENT, record.getContentLength());
        values.put(SqlConst.TB_TASK_FILE_NAME, record.getFileName());
        values.put(SqlConst.TB_TASK_MIME_TYPE, record.getMimeType());
        values.put(SqlConst.TB_TASK_ETAG, record.geteTag());
        values.put(SqlConst.TB_TASK_DISPOSITION, record.getDisposition());
        values.put(SqlConst.TB_TASK_STATE, record.getState());
        values.put(SqlConst.TB_SUB, record.getTasksToString());
        values.put(SqlConst.TB_CREATE_AT, record.getCreateAt());
        return values;
    }

    private TaskRecord cursorToRecord(Cursor cursor){
        TaskRecord record = new TaskRecord();
        record.setId(cursor.getLong(0));
        record.setDownloadUrl(cursor.getString(1));
        record.setFilePath(cursor.getString(2));
        record.setFinishedLength(cursor.getLong(3));
        record.setContentLength(cursor.getLong(4));
        record.setState(cursor.getInt(5));
        record.setFileName(cursor.getString(6));
        record.setMimeType(cursor.getString(7));
        record.seteTag(cursor.getString(8));
        record.setTasksToArray(cursor.getString(9));
        record.setCreateAt(cursor.getLong(10));
        record.setDisposition(cursor.getString(11));
        return record;
    }
}
