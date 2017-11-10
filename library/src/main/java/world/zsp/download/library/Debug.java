package world.zsp.download.library;

import android.util.Log;

/**
 * Created by zsp on 2017/11/3.
 */

public class Debug
{
    private static final String TAG = "DownLoader";

    public static boolean enable = true;

    public static void log(String s){
        if (enable){
            Log.d(TAG,s);
        }
    }
}
