package com.zsp.filedownloader;

import android.provider.BaseColumns;

/**
 * Created by zsp on 2017/11/2.
 */

public class SqlConst {
    static final String TB_TASK = "task_info";
    static final String TB_TASK_URL = "download_url";
    static final String TB_TASK_DIR_PATH = "file_path";
    static final String TB_TASK_FINISHED = "finished_length";
    static final String TB_TASK_CONTENT = "content_length";
    static final String TB_TASK_FILE_NAME = "file_name";
    static final String TB_TASK_MIME_TYPE = "mime_type";
    static final String TB_TASK_ETAG = "e_tag";
    static final String TB_TASK_DISPOSITION = "disposition";
    static final String TB_TASK_STATE = "task_state";
    static final String TB_SUB_TASK = "sub_task";
    static final String TB_CREATE_AT = "create_at";

    static final String TB_THREAD = "sub_task_info";
    static final String TB_THREAD_START = "start";
    static final String TB_THREAD_END = "end";
    static final String TB_THREAD_FINISHED = "finished";
    static final String TB_TASK_ID = "task_id";

    static final String TB_TASK_SQL_CREATE = "CREATE TABLE " +
            SqlConst.TB_TASK + "(" +
            BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            SqlConst.TB_TASK_URL + " CHAR, " +
            SqlConst.TB_TASK_DIR_PATH + " CHAR, " +
            SqlConst.TB_TASK_FINISHED + " INTEGER, " +
            SqlConst.TB_TASK_CONTENT + " INTEGER , "+
            SqlConst.TB_TASK_STATE + " INTEGER , "+
            SqlConst.TB_TASK_FILE_NAME + " CHAR, " +
            SqlConst.TB_TASK_MIME_TYPE + " CHAR, " +
            SqlConst.TB_TASK_ETAG + " CHAR, " +
            SqlConst.TB_SUB_TASK + " CHAR, " +
            SqlConst.TB_CREATE_AT + " CHAR, " +
            SqlConst.TB_TASK_DISPOSITION + " CHAR)";

    static final String TB_THREAD_SQL_CREATE = "CREATE TABLE " +
            SqlConst.TB_THREAD + "(" +
            BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            SqlConst.TB_TASK_ID + " INTEGER, " +
            SqlConst.TB_THREAD_START + " INTEGER, " +
            SqlConst.TB_THREAD_END + " INTEGER, " +
            SqlConst.TB_THREAD_FINISHED + " INTEGER)";

    static final String TB_TASK_SQL_UPGRADE = "DROP TABLE IF EXISTS " +
            SqlConst.TB_TASK;
    static final String TB_THREAD_SQL_UPGRADE = "DROP TABLE IF EXISTS " +
            SqlConst.TB_THREAD;
}
