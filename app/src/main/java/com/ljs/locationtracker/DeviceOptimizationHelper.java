package com.ljs.locationtracker;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import java.util.Locale;

/**
 * 设备优化工具类
 */
public class DeviceOptimizationHelper {
    private static final String TAG = "DeviceOptimization";
    
    /**
     * 设备品牌枚举
     */
    public enum DeviceBrand {
        HONOR("荣耀/华为"),
        XIAOMI("小米/红米"),
        OPPO("OPPO"),
        VIVO("vivo"),
        SAMSUNG("三星"),
        ONEPLUS("一加"),
        MEIZU("魅族"),
        OTHER("其他");
        
        private final String displayName;
        
        DeviceBrand(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * 检测设备品牌
     */
    public static DeviceBrand detectDeviceBrand() {
        try {
            String manufacturer = Build.MANUFACTURER != null ? Build.MANUFACTURER.toLowerCase(Locale.ROOT) : "";
            String brand = Build.BRAND != null ? Build.BRAND.toLowerCase(Locale.ROOT) : "";
            String model = Build.MODEL != null ? Build.MODEL.toLowerCase(Locale.ROOT) : "";
            
            Log.d(TAG, "设备信息 - Manufacturer: " + manufacturer + ", Brand: " + brand + ", Model: " + model);
            
            if (manufacturer.contains("huawei") || manufacturer.contains("honor") ||
                brand.contains("huawei") || brand.contains("honor") ||
                model.contains("honor") || model.contains("huawei")) {
                return DeviceBrand.HONOR;
            } else if (manufacturer.contains("xiaomi") || brand.contains("xiaomi") ||
                       manufacturer.contains("redmi") || brand.contains("redmi") ||
                       model.contains("mi") || model.contains("redmi")) {
                return DeviceBrand.XIAOMI;
            } else if (manufacturer.contains("oppo") || brand.contains("oppo") ||
                       model.contains("oppo")) {
                return DeviceBrand.OPPO;
            } else if (manufacturer.contains("vivo") || brand.contains("vivo") ||
                       model.contains("vivo")) {
                return DeviceBrand.VIVO;
            } else if (manufacturer.contains("samsung") || brand.contains("samsung") ||
                       model.contains("samsung")) {
                return DeviceBrand.SAMSUNG;
            } else if (manufacturer.contains("oneplus") || brand.contains("oneplus") ||
                       model.contains("oneplus")) {
                return DeviceBrand.ONEPLUS;
            } else if (manufacturer.contains("meizu") || brand.contains("meizu") ||
                       model.contains("meizu")) {
                return DeviceBrand.MEIZU;
            } else {
                return DeviceBrand.OTHER;
            }
        } catch (Exception e) {
            Log.e(TAG, "设备品牌检测失败", e);
            return DeviceBrand.OTHER;
        }
    }
    
    /**
     * 获取设备优化建议
     */
    public static String getOptimizationTips(DeviceBrand brand) {
        switch (brand) {
            case HONOR:
                return "📱 检测到荣耀/华为设备\n" +
                       "💡 建议开启以下权限以确保后台运行：\n" +
                       "1. 设置 > 应用 > 应用管理 > 位置上报 > 自启动：开启\n" +
                       "2. 设置 > 应用 > 应用管理 > 位置上报 > 后台活动：允许\n" +
                       "3. 设置 > 电池 > 应用启动管理 > 位置上报：手动管理，全部开启\n" +
                       "4. 设置 > 隐私 > 定位服务 > 位置上报：始终允许\n" +
                       "5. 设置 > 通知和状态栏 > 位置上报：允许通知\n\n" +
                       "⚠️ 防骗提醒：本应用完全免费开源，请勿相信任何付费版本或收费服务！";
                       
            case XIAOMI:
                return "📱 检测到小米/红米设备\n" +
                       "💡 建议开启以下权限以确保后台运行：\n" +
                       "1. 设置 > 应用管理 > 位置上报 > 自启动：允许\n" +
                       "2. 设置 > 应用管理 > 位置上报 > 后台弹出界面：允许\n" +
                       "3. 设置 > 电池与性能 > 应用配置 > 位置上报：无限制\n" +
                       "4. 设置 > 隐私保护 > 定位服务 > 位置上报：始终允许\n" +
                       "5. 设置 > 通知管理 > 位置上报：允许通知\n\n" +
                       "⚠️ 防骗提醒：本应用完全免费开源，请勿相信任何付费版本或收费服务！";
                       
            case OPPO:
                return "📱 检测到OPPO设备\n" +
                       "💡 建议开启以下权限以确保后台运行：\n" +
                       "1. 设置 > 应用管理 > 位置上报 > 自启动：允许\n" +
                       "2. 设置 > 应用管理 > 位置上报 > 后台运行：允许\n" +
                       "3. 设置 > 电池 > 应用耗电管理 > 位置上报：允许后台运行\n" +
                       "4. 设置 > 隐私 > 定位服务 > 位置上报：始终允许\n" +
                       "5. 设置 > 通知与状态栏 > 位置上报：允许通知\n\n" +
                       "⚠️ 防骗提醒：本应用完全免费开源，请勿相信任何付费版本或收费服务！";
                       
            case VIVO:
                return "📱 检测到vivo设备\n" +
                       "💡 建议开启以下权限以确保后台运行：\n" +
                       "1. 设置 > 应用管理 > 位置上报 > 自启动：允许\n" +
                       "2. 设置 > 应用管理 > 位置上报 > 后台运行：允许\n" +
                       "3. 设置 > 电池 > 后台高耗电应用 > 位置上报：允许\n" +
                       "4. 设置 > 隐私与安全 > 定位服务 > 位置上报：始终允许\n" +
                       "5. 设置 > 通知与状态栏 > 位置上报：允许通知\n\n" +
                       "⚠️ 防骗提醒：本应用完全免费开源，请勿相信任何付费版本或收费服务！";
                       
            case SAMSUNG:
                return "📱 检测到三星设备\n" +
                       "💡 建议开启以下权限以确保后台运行：\n" +
                       "1. 设置 > 应用 > 位置上报 > 电池 > 不受限制\n" +
                       "2. 设置 > 应用 > 位置上报 > 权限 > 后台活动：允许\n" +
                       "3. 设置 > 电池 > 后台使用限制 > 位置上报：不受限制\n" +
                       "4. 设置 > 隐私 > 定位服务 > 位置上报：始终允许\n" +
                       "5. 设置 > 通知 > 位置上报：允许通知\n\n" +
                       "⚠️ 防骗提醒：本应用完全免费开源，请勿相信任何付费版本或收费服务！";
                       
            case ONEPLUS:
                return "📱 检测到一加设备\n" +
                       "💡 建议开启以下权限以确保后台运行：\n" +
                       "1. 设置 > 应用管理 > 位置上报 > 自启动：允许\n" +
                       "2. 设置 > 应用管理 > 位置上报 > 后台运行：允许\n" +
                       "3. 设置 > 电池 > 应用耗电管理 > 位置上报：无限制\n" +
                       "4. 设置 > 隐私 > 定位服务 > 位置上报：始终允许\n" +
                       "5. 设置 > 通知和状态栏 > 位置上报：允许通知\n\n" +
                       "⚠️ 防骗提醒：本应用完全免费开源，请勿相信任何付费版本或收费服务！";
                       
            case MEIZU:
                return "📱 检测到魅族设备\n" +
                       "💡 建议开启以下权限以确保后台运行：\n" +
                       "1. 设置 > 应用管理 > 位置上报 > 自启动：允许\n" +
                       "2. 设置 > 应用管理 > 位置上报 > 后台运行：允许\n" +
                       "3. 设置 > 电池 > 应用管理 > 位置上报：无限制\n" +
                       "4. 设置 > 隐私 > 定位服务 > 位置上报：始终允许\n" +
                       "5. 设置 > 通知和状态栏 > 位置上报：允许通知\n\n" +
                       "⚠️ 防骗提醒：本应用完全免费开源，请勿相信任何付费版本或收费服务！";
                       
            default:
                return "📱 检测到其他品牌设备\n" +
                       "💡 建议开启以下权限以确保后台运行：\n" +
                       "1. 设置 > 应用管理 > 位置上报 > 自启动：允许\n" +
                       "2. 设置 > 应用管理 > 位置上报 > 后台运行：允许\n" +
                       "3. 设置 > 电池 > 应用耗电管理 > 位置上报：无限制\n" +
                       "4. 设置 > 隐私 > 定位服务 > 位置上报：始终允许\n" +
                       "5. 设置 > 通知管理 > 位置上报：允许通知\n\n" +
                       "⚠️ 防骗提醒：本应用完全免费开源，请勿相信任何付费版本或收费服务！";
        }
    }
    
    /**
     * 应用设备优化策略
     */
    public static void applyDeviceOptimization(Context context) {
        DeviceBrand brand = detectDeviceBrand();
        Log.d(TAG, "检测到设备品牌: " + brand.getDisplayName());
        
        // 根据品牌应用不同的优化策略
        switch (brand) {
            case HONOR:
                // 使用对话框引导用户设置，而不是直接打开设置页面
                Log.d(TAG, "检测到荣耀/华为设备，将通过对话框引导用户设置");
                // 注意：实际的对话框显示在MainActivity中处理
                break;
            case XIAOMI:
                applyXiaomiOptimization(context);
                break;
            case OPPO:
                applyOppoOptimization(context);
                break;
            case VIVO:
                applyVivoOptimization(context);
                break;
            default:
                applyDefaultOptimization(context);
                break;
        }
    }
    
    /**
     * 小米设备优化
     */
    private static void applyXiaomiOptimization(Context context) {
        try {
            Log.d(TAG, "应用小米设备优化策略");
            // 可在此处补充新版本对小米自启动、后台弹窗、电池优化等引导
        } catch (Exception e) {
            Log.e(TAG, "小米设备优化失败", e);
        }
    }
    
    /**
     * OPPO设备优化
     */
    private static void applyOppoOptimization(Context context) {
        try {
            Log.d(TAG, "应用OPPO设备优化策略");
            // 可在此处补充新版本对OPPO自启动、后台弹窗、电池优化等引导
        } catch (Exception e) {
            Log.e(TAG, "OPPO设备优化失败", e);
        }
    }
    
    /**
     * vivo设备优化
     */
    private static void applyVivoOptimization(Context context) {
        try {
            Log.d(TAG, "应用vivo设备优化策略");
            // 可在此处补充新版本对vivo自启动、后台弹窗、电池优化等引导
        } catch (Exception e) {
            Log.e(TAG, "vivo设备优化失败", e);
        }
    }
    
    /**
     * 默认设备优化
     */
    private static void applyDefaultOptimization(Context context) {
        try {
            Log.d(TAG, "应用默认设备优化策略");
            // 通用优化逻辑
        } catch (Exception e) {
            Log.e(TAG, "默认设备优化失败", e);
        }
    }
} 