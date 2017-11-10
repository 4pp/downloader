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
        SQLiteDatabase db = RecordManager.openDatabase();
        ContentValues values = contentValuesToRecord(record);
        long id = db.insert(TABLE_NAME, null, values);
        record.setId(id);
        Debug.log("添加一条子任务记录" + record);
        return id;
    }

    public int update(SubTaskRecord record) {
        SQLiteDatabase db = RecordManager.openDatabase();
        ContentValues values = contentValuesToRecord(record);
        String whereClause = BaseColumns._ID + "=?";
        String[] whereArgs = {String.valueOf(record.getId())};
        int count = db.update(TABLE_NAME, values, whereClause, whereArgs);
        //Debug.log("更新"+count+"条子任务记录" + record);
        return count;
    }

    public int delete(String whereClause,String[] whereArgs) {
        SQLiteDatabase db = RecordManager.openDatabase();
        return db.delete(TABLE_NAME, whereClause, whereArgs);
    }

    public int deleteByTaskId(long pid) {
        String whereClause = SqlConst.TB_TASK_ID+"=?";
        String[] whereArgs = {
                String.valueOf(pid)
        };
        int count =  delete(whereClause,whereArgs);
        Debug.log("删除"+count+"条子任务记录 taskId="+pid);
        return count;

    }

    public SubTaskRecord querySingle(String whereClause, String[] whereArgs) {
        SQLiteDatabase db = RecordManager.openDatabase();
        String sql = "select * from "+ TABLE_NAME +" where "+whereClause;
        Cursor cursor = db.rawQuery(sql,whereArgs);
        SubTaskRecord record = null;
        if (cursor.moveToFirst()) {
            record = cursorToRecord(cursor);
        }
        cursor.close();
        return record;
    }

    public List<SubTaskRecord> queryByTaskId(long taskId){
        String whereClause = SqlConst.TB_TASK_ID+"=?";
        String[] whereArgs = {
                String.valueOf(taskId)
        };
        return query(whereClause,whereArgs);
    }


    public List<SubTaskRecord> query(String whereClause, String[] whereArgs) {
        List list = null;
        SQLiteDatabase db = RecordManager.openDatabase();
        String sql = "select * from "+ TABLE_NAME +" where "+whereClause;
        Cursor cursor = db.rawQuery(sql,whereArgs);
        if (cursor.getCount() > 0){
            list = new LinkedList();
            if (cursor.moveToFirst()) {
                Debug.log("查询开始================================");
                for (int i = 0; i < cursor.getCount(); i++) {
                    SubTaskRecord record = cursorToRecord(cursor);
                    list.add(record);
                    Debug.log(record.toString());
                    cursor.moveToNext();
                }
                Debug.log("查询完成================================");
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
        values.put(SqlConst.TB_THREAD_FINISHED, record.getFinished());
        return values;
    }

    private SubTaskRecord cursorToRecord(Cursor cursor){
        SubTaskRecord record = new SubTaskRecord();
        record.setId(cursor.getLong(0));
        record.setTaskID(cursor.getLong(1));
        record.setStart(cursor.getLong(2));
        record.setEnd(cursor.getLong(3));
        record.setFinished(cursor.getLong(4));
        return record;
    }
}
