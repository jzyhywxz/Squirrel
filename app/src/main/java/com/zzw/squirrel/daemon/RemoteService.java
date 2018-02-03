package com.zzw.squirrel.daemon;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.zzw.squirrel.acquire.AcquireService;
import com.zzw.squirrel.util.AppHelper;
import com.zzw.squirrel.util.LimitedLog;

public class RemoteService extends Service {
    private static final String SERVICE_NAME = RemoteService.class.getSimpleName();

    private IBinder remoteBinder;
    private ServiceConnection localConnection;

    // if you would like to keep some services alive,
    // please declare corresponding service connections here
    private ServiceConnection acquireConnection;

    @Override
    public void onCreate() {
        super.onCreate();

        remoteBinder = new RemoteServiceBinder();
        localConnection = new LocalServiceConnection();

        // please assign values to corresponding service connections here
        acquireConnection = new AcquireServiceConnection();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        bindService(new Intent(RemoteService.this, LocalService.class), localConnection, Context.BIND_IMPORTANT);

        // please bind corresponding service connections here
        bindService(new Intent(RemoteService.this, AcquireService.class), acquireConnection, Context.BIND_IMPORTANT);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return remoteBinder;
    }

    private class RemoteServiceBinder extends IDaemonInterface.Stub {
        @Override
        public String getServerName() throws RemoteException {
            return SERVICE_NAME;
        }
    }

    private class LocalServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                IDaemonInterface binder = IDaemonInterface.Stub.asInterface(service);
                LimitedLog.d("remote connect to local: " + binder.getServerName());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            startService(new Intent(RemoteService.this, LocalService.class));
            bindService(new Intent(RemoteService.this, LocalService.class), localConnection, Context.BIND_IMPORTANT);
        }
    }

    private class AcquireServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                IDaemonInterface binder = IDaemonInterface.Stub.asInterface(service);
                LimitedLog.d("local connect to acquire: " + binder.getServerName());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            startService(new Intent(RemoteService.this, AcquireService.class));
            bindService(new Intent(RemoteService.this, AcquireService.class), acquireConnection, Context.BIND_IMPORTANT);
        }
    }
}
