package com.ljs.locationtracker;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

public class LocationForcegroundService extends Service {

    // 通知数据
    private int batteryLevel = 0;
    private double latitude = 0.0;
    private double longitude = 0.0;
    private int reportCount = 0;
    private long timeSinceLastReport = 0;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Android O上才显示通知栏
        if(Build.VERSION.SDK_INT >= 26) {
            showNotify();
        } else {
            // Android 4.0-7.1 使用旧的前台服务方式
            if(ltmService.getNotificationEnable() == 1) {
                startForeground(Utils.NOTIFY_ID, Utils.buildNotification(getApplicationContext(), batteryLevel, latitude, longitude, reportCount, timeSinceLastReport));
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //显示通知栏
    @SuppressLint("NewApi")
    public void showNotify(){
        //调用这个方法把服务设置成前台服务
        if(ltmService.getNotificationEnable() == 1) {
            Notification notification = Utils.buildNotification(getApplicationContext(), batteryLevel, latitude, longitude, reportCount, timeSinceLastReport);
            if (notification != null) {
                startForeground(Utils.NOTIFY_ID, notification);
            }
        }
    }
    
    // 更新通知内容
    public void updateNotification(int batteryLevel, double latitude, double longitude, int reportCount, long timeSinceLastReport) {
        this.batteryLevel = batteryLevel;
        this.latitude = latitude;
        this.longitude = longitude;
        this.reportCount = reportCount;
        this.timeSinceLastReport = timeSinceLastReport;
        
        if(ltmService.getNotificationEnable() == 1) {
            Notification notification = Utils.buildNotification(getApplicationContext(), batteryLevel, latitude, longitude, reportCount, timeSinceLastReport);
            if (notification != null) {
                android.app.NotificationManager notificationManager = (android.app.NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.notify(Utils.NOTIFY_ID, notification);
            }
        }
    }

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        LocationForcegroundService getService() {
            return LocationForcegroundService.this;
        }
    }
}