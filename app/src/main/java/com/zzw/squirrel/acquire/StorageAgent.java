package com.zzw.squirrel.acquire;

import com.zzw.squirrel.store.WriterHelper;
import com.zzw.squirrel.util.LimitedLog;

import java.util.LinkedList;
import java.util.concurrent.Semaphore;

/**
 * Created by zzw on 2018/2/1.
 */

public class StorageAgent<T> {
    private static final String CLASS_NAME = StorageAgent.class.getSimpleName();
    private LinkedList<T> queue = new LinkedList<>();
    private WriterHelper writer = new WriterHelper();

    private Semaphore semaphore;
    private boolean loopRunning = false;
    private boolean queueOpened = false;

    private Thread thread;

    public synchronized boolean loop(String filename) {
        if (isLoopRunning()) {
            LimitedLog.d(CLASS_NAME + "#loop: return false");

            return false;
        }

        queue.clear();
        writer.open(filename);

        semaphore = new Semaphore(0);
        setLoopRunning(true);
        setQueueOpened(true);

        thread = new Thread() {
            @Override
            public void run() {
                LimitedLog.d(CLASS_NAME + ".Thread#run loop start");

                try {
                    while (isLoopRunning() && (!this.isInterrupted())) {
                        semaphore.acquire();
                        T data = dequeue();
                        if (data != null) {
                            writer.println(data.toString());
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                LimitedLog.d(CLASS_NAME + ".Thread#run loop stop");
            }
        };
        thread.start();

        LimitedLog.d(CLASS_NAME + "#loop: return true");

        return true;
    }

    public synchronized boolean recycle() {
        if (!isLoopRunning()) {
            LimitedLog.d(CLASS_NAME + "#recycle: return false");

            return false;
        }

        setLoopRunning(false);
        setQueueOpened(false);
        semaphore.release();
        thread.interrupt();

        T data;
        while ((data = dequeue()) != null) {
            writer.println(data.toString());
        }
        writer.close();

        thread = null;
        semaphore = null;

        LimitedLog.d(CLASS_NAME + "#recycle: return true");

        return true;
    }

    private synchronized void setLoopRunning(boolean running) {
        this.loopRunning = running;
    }

    public synchronized boolean isLoopRunning() {
        return loopRunning;
    }

    private synchronized void setQueueOpened(boolean opened) {
        this.queueOpened = opened;
    }

    private synchronized boolean isQueueOpened() {
        return queueOpened;
    }

    public synchronized void enqueue(T data) {
        if (isQueueOpened() && (data != null)) {
            queue.addLast(data);
            semaphore.release();
        }
    }

    private synchronized T dequeue() {
        return queue.isEmpty() ? null : queue.removeFirst();
    }
}
