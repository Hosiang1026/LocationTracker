package com.ljs.locationtracker;

import android.Manifest;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.content.BroadcastReceiver;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.net.SocketTimeoutException;
import javax.net.ssl.SSLHandshakeException;

import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import java.util.concurrent.TimeUnit;

public class ltmService extends Service {
    public static String HOST = "";
    //服务器内置主题，用来监测当前服务器上连接的客户端数量（$SYS/broker/clients/connected）
    public static String TOPIC1 = "location/myphone";
    private static String clientid = BuildConfig.CLIENT_ID;
    private String userName = BuildConfig.MQTT_USERNAME;
    private String passWord = BuildConfig.MQTT_PASSWORD;
    private String TAG = "ljstag";
    private int time = 0;

    // 使用volatile确保多线程可见性
    private static volatile boolean isfrommain = false;
    private static volatile int mode = 0;
    private static volatile int notification_enable = 0;

    // 使用Android原生定位
    private LocationManager locationManager = null;
    private LocationListener locationListener = null;
    private int cnt=0;
    Intent serviceIntent = null;
    boolean isSartLocation = false;
    double PI = 3.14159265358979324;
    private BootBroadcastReceiver receiver;
    
    // 状态跟踪
    private int reportCount = 0;
    private boolean isConnected = false;
    private boolean isLocationRunning = false;
    
    // 数据去重机制
    private String lastReportedData = "";
    private long lastReportTime = 0;
    
    // 通知服务引用
    private LocationForcegroundService notificationService = null;
    
    // 新增：WakeLock机制
    private PowerManager.WakeLock wakeLock = null;
    private boolean isScreenOff = false;
    private boolean isLowBattery = false;
    private Handler keepAliveHandler = new Handler(android.os.Looper.getMainLooper());
    
    // 立即上报广播接收器
    private BroadcastReceiver immediateReportReceiver = null;

    private static final String LOCATION_WORK_NAME = "LocationPeriodicWork";
    private void startLocationWorker(long intervalSeconds) {
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
            LocationWorker.class, intervalSeconds, TimeUnit.SECONDS)
            .build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            LOCATION_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        );
        sendLogBroadcast("已启动 WorkManager 定时定位任务，周期: " + intervalSeconds + "秒", "INFO");
    }
    private void stopLocationWorker() {
        WorkManager.getInstance(this).cancelUniqueWork(LOCATION_WORK_NAME);
        sendLogBroadcast("已停止 WorkManager 定时定位任务", "INFO");
    }

    public ltmService() {
    }

    /**
     * 获取WakeLock
     */
    private void acquireWakeLock() {
        try {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            // 使用兼容的方法，避免过时API警告
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                // Android 5.0+ 使用新的WakeLock方法
                if (isScreenOff) {
                    // 屏幕熄灭时使用更强的WakeLock
                    wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "LocationTracker::FullWakeLock");
                    sendLogBroadcast("📱 屏幕熄灭，使用PARTIAL_WAKE_LOCK增强保活", "INFO");
                } else {
                    // 屏幕点亮时使用普通WakeLock
                    wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "LocationTracker::WakeLock");
                    sendLogBroadcast("📱 屏幕点亮，使用PARTIAL_WAKE_LOCK", "INFO");
                }
            } else {
                // Android 4.0-4.4 使用旧方法
                if (isScreenOff) {
                    wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "LocationTracker::FullWakeLock");
                    sendLogBroadcast("📱 屏幕熄灭，使用FULL_WAKE_LOCK增强保活", "INFO");
                } else {
                    wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "LocationTracker::WakeLock");
                    sendLogBroadcast("📱 屏幕点亮，使用PARTIAL_WAKE_LOCK", "INFO");
                }
            }
            wakeLock.acquire();
            sendLogBroadcast("✅ 已获取增强WakeLock，确保后台运行", "SUCCESS");
        } catch (Exception e) {
            Log.e(TAG, "获取WakeLock失败", e);
            LocationTrackerApplication.logError("获取WakeLock失败", e);
            sendLogBroadcast("获取WakeLock失败: " + e.getMessage(), "ERROR");
        }
    }

    /**
     * 释放WakeLock
     */
    private void releaseWakeLock() {
        try {
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
                wakeLock = null;
                sendLogBroadcast("已释放WakeLock", "INFO");
            }
        } catch (Exception e) {
            Log.e(TAG, "释放WakeLock失败", e);
            LocationTrackerApplication.logError("释放WakeLock失败", e);
        }
    }

    /**
     * 检查是否为省电模式
     */
    private boolean isPowerSaveMode() {
        try {
            if (Build.VERSION.SDK_INT >= 21) {
                PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
                return pm.isPowerSaveMode();
            } else {
                // 低版本无省电模式，始终返回false
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "检查省电模式失败", e);
            LocationTrackerApplication.logError("检查省电模式失败", e);
            return false;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        
        // 防重复启动检查
        if (isLocationRunning) {
            Log.d(TAG, "服务已在运行，跳过重复启动");
            sendLogBroadcast("服务已在运行，跳过重复启动", "INFO");
            
            // 检查是否需要重新启动定位服务（配置变更）
            try {
                // 重新加载配置
                SQLiteDatabase db = null;
                Cursor cursor = null;
                try {
                    DataBaseOpenHelper dbHelper = new DataBaseOpenHelper(this);
                    db = dbHelper.getReadableDatabase();
                    cursor = db.rawQuery("select * from " + Contant.TABLENAME, null);
                    
                    if (cursor != null && cursor.moveToFirst()) {
                        int urlIndex = cursor.getColumnIndex("url");
                        int timeIndex = cursor.getColumnIndex("time");
                        int notificationIndex = cursor.getColumnIndex("notification_enable");
                        
                        if (urlIndex >= 0 && timeIndex >= 0 && notificationIndex >= 0) {
                            String newHost = cursor.getString(urlIndex);
                            int newTime = cursor.getInt(timeIndex);
                            int newNotification = cursor.getInt(notificationIndex);
                            
                            // 检查配置是否发生变化（包括从mode变量传递的配置）
                            boolean configChanged = !HOST.equals(newHost) || time != newTime || notification_enable != newNotification;
                            
                            // 如果mode变量有新的配置值，也视为配置变更
                            if (mode != 0 && mode != time) {
                                configChanged = true;
                                newTime = mode; // 使用mode变量的值
                                sendLogBroadcast("检测到通过setTimeInterval设置的配置变更: " + mode + "秒", "INFO");
                            }
                            
                            if (configChanged) {
                                sendLogBroadcast("检测到配置变更，重新启动定位服务", "INFO");
                                
                                // 更新配置
                                HOST = newHost;
                                time = newTime;
                                notification_enable = newNotification;
                                
                                // 同步mode变量，确保一致性
                                mode = newTime;
                                
                                // 重新启动定位服务
                                if (locationManager != null && locationListener != null) {
                                    locationManager.removeUpdates(locationListener);
                                }
                                
                                // 重新启动定位
                                startLocationUpdates();
                            }
                        }
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                    if (db != null) {
                        db.close();
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "检查配置变更失败", e);
                sendLogBroadcast("检查配置变更失败: " + e.getMessage(), "ERROR");
            }
            
            return START_STICKY;
        }
        
        sendLogBroadcast("=== 位置服务启动 ===", "INFO");
        
        // 重置所有计数器
        reportCount = 0;
        lastReportedData = "";
        lastReportTime = 0;
        
        sendLogBroadcast("🔄 上报次数已重置为0", "INFO");
        
        // 立即广播重置后的状态
        updateStatus();

        try {
            // 获取WakeLock
            acquireWakeLock();
            
            init();
            
            // 强制启动前台服务，无论通知设置如何
            Intent notificationIntent = new Intent(this, LocationForcegroundService.class);
            startService(notificationIntent);
            
            // 获取通知服务的引用
            try {
                // 通过绑定服务获取引用
                bindService(notificationIntent, new ServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName name, IBinder service) {
                        LocationForcegroundService.LocalBinder binder = (LocationForcegroundService.LocalBinder) service;
                        notificationService = binder.getService();
                        sendLogBroadcast("通知服务连接成功", "SUCCESS");
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName name) {
                        notificationService = null;
                        sendLogBroadcast("通知服务连接断开", "WARNING");
                    }
                }, Context.BIND_AUTO_CREATE);
            } catch (Exception e) {
                Log.e(TAG, "绑定通知服务失败", e);
                sendLogBroadcast("绑定通知服务失败: " + e.getMessage(), "ERROR");
            }
            
            // 设置保活定时器，定期检查服务状态
            // startKeepAliveTimer();
            startLocationWorker(time);
            
        if(isSartLocation) {
            //如果使用{@link AMapLocationClient#enableBackgroundLocation(int, Notification)}，这段代码可以不要
            if (null != serviceIntent) {
                startService(serviceIntent);
            }
        }
        } catch (Exception e) {
            Log.e(TAG, "onStartCommand执行失败", e);
            sendLogBroadcast("服务启动失败: " + e.getMessage(), "ERROR");
            sendLogBroadcast("错误详情: " + e.toString(), "ERROR");
        }
        
        // 返回START_STICKY，确保服务被杀死后自动重启
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: ");
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * 根据屏幕状态调整定位更新间隔
     */
    private void adjustLocationUpdatesForScreenState() {
        try {
            if (locationManager != null && locationListener != null) {
                // 先移除当前的定位监听
                locationManager.removeUpdates(locationListener);
                
                // 重新启动定位，使用新的间隔
                String providerToUse = null;
                // 屏幕熄灭时强制使用GPS定位，因为GPS在省电模式下更新频率更稳定
                if (isScreenOff) {
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        providerToUse = LocationManager.GPS_PROVIDER;
                        sendLogBroadcast("📱 屏幕熄灭，强制使用GPS定位", "INFO");
                    } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        providerToUse = LocationManager.NETWORK_PROVIDER;
                        sendLogBroadcast("📱 屏幕熄灭，GPS不可用，使用网络定位", "INFO");
                    } else if (locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
                        providerToUse = LocationManager.PASSIVE_PROVIDER;
                        sendLogBroadcast("📱 屏幕熄灭，使用被动定位", "INFO");
                    }
                } else {
                    // 屏幕点亮时按正常优先级选择
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        providerToUse = LocationManager.GPS_PROVIDER;
                    } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        providerToUse = LocationManager.NETWORK_PROVIDER;
                    } else if (locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
                        providerToUse = LocationManager.PASSIVE_PROVIDER;
                    }
                }
                
                if (providerToUse != null) {
                    // 屏幕熄灭时使用配置间隔的1/2来对抗系统限制
                    long updateInterval;
                    if (isScreenOff) {
                        updateInterval = (time * 1000) / 2; // 屏幕熄灭时使用配置间隔的1/2
                        sendLogBroadcast("📱 屏幕熄灭，使用1/2间隔定位: " + (updateInterval/1000) + "秒", "INFO");
                    } else {
                        updateInterval = time * 1000; // 正常间隔
                        sendLogBroadcast("📱 屏幕点亮，使用正常间隔定位: " + (updateInterval/1000) + "秒", "INFO");
                    }
                    
                    // 使用兼容的定位请求方法
                    requestLocationUpdatesCompat(providerToUse, updateInterval, 0, locationListener);
                        sendLogBroadcast("✅ 定位服务已重新启动，间隔: " + (updateInterval/1000) + "秒", "SUCCESS");
                } else {
                    sendLogBroadcast("❌ 没有可用的定位提供者", "ERROR");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "调整定位间隔失败", e);
            sendLogBroadcast("调整定位间隔失败: " + e.getMessage(), "ERROR");
        }
    }

    private void init() {
        try {
            // 初始化定位管理器
            if (locationManager == null) {
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (locationManager == null) {
                    sendLogBroadcast("无法获取定位服务", "ERROR");
                    return;
                }
            }
            
            // 检查定位权限
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                sendLogBroadcast("定位权限被拒绝，无法启动定位服务", "ERROR");
                        return;
                    }
            
            // 加载配置
            SQLiteDatabase db = null;
            Cursor cursor = null;
            try {
                DataBaseOpenHelper dbHelper = new DataBaseOpenHelper(this);
                db = dbHelper.getReadableDatabase();
                cursor = db.rawQuery("select * from " + Contant.TABLENAME, null);
                
                if (cursor != null && cursor.moveToFirst()) {
                    // 使用兼容的列索引获取方法，避免过时API警告
                    int urlIndex = cursor.getColumnIndex("url");
                    int timeIndex = cursor.getColumnIndex("time");
                    int notificationIndex = cursor.getColumnIndex("notification_enable");
                    
                    if (urlIndex >= 0 && timeIndex >= 0 && notificationIndex >= 0) {
                        HOST = cursor.getString(urlIndex);
                        time = cursor.getInt(timeIndex);
                        notification_enable = cursor.getInt(notificationIndex);
                        
                        // 检查mode变量是否有新的配置值，如果有则优先使用
                        if (mode != 0 && mode != time) {
                            sendLogBroadcast("检测到通过setTimeInterval设置的配置，使用新配置: " + mode + "秒", "INFO");
                            time = mode; // 使用mode变量的值
                        }
                    } else {
                        sendLogBroadcast("数据库列索引获取失败", "ERROR");
                        return;
                }
                
                    sendLogBroadcast("配置加载成功: URL=" + HOST + ", 间隔=" + time + "秒", "SUCCESS");
                } else {
                    sendLogBroadcast("数据库中没有找到配置信息", "ERROR");
                    return;
                }
            } catch (Exception e) {
                sendLogBroadcast("加载配置失败: " + e.getMessage(), "ERROR");
                return;
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                if (db != null) {
                    db.close();
                }
            }
            
            // 验证配置
            if (HOST == null || HOST.trim().isEmpty()) {
                sendLogBroadcast("Webhook URL为空，无法启动服务", "ERROR");
                return;
            }
            
            if (time < 10 || time > 10800) {
                sendLogBroadcast("时间间隔配置无效，必须在10-10800秒之间", "ERROR");
                return;
            }
            
            // 初始化定位监听器
            if (locationListener == null) {
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (location != null) {
                            // 处理位置变化
                            handleLocationUpdate(location);
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                        String statusText = "";
                    switch (status) {
                        case LocationProvider.AVAILABLE:
                            statusText = "可用";
                            break;
                        case LocationProvider.TEMPORARILY_UNAVAILABLE:
                            statusText = "暂时不可用";
                            break;
                        case LocationProvider.OUT_OF_SERVICE:
                                statusText = "服务外";
                            break;
                    }
                        sendLogBroadcast("定位状态变化: " + provider + " - " + statusText, "INFO");
                }

                @Override
                public void onProviderEnabled(String provider) {
                    sendLogBroadcast("定位提供者启用: " + provider, "SUCCESS");
                }

                @Override
                public void onProviderDisabled(String provider) {
                        sendLogBroadcast("定位提供者禁用: " + provider, "WARNING");
                        // 尝试切换到备用定位提供者
                    trySwitchToBackupProvider(provider);
                }
            };
            }
            
            // 启动定位服务
            startLocationUpdates();
            
        } catch (Exception e) {
            Log.e(TAG, "初始化服务失败", e);
            LocationTrackerApplication.logError("初始化服务失败", e);
            sendLogBroadcast("初始化服务失败: " + e.getMessage(), "ERROR");
        }
    }

    /**
     * 停止定位
     *
     * @since 2.8.0
     * @author hongming.wang
     *
     */
    private void stopLocation(){
        // 停止定位
        locationManager.removeUpdates(locationListener);
        isSartLocation = false;
        isLocationRunning = false;
        updateStatus();
        sendLogBroadcast("位置服务已停止", "INFO");
    }
    /**
     * 销毁定位
     *
     * @since 2.8.0
     * @author hongming.wang
     *
     */
    private void destroyLocation(){
        // 销毁定位
        stopLocation();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate:");
        
        try {
        IntentFilter recevierFilter=new IntentFilter();
        recevierFilter.addAction(Intent.ACTION_SCREEN_ON);
        recevierFilter.addAction(Intent.ACTION_SCREEN_OFF);
        recevierFilter.addAction(Intent.ACTION_USER_PRESENT);
        recevierFilter.addAction("com.ljs.locationtracker.start");
        receiver=new BootBroadcastReceiver();
        registerReceiver(receiver, recevierFilter);
        
        // 创建自定义广播接收器来处理屏幕状态变化
        IntentFilter screenFilter = new IntentFilter();
        screenFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                    isScreenOff = true;
                    sendLogBroadcast("屏幕已熄灭，切换到省电模式", "INFO");
                    // 重新调整定位间隔
                    adjustLocationUpdatesForScreenState();
                } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
                    isScreenOff = false;
                    sendLogBroadcast("屏幕已点亮，恢复正常模式", "INFO");
                    // 重新调整定位间隔
                    adjustLocationUpdatesForScreenState();
                }
            }
        }, screenFilter);
        
        // 创建立即上报广播接收器
        IntentFilter immediateReportFilter = new IntentFilter();
        immediateReportFilter.addAction("com.ljs.locationtracker.IMMEDIATE_REPORT");
        immediateReportReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("com.ljs.locationtracker.IMMEDIATE_REPORT".equals(intent.getAction())) {
                    sendLogBroadcast("收到立即上报广播，准备立即上报位置", "INFO");
                    performImmediateLocationReport();
                }
            }
        };
        registerReceiver(immediateReportReceiver, immediateReportFilter);
            
            // 立即发送初始状态
            updateStatus();
            
        } catch (Exception e) {
            Log.e(TAG, "onCreate执行失败", e);
            sendLogBroadcast("服务创建失败: " + e.getMessage(), "ERROR");
        }
        
        // 注册省电模式变化广播
        if (Build.VERSION.SDK_INT >= 21) {
            IntentFilter powerSaveFilter = new IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);
            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    boolean powerSave = isPowerSaveMode();
                    long newInterval = powerSave ? Math.max(time * 2, 600) : time;
                    stopLocationWorker();
                    startLocationWorker(newInterval);
                    sendLogBroadcast("省电模式变化，WorkManager 周期调整为 " + newInterval + " 秒", "INFO");
                }
            }, powerSaveFilter);
        }
    }

    @Override
    public void onDestroy() {   //com.ljs.ltmservice.start
        Log.d(TAG, "onDestroy: ");
        
        // 释放WakeLock
        releaseWakeLock();
        
        // 停止保活定时器
        if (keepAliveHandler != null) {
            keepAliveHandler.removeCallbacksAndMessages(null);
        }
        
        // 注销广播接收器
        if (receiver != null) {
            try {
                unregisterReceiver(receiver);
            } catch (Exception e) {
                Log.e(TAG, "注销广播接收器失败", e);
            }
        }
        
        // 停止定位服务
        if (locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.removeUpdates(locationListener);
        }
        
        // 销毁定位客户端
        destroyLocation();
        
        sendLogBroadcast("位置服务已销毁", "INFO");
        
        Intent  intent=new Intent("com.ljs.locationtracker.start");
        sendBroadcast(intent);
        
        // 注销立即上报广播接收器
        if (immediateReportReceiver != null) {
            try {
                unregisterReceiver(immediateReportReceiver);
            } catch (Exception e) {
                Log.e(TAG, "注销立即上报广播接收器失败", e);
            }
        }
        stopLocationWorker();
        super.onDestroy();
    }

    private int getBatteryLevel() {
        try {
            if (Build.VERSION.SDK_INT >= 21) {
                BatteryManager batteryManager = (BatteryManager) getSystemService(BATTERY_SERVICE);
                int level = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
                if (level == -1) {
                    // 如果无法获取电量，尝试其他方法
                    IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                    Intent batteryStatus = registerReceiver(null, iFilter);
                    if (batteryStatus != null) {
                        int batteryLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                        int batteryScale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                        if (batteryLevel != -1 && batteryScale != -1) {
                            level = (int) ((batteryLevel / (float) batteryScale) * 100);
                        }
                    }
                }
                return level >= 0 ? level : 0;
            } else {
                // 低版本用广播方式获取电量
                IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                Intent batteryStatus = registerReceiver(null, iFilter);
                if (batteryStatus != null) {
                    int batteryLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    int batteryScale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                    if (batteryLevel != -1 && batteryScale != -1) {
                        return (int) ((batteryLevel / (float) batteryScale) * 100);
                    }
                }
                return 0;
            }
        } catch (Exception e) {
            Log.e(TAG, "获取电池电量失败", e);
            return 0;
        }
    }

    /**
     * 检查电量状态并处理低电量情况
     */
    private void checkBatteryStatus() {
        int batteryLevel = getBatteryLevel();
        boolean wasLowBattery = isLowBattery;
        
        // 检查是否进入低电量状态（电量 <= 10%）
        isLowBattery = batteryLevel <= 10;
        
        // 如果电量状态发生变化，记录日志
        if (isLowBattery && !wasLowBattery) {
            sendLogBroadcast("⚠️ 电量低于10%，停止定位上报以节省电量", "WARNING");
            sendLogBroadcast("📱 服务继续运行，等待电量恢复", "INFO");
            
            // 停止定位更新但保持服务运行
            if (locationManager != null && locationListener != null) {
                try {
                    locationManager.removeUpdates(locationListener);
                    sendLogBroadcast("定位服务已暂停", "INFO");
                } catch (Exception e) {
                    Log.e(TAG, "停止定位更新失败", e);
                }
            }
            
        } else if (!isLowBattery && wasLowBattery) {
            sendLogBroadcast("✅ 电量已恢复，重新启动定位上报", "SUCCESS");
            
            // 重新启动定位更新
            if (locationManager != null && locationListener != null) {
                try {
                    String providerToUse = null;
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        providerToUse = LocationManager.GPS_PROVIDER;
                    } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        providerToUse = LocationManager.NETWORK_PROVIDER;
                    } else if (locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
                        providerToUse = LocationManager.PASSIVE_PROVIDER;
                    }
                    
                    if (providerToUse != null) {
                        long updateInterval = time * 1000;
                        // 使用兼容的定位请求方法
                        requestLocationUpdatesCompat(providerToUse, updateInterval, 0, locationListener);
                            sendLogBroadcast("定位服务已恢复", "SUCCESS");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "重新启动定位更新失败", e);
                    sendLogBroadcast("重新启动定位更新失败: " + e.getMessage(), "ERROR");
                }
            }
        }
        
        // 更新状态显示
        updateStatus();
    }

    private void sendDataToWebhookWithNotification(String data, Location location) {
        // 使用后台线程执行网络请求
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 每次只尝试一次请求，不进行重试
                    boolean reportSuccess = performSingleWebhookRequest(data, "数据上报", 1); // 只尝试1次
                    
                    if (reportSuccess) {
                        reportCount++;
                        isConnected = true;
                        sendLogBroadcast("[数据上报]上报数据成功 #" + reportCount, "SUCCESS");
                        
                        // 数据上报成功后，更新通知
                        updateNotificationContent(location);
                    } else {
                        isConnected = false;
                        sendLogBroadcast("[数据上报]上报数据失败，网络连接失败", "WARNING");
                    }
                    
                    // 无论成功失败都更新状态
                    updateStatus();
                    
                } catch (Exception e) {
                    Log.e(TAG, "发送数据到Webhook时发生异常", e);
                    isConnected = false;
                    sendLogBroadcast("发送数据异常: " + e.getMessage(), "ERROR");
                    updateStatus();
                }
            }
        }).start();
    }
    
    /**
     * 执行单次Webhook请求，包含重试机制
     */
    private boolean performSingleWebhookRequest(String data, String requestType, int maxRetries) {
        // 添加重试机制
        int retryCount = 0;
        boolean success = false;
        
        while (retryCount < maxRetries && !success) {
            try {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                        .retryOnConnectionFailure(true) // 启用连接失败重试
                .build();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, data);
        Request request = new Request.Builder()
                .url(HOST)
                .post(body)
                .addHeader("User-Agent", "LocationTracker/1.0")
                .build();

        sendLogBroadcast("[" + requestType + "] 即将发送数据: " + data, "INFO");
        sendLogBroadcast("[" + requestType + "] 发送HTTP请求到: " + HOST, "INFO");

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                sendLogBroadcast("[" + requestType + "] 请求成功，响应状态码: " + response.code(), "SUCCESS");
                success = true;
            } else {
                sendLogBroadcast("[" + requestType + "] 发送HTTP请求失败，状态码: " + response.code(), "ERROR");
                String responseBody = response.body() != null ? response.body().string() : "无响应内容";
                sendLogBroadcast("[" + requestType + "] 响应内容: " + responseBody, "ERROR");
                retryCount++;
                // 不再输出重试日志
                if (retryCount < maxRetries) {
                    try {
                        Thread.sleep(2000); // 等待2秒后重试
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        sendLogBroadcast("[" + requestType + "] 重试等待被中断", "WARNING");
                        break;
                    }
                }
            }
        }
                
            } catch (SocketTimeoutException e) {
                Log.e(TAG, "[" + requestType + "] 网络请求超时", e);
                sendLogBroadcast("[" + requestType + "] 网络请求超时: " + e.getMessage(), "ERROR");
                retryCount++;
                // 不再输出重试日志
                if (retryCount < maxRetries) {
                    try {
                        Thread.sleep(3000); // 超时错误等待更长时间
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        sendLogBroadcast("[" + requestType + "] 重试等待被中断", "WARNING");
                        break;
                    }
                }
            } catch (SSLHandshakeException e) {
                Log.e(TAG, "[" + requestType + "] SSL握手失败", e);
                sendLogBroadcast("[" + requestType + "] SSL握手失败: " + e.getMessage(), "ERROR");
                retryCount++;
                // 不再输出重试日志
                if (retryCount < maxRetries) {
                    try {
                        Thread.sleep(3000); // SSL错误等待更长时间
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        sendLogBroadcast("[" + requestType + "] 重试等待被中断", "WARNING");
                        break;
                    }
            }
        } catch (IOException e) {
                Log.e(TAG, "[" + requestType + "] 发送HTTP请求失败", e);
                sendLogBroadcast("[" + requestType + "] 发送HTTP请求失败: " + e.getMessage(), "ERROR");
                sendLogBroadcast("[" + requestType + "] 错误详情: " + e.toString(), "ERROR");
                retryCount++;
                // 不再输出重试日志
                if (retryCount < maxRetries) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        sendLogBroadcast("[" + requestType + "] 重试等待被中断", "WARNING");
                        break;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "[" + requestType + "] 网络请求异常", e);
                sendLogBroadcast("[" + requestType + "] 网络请求异常: " + e.getMessage(), "ERROR");
                sendLogBroadcast("[" + requestType + "] 错误详情: " + e.toString(), "ERROR");
                retryCount++;
                // 不再输出重试日志
                if (retryCount < maxRetries) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        sendLogBroadcast("[" + requestType + "] 重试等待被中断", "WARNING");
                        break;
                    }
                }
            }
        }
        
        if (!success) {
            // 移除"已达到最大重试次数"的日志
        }
        
        return success;
    }
    
    /**
     * 发送日志广播
     */
    private void sendLogBroadcast(String message, String type) {
        try {
            Intent intent = new Intent(MainActivity.ACTION_LOG_UPDATE);
            intent.putExtra(MainActivity.EXTRA_LOG_MESSAGE, message);
            intent.putExtra(MainActivity.EXTRA_LOG_TYPE, type);
            sendBroadcast(intent);
        } catch (Exception e) {
            Log.e(TAG, "发送日志广播失败: " + message, e);
            // 如果广播发送失败，至少记录到系统日志
            Log.d(TAG, "日志消息: [" + type + "] " + message);
        }
    }
    
    /**
     * 更新状态广播
     */
    private void updateStatus() {
        try {
            Intent intent = new Intent(MainActivity.ACTION_STATUS_UPDATE);
            intent.putExtra(MainActivity.EXTRA_CONNECTION_STATUS, 
                isConnected ? getString(R.string.connected) : getString(R.string.disconnected));
            
            // 根据电量状态显示不同的定位状态
            String locationStatus;
            if (isLowBattery) {
                locationStatus = "低电量暂停";
            } else {
                locationStatus = isLocationRunning ? getString(R.string.running) : getString(R.string.stopped);
            }
            intent.putExtra(MainActivity.EXTRA_LOCATION_STATUS, locationStatus);
            
            int batteryLevel = getBatteryLevel();
            intent.putExtra(MainActivity.EXTRA_BATTERY_LEVEL, batteryLevel > 0 ? batteryLevel + "%" : "--");
            intent.putExtra(MainActivity.EXTRA_REPORT_COUNT, String.valueOf(reportCount));
            
            sendBroadcast(intent);
            
        } catch (Exception e) {
            Log.e(TAG, "更新状态失败", e);
            sendLogBroadcast("更新状态失败: " + e.getMessage(), "ERROR");
            sendLogBroadcast("错误详情: " + e.toString(), "ERROR");
        }
    }

    // 线程安全的getter和setter方法
    public static boolean isFromMain() {
        return isfrommain;
    }
    
    public static void setFromMain(boolean fromMain) {
        isfrommain = fromMain;
    }
    
    public static int getMode() {
        return mode;
    }
    
    public static void setMode(int modeValue) {
        mode = modeValue;
    }
    
    /**
     * 设置时间间隔配置
     */
    public static void setTimeInterval(int timeValue) {
        // 验证时间间隔的有效性
        if (timeValue < 10 || timeValue > 10800) {
            Log.w("ltmService", "setTimeInterval: 无效的时间间隔 " + timeValue + "，使用默认值60");
            timeValue = 60; // 使用默认值
        }
        // 设置到mode变量，服务会在下次检查时同步到time变量
        mode = timeValue;
    }
    
    public static int getNotificationEnable() {
        return notification_enable;
    }
    
    public static void setNotificationEnable(int enable) {
        notification_enable = enable;
    }

    /**
     * 尝试切换到备用定位提供者
     */
    private void trySwitchToBackupProvider(String currentProvider) {
        try {
            // 停止当前定位提供者
            locationManager.removeUpdates(locationListener);
            
            // 按优先级尝试备用方案
            if (currentProvider.equals(LocationManager.GPS_PROVIDER)) {
                // 如果GPS失败，尝试网络定位
                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    try {
                        long updateInterval = time * 1000;
                        // 使用兼容的定位请求方法
                        requestLocationUpdatesCompat(LocationManager.NETWORK_PROVIDER, updateInterval, 0, locationListener);
                            sendLogBroadcast("已切换到WLAN/移动网络定位", "SUCCESS");
                            return;
                    } catch (Exception e) {
                        sendLogBroadcast("切换到WLAN/移动网络定位失败: " + e.getMessage(), "ERROR");
                    }
                }
                
                // 如果网络定位也失败，尝试被动定位
                if (locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
                    try {
                        long updateInterval = time * 1000;
                        // 使用兼容的定位请求方法
                        requestLocationUpdatesCompat(LocationManager.PASSIVE_PROVIDER, updateInterval, 0, locationListener);
                            sendLogBroadcast("已切换到被动定位", "SUCCESS");
                            return;
                    } catch (Exception e) {
                        sendLogBroadcast("切换到被动定位失败: " + e.getMessage(), "ERROR");
                    }
                }
            } else if (currentProvider.equals(LocationManager.NETWORK_PROVIDER)) {
                // 如果网络定位失败，尝试被动定位
                if (locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
                    try {
                        long updateInterval = time * 1000;
                        // 使用兼容的定位请求方法
                        requestLocationUpdatesCompat(LocationManager.PASSIVE_PROVIDER, updateInterval, 0, locationListener);
                            sendLogBroadcast("已切换到被动定位", "SUCCESS");
                            return;
                    } catch (Exception e) {
                        sendLogBroadcast("切换到被动定位失败: " + e.getMessage(), "ERROR");
                    }
                }
            }
            
            // 如果所有备用方案都失败
            sendLogBroadcast("所有定位方式均不可用，定位服务停止", "ERROR");
            isLocationRunning = false;
            updateStatus();
            
        } catch (Exception e) {
            Log.e(TAG, "切换定位提供者失败", e);
            sendLogBroadcast("切换定位提供者失败: " + e.getMessage(), "ERROR");
        }
    }

    /**
     * 更新通知内容
     */
    private void updateNotificationContent(Location location) {
        try {
            if (notificationService != null && ltmService.getNotificationEnable() == 1) {
                int batteryLevel = getBatteryLevel();
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                
                // 只在系统日志中记录，不在运行日志中显示
                Log.d(TAG, "数据上报成功，更新通知内容");
                Log.d(TAG, "通知内容 - 电量:" + batteryLevel + "%, 位置:" + latitude + "," + longitude);
                Log.d(TAG, "上报次数:" + reportCount + ", 上报数据与通知内容一致");
                
                notificationService.updateNotification(batteryLevel, latitude, longitude, 0, 0); // 使用当前时间，不需要timeSinceLastReport参数
                
                // 不在运行日志中显示通知内容更新信息
            } else {
                if (notificationService == null) {
                    Log.w(TAG, "通知服务未初始化");
                }
                if (ltmService.getNotificationEnable() != 1) {
                    Log.d(TAG, "通知功能未启用");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "更新通知内容失败", e);
            sendLogBroadcast("更新通知内容失败: " + e.getMessage(), "ERROR");
        }
    }

    /**
     * 启动保活定时器（每次随机60秒~配置上报间隔之间，如果配置间隔≤60秒则固定60秒）
     */
    private void startKeepAliveTimer() {
        try {
            // 生成保活定时器间隔
            int min = 60; // 秒
            int max;
            
            // 验证time变量的有效性，如果无效则使用默认值
            if (time <= 0) {
                sendLogBroadcast("配置的上报间隔无效，使用默认值60秒", "WARNING");
                time = 60; // 设置默认值
            }
            
            // 如果配置的上报间隔小于等于60秒，则保活定时器固定为60秒
            if (time <= 60) {
                max = 60; // 固定为60秒
            } else {
                max = time; // 使用配置的上报更新间隔作为最大值
            }
            
            // 确保max >= min，避免负数或0的情况
            if (max < min) {
                max = min;
                sendLogBroadcast("保活定时器间隔计算异常，使用最小值60秒", "WARNING");
            }
            
            int randomSeconds = min + (int)(Math.random() * (max - min + 1));
            long delayMillis = randomSeconds * 1000L;
            sendLogBroadcast("保活定时器本轮间隔: " + randomSeconds + "秒 (范围: 60-" + max + "秒)", "INFO");
            keepAliveHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkAndRestartService();
                    // 递归调用，形成定时器
                    startKeepAliveTimer();
                }
            }, delayMillis);
        } catch (Exception e) {
            Log.e(TAG, "启动保活定时器失败", e);
            sendLogBroadcast("启动保活定时器失败: " + e.getMessage(), "ERROR");
            
            // 异常情况下使用默认60秒间隔
            try {
                keepAliveHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        checkAndRestartService();
                        startKeepAliveTimer();
                    }
                }, 60000); // 60秒
                sendLogBroadcast("使用默认60秒保活间隔", "INFO");
            } catch (Exception ex) {
                Log.e(TAG, "设置默认保活间隔也失败", ex);
            }
        }
    }
    
    /**
     * 检查并重启服务
     */
    private void checkAndRestartService() {
        try {
            // 检查电量状态
            checkBatteryStatus();
            
            // 检查定位是否还在运行
            if (!isLocationRunning || locationManager == null) {
                sendLogBroadcast("检测到服务异常，正在重启...", "WARNING");
                
                // 重新初始化定位
                if (locationManager != null) {
                    locationManager.removeUpdates(locationListener);
                }
                
                // 重新启动定位
                init();
            }
            
            // 检查WakeLock是否还在持有
            if (wakeLock == null || !wakeLock.isHeld()) {
                sendLogBroadcast("检测到WakeLock丢失，重新获取...", "WARNING");
                acquireWakeLock();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "检查服务状态失败", e);
            sendLogBroadcast("服务状态检查失败: " + e.getMessage(), "ERROR");
        }
    }

    private void sendDataToWebhook(String data) {
        // 使用后台线程执行网络请求
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 每次只尝试一次请求，不进行重试
                    boolean reportSuccess = performSingleWebhookRequest(data, "数据上报", 1); // 只尝试1次
                    
                    if (reportSuccess) {
                        reportCount++;
                        isConnected = true;
                        sendLogBroadcast("[数据上报]上报数据成功 #" + reportCount, "SUCCESS");
                    } else {
                        isConnected = false;
                        sendLogBroadcast("[数据上报]上报数据失败，网络连接失败", "WARNING");
                    }
                    
                    // 无论成功失败都更新状态
                    updateStatus();
                    
                } catch (Exception e) {
                    Log.e(TAG, "发送数据到Webhook时发生异常", e);
                    isConnected = false;
                    sendLogBroadcast("发送数据异常: " + e.getMessage(), "ERROR");
                    updateStatus();
                }
            }
        }).start();
    }

    /**
     * 立即上报当前位置
     */
    private void performImmediateLocationReport() {
        try {
            sendLogBroadcast("开始立即上报位置...", "INFO");
            
            // 检查电量状态
            checkBatteryStatus();
            
            // 如果电量低于10%，不进行上报
            if (isLowBattery) {
                sendLogBroadcast("电量低于10%，跳过立即上报", "WARNING");
                return;
            }
            
            // 检查定位管理器是否可用
            if (locationManager == null) {
                sendLogBroadcast("定位管理器不可用，无法立即上报", "ERROR");
                return;
            }
            
            // 尝试获取最后已知位置
            Location lastKnownLocation = null;
            String providerName = "";
            
            // 按优先级尝试获取最后已知位置
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    providerName = "GPS";
                }
            }
            
            if (lastKnownLocation == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    providerName = "WLAN/移动网络";
                }
            }
            
            if (lastKnownLocation == null && locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                    providerName = "被动定位";
                }
            }
            
            if (lastKnownLocation != null) {
                sendLogBroadcast("获取到" + providerName + "最后已知位置，立即上报", "SUCCESS");
                
                // 构建位置数据
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("latitude", lastKnownLocation.getLatitude());
                    jsonObject.put("longitude", lastKnownLocation.getLongitude());
                    jsonObject.put("altitude", lastKnownLocation.getAltitude());
                    jsonObject.put("gps_accuracy", lastKnownLocation.getAccuracy());
                    jsonObject.put("battery", getBatteryLevel());
                    jsonObject.put("speed", lastKnownLocation.getSpeed());
                    jsonObject.put("bearing", lastKnownLocation.getBearing());
                    jsonObject.put("timestamp", System.currentTimeMillis());
                    jsonObject.put("provider", lastKnownLocation.getProvider());
                    jsonObject.put("screen_off", isScreenOff);
                    jsonObject.put("power_save_mode", isPowerSaveMode());
                    jsonObject.put("immediate_report", true); // 标记为立即上报
                } catch (Exception e) {
                    Log.e(TAG, "构建立即上报JSON对象失败", e);
                    sendLogBroadcast("构建立即上报数据失败: " + e.getMessage(), "ERROR");
                    return;
                }
                
                String immediateData = jsonObject.toString();
                
                // 立即上报数据
                sendDataToWebhookWithNotification(immediateData, lastKnownLocation);
                
                sendLogBroadcast("立即上报数据已发送", "SUCCESS");
                
            } else {
                sendLogBroadcast("无法获取最后已知位置，等待新的位置更新", "WARNING");
                sendLogBroadcast("请确保GPS已开启且定位权限已授予", "INFO");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "立即上报位置失败", e);
            sendLogBroadcast("立即上报位置失败: " + e.getMessage(), "ERROR");
        }
    }

    /**
     * 兼容的定位请求方法，处理过时API警告
     */
    private void requestLocationUpdatesCompat(String provider, long minTime, float minDistance, LocationListener listener) {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // 使用传统的requestLocationUpdates方法，在API 31+上会显示过时警告
                // 但这是为了保持对Android 4.0+的兼容性
                // 注意：这里仍然使用过时API，但通过封装方法可以更好地控制警告
                locationManager.requestLocationUpdates(provider, minTime, minDistance, listener);
            } else {
                sendLogBroadcast("定位权限被拒绝，无法启动位置服务", "ERROR");
            }
        } catch (SecurityException e) {
            sendLogBroadcast("定位权限不足: " + e.getMessage(), "ERROR");
        } catch (Exception e) {
            sendLogBroadcast("启动定位失败: " + e.getMessage(), "ERROR");
        }
    }

    /**
     * 启动定位更新
     */
    private void startLocationUpdates() {
        try {
            // 检查定位提供者是否可用，按优先级选择
            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            boolean isPassiveEnabled = locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER);
            
            String providerToUse = null;
            String providerName = "";
            
            // 按优先级选择定位提供者
            if (isGpsEnabled) {
                providerToUse = LocationManager.GPS_PROVIDER;
                providerName = "GPS";
                sendLogBroadcast("GPS已开启，使用GPS定位", "SUCCESS");
            } else if (isNetworkEnabled) {
                providerToUse = LocationManager.NETWORK_PROVIDER;
                providerName = "WLAN/移动网络";
                sendLogBroadcast("GPS不可用，使用WLAN/移动网络定位", "WARNING");
            } else if (isPassiveEnabled) {
                providerToUse = LocationManager.PASSIVE_PROVIDER;
                providerName = "被动定位";
                sendLogBroadcast("GPS和网络定位不可用，使用被动定位", "WARNING");
            } else {
                sendLogBroadcast("所有定位功能未开启", "ERROR");
                sendLogBroadcast("请在系统设置中开启定位功能", "ERROR");
                isLocationRunning = false;
                return;
            }
            
            // 尝试启动定位服务
            boolean locationStarted = false;
            try {
                long updateInterval = time * 1000; // 使用配置的间隔时间
                
                // 使用兼容的定位请求方法
                requestLocationUpdatesCompat(providerToUse, updateInterval, 0, locationListener);
                locationStarted = true;
                sendLogBroadcast("成功启动" + providerName + "定位", "SUCCESS");
                sendLogBroadcast("定位间隔设置为" + (updateInterval/1000) + "秒", "INFO");
            } catch (SecurityException e) {
                sendLogBroadcast("定位权限不足: " + e.getMessage(), "ERROR");
            } catch (Exception e) {
                sendLogBroadcast("启动" + providerName + "定位失败: " + e.getMessage(), "ERROR");
            }
            
            // 如果首选定位方式失败，尝试备用方案
            if (!locationStarted) {
                sendLogBroadcast("尝试备用定位方案...", "INFO");
                
                // 尝试网络定位作为备用
                if (isNetworkEnabled && !providerToUse.equals(LocationManager.NETWORK_PROVIDER)) {
                    try {
                        long updateInterval = time * 1000;
                        // 使用兼容的定位请求方法
                        requestLocationUpdatesCompat(LocationManager.NETWORK_PROVIDER, updateInterval, 0, locationListener);
                        locationStarted = true;
                        sendLogBroadcast("备用方案：成功启动WLAN/移动网络定位", "SUCCESS");
                    } catch (Exception e) {
                        sendLogBroadcast("备用WLAN/移动网络定位失败: " + e.getMessage(), "ERROR");
                    }
                }
                
                // 如果网络定位也失败，尝试被动定位
                if (!locationStarted && isPassiveEnabled && !providerToUse.equals(LocationManager.PASSIVE_PROVIDER)) {
                    try {
                        long updateInterval = time * 1000;
                        // 使用兼容的定位请求方法
                        requestLocationUpdatesCompat(LocationManager.PASSIVE_PROVIDER, updateInterval, 0, locationListener);
                        locationStarted = true;
                        sendLogBroadcast("备用方案：成功启动被动定位", "SUCCESS");
                    } catch (Exception e) {
                        sendLogBroadcast("备用被动定位失败: " + e.getMessage(), "ERROR");
                    }
                }
            }
            
            if (locationStarted) {
                isSartLocation = true;
                isLocationRunning = true;
                sendLogBroadcast("[应用服务]服务启动成功", "SUCCESS");
                setFromMain(false);
                Toast.makeText(getApplicationContext(), "开始定位", Toast.LENGTH_SHORT).show();
                updateStatus();
            } else {
                sendLogBroadcast("所有定位方式均启动失败", "ERROR");
                isLocationRunning = false;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "启动定位更新失败", e);
            LocationTrackerApplication.logError("启动定位更新失败", e);
            sendLogBroadcast("启动定位更新失败: " + e.getMessage(), "ERROR");
        }
    }
    
    /**
     * 处理位置更新
     */
    private void handleLocationUpdate(Location location) {
        try {
            // 检查电量状态
            checkBatteryStatus();
            
            // 如果电量低于10%，停止定位上报
            if (isLowBattery) {
                return;
            }
            
            // 只上报基本的位置信息
            if (location.hasAccuracy() && location.getAccuracy() > 0) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("latitude", location.getLatitude());
                    jsonObject.put("longitude", location.getLongitude());
                    jsonObject.put("altitude", location.getAltitude());
                    jsonObject.put("gps_accuracy", location.getAccuracy());
                    jsonObject.put("battery", getBatteryLevel());
                    jsonObject.put("speed", location.getSpeed());
                    jsonObject.put("bearing", location.getBearing());
                    jsonObject.put("timestamp", System.currentTimeMillis());
                    jsonObject.put("provider", location.getProvider());
                    jsonObject.put("screen_off", isScreenOff);
                    jsonObject.put("power_save_mode", isPowerSaveMode());
                } catch (Exception e) {
                    Log.e(TAG, "构建JSON对象失败", e);
                    sendLogBroadcast("构建位置数据失败: " + e.getMessage(), "ERROR");
                    return;
                }
                
                String currentData = jsonObject.toString();
                long currentTime = System.currentTimeMillis();
                
                // 检查数据是否发生变化且满足时间间隔要求
                boolean dataChanged = !currentData.equals(lastReportedData);
                boolean timeElapsed = (currentTime - lastReportTime) >= (time * 1000);
                
                // 添加详细的调试日志
                long timeSinceLastReport = (currentTime - lastReportTime) / 1000;
                
                // 检查时间计算是否异常
                if (lastReportTime == 0 || timeSinceLastReport < 0 || timeSinceLastReport > 3600) {
                    sendLogBroadcast("⚠️ 检测到时间计算异常，重置lastReportTime", "WARNING");
                    lastReportTime = currentTime - (time * 1000);
                    timeSinceLastReport = time;
                }
                
                sendLogBroadcast("🔍 定位更新 - 距离上次上报: " + timeSinceLastReport + "秒，配置间隔: " + time + "秒", "INFO");
                sendLogBroadcast("🔍 位置变化: " + dataChanged + "，时间间隔满足: " + timeElapsed + "，电量正常: " + (!isLowBattery), "INFO");
                
                // 要求位置发生变化且时间间隔满足才上报
                boolean shouldReport = dataChanged && timeElapsed;
                
                // 如果电量低于10%，不进行上报
                if (isLowBattery) {
                    shouldReport = false;
                    sendLogBroadcast("⚠️ 电量低于10%，跳过上报", "WARNING");
                }
                
                // 屏幕熄灭时，确保正常上报
                if (isScreenOff && shouldReport) {
                    sendLogBroadcast("📱 屏幕熄灭期间，正常上报位置数据", "INFO");
                }
                
                if (shouldReport) {
                    lastReportedData = currentData;
                    lastReportTime = currentTime;
                    
                    // 发送数据到webhook，并在成功后更新通知
                    sendDataToWebhookWithNotification(currentData, location);
                } else {
                    if (!dataChanged) {
                        sendLogBroadcast("⏳ 位置未变化，跳过上报", "INFO");
                    } else if (!timeElapsed) {
                        sendLogBroadcast("⏳ 时间间隔未满足，跳过上报", "INFO");
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "处理位置更新失败", e);
            LocationTrackerApplication.logError("处理位置更新失败", e);
            sendLogBroadcast("处理位置更新失败: " + e.getMessage(), "ERROR");
        }
    }

}