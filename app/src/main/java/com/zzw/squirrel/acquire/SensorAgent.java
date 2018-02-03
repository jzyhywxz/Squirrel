package com.zzw.squirrel.acquire;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.zzw.squirrel.util.LimitedLog;

/**
 * Created by zzw on 2018/2/1.
 */

public abstract class SensorAgent implements SensorEventListener {
    private static final String CLASS_NAME = SensorAgent.class.getSimpleName();
    private Context context;
    private boolean isRegistered = false;

    public SensorAgent(Context context) {
        this.context = context;
    }

    public synchronized boolean register(int type) {
        if (!isRegistered) {
            LimitedLog.d(CLASS_NAME + "#register: return true");

            SensorManager manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            Sensor sensor = manager.getDefaultSensor(type);
            manager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
            isRegistered = true;
            return true;
        } else {
            LimitedLog.d(CLASS_NAME + "#register: return false");

            return false;
        }
    }

    public synchronized boolean unregister() {
        if (isRegistered) {
            LimitedLog.d(CLASS_NAME + "#unregister: return true");

            SensorManager manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            manager.unregisterListener(this);
            isRegistered = false;
            return true;
        } else {
            LimitedLog.d(CLASS_NAME + "#unregister: return false");

            return false;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    public static class SensorData {
        public final long timestamp;
        public final float x;
        public final float y;
        public final float z;

        public SensorData(long timestamp, float x, float y, float z) {
            this.timestamp = timestamp;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public SensorData(SensorEvent event) {
            this.timestamp = event.timestamp;
            float[] values = event.values;
            this.x = values[0];
            this.y = values[1];
            this.z = values[2];
        }

        @Override
        public String toString() {
            return timestamp + "," + x + "," + y + "," + z;
        }
    }
}
