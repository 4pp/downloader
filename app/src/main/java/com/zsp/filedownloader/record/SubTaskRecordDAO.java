package com.zsp.filedownloader.record;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.zsp.filedownloader.Debug;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by zsp on 2017/11/3.
 */

public class SubTaskRecordDAO {

    static final String TABLE_NAME = SqlConst.TB_THREAD;

    public long add(SubTaskRecord record) {
        Debug.log("添加子任务记录" + record);
        SQLiteDatabase db = RecordManager.openDatabase();
        ContentValues values = contentValuesToRecord(record);
        long id = db.insert(TABLE_NAME, null, values);
        record.setId(id);
        return id;
    }

    public int update(SubTaskRecord record) {
        Debug.log("更新子任务记录" + record);
        SQLiteDatabase db = RecordManager.openDatabase();
        ContentValues values = contentValuesToRecord(record);
        String whereClause = BaseColumns._ID + "=?";
        String[] whereArgs = {String.valueOf(record.getId())};
        return db.update(TABLE_NAME, values, whereClause, whereArgs);
    }

    public int delete(long id,long pid) {
        Debug.log("删除子任务记录 子id=" + id + "父id="+pid);
        SQLiteDatabase db = RecordManager.openDatabase();
        String whereClause = BaseColumns._ID + "=? and "+SqlConst.TB_TASK_ID+"=?";
        String[] whereArgs = {
                String.valueOf(id),
                String.valueOf(pid)
        };
        return db.delete(TABLE_NAME, whereClause, whereArgs);
    }

    public SubTaskRecord querySingle(String whereClause,String[] whereArgs) {
        SQLiteDatabase db = RecordManager.openDatabase();
        Cursor cursor = db.rawQuery(TABLE_NAME,whereArgs);
        SubTaskRecord record = null;
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
                    SubTaskRecord record = cursorToRecord(cursor);
                    list.add(record);
                    cursor.moveToNext();
                }
            }
        }

        cursor.close();
        return list;
    }

    private ContentValues contentValuesToRecord(SubTaskRecord record){
        ContentValues values = new ContentValues();
        values.put(SqlConst.TB_TASK_ID, record.getTaskID());
        values.put(SqlConst.TB_THREAD_START, record.getStart());
        values.put(SqlConst.TB_THREAD_END, record.getEnd());
        values.put(SqlConst.TB_THREAD_FINISHED, record.getFinshed());
        return values;
    }

    private SubTaskRecord cursorToRecord(Cursor cursor){
        SubTaskRecord record = new SubTaskRecord();
        record.setId(cursor.getLong(0));
        record.setStart(cursor.getLong(1));
        record.setEnd(cursor.getLong(2));
        record.setFinshed(cursor.getLong(3));
        return record;
    }
}
