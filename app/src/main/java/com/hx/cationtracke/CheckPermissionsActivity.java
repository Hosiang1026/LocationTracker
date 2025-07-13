package com.hx.cationtracke;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class CheckPermissionsActivity extends AppCompatActivity {
    /**
     * 需要进行检测的权限数组
     */
    protected String[] needPermissions = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE
    };

    private static final int PERMISSON_REQUESTCODE = 0;

    /**
     * 判断是否需要检测，防止不停的弹框
     */
    private boolean isNeedCheck = true;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        
        // 在attachBaseContext中强制设置兼容主题，确保在Activity创建的最早阶段就生效
        try {
            Log.d("CheckPermissionsActivity", "attachBaseContext - 强制设置兼容主题");
            setTheme(R.style.Theme_AppCompat_DayNight_NoActionBar);
        } catch (Exception e) {
            Log.e("CheckPermissionsActivity", "attachBaseContext设置主题失败", e);
            try {
                setTheme(R.style.Theme_AppCompat_NoActionBar);
            } catch (Exception ex) {
                Log.e("CheckPermissionsActivity", "attachBaseContext设置基础主题也失败", ex);
                try {
                    setTheme(R.style.Theme_AppCompat);
                } catch (Exception exc) {
                    Log.e("CheckPermissionsActivity", "attachBaseContext所有主题设置都失败", exc);
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 强制设置兼容主题，确保在省电模式下也能正常工作
        forceSetCompatibleTheme();
        
        // 额外的安全措施：通过Application检查主题兼容性
        LocationTrackerApplication.ensureCompatibleTheme(this);
    }
    
    /**
     * 强制设置兼容主题，确保在任何情况下都不会出现主题兼容性问题
     */
    private void forceSetCompatibleTheme() {
        try {
            // 智能主题设置：先获取系统状态，再设置最合适的主题
            setThemeIntelligently();
        } catch (Exception e) {
            Log.e("CheckPermissionsActivity", "智能主题设置失败，使用兜底方案", e);
            // 兜底方案：强制设置最兼容的主题
            setFallbackTheme();
        }
    }
    
    /**
     * CheckPermissionsActivity智能主题设置：先获取系统主题和状态，再设置最合适的App主题
     */
    private void setThemeIntelligently() {
        try {
            Log.d("CheckPermissionsActivity", "开始智能主题设置");
            
            // 1. 获取系统当前主题信息
            String systemTheme = getSystemThemeInfo();
            Log.d("CheckPermissionsActivity", "系统主题信息: " + systemTheme);
            
            // 2. 检查系统状态（省电模式、夜间模式等）
            boolean isPowerSaveMode = isPowerSaveModeEnabled();
            boolean isNightMode = isNightModeEnabled();
            boolean isHighContrast = isHighContrastEnabled();
            
            Log.d("CheckPermissionsActivity", String.format("系统状态 - 省电模式: %s, 夜间模式: %s, 高对比度: %s", 
                isPowerSaveMode, isNightMode, isHighContrast));
            
            // 3. 根据系统状态选择最合适的主题
            int selectedTheme = selectOptimalTheme(isPowerSaveMode, isNightMode, isHighContrast);
            
            // 4. 应用选定的主题
            setTheme(selectedTheme);
            
            Log.d("CheckPermissionsActivity", "智能主题设置完成，使用主题ID: " + selectedTheme);
            
        } catch (Exception e) {
            Log.e("CheckPermissionsActivity", "智能主题设置过程中发生异常", e);
            setFallbackTheme();
        }
    }
    
    /**
     * 获取CheckPermissionsActivity系统主题信息
     */
    private String getSystemThemeInfo() {
        try {
            android.content.res.Resources.Theme systemTheme = getTheme();
            if (systemTheme != null) {
                return systemTheme.toString();
            }
            
            // 获取系统配置信息
            android.content.res.Configuration config = getResources().getConfiguration();
            StringBuilder info = new StringBuilder();
            info.append("API Level: ").append(android.os.Build.VERSION.SDK_INT);
            info.append(", UI Mode: ").append(config.uiMode);
            info.append(", Screen Layout: ").append(config.screenLayout);
            info.append(", Orientation: ").append(config.orientation);
            
            return info.toString();
            
        } catch (Exception e) {
            Log.e("CheckPermissionsActivity", "获取系统主题信息失败", e);
            return "获取失败";
        }
    }
    
    /**
     * 检查CheckPermissionsActivity是否启用省电模式
     */
    private boolean isPowerSaveModeEnabled() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 21) {
                android.os.PowerManager pm = (android.os.PowerManager) getSystemService(android.content.Context.POWER_SERVICE);
                return pm != null && pm.isPowerSaveMode();
            }
        } catch (Exception e) {
            Log.e("CheckPermissionsActivity", "检查省电模式失败", e);
        }
        return false;
    }
    
    /**
     * 检查CheckPermissionsActivity是否启用夜间模式
     */
    private boolean isNightModeEnabled() {
        try {
            android.content.res.Configuration config = getResources().getConfiguration();
            int nightMode = config.uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
            return nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        } catch (Exception e) {
            Log.e("CheckPermissionsActivity", "检查夜间模式失败", e);
        }
        return false;
    }
    
    /**
     * 检查CheckPermissionsActivity是否启用高对比度模式
     */
    private boolean isHighContrastEnabled() {
        try {
            android.content.res.Configuration config = getResources().getConfiguration();
            int uiModeType = config.uiMode & android.content.res.Configuration.UI_MODE_TYPE_MASK;
            return uiModeType != android.content.res.Configuration.UI_MODE_TYPE_NORMAL;
        } catch (Exception e) {
            Log.e("CheckPermissionsActivity", "检查高对比度模式失败", e);
        }
        return false;
    }
    
    /**
     * CheckPermissionsActivity根据系统状态选择最优主题
     */
    private int selectOptimalTheme(boolean isPowerSaveMode, boolean isNightMode, boolean isHighContrast) {
        try {
            // 优先级1：省电模式 - 使用最基础的AppCompat主题
            if (isPowerSaveMode) {
                Log.d("CheckPermissionsActivity", "省电模式检测，使用最基础AppCompat主题");
                return R.style.Theme_AppCompat;
            }
            
            // 优先级2：夜间模式 - 使用DayNight主题
            if (isNightMode) {
                Log.d("CheckPermissionsActivity", "夜间模式检测，使用DayNight主题");
                return R.style.Theme_AppCompat_DayNight_NoActionBar;
            }
            
            // 优先级3：高对比度模式 - 使用基础AppCompat主题
            if (isHighContrast) {
                Log.d("CheckPermissionsActivity", "高对比度模式检测，使用基础AppCompat主题");
                return R.style.Theme_AppCompat_NoActionBar;
            }
            
            // 优先级4：正常模式 - 使用标准DayNight主题
            Log.d("CheckPermissionsActivity", "正常模式，使用标准DayNight主题");
            return R.style.Theme_AppCompat_DayNight_NoActionBar;
            
        } catch (Exception e) {
            Log.e("CheckPermissionsActivity", "选择主题失败，使用兜底主题", e);
            return R.style.Theme_AppCompat;
        }
    }
    
    /**
     * CheckPermissionsActivity兜底主题设置
     */
    private void setFallbackTheme() {
        try {
            Log.w("CheckPermissionsActivity", "使用兜底主题设置");
            
            // 尝试设置最兼容的主题
            setTheme(R.style.Theme_AppCompat_DayNight_NoActionBar);
            Log.d("CheckPermissionsActivity", "成功设置Theme_AppCompat_DayNight_NoActionBar主题");
        } catch (Exception e) {
            Log.e("CheckPermissionsActivity", "设置Theme_AppCompat_DayNight_NoActionBar主题失败", e);
            try {
                // 如果失败，尝试基础AppCompat主题
                setTheme(R.style.Theme_AppCompat_NoActionBar);
                Log.d("CheckPermissionsActivity", "成功设置Theme_AppCompat_NoActionBar主题");
            } catch (Exception ex) {
                Log.e("CheckPermissionsActivity", "设置Theme_AppCompat_NoActionBar主题失败", ex);
                try {
                    // 最后尝试最基础的AppCompat主题
                    setTheme(R.style.Theme_AppCompat);
                    Log.d("CheckPermissionsActivity", "成功设置Theme_AppCompat主题");
                } catch (Exception exc) {
                    Log.e("CheckPermissionsActivity", "所有主题设置都失败，应用可能崩溃", exc);
                    // 这里不抛出异常，让应用继续运行
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= 23
                && getApplicationInfo().targetSdkVersion >= 23) {
            if (isNeedCheck) {
                checkPermissions(needPermissions);
            }
        }
    }

    /**
     *
     * @param permissions
     * @since 2.5.0
     *
     */
    private void checkPermissions(String... permissions) {
        try {
            if (Build.VERSION.SDK_INT >= 23
                    && getApplicationInfo().targetSdkVersion >= 23) {
                List<String> needRequestPermissonList = findDeniedPermissions(permissions);
                if (null != needRequestPermissonList
                        && needRequestPermissonList.size() > 0) {
                    String[] array = needRequestPermissonList.toArray(new String[needRequestPermissonList.size()]);
                    try {
                        Method method = getClass().getMethod("requestPermissions", new Class[]{String[].class,
                                int.class});
                        method.invoke(this, array, PERMISSON_REQUESTCODE);
                    } catch (Exception e) {
                        Log.e("CheckPermissionsActivity", "请求权限失败", e);
                        // 如果反射失败，尝试直接调用
                        if (Build.VERSION.SDK_INT >= 23) {
                            requestPermissions(array, PERMISSON_REQUESTCODE);
                        }
                    }
                }
            }
        } catch (Throwable e) {
            Log.e("CheckPermissionsActivity", "检查权限失败", e);
            LocationTrackerApplication.logError("检查权限失败", e);
        }
    }

    /**
     * 获取权限集中需要申请权限的列表
     *
     * @param permissions
     * @return
     * @since 2.5.0
     *
     */
    private List<String> findDeniedPermissions(String[] permissions) {
        List<String> needRequestPermissonList = new ArrayList<String>();
        if (Build.VERSION.SDK_INT >= 23
                && getApplicationInfo().targetSdkVersion >= 23){
            try {
                for (String perm : permissions) {
                    Method checkSelfMethod = getClass().getMethod("checkSelfPermission", String.class);
                    Method shouldShowRequestPermissionRationaleMethod = getClass().getMethod("shouldShowRequestPermissionRationale",
                            String.class);
                    if ((Integer)checkSelfMethod.invoke(this, perm) != PackageManager.PERMISSION_GRANTED
                            || (Boolean)shouldShowRequestPermissionRationaleMethod.invoke(this, perm)) {
                        needRequestPermissonList.add(perm);
                    }
                }
            } catch (Throwable e) {

            }
        }
        return needRequestPermissonList;
    }

    /**
     * 检测是否所有的权限都已经授权
     * @param grantResults
     * @return
     * @since 2.5.0
     *
     */
    private boolean verifyPermissions(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @TargetApi(23)
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] paramArrayOfInt) {
        if (requestCode == PERMISSON_REQUESTCODE) {
            if (!verifyPermissions(paramArrayOfInt)) {
                showMissingPermissionDialog();
                isNeedCheck = false;
            }
        }
    }

    /**
     * 显示提示信息
     *
     * @since 2.5.0
     *
     */
    private void showMissingPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage("当前应用缺少必要权限。\\n\\n请点击\\\"设置\\\"-\\\"权限\\\"-打开所需权限。");

        // 拒绝, 退出应用
        builder.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

        builder.setPositiveButton("设置",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startAppSettings();
                    }
                });

        builder.setCancelable(false);

        builder.show();
    }

    /**
     *  启动应用的设置
     *
     * @since 2.5.0
     *
     */
    private void startAppSettings() {
        Intent intent = new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            this.finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


}