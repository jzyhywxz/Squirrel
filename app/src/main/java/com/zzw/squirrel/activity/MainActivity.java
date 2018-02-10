package com.zzw.squirrel.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
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

public class MainActivity extends AppCompatActivity implements
        ServiceConnection, ZipHelper.OnZipProgressListener {
    private static final String ACTIVITY_NAME = MainActivity.class.getSimpleName();

    private Button startBt;
    private Button stopBt;
    private Button packBt;
    private TextView infoTx;

    private Handler acquireHandler;
    private IDaemonInterface acquireBinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LimitedLog.d(ACTIVITY_NAME + "#onCreate");

        initView();
        initEvent();

        acquireHandler = new AcquireHandle(MainActivity.this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkPermissions()) {
                applyPermissions();
            } else {
                enter();
            }
        } else {
            enter();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LimitedLog.d(ACTIVITY_NAME + "#onResume");

        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) && checkPermissions()) {
            activeBinder();
        } else {
            activeBinder();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LimitedLog.d(ACTIVITY_NAME + "#onPause");

        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) && checkPermissions()) {
            unbindService(MainActivity.this);
        } else {
            unbindService(MainActivity.this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LimitedLog.d(ACTIVITY_NAME + "#onDestroy");
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

                try {
                    acquireBinder.startServer();
                } catch (RemoteException e) {
                    LimitedLog.e(e.getMessage());
                }

                setButtonState(false, true, true);
                infoTx.setText("正在采集数据");
            }
        });
        stopBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LimitedLog.d("stopBUTTON#onClick");

                try {
                    acquireBinder.stopServer();
                } catch (RemoteException e) {
                    LimitedLog.e(e.getMessage());
                }

                setButtonState(true, false, true);
                infoTx.setText("未在采集数据");
            }
        });
        packBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LimitedLog.d("packBUTTON#onClick");

                try {
                    acquireBinder.stopServer();
                } catch (RemoteException e) {
                    LimitedLog.e(e.getMessage());
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
                                        ZipHelper zipHelper = new ZipHelper();
                                        zipHelper.setOnZipProgressListener(MainActivity.this);
                                        zipHelper.compress(AppHelper.getDataDir(), AppHelper.getPackFile());

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
                                        ZipHelper zipHelper = new ZipHelper();
                                        zipHelper.setOnZipProgressListener(MainActivity.this);
                                        zipHelper.compress(AppHelper.getDataDir(), AppHelper.getPackFile());

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
                    } else {
                        enter();
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    private void enter() {
//        startService(new Intent(MainActivity.this, LocalService.class));
//        startService(new Intent(MainActivity.this, RemoteService.class));
//        startService(new Intent(MainActivity.this, AcquireService.class));
    }

    private void activeBinder() {
        if (acquireBinder == null) {
            // enable acquire binder
            acquireHandler.sendEmptyMessage(AcquireHandle.SHOW_WAIT_DIALOG);

            startService(new Intent(MainActivity.this, LocalService.class));
            startService(new Intent(MainActivity.this, RemoteService.class));
            startService(new Intent(MainActivity.this, AcquireService.class));

            bindService(new Intent(MainActivity.this, AcquireService.class),
                    MainActivity.this,
                    Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT);
        } else {
            try {
                acquireBinder.getServerName();
            } catch (RemoteException e) {
                // enable acquire binder
                acquireHandler.sendEmptyMessage(AcquireHandle.SHOW_WAIT_DIALOG);

                startService(new Intent(MainActivity.this, LocalService.class));
                startService(new Intent(MainActivity.this, RemoteService.class));
                startService(new Intent(MainActivity.this, AcquireService.class));

                bindService(new Intent(MainActivity.this, AcquireService.class),
                        MainActivity.this,
                        Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT);
            }
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

    // ==================== ServiceConnection ====================
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        LimitedLog.d(ACTIVITY_NAME + "#onServiceConnected");
        acquireBinder = IDaemonInterface.Stub.asInterface(service);

        try {
            final boolean isRunning = acquireBinder.isServerRunning();
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setButtonState(!isRunning, isRunning, true);
                    infoTx.setText((isRunning) ? "正在采集数据" : "未在采集数据");
                }
            });
        } catch (RemoteException e) {
            LimitedLog.e(e.getMessage());
        }

        if (acquireHandler != null) {
            acquireHandler.sendEmptyMessage(AcquireHandle.HIDE_WAIT_DIALOG);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        LimitedLog.d(ACTIVITY_NAME + "#onServiceDisconnected");
        acquireBinder = null;
    }
    // ==================== ServiceConnection ====================

    // ==================== OnZipProgressListener ====================
    @Override
    public void onStarting(final int count) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                infoTx.setText("开始压缩");
            }
        });
    }

    @Override
    public void onProgressUpdate(final int count, final int index, final String filename) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                infoTx.setText(String.format("正在压缩 [%3d / %3d] %s", index, count, filename));
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

    // ==================== Custom Handle ====================
    private static class AcquireHandle extends Handler {
        public static final int SHOW_WAIT_DIALOG = 0;
        public static final int HIDE_WAIT_DIALOG = 1;
        private ProgressDialog dialog;

        public AcquireHandle(Context context) {
            dialog = new ProgressDialog(context);
            dialog.setTitle("正在同步");
            dialog.setMessage("同步中，请稍候...");
            dialog.setCancelable(false);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_WAIT_DIALOG: {
                    if (!dialog.isShowing()) {
                        dialog.show();
                    }
                    break;
                }
                case HIDE_WAIT_DIALOG: {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    break;
                }
                default: {
                    super.handleMessage(msg);
                    break;
                }
            }
        }
    }
    // ==================== Custom Handle ====================
}
