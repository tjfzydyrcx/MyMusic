package com.example.administrator.mymousic.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Created by Administrator on 2018-03-07 0007.
 */

public class ShakeListener implements SensorEventListener {
    //速度阈值
    private static final int SPEED_SHAKEHOLD = 2000;
    //检测时间间隔
    private static final int UPTATE_INTERVAL_TIME = 70;
    //传感器管理器
    private SensorManager sensorManager;
    //传感器
    private Sensor sensor;
    //重力感应监听器
    private OnShakeListener onShakeListener;
    // 上下文
    private Context mContext;
    // 手机上一个位置时重力感应坐标
    private float lastX;
    private float lastY;
    private float lastZ;
    // 上次检测时间
    private long lastUpdateTime;

    public ShakeListener(Context mContext) {
        this.mContext = mContext;
    }

    public void start() {
        sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        if (sensor != null) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    // 设置重力感应监听器
    public void setOnShakeListener(OnShakeListener listener) {
        onShakeListener = listener;
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long currentUpdateTime = System.currentTimeMillis();
        long timeInterval = currentUpdateTime - lastUpdateTime;
        if (timeInterval < UPTATE_INTERVAL_TIME) {
            return;
        }
        lastUpdateTime = currentUpdateTime;
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        float deltaX = x - lastX;
        float deltaY = y - lastY;
        float deltaZ = z - lastZ;
        lastX = x;
        lastY = y;
        lastZ = z;
        Log.e("jiashudu==", x + "===y==" + y + "z==" + z);
        double speed = Math.sqrt(deltaX * deltaX + deltaY * deltaY * deltaZ * deltaZ) / timeInterval * 10000;
        if (speed > SPEED_SHAKEHOLD) {
            Log.e("speed==", speed + "");
            onShakeListener.onShake();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    // 摇晃监听接口
    public interface OnShakeListener {
        public void onShake();
    }

}
