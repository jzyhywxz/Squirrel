package com.zzw.squirrel.store;

import com.zzw.squirrel.util.LimitedLog;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by zzw on 2018/2/1.
 */

public class WriterHelper {
    private static final String CLASS_NAME = WriterHelper.class.getSimpleName();
    private PrintWriter writer = null;

    public void open(File file) {
        if (file == null) {
            return;
        }

        if (writer != null) {
            writer.flush();
            writer.close();
            writer = null;
        }

        File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }

        try {
            writer = new PrintWriter(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void open(String filename) {
        if (filename != null) {
            open(new File(filename));
        }
    }

    public void printf(String format, Object[] args) {
        if (writer != null) {
            writer.printf(format, args);
        } else {
            LimitedLog.d(CLASS_NAME + "#printf: null");
        }
    }

    public void print(Object obj) {
        if (writer != null) {
            writer.print(obj);
        } else {
            LimitedLog.d(CLASS_NAME + "#print: null");
        }
    }

    public void println(Object obj) {
        if (writer != null) {
            writer.println(obj);
        } else {
            LimitedLog.d(CLASS_NAME + "#println(Object): null");
        }
    }

    public void println() {
        if (writer != null) {
            writer.println();
        } else {
            LimitedLog.d(CLASS_NAME + "#println(): null");
        }
    }

    public void close() {
        if (writer != null) {
            writer.flush();
            writer.close();
            writer = null;
        }
    }
}
