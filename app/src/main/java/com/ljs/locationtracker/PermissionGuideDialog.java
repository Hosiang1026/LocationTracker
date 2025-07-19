package com.ljs.locationtracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

public class PermissionGuideDialog {
    private static final String TAG = "PermissionGuide";

    // 权限申请说明对话框
    public static void showPermissionRequestDialog(Context context) {
        try {
            String title = "权限申请";
            String message = "为了确保位置上报功能正常工作，需要以下权限：\n\n" +
                    "• 定位权限：获取设备位置信息\n" +
                    "• 网络权限：发送位置数据\n" +
                    "• 前台服务权限：保持应用在后台运行\n\n" +
                    "请在接下来的对话框中授予这些权限。";
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(title);
            builder.setMessage(message);
            builder.setPositiveButton("确定", (dialog, which) -> dialog.dismiss());
            builder.setCancelable(false);
            builder.show();
        } catch (Exception e) {
            Log.e(TAG, "显示权限申请对话框失败", e);
        }
    }

    // 权限被拒绝说明对话框
    public static void showPermissionDeniedDialog(Context context) {
        try {
            String title = "权限被拒绝";
            String message = "部分权限被拒绝，可能影响应用功能：\n\n" +
                    "• 定位权限：无法获取位置信息\n" +
                    "• 网络权限：无法发送数据\n" +
                    "• 前台服务权限：应用可能被系统杀死\n\n" +
                    "建议前往设置页面手动开启权限。";
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(title);
            builder.setMessage(message);
            builder.setPositiveButton("前往设置", (dialog, which) -> openDefaultSettings(context));
            builder.setNegativeButton("稍后设置", (dialog, which) -> dialog.dismiss());
            builder.setCancelable(false);
            builder.show();
        } catch (Exception e) {
            Log.e(TAG, "显示权限拒绝对话框失败", e);
        }
    }

    // 设备优化建议对话框
    public static void showDeviceOptimizationDialog(Context context, DeviceOptimizationHelper.DeviceBrand brand) {
        try {
            String title = "设备优化建议";
            String message = DeviceOptimizationHelper.getOptimizationTips(brand);
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(title);
            builder.setMessage(message);
            builder.setPositiveButton("前往设置", (dialog, which) -> openDeviceSettings(context, brand.name()));
            builder.setNegativeButton("稍后设置", (dialog, which) -> dialog.dismiss());
            builder.setNeutralButton("不再提醒", (dialog, which) -> dialog.dismiss());
            builder.setCancelable(false);
            builder.show();
        } catch (Exception e) {
            Log.e(TAG, "显示设备优化对话框失败", e);
        }
    }

    // 检查是否应该显示设备优化建议
    public static boolean shouldShowDeviceOptimization(Context context) {
        try {
            android.content.SharedPreferences prefs = context.getSharedPreferences("device_optimization", Context.MODE_PRIVATE);
            return !prefs.getBoolean("dismissed", false);
        } catch (Exception e) {
            Log.e(TAG, "检查设备优化提醒设置失败", e);
            return true; // 出错时默认显示
        }
    }

    // 根据品牌打开设置页面
    private static void openDeviceSettings(Context context, String deviceBrand) {
        try {
            switch (deviceBrand.toUpperCase()) {
                case "HONOR":
                    openHonorSettings(context); break;
                case "XIAOMI":
                    openXiaomiSettings(context); break;
                case "OPPO":
                    openOppoSettings(context); break;
                case "VIVO":
                    openVivoSettings(context); break;
                case "SAMSUNG":
                    openSamsungSettings(context); break;
                default:
                    openDefaultSettings(context); break;
            }
        } catch (Exception e) {
            Log.e(TAG, "打开设备设置失败", e);
            openDefaultSettings(context);
        }
    }

    private static void openHonorSettings(Context context) {
        try {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setClassName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity");
            if (isIntentAvailable(context, intent)) {
                context.startActivity(intent);
            } else {
                openDefaultSettings(context);
            }
        } catch (Exception e) {
            Log.e(TAG, "打开荣耀设置失败", e);
            openDefaultSettings(context);
        }
    }
    private static void openXiaomiSettings(Context context) {
        try {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity");
            if (isIntentAvailable(context, intent)) {
                context.startActivity(intent);
            } else {
                openDefaultSettings(context);
            }
        } catch (Exception e) {
            Log.e(TAG, "打开小米设置失败", e);
            openDefaultSettings(context);
        }
    }
    private static void openOppoSettings(Context context) {
        try {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setClassName("com.coloros.safecenter", "com.coloros.safecenter.sysfloatwindow.FloatWindowListActivity");
            if (isIntentAvailable(context, intent)) {
                context.startActivity(intent);
            } else {
                openDefaultSettings(context);
            }
        } catch (Exception e) {
            Log.e(TAG, "打开OPPO设置失败", e);
            openDefaultSettings(context);
        }
    }
    private static void openVivoSettings(Context context) {
        try {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setClassName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity");
            if (isIntentAvailable(context, intent)) {
                context.startActivity(intent);
            } else {
                openDefaultSettings(context);
            }
        } catch (Exception e) {
            Log.e(TAG, "打开vivo设置失败", e);
            openDefaultSettings(context);
        }
    }
    private static void openSamsungSettings(Context context) {
        try {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setClassName("com.android.settings", "com.android.settings.applications.InstalledAppDetails");
            intent.putExtra("pkg", context.getPackageName());
            if (isIntentAvailable(context, intent)) {
                context.startActivity(intent);
            } else {
                openDefaultSettings(context);
            }
        } catch (Exception e) {
            Log.e(TAG, "打开三星设置失败", e);
            openDefaultSettings(context);
        }
    }
    private static void openDefaultSettings(Context context) {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "打开默认设置失败", e);
        }
    }
    private static boolean isIntentAvailable(Context context, Intent intent) {
        try {
            PackageManager pm = context.getPackageManager();
            return pm.resolveActivity(intent, 0) != null;
        } catch (Exception e) {
            return false;
        }
    }
} 