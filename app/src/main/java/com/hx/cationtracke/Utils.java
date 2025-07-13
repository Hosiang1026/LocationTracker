package com.hx.cationtracke;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import android.Manifest;
import androidx.core.app.NotificationCompat;
import com.hx.cationtracke.LocationTrackerApplication;

public class Utils {
    public static final int NOTIFY_ID = 2001;
    private static final String NOTIFICATION_CHANNEL_NAME = "AMapBackgroundLocation";
    private static NotificationManager notificationManager = null;
    private static boolean isCreatedChannel = false;

    /**
     * 创建一个通知栏，API>=26时才有效
     * @param context
     * @param batteryLevel 电池电量
     * @param latitude 纬度
     * @param longitude 经度
     * @param reportCount 上报次数
     * @param timeSinceLastReport 距离上次上报时间（秒）
     * @return
     */
    public static Notification buildNotification(Context context, int batteryLevel, double latitude, double longitude, int reportCount, long timeSinceLastReport) {
        try {
            if (context == null) {
                Log.e("Utils", "Context为null，无法创建通知");
                return null;
            }
            
            Context mContext = context.getApplicationContext();
            NotificationCompat.Builder builder = null;
            Notification notification = null;
            
            String channelId = mContext.getPackageName();
            if (android.os.Build.VERSION.SDK_INT >= 26) {
                //Android O上对Notification进行了修改，如果设置的targetSDKVersion>=26建议使用此种方式创建通知栏
                if (null == notificationManager) {
                    notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (notificationManager == null) {
                        Log.e("Utils", "无法获取NotificationManager");
                        return null;
                    }
                }
                if (!isCreatedChannel) {
                    try {
                        NotificationChannel notificationChannel = new NotificationChannel(channelId,
                                NOTIFICATION_CHANNEL_NAME,
                                NotificationManager.IMPORTANCE_DEFAULT);
                        notificationChannel.enableLights(true);//是否在桌面icon右上角展示小圆点
                        notificationChannel.setLightColor(Color.BLUE); //小圆点颜色
                        notificationChannel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知
                        notificationManager.createNotificationChannel(notificationChannel);
                        isCreatedChannel = true;
                    } catch (Exception e) {
                        Log.e("Utils", "创建通知渠道失败", e);
                        return null;
                    }
                }
                builder = new NotificationCompat.Builder(mContext, channelId);
            } else {
                // Android 4.0-7.1 使用兼容库
                builder = new NotificationCompat.Builder(mContext);
            }

            // 构建通知内容 - 显示当前上报时间
            String currentTimeStr = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date());
            String contentText = String.format("电量:%d%% | 位置:%.4f,%.4f | 当前上报时间:%s", 
                batteryLevel, latitude, longitude, currentTimeStr);

            // 使用drawable资源作为通知图标，确保在Android 8.0+上正确显示
            builder.setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentText(contentText)  // 只设置内容，不设置标题
                    .setWhen(System.currentTimeMillis());

            notification = builder.build();
            return notification;
        } catch (Exception e) {
            Log.e("Utils", "创建通知失败", e);
            LocationTrackerApplication.logError("创建通知失败", e);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取app的名称
     * @param context
     * @return
     */
    public static String getAppName(Context context) {
        String appName = "";
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
            int labelRes = packageInfo.applicationInfo.labelRes;
            appName =  context.getResources().getString(labelRes);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return appName;
    }

    /**
     * 检查并申请后台定位权限（Android 10+）
     */
    public static boolean checkAndRequestBackgroundLocation(Context context) {
        if (Build.VERSION.SDK_INT >= 29) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) 
                    != PackageManager.PERMISSION_GRANTED) {
                // 显示后台定位权限说明对话框
                showBackgroundLocationDialog(context);
                return false;
            }
        }
        return true;
    }
    
    /**
     * 显示后台定位权限说明对话框
     */
    private static void showBackgroundLocationDialog(Context context) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("后台定位权限");
            builder.setMessage("为了在应用后台运行时也能获取位置信息，需要授予后台定位权限。\n\n" +
                    "请在接下来的系统设置中，选择\"始终允许\"以启用后台定位功能。");
            builder.setPositiveButton("去设置", (dialog, which) -> {
                // 跳转到应用设置页面
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                intent.setData(uri);
                context.startActivity(intent);
            });
            builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
            builder.setCancelable(false);
            builder.show();
        } catch (Exception e) {
            Log.e("Utils", "显示后台定位权限对话框失败", e);
        }
    }
    
    /**
     * 检查所有位置相关权限是否已授予
     */
    public static boolean checkAllLocationPermissions(Context context) {
        boolean hasBasicLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED;
        boolean hasCoarseLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED;
        boolean hasBackgroundLocation = true; // 默认true，避免Android 10以下版本的问题
        
        if (Build.VERSION.SDK_INT >= 29) {
            hasBackgroundLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) 
                    == PackageManager.PERMISSION_GRANTED;
        }
        
        return hasBasicLocation && hasCoarseLocation && hasBackgroundLocation;
    }
}
