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
    private int count;
    private int index;
    private OnZipProgressListener listener;

    public void setOnZipProgressListener(OnZipProgressListener listener) {
        this.listener = listener;
    }

    public void compress(String srcPath, String destPath) {
        count = 0;
        index = 0;

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

        count = getFileCount(srcFile);
        if (count <= 0) {
            return;
        }

        File destParentFile = destFile.getParentFile();
        if (!destParentFile.exists()) {
            destParentFile.mkdirs();
        }

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

    private void doCompress(String parentPath, File srcFile, ZipOutputStream os, OnZipProgressListener listener) {
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

                index++;
                if (listener != null) {
                    listener.onProgressUpdate(count, index, parentPath + File.separator + srcFile.getName());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (listener != null) {
                listener.onCatchException(e);
            }
        }
    }

    private int getFileCount(File srcFile) {
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
        void onStarting(final int count);
        void onProgressUpdate(final int count, final int index, final String filename);
        void onFinished(final String destPath);
        void onCatchException(final IOException e);
    }
}
