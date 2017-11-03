package com.zsp.filedownloader.record;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by zsp on 2017/11/2.
 */

public class RecordManager {

    private static final String TAG = "RecordManager";

    private static Context sContext;
    private static DBHelper sDatabaseHelper;
    private static boolean sIsInitialized = false;

    public static synchronized void initialize(Context ctx) {
        if(sIsInitialized) {
            Log.d(TAG, "RecordManager already initialized.");
        } else {
            sContext = ctx;
            sDatabaseHelper = new DBHelper(sContext);
            openDatabase();
            sIsInitialized = true;
            Log.d(TAG, "RecordManager initialized successfully.");
        }
    }

    public static synchronized SQLiteDatabase openDatabase() {
        return sDatabaseHelper.getWritableDatabase();
    }

    private SubTaskRecordDAO subTaskRecord;
    private TaskRecordDAO taskRecord;

    public RecordManager(){
        taskRecord = new TaskRecordDAO();
        subTaskRecord = new SubTaskRecordDAO();
    }

    public SubTaskRecordDAO subTask() {
        return subTaskRecord;
    }

    public TaskRecordDAO task() {
        return taskRecord;
    }
}
