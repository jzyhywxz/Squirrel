package com.zzw.squirrel.util;

import android.util.Log;

/**
 * Created by zzw on 2018/2/3.
 */

public class LimitedLog {
    private static final int VERBOSE_LEVEL = 1;
    private static final int DEBUG_LEVEL = 2;
    private static final int INFO_LEVEL = 3;
    private static final int WARN_LEVEL = 4;
    private static final int ERROR_LEVEL = 5;
    private static final int ASSERT_LEVEL = 6;

    private static final int LIMITED_LEVEL = ASSERT_LEVEL;

    public static void v(Object msg) {
        if (VERBOSE_LEVEL >= LIMITED_LEVEL && msg != null) {
            Log.v(AppHelper.APP_TAG, msg.toString());
        }
    }

    public static void d(Object msg) {
        if (DEBUG_LEVEL >= LIMITED_LEVEL && msg != null) {
            Log.d(AppHelper.APP_TAG, msg.toString());
        }
    }

    public static void i(Object msg) {
        if (INFO_LEVEL >= LIMITED_LEVEL && msg != null) {
            Log.i(AppHelper.APP_TAG, msg.toString());
        }
    }

    public static void w(Object msg) {
        if (WARN_LEVEL >= LIMITED_LEVEL && msg != null) {
            Log.w(AppHelper.APP_TAG, msg.toString());
        }
    }

    public static void e(Object msg) {
        if (ERROR_LEVEL >= LIMITED_LEVEL && msg != null) {
            Log.e(AppHelper.APP_TAG, msg.toString());
        }
    }
}
