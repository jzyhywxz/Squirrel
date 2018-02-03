package com.zzw.squirrel.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.zzw.squirrel.R;
import com.zzw.squirrel.acquire.AcquireService;
import com.zzw.squirrel.daemon.LocalService;
import com.zzw.squirrel.daemon.OnePixelHelper;
import com.zzw.squirrel.daemon.RemoteService;
import com.zzw.squirrel.daemon.ScreenHelper;
import com.zzw.squirrel.util.AppHelper;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements ScreenHelper.ScreenStateListener {
    private ScreenHelper screenHelper;

    private TextView infoText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkPermissions()) {
                start();
            } else {
                applyPermissions();
            }
        } else {
            start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stop();
    }

    private boolean checkPermissions() {
        boolean isGranted = true;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.
                WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            isGranted = false;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.
                READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            isGranted = false;
        }
        return isGranted;
    }

    private void applyPermissions() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_PHONE_STATE
                }, 1);
    }

    private void start() {
        screenHelper = new ScreenHelper(this);
        screenHelper.setScreenStateListener(this);
        screenHelper.register();

        startService(new Intent(MainActivity.this, LocalService.class));
        startService(new Intent(MainActivity.this, RemoteService.class));
        startService(new Intent(MainActivity.this, AcquireService.class));

        TextView infoText = findViewById(R.id.info_text);
        infoText.setText("数据采集开始");
    }

    private void stop() {
        screenHelper.unregister();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        Log.d(AppHelper.APP_TAG, "requestCode: " + requestCode + ", permissions: " + Arrays.toString(permissions) + ", grantResults: " + Arrays.toString(grantResults));
        switch (requestCode) {
            case 1:
                if (grantResults != null && grantResults.length > 0) {
                    boolean isGranted = true;
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            isGranted = false;
                            break;
                        }
                    }
                    if (isGranted) {
                        start();
                    } else {
                        finish();
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    @Override
    public void onScreenOn() {
        OnePixelHelper.getInstance(this).finishActivity();
    }

    @Override
    public void onScreenOff() {
        OnePixelHelper.getInstance(this).startActivity();
    }

    @Override
    public void onUserPresent() {}
}
