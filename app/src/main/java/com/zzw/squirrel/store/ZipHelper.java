package com.zzw.squirrel.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by zzw on 2018/2/9.
 */

public class ZipHelper {
    public static void compress(String srcPath, String destPath, OnZipProgressListener listener) {
        if (srcPath == null || destPath == null) {
            return;
        }

        File srcFile = new File(srcPath);
        if (!srcFile.exists()) {
            return;
        }

        File destFile = new File(destPath);
        if (destFile.exists()) {
            return;
        }

        File destParentFile = destFile.getParentFile();
        if (!destParentFile.exists()) {
            destParentFile.mkdirs();
        }

        int count = getFileCount(srcFile);

        ZipOutputStream os = null;
        try {
            os = new ZipOutputStream(new FileOutputStream(destFile));

            if (listener != null) {
                listener.onStarting(count);
            }

            doCompress("", srcFile, os, listener);

            if (listener != null) {
                listener.onFinished(destPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (listener != null) {
                listener.onCatchException(e);
            }
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (listener != null) {
                    listener.onCatchException(e);
                }
            }
        }
    }

    private static void doCompress(String parentPath, File srcFile, ZipOutputStream os, OnZipProgressListener listener) {
        try {
            if (srcFile.isDirectory()) {
                parentPath = parentPath + File.separator + srcFile.getName();
                File[] srcFiles = srcFile.listFiles();
                if (srcFiles == null || srcFiles.length <= 0) {
                    os.putNextEntry(new ZipEntry(parentPath));
                    os.closeEntry();
                } else {
                    for (int i = 0; i < srcFiles.length; i++) {
                        doCompress(parentPath, srcFiles[i], os, listener);
                    }
                }
            } else {
                FileInputStream is = new FileInputStream(srcFile);
                byte[] buffer = new byte[1024 * 1024];
                os.putNextEntry(new ZipEntry(parentPath + File.separator + srcFile.getName()));
                int len;
                while ((len = is.read(buffer)) > 0) {
                    os.write(buffer, 0, len);
                    os.flush();
                }
                os.closeEntry();
                is.close();

                if (listener != null) {
                    listener.onProgressUpdate(parentPath + File.separator + srcFile.getName());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (listener != null) {
                listener.onCatchException(e);
            }
        }
    }

    private static int getFileCount(File srcFile) {
        if (srcFile.isDirectory()) {
            File[] files = srcFile.listFiles();
            if (files == null || files.length <= 0) {
                return 0;
            } else {
                int count = 0;
                for (File file : files) {
                    count += getFileCount(file);
                }
                return count;
            }
        } else {
            return 1;
        }
    }

    public interface OnZipProgressListener {
        void onStarting(int total);
        void onProgressUpdate(String filename);
        void onFinished(String destPath);
        void onCatchException(IOException e);
    }
}
