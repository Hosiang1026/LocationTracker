package com.ljs.locationtracker;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import androidx.annotation.Nullable;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.util.concurrent.TimeUnit;

public class ConfigSyncService extends Service {
    private static final String TAG = "ConfigSyncService";
    private static final String CONFIG_URL = BuildConfig.CONFIG_URL;
    private static final String HEARTBEAT_URL = BuildConfig.HEARTBEAT_URL;
    private static final int SYNC_INTERVAL = 300000; // 5分钟同步一次
    
    private OkHttpClient client;
    private boolean isRunning = false;
    private Thread syncThread;
    
    @Override
    public void onCreate() {
        super.onCreate();
        client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning) {
            startConfigSync();
        }
        return START_STICKY;
    }
    
    private void startConfigSync() {
        isRunning = true;
        syncThread = new Thread(() -> {
            while (isRunning) {
                try {
                    syncConfigFromServer();
                    sendHeartbeat();
                    Thread.sleep(SYNC_INTERVAL);
                } catch (InterruptedException e) {
                    Log.e(TAG, "配置同步线程被中断", e);
                    break;
                } catch (Exception e) {
                    Log.e(TAG, "配置同步失败", e);
                    try {
                        Thread.sleep(60000); // 失败后等待1分钟再重试
                    } catch (InterruptedException ie) {
                        break;
                    }
                }
            }
        });
        syncThread.start();
    }
    
    private void syncConfigFromServer() {
        try {
            String deviceId = getDeviceId();
            Request request = new Request.Builder()
                .url(CONFIG_URL + "/" + deviceId)
                .addHeader("Device-ID", deviceId)
                .build();
                
            Response response = client.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                String jsonString = response.body().string();
                JSONObject config = new JSONObject(jsonString);
                
                // 检查配置是否有变化
                if (hasConfigChanged(config)) {
                    updateLocalConfig(config);
                    restartLocationService();
                    Log.i(TAG, "配置已更新并应用");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "从服务器获取配置失败", e);
        }
    }
    
    private void sendHeartbeat() {
        try {
            String deviceId = getDeviceId();
            Request request = new Request.Builder()
                .url(HEARTBEAT_URL)
                .addHeader("Device-ID", deviceId)
                .post(new okhttp3.RequestBody() {
                    @Override
                    public okhttp3.MediaType contentType() {
                        return okhttp3.MediaType.parse("application/json");
                    }
                    
                    @Override
                    public void writeTo(okio.BufferedSink sink) throws IOException {
                        sink.writeUtf8("{}");
                    }
                    
                    @Override
                    public long contentLength() throws IOException {
                        return 2;
                    }
                })
                .build();
                
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                Log.d(TAG, "心跳发送成功");
            }
        } catch (Exception e) {
            Log.e(TAG, "发送心跳失败", e);
        }
    }
    
    private String getDeviceId() {
        // 使用设备唯一标识符
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        return androidId != null ? androidId : "unknown_device";
    }
    
    private boolean hasConfigChanged(JSONObject newConfig) {
        try {
            SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE);
            String currentWebhook = prefs.getString("webhook_url", "");
            int currentInterval = prefs.getInt("update_interval", 600);
            boolean currentNotification = prefs.getBoolean("notification_enabled", true);
            
            String newWebhook = newConfig.optString("webhook_url", "");
            int newInterval = newConfig.optInt("update_interval", 600);
            boolean newNotification = newConfig.optBoolean("notification_enabled", true);
            
            return !currentWebhook.equals(newWebhook) ||
                   currentInterval != newInterval ||
                   currentNotification != newNotification;
        } catch (Exception e) {
            Log.e(TAG, "检查配置变化失败", e);
            return false;
        }
    }
    
    private void updateLocalConfig(JSONObject config) {
        try {
            SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            
            editor.putString("webhook_url", config.optString("webhook_url", ""));
            editor.putInt("update_interval", config.optInt("update_interval", 600));
            editor.putBoolean("notification_enabled", config.optBoolean("notification_enabled", true));
            editor.putLong("last_sync", System.currentTimeMillis());
            
            editor.apply();
        } catch (Exception e) {
            Log.e(TAG, "更新本地配置失败", e);
        }
    }
    
    private void restartLocationService() {
        try {
            // 停止当前服务
            Intent stopIntent = new Intent(this, ltmService.class);
            stopService(stopIntent);
            
            // 启动新服务
            Intent startIntent = new Intent(this, ltmService.class);
            startService(startIntent);
        } catch (Exception e) {
            Log.e(TAG, "重启位置服务失败", e);
        }
    }
    
    @Override
    public void onDestroy() {
        isRunning = false;
        if (syncThread != null) {
            syncThread.interrupt();
        }
        super.onDestroy();
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
} 