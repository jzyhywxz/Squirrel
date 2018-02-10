package com.zzw.squirrel.activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.zzw.squirrel.R;
import com.zzw.squirrel.acquire.AcquireService;
import com.zzw.squirrel.daemon.IDaemonInterface;
import com.zzw.squirrel.daemon.LocalService;
import com.zzw.squirrel.daemon.OnePixelHelper;
import com.zzw.squirrel.daemon.RemoteService;
import com.zzw.squirrel.daemon.ScreenHelper;
import com.zzw.squirrel.store.ZipHelper;
import com.zzw.squirrel.util.AppHelper;
import com.zzw.squirrel.util.LimitedLog;

import java.io.IOException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements ZipHelper.OnZipProgressListener {
    private static final String ACTIVITY_NAME = MainActivity.class.getSimpleName();

    private Button startBt;
    private Button stopBt;
    private Button packBt;
    private TextView infoTx;

    private IDaemonInterface binder;
    private ServiceConnection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LimitedLog.d(ACTIVITY_NAME + "#onCreate");

        initView();
        initEvent();

        if (savedInstanceState != null) {
            boolean startState = savedInstanceState.getBoolean("START_STATE", true);
            boolean stopState = savedInstanceState.getBoolean("STOP_STATE", false);
            boolean packState = savedInstanceState.getBoolean("PACK_STATE", true);
            setButtonState(startState, stopState, packState);
            infoTx.setText(savedInstanceState.getString("INFO_TEXT", ""));
        } else {
            setButtonState(true, false, true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkPermissions()) {
                applyPermissions();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LimitedLog.d(ACTIVITY_NAME + "#onDestroy");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("START_STATE", startBt.isClickable());
        outState.putBoolean("STOP_STATE", stopBt.isClickable());
        outState.putBoolean("PACK_STATE", packBt.isClickable());
        outState.putString("INFO_TEXT", infoTx.getText().toString());
    }

    private void initView() {
        startBt = findViewById(R.id.start_bt);
        stopBt = findViewById(R.id.stop_bt);
        packBt = findViewById(R.id.pack_bt);
        infoTx = findViewById(R.id.info_tx);
    }

    private void initEvent() {
        startBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LimitedLog.d("startBUTTON#onClick");

                startService(new Intent(MainActivity.this, LocalService.class));
                startService(new Intent(MainActivity.this, RemoteService.class));
                startService(new Intent(MainActivity.this, AcquireService.class));
                bindService(new Intent(MainActivity.this, AcquireService.class),
                        new AcquireServiceConnection(METHOD_isServerStopped),
                        Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT);

                infoTx.setText("数据采集正在进行");
                setButtonState(false, true, true);
            }
        });
        stopBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LimitedLog.d("stopBUTTON#onClick");

                try {
                    if (binder != null) {
                        binder.stopServer();
                        infoTx.setText("数据采集已经停止");
                        setButtonState(true, false, true);
                    } else {
                        bindService(new Intent(MainActivity.this, AcquireService.class),
                                new AcquireServiceConnection(METHOD_stopServer),
                                Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
        packBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LimitedLog.d("packBUTTON#onClick");

                try {
                    if (binder != null) {
                        binder.stopServer();
                    } else {
                        bindService(new Intent(MainActivity.this, AcquireService.class),
                                new AcquireServiceConnection(METHOD_stopServer),
                                Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                setButtonState(false, false, false);

                new AlertDialog.Builder(MainActivity.this).
                        setTitle("压缩打包").
                        setMessage("是否在压缩完成后删除原数据？").
                        setCancelable(true).
                        setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                setButtonState(true, false, true);
                            }
                        }).
                        setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new Thread() {
                                    @Override
                                    public void run() {
                                        ZipHelper.compress(AppHelper.getDataDir(),
                                                AppHelper.getPackFile(),
                                                MainActivity.this);
                                        AppHelper.deleteDataFiles();
                                        MainActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                setButtonState(true, false, true);
                                            }
                                        });
                                    }
                                }.start();
                            }
                        }).
                        setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new Thread() {
                                    @Override
                                    public void run() {
                                        ZipHelper.compress(AppHelper.getDataDir(),
                                                AppHelper.getPackFile(),
                                                MainActivity.this);
                                        MainActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                setButtonState(true, false, true);
                                            }
                                        });
                                    }
                                }.start();
                            }
                        }).show();
            }
        });
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
                    if (!isGranted) {
                        finish();
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    private void setButtonState(boolean start, boolean stop, boolean pack) {
        startBt.setClickable(start);
        startBt.setEnabled(start);
        stopBt.setClickable(stop);
        stopBt.setEnabled(stop);
        packBt.setClickable(pack);
        packBt.setEnabled(pack);
    }

    // ==================== OnZipProgressListener ====================
    private int packTotal = 0;
    private int packCurrent = 0;

    @Override
    public void onStarting(final int total) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                packTotal = total;
                packCurrent = 0;
                infoTx.setText("开始压缩");
            }
        });
    }

    @Override
    public void onProgressUpdate(final String filename) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                packCurrent++;
                infoTx.setText(String.format("正在压缩 [%3d / %3d] %s", packCurrent, packTotal, filename));
            }
        });
    }

    @Override
    public void onFinished(final String destPath) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                infoTx.setText("压缩完成 保存路径 " + destPath);
            }
        });
    }

    @Override
    public void onCatchException(final IOException e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                infoTx.setText("压缩出错 " + e.getMessage());
            }
        });
    }
    // ==================== OnZipProgressListener ====================

    private static final int METHOD_isServerStopped = 0;
    private static final int METHOD_stopServer = 1;

    private class AcquireServiceConnection implements ServiceConnection {
        private int methodId;

        public AcquireServiceConnection(int methodId) {
            this.methodId = methodId;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LimitedLog.d(ACTIVITY_NAME + ".AcquireServiceConnection#onServiceConnected");
            binder = IDaemonInterface.Stub.asInterface(service);
            try {
                switch (methodId) {
                    case METHOD_isServerStopped: {
                        break;
                    }
                    case METHOD_stopServer: {
                        binder.stopServer();
                        setButtonState(true, false, true);
                        break;
                    }
                    default: {
                        break;
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LimitedLog.d(ACTIVITY_NAME + ".AcquireServiceConnection#onServiceDisconnected");
        }
    }
}
