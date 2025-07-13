package com.hx.cationtracke;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * 荣耀手机保活工具类
 */
public class HonorKeepAliveHelper {
    private static final String TAG = "HonorKeepAlive";
    
    /**
     * 检查是否为荣耀/华为手机
     */
    public static boolean isHonorDevice() {
        try {
            String manufacturer = Build.MANUFACTURER != null ? Build.MANUFACTURER.toLowerCase(Locale.ROOT) : "";
            String brand = Build.BRAND != null ? Build.BRAND.toLowerCase(Locale.ROOT) : "";
            String model = Build.MODEL != null ? Build.MODEL.toLowerCase(Locale.ROOT) : "";
            
            return manufacturer.contains("huawei") || 
                   manufacturer.contains("honor") ||
                   brand.contains("huawei") || 
                   brand.contains("honor") ||
                   model.contains("honor") ||
                   model.contains("huawei");
        } catch (Exception e) {
            Log.e(TAG, "检查荣耀设备失败", e);
            return false;
        }
    }
    
    /**
     * 应用荣耀手机保活策略
     */
    public static void applyHonorKeepAlive(Context context) {
        if (!isHonorDevice()) {
            return;
        }
        
        Log.d(TAG, "检测到荣耀/华为设备，应用特殊保活策略");
        
        try {
            // 1. 设置进程优先级
            setProcessPriority();
            
            // 2. 清理内存
            clearMemory(context);
            
            // 3. 检查并重启服务
            checkAndRestartService(context);
            
            // 4. 设置自启动白名单
            setAutoStartWhitelist(context);
            
        } catch (Exception e) {
            Log.e(TAG, "应用荣耀保活策略失败", e);
        }
    }
    
    /**
     * 设置进程优先级
     */
    private static void setProcessPriority() {
        try {
            // 设置进程优先级为前台
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_FOREGROUND);
        } catch (Exception e) {
            Log.e(TAG, "设置进程优先级失败", e);
        }
    }
    
    /**
     * 清理内存
     */
    private static void clearMemory(Context context) {
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null && Build.VERSION.SDK_INT >= 19) {
                am.clearApplicationUserData();
            }
        } catch (Exception e) {
            Log.e(TAG, "清理内存失败", e);
        }
    }
    
    /**
     * 检查并重启服务
     */
    private static void checkAndRestartService(Context context) {
        try {
            if (!isServiceRunning(context, "com.hx.cationtracke.ltmService")) {
                Log.d(TAG, "检测到服务未运行，正在重启...");
                Intent serviceIntent = new Intent(context, ltmService.class);
                context.startService(serviceIntent);
            }
        } catch (Exception e) {
            Log.e(TAG, "重启服务失败", e);
        }
    }
    
    /**
     * 设置自启动白名单
     */
    private static void setAutoStartWhitelist(Context context) {
        try {
            // 尝试打开荣耀/华为的自启动管理页面
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            // 荣耀/华为自启动管理页面
            intent.setClassName("com.huawei.systemmanager", 
                              "com.huawei.systemmanager.optimize.process.ProtectActivity");
            
            if (isIntentAvailable(context, intent)) {
                context.startActivity(intent);
            } else {
                // 备用方案：打开应用管理页面
                intent.setClassName("com.android.settings", 
                                  "com.android.settings.applications.InstalledAppDetails");
                if (isIntentAvailable(context, intent)) {
                    context.startActivity(intent);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "设置自启动白名单失败", e);
        }
    }
    
    /**
     * 检查服务是否运行
     */
    private static boolean isServiceRunning(Context context, String serviceName) {
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningServiceInfo> services = am.getRunningServices(Integer.MAX_VALUE);
            
            for (ActivityManager.RunningServiceInfo service : services) {
                if (service.service.getClassName().equals(serviceName)) {
                    return true;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "检查服务状态失败", e);
        }
        return false;
    }
    
    /**
     * 检查Intent是否可用
     */
    private static boolean isIntentAvailable(Context context, Intent intent) {
        try {
            PackageManager pm = context.getPackageManager();
            return pm.resolveActivity(intent, 0) != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取荣耀手机优化建议
     */
    public static String getHonorOptimizationTips() {
        return "荣耀手机优化建议：\n" +
               "1. 设置 > 应用 > 应用管理 > 位置上报 > 自启动：开启\n" +
               "2. 设置 > 应用 > 应用管理 > 位置上报 > 后台活动：允许\n" +
               "3. 设置 > 电池 > 应用启动管理 > 位置上报：手动管理，全部开启\n" +
               "4. 设置 > 隐私 > 定位服务 > 位置上报：始终允许\n" +
               "5. 设置 > 通知和状态栏 > 位置上报：允许通知";
    }
} 