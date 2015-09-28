package com.whl.test.webview.browser;

import android.util.Log;

import com.whl.test.webview.BuildConfig;


/**
 * 通过
 * Created by 1 on 2015/9/11.
 */
public class WebConsoleLog {

    public static void VERBOSE(String tag, String msg) {
        if (BuildConfig.DEBUG)
            Log.println(Log.VERBOSE, tag, msg);
    }

    public static void INFO(String tag, String msg) {
        if (BuildConfig.DEBUG)
            Log.println(Log.INFO, tag, msg);
    }

    public static void DEBUG(String tag, String msg) {
        if (BuildConfig.DEBUG)
            Log.println(Log.ERROR, tag, msg);
    }

    public static void WARN(String tag, String msg) {
        if (BuildConfig.DEBUG)
            Log.println(Log.WARN, tag, msg);
    }

    public static void ERROR(String tag, String msg) {
        if (BuildConfig.DEBUG)
            Log.println(Log.ERROR, tag, msg);
    }
}
