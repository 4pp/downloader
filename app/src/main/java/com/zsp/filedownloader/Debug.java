package com.zsp.filedownloader;

import android.util.Log;

/**
 * Created by zsp on 2017/11/3.
 */

public class Debug
{
    private static final String TAG = "DL";

    public static boolean enable = true;

    public static void log(String s){
        if (enable){
            Log.d(TAG,s);
        }
    }
}
