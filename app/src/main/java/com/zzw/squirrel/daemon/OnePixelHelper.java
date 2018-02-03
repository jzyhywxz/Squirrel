package com.zzw.squirrel.daemon;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.zzw.squirrel.util.AppHelper;
import com.zzw.squirrel.util.LimitedLog;

import java.lang.ref.WeakReference;

/**
 * Created by zzw on 2018/2/1.
 */

public class OnePixelHelper {
    private Context context;
    private WeakReference<Activity> activityReference;

    private static OnePixelHelper mInstance;

    private OnePixelHelper(Context context){
        this.context = context;
    }

    // 单例模式
    public static OnePixelHelper getInstance(Context context){
        if (mInstance == null) {
            synchronized (OnePixelHelper.class) {
                if (mInstance == null) {
                    mInstance = new OnePixelHelper(context);
                }
            }
        }
        return mInstance;
    }

    public void referenceActivity(Activity activity) {
        LimitedLog.d("reference one pixel activity");

        activityReference = new WeakReference<Activity>(activity);
    }

    public void startActivity() {
        LimitedLog.d("start one pixel activity");

        Intent intent = new Intent(context, OnePixelActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void finishActivity() {
        LimitedLog.d("finish one pixel activity");

        if(activityReference != null) {
            Activity activity = activityReference.get();
            if(activity != null) {
                activity.finish();
            }
        }
    }
}
