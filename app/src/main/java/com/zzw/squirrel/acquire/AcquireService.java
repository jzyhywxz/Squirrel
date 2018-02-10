package com.zzw.squirrel.acquire;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.zzw.squirrel.R;
import com.zzw.squirrel.daemon.IDaemonInterface;
import com.zzw.squirrel.daemon.LocalService;
import com.zzw.squirrel.daemon.MediaPlayerHelper;
import com.zzw.squirrel.daemon.RemoteService;
import com.zzw.squirrel.util.AppHelper;
import com.zzw.squirrel.util.LimitedLog;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class AcquireService extends Service {
    private static final String SERVICE_NAME = AcquireService.class.getSimpleName();

    private ServiceConnection localConnection;

    public static final int NOTIFICATION_ID = 88;

    private StorageAgent<SensorAgent.SensorData> accStorageAgent;
    private StorageAgent<SensorAgent.SensorData> laccStorageAgent;
    private StorageAgent<SensorAgent.SensorData> gyroStorageAgent;

    private SensorAgent accSensorAgent;
    private SensorAgent laccSensorAgent;
    private SensorAgent gyroSensorAgent;

    private MediaPlayerHelper mediaPlayerHelper;

    private Timer timer;

    private boolean isStopped = true;

    @Override
    public void onCreate() {
        super.onCreate();
        doInitialize();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (isStopped) {
            doStart();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!isStopped) {
            doStop();
        }
        // restart self
        startService(new Intent(getApplicationContext(), AcquireService.class));
    }

    @Override
    public IBinder onBind(Intent intent) {
        LimitedLog.d(SERVICE_NAME + "#onBind");

        return new AcquireServiceBinder();
    }

    private void doInitialize() {
        LimitedLog.d(SERVICE_NAME + "#onCreate");

        localConnection = new LocalServiceConnection();

        // initial tasks
        accStorageAgent = new StorageAgent<>();
        laccStorageAgent = new StorageAgent<>();
        gyroStorageAgent = new StorageAgent<>();

        accSensorAgent = new SensorAgent(getApplicationContext()) {
            @Override
            public void onSensorChanged(SensorEvent event) {
                accStorageAgent.enqueue(new SensorData(event));
            }
        };
        laccSensorAgent = new SensorAgent(getApplicationContext()) {
            @Override
            public void onSensorChanged(SensorEvent event) {
                laccStorageAgent.enqueue(new SensorData(event));
            }
        };
        gyroSensorAgent = new SensorAgent(getApplicationContext()) {
            @Override
            public void onSensorChanged(SensorEvent event) {
                gyroStorageAgent.enqueue(new SensorData(event));
            }
        };

        mediaPlayerHelper = new MediaPlayerHelper(this);

        timer = new Timer(true);
    }

    private void doStart() {
        LimitedLog.d(SERVICE_NAME + "#onStartCommand");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Notification notification = new Notification.Builder(this).
                    setSmallIcon(R.mipmap.ic_launcher).
                    setContentTitle("Squirrel").
                    setContentText("data acquisition running...").
                    build();
            startForeground(NOTIFICATION_ID, notification);
        } else {
            startForeground(NOTIFICATION_ID, new Notification());
        }

        // start playing silent music
        mediaPlayerHelper.start();

        // bind to LocalService
        bindService(new Intent(AcquireService.this, LocalService.class), localConnection, Context.BIND_IMPORTANT);

        // start tasks
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (accStorageAgent.isLoopRunning()) {
                    accStorageAgent.recycle();
                }
                if (laccStorageAgent.isLoopRunning()) {
                    laccStorageAgent.recycle();
                }
                if (gyroStorageAgent.isLoopRunning()) {
                    gyroStorageAgent.recycle();
                }

                String dataFilePrefix = AppHelper.getDataFilePrefix();
                accStorageAgent.loop(dataFilePrefix + "-acc.txt");
                laccStorageAgent.loop(dataFilePrefix + "-lacc.txt");
                gyroStorageAgent.loop(dataFilePrefix + "-gyro.txt");
            }
        }, 0, 30 * 60 * 1000);

        // register sensors
        accSensorAgent.register(Sensor.TYPE_ACCELEROMETER);
        laccSensorAgent.register(Sensor.TYPE_LINEAR_ACCELERATION);
        gyroSensorAgent.register(Sensor.TYPE_GYROSCOPE);

        isStopped = false;
    }

    private void doStop() {
        LimitedLog.d(SERVICE_NAME + "#onDestroy");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.cancel(NOTIFICATION_ID);
        }
        stopForeground(true);

        // stop tasks
        accSensorAgent.unregister();
        laccSensorAgent.unregister();
        gyroSensorAgent.unregister();

        accStorageAgent.recycle();
        laccStorageAgent.recycle();
        gyroStorageAgent.recycle();

        mediaPlayerHelper.stop();

        timer.cancel();

        isStopped = true;
    }

    private class AcquireServiceBinder extends IDaemonInterface.Stub {
        @Override
        public String getServerName() throws RemoteException {
            return SERVICE_NAME;
        }

        @Override
        public void stopServer() throws RemoteException {
            if (!isStopped) {
                doStop();
            }
        }

        @Override
        public boolean isServerStopped() throws RemoteException {
            return isStopped;
        }
    }

    private class LocalServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LimitedLog.d(SERVICE_NAME + "#onServiceConnected");

            try {
                IDaemonInterface binder = IDaemonInterface.Stub.asInterface(service);
                Log.d(AppHelper.APP_TAG, "acquire connect to local: " + binder.getServerName());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LimitedLog.d(SERVICE_NAME + "#onServiceDisconnected");

            startService(new Intent(AcquireService.this, LocalService.class));
            bindService(new Intent(AcquireService.this, LocalService.class), localConnection, Context.BIND_IMPORTANT);
            startService(new Intent(AcquireService.this, RemoteService.class));
        }
    }
}
