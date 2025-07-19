package com.ljs.locationtracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d("ljstag", "收到广播: " + action);
        
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            // 开机启动
            Log.d("ljstag", "系统启动完成，启动位置服务");
            Intent service = new Intent(context, ltmService.class);
            context.startService(service);
        } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
            // 屏幕熄灭时，确保服务继续运行
            Log.d("ljstag", "屏幕熄灭，确保位置服务继续运行");
            Intent service = new Intent(context, ltmService.class);
            context.startService(service);
        } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
            // 屏幕点亮时，检查并确保服务运行
            Log.d("ljstag", "屏幕点亮，检查位置服务状态");
            Intent service = new Intent(context, ltmService.class);
            context.startService(service);
        } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
            // 用户解锁时，检查并确保服务运行
            Log.d("ljstag", "用户解锁，检查位置服务状态");
            Intent service = new Intent(context, ltmService.class);
            context.startService(service);
        } else if ("com.ljs.ltmservice.start".equals(action)) {
            // 自定义启动广播
            Log.d("ljstag", "收到自定义启动广播，重启位置服务");
            Intent service = new Intent(context, ltmService.class);
            context.startService(service);
        } else if (Intent.ACTION_MY_PACKAGE_REPLACED.equals(action) || 
                   Intent.ACTION_PACKAGE_REPLACED.equals(action) ||
                   Intent.ACTION_PACKAGE_ADDED.equals(action) ||
                   Intent.ACTION_PACKAGE_RESTARTED.equals(action)) {
            // 应用更新或重启时，确保服务运行
            Log.d("ljstag", "应用状态变化，确保位置服务运行");
            Intent service = new Intent(context, ltmService.class);
            context.startService(service);
        }
    }
}