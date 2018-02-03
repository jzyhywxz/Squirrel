package com.zzw.squirrel.daemon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.zzw.squirrel.util.AppHelper;
import com.zzw.squirrel.util.LimitedLog;

/**
 * Created by zzw on 2018/2/1.
 */

public class ScreenHelper {
    private Context context;
    private ScreenBroadcastReceiver screenBroadcastReceiver;
    private ScreenStateListener screenStateListener;

    public ScreenHelper(Context context) {
        this.context = context;
        this.screenBroadcastReceiver = new ScreenBroadcastReceiver();
    }

    public void setScreenStateListener(ScreenStateListener listener) {
        screenStateListener = listener;
    }

    public void register() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
        context.registerReceiver(screenBroadcastReceiver, intentFilter);
    }

    public void unregister() {
        context.unregisterReceiver(screenBroadcastReceiver);
    }

    public  class ScreenBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            LimitedLog.d("screen receiver: " + action);

            if(screenStateListener != null){
                if(Intent.ACTION_SCREEN_ON.equals(action)) {
                    screenStateListener.onScreenOn();
                } else if(Intent.ACTION_SCREEN_OFF.equals(action)) {
                    screenStateListener.onScreenOff();
                } else if(Intent.ACTION_USER_PRESENT.equals(action)) {
                    screenStateListener.onUserPresent();
                }
            }
        }
    }

    public interface ScreenStateListener {
        void onScreenOn();
        void onScreenOff();
        void onUserPresent();
    }
}
