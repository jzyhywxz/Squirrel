package com.zzw.squirrel.daemon;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import com.zzw.squirrel.activity.MainActivity;
import com.zzw.squirrel.util.AppHelper;

import java.util.List;

public class OnePixelActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.setGravity(Gravity.LEFT | Gravity.TOP);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.x = 0;
        lp.y = 0;
        lp.width = 1;
        lp.height = 1;
        window.setAttributes(lp);

        OnePixelHelper.getInstance(this).referenceActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        boolean isAlive = false;
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcessInfos = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessInfos) {
            if (appProcessInfo.processName.equals(AppHelper.PACKAGE_NAME)) {
                isAlive = true;
                break;
            }
        }

        if (!isAlive) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }
}
