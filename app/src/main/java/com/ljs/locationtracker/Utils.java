package com.ljs.locationtracker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

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
            Notification.Builder builder = null;
            Notification notification = null;
            if (android.os.Build.VERSION.SDK_INT >= 26) {
                if (null == notificationManager) {
                    notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (notificationManager == null) {
                        Log.e("Utils", "无法获取NotificationManager");
                        return null;
                    }
                }
                String channelId = mContext.getPackageName();
                if (!isCreatedChannel) {
                    try {
                        NotificationChannel notificationChannel = new NotificationChannel(channelId,
                                NOTIFICATION_CHANNEL_NAME,
                                NotificationManager.IMPORTANCE_DEFAULT);
                        notificationChannel.enableLights(true);
                        notificationChannel.setLightColor(Color.BLUE);
                        notificationChannel.setShowBadge(true);
                        notificationManager.createNotificationChannel(notificationChannel);
                        isCreatedChannel = true;
                    } catch (Exception e) {
                        Log.e("Utils", "创建通知渠道失败", e);
                        return null;
                    }
                }
                builder = new Notification.Builder(mContext, channelId);
            } else {
                builder = new Notification.Builder(mContext);
            }
            // 构建通知内容 - 仅显示电量、位置、时间
            String currentTimeStr = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date());
            String contentText = String.format("电量:%d%% | 位置:%.4f,%.4f | 时间:%s", 
                batteryLevel, latitude, longitude, currentTimeStr);
            builder.setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentText(contentText)
                    .setWhen(System.currentTimeMillis());
            if (Build.VERSION.SDK_INT >= 16) {
                notification = builder.build();
            } else {
                notification = builder.getNotification();
            }
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
}
