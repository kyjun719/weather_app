package com.jun.weather.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import static com.jun.weather.BaseApplication.isDebug;

/**
 * 디버그 빌드일때만 로그를 찍기위한 클래스
 */
public class CLogger {
    private static boolean isDebug = false;
    public static void init() {
        isDebug = isDebug();
    }
    /**
     * verbose 로그 출력
     * @param msg msg
     */
    public static void v(Object msg) {
        if(isDebug) {
            Log.v(getTag(), msg.toString());
        }
    }

    /**
     * debug 로그 출력
     * @param msg msg
     */
    public static void d(Object msg) {
        if(isDebug) {
            Log.d(getTag(), msg.toString());
        }
    }

    /**
     * info 로그 출력
     * @param msg msg
     */
    public static void i(Object msg) {
        if(isDebug) {
            Log.i(getTag(), msg.toString());
        }
    }

    /**
     * error 로그 출력
     * @param msg msg
     */
    public static void e(Object msg) {
        if(isDebug) {
            Log.e(getTag(), msg.toString());
        }
    }

    private static String getTag() {
        StackTraceElement[] ste = new Throwable().getStackTrace();
        //순서
        //com.jun.weather.util.CLogger>>getTag
        //com.jun.weather.util.CLogger>>d
        //com.jun.weather.ui.MainActivity>>onResume
        return ste[2].getClassName();
    }
}