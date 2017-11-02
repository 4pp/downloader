package com.zsp.filedownloader;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.zsp.filedownloader.SqlConst.TB_TASK_SQL_CREATE;
import static com.zsp.filedownloader.SqlConst.TB_THREAD_SQL_CREATE;

import static com.zsp.filedownloader.SqlConst.TB_TASK_SQL_UPGRADE;
import static com.zsp.filedownloader.SqlConst.TB_THREAD_SQL_UPGRADE;

/**
 * Created by zsp on 2017/11/2.
 */

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "download.db";
    private static final int DB_VERSION = 1;

    public DBHelper(Context context){
        super(context,DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TB_TASK_SQL_CREATE);
        db.execSQL(TB_THREAD_SQL_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(TB_TASK_SQL_UPGRADE);
        db.execSQL(TB_THREAD_SQL_UPGRADE);
        onCreate(db);
    }
}
