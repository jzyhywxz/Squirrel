package com.zzw.squirrel.daemon;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import com.zzw.squirrel.acquire.AcquireService;
import com.zzw.squirrel.activity.MainActivity;
import com.zzw.squirrel.util.AppHelper;
import com.zzw.squirrel.util.LimitedLog;

import java.util.List;

public class AwakenReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        LimitedLog.d("system receiver: " + intent.getAction());

        boolean isAlive = false;
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcessInfos = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessInfos) {
            if (appProcessInfo.processName.equals(AppHelper.PACKAGE_NAME)) {
                isAlive = true;
                break;
            }
        }

        if (!isAlive) {
            startServices(context);
        }
    }

    private void onNetworkChanged(Context context, Intent intent) {
        String action = intent.getAction();

        // wifi state changed
        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,0);
            switch (wifiState) {
                case WifiManager.WIFI_STATE_DISABLED:
                    Toast.makeText(context, "wifi closed", Toast.LENGTH_SHORT).show();
                    break;
                case WifiManager.WIFI_STATE_ENABLED:
                    Toast.makeText(context, "wifi opened", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }

        // network state changed
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            Parcelable extra = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (null != extra) {
                NetworkInfo networkInfo = (NetworkInfo) extra;
                NetworkInfo.State state = networkInfo.getState();
                boolean isConnected = (state == NetworkInfo.State.CONNECTED);
                if (isConnected) {
                    Toast.makeText(context, "network is available", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "network is unavailable", Toast.LENGTH_SHORT).show();
                }
            }
        }

        // 监听网络连接状态，包括wifi和移动网络数据的打开和关闭。
        // 由于上面已经对wifi进行了处理，因此这里只对移动网络进行处理，其中：
        // 移动网络 -> ConnectivityManager.TYPE_MOBILE
        // Wifi    -> ConnectivityManager.TYPE_WIFI
        // other   -> ConnectivityManager.EXTRA_NETWORK_INFO
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo gprs = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (gprs.isConnected()) {
                Toast.makeText(context, "mobile network opened", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "mobile network closed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startServices(Context context) {
        context.startService(new Intent(context, LocalService.class));
        context.startService(new Intent(context, RemoteService.class));
        context.startService(new Intent(context, AcquireService.class));
    }
}
