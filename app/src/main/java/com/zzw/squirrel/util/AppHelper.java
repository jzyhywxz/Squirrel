package com.zzw.squirrel.util;

import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zzw on 2018/1/31.
 */

public class AppHelper {
    public static final String APP_TAG = "SQUIRREL";
    public static final String PACKAGE_NAME = "com.zzw.squirrel";

    public static final SimpleDateFormat VISUAL_SDF = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    public static final SimpleDateFormat FORMAT_SDF = new SimpleDateFormat("yyyyMMdd_HHmmss");

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
    public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HHmmss");

    public static String getExternalRoot() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            return null;
        }
    }

    public static String getAppRoot() {
        String externalRoot = getExternalRoot();
        return (externalRoot == null) ? null : (externalRoot + File.separator + "squirrel");
    }

    public static String getDataDir() {
        String appRoot = getAppRoot();
        return (appRoot == null) ? null : (appRoot + File.separator + "data");
    }

    public static String getDataFilePrefix() {
        String dataDir = getDataDir();
        if (dataDir != null) {
            long timestamp = System.currentTimeMillis();
            String date = DATE_FORMAT.format(new Date(timestamp));
            String time = TIME_FORMAT.format(new Date(timestamp));
            return dataDir + File.separator + date + File.separator + time;
        } else {
            return null;
        }
    }
}
