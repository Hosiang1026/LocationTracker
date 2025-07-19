package com.ljs.locationtracker;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 全局Application类，用于处理全局异常和初始化
 */
public class LocationTrackerApplication extends Application {
    private static final String TAG = "LocationTrackerApp";
    private static LocationTrackerApplication instance;
    private Thread.UncaughtExceptionHandler defaultHandler;
    private boolean isCrashHandling = false;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        
        // 强制设置应用级别的兼容主题，确保在任何情况下都不会崩溃
        forceSetCompatibleTheme();
        
        // 执行全局主题安全检查
        performGlobalThemeSafetyCheck();
        
        // 设置全局异常处理器
        setupGlobalExceptionHandler();
        
        // 清理旧的崩溃日志
        cleanOldCrashLogs();
        
        Log.d(TAG, "LocationTrackerApplication 初始化完成");
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        
        // 在Application的attachBaseContext中强制设置兼容主题
        try {
            Log.d(TAG, "Application attachBaseContext - 强制设置兼容主题");
            setTheme(R.style.Theme_AppCompat_DayNight_NoActionBar);
        } catch (Exception e) {
            Log.e(TAG, "Application attachBaseContext设置主题失败", e);
            try {
                setTheme(R.style.Theme_AppCompat_NoActionBar);
            } catch (Exception ex) {
                Log.e(TAG, "Application attachBaseContext设置基础主题也失败", ex);
                try {
                    setTheme(R.style.Theme_AppCompat);
                } catch (Exception exc) {
                    Log.e(TAG, "Application attachBaseContext所有主题设置都失败", exc);
                }
            }
        }
    }

    /**
     * 强制设置兼容主题，确保在任何情况下都不会出现主题兼容性问题
     */
    private void forceSetCompatibleTheme() {
        try {
            // 智能主题设置：先获取系统状态，再设置最合适的主题
            setThemeIntelligently();
        } catch (Exception e) {
            Log.e(TAG, "智能主题设置失败，使用兜底方案", e);
            // 兜底方案：强制设置最兼容的主题
            setFallbackTheme();
        }
    }
    
    /**
     * 智能主题设置：先获取系统主题和状态，再设置最合适的App主题
     */
    private void setThemeIntelligently() {
        try {
            Log.d(TAG, "开始智能主题设置");
            
            // 1. 获取系统当前主题信息
            String systemTheme = getSystemThemeInfo();
            Log.d(TAG, "系统主题信息: " + systemTheme);
            
            // 2. 检查系统状态（省电模式、夜间模式等）
            boolean isPowerSaveMode = isPowerSaveModeEnabled();
            boolean isNightMode = isNightModeEnabled();
            boolean isHighContrast = isHighContrastEnabled();
            
            Log.d(TAG, String.format("系统状态 - 省电模式: %s, 夜间模式: %s, 高对比度: %s", 
                isPowerSaveMode, isNightMode, isHighContrast));
            
            // 3. 根据系统状态选择最合适的主题
            int selectedTheme = selectOptimalTheme(isPowerSaveMode, isNightMode, isHighContrast);
            
            // 4. 应用选定的主题
            setTheme(selectedTheme);
            
            // 5. 验证主题是否设置成功
            if (validateThemeSet(selectedTheme)) {
                Log.d(TAG, "智能主题设置完成，使用主题ID: " + selectedTheme);
            } else {
                Log.w(TAG, "主题设置可能失败，使用兜底方案");
                setFallbackTheme();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "智能主题设置过程中发生异常", e);
            setFallbackTheme();
        }
    }
    
    /**
     * 获取系统主题信息
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
            Log.e(TAG, "获取系统主题信息失败", e);
            return "获取失败";
        }
    }
    
    /**
     * 检查是否启用省电模式
     */
    private boolean isPowerSaveModeEnabled() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 21) {
                android.os.PowerManager pm = (android.os.PowerManager) getSystemService(android.content.Context.POWER_SERVICE);
                return pm != null && pm.isPowerSaveMode();
            }
        } catch (Exception e) {
            Log.e(TAG, "检查省电模式失败", e);
        }
        return false;
    }
    
    /**
     * 检查是否启用夜间模式
     */
    private boolean isNightModeEnabled() {
        try {
            android.content.res.Configuration config = getResources().getConfiguration();
            int nightMode = config.uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
            return nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        } catch (Exception e) {
            Log.e(TAG, "检查夜间模式失败", e);
        }
        return false;
    }
    
    /**
     * 检查是否启用高对比度模式
     */
    private boolean isHighContrastEnabled() {
        try {
            android.content.res.Configuration config = getResources().getConfiguration();
            // 修复：正确检测高对比度模式
            // 高对比度模式通常通过辅助功能服务检测，这里简化处理
            // 如果系统配置显示为特殊模式，则认为是高对比度
            int uiModeType = config.uiMode & android.content.res.Configuration.UI_MODE_TYPE_MASK;
            return uiModeType != android.content.res.Configuration.UI_MODE_TYPE_NORMAL;
        } catch (Exception e) {
            Log.e(TAG, "检查高对比度模式失败", e);
        }
        return false;
    }
    
    /**
     * 根据系统状态选择最优主题
     */
    private int selectOptimalTheme(boolean isPowerSaveMode, boolean isNightMode, boolean isHighContrast) {
        try {
            // 优先级1：省电模式 - 使用最基础的AppCompat主题
            if (isPowerSaveMode) {
                Log.d(TAG, "省电模式检测，使用最基础AppCompat主题");
                return R.style.Theme_AppCompat;
            }
            
            // 优先级2：夜间模式 - 使用DayNight主题
            if (isNightMode) {
                Log.d(TAG, "夜间模式检测，使用DayNight主题");
                return R.style.Theme_AppCompat_DayNight_NoActionBar;
            }
            
            // 优先级3：高对比度模式 - 使用基础AppCompat主题
            if (isHighContrast) {
                Log.d(TAG, "高对比度模式检测，使用基础AppCompat主题");
                return R.style.Theme_AppCompat_NoActionBar;
            }
            
            // 优先级4：正常模式 - 使用标准DayNight主题
            Log.d(TAG, "正常模式，使用标准DayNight主题");
            return R.style.Theme_AppCompat_DayNight_NoActionBar;
            
        } catch (Exception e) {
            Log.e(TAG, "选择主题失败，使用兜底主题", e);
            return R.style.Theme_AppCompat;
        }
    }
    
    /**
     * 兜底主题设置
     */
    private void setFallbackTheme() {
        try {
            Log.w(TAG, "使用兜底主题设置");
            
            // 尝试设置最兼容的主题
            setTheme(R.style.Theme_AppCompat_DayNight_NoActionBar);
            Log.d(TAG, "成功设置Theme_AppCompat_DayNight_NoActionBar主题");
        } catch (Exception e) {
            Log.e(TAG, "设置Theme_AppCompat_DayNight_NoActionBar主题失败", e);
            try {
                // 如果失败，尝试基础AppCompat主题
                setTheme(R.style.Theme_AppCompat_NoActionBar);
                Log.d(TAG, "成功设置Theme_AppCompat_NoActionBar主题");
            } catch (Exception ex) {
                Log.e(TAG, "设置Theme_AppCompat_NoActionBar主题失败", ex);
                try {
                    // 最后尝试最基础的AppCompat主题
                    setTheme(R.style.Theme_AppCompat);
                    Log.d(TAG, "成功设置Theme_AppCompat主题");
                } catch (Exception exc) {
                    Log.e(TAG, "所有主题设置都失败，应用可能崩溃", exc);
                    // 这里不抛出异常，让应用继续运行
                }
            }
        }
    }

    /**
     * 设置全局异常处理器
     */
    private void setupGlobalExceptionHandler() {
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                if (isCrashHandling) {
                    // 如果已经在处理崩溃，直接使用默认处理器
                    if (defaultHandler != null) {
                        defaultHandler.uncaughtException(thread, ex);
                    }
                    return;
                }
                
                isCrashHandling = true;
                
                try {
                    // 记录崩溃信息
                    handleCrash(thread, ex);
                } catch (Exception e) {
                    Log.e(TAG, "处理崩溃时发生异常", e);
                } finally {
                    // 延迟1秒后退出，确保日志写入完成
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (defaultHandler != null) {
                                defaultHandler.uncaughtException(thread, ex);
                            } else {
                                System.exit(1);
                            }
                        }
                    }, 1000);
                }
            }
        });
    }

    /**
     * 处理崩溃
     */
    private void handleCrash(Thread thread, Throwable ex) {
        try {
            // 记录到系统日志
            Log.e(TAG, "应用崩溃", ex);
            
            // 记录到文件
            writeCrashLogToFile(thread, ex);
            
            // 显示崩溃提示（在主线程中）
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    try {
                        Toast.makeText(getApplicationContext(), 
                            "应用遇到问题，即将退出", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e(TAG, "显示崩溃提示失败", e);
                    }
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "处理崩溃失败", e);
        }
    }

    /**
     * 将崩溃日志写入文件
     */
    private void writeCrashLogToFile(Thread thread, Throwable ex) {
        try {
            // 尝试多个存储位置
            File logDir = null;
            
            // 首先尝试外部存储
            File externalDir = getExternalFilesDir(null);
            if (externalDir != null) {
                logDir = new File(externalDir, "crash_logs");
                if (!logDir.exists()) {
                    logDir.mkdirs();
                }
            }
            
            // 如果外部存储不可用，尝试内部存储
            if (logDir == null || !logDir.exists() || !logDir.canWrite()) {
                File internalDir = getFilesDir();
                if (internalDir != null) {
                    logDir = new File(internalDir, "crash_logs");
                    if (!logDir.exists()) {
                        logDir.mkdirs();
                    }
                }
            }
            
            // 如果仍然无法创建目录，使用缓存目录
            if (logDir == null || !logDir.exists() || !logDir.canWrite()) {
                File cacheDir = getCacheDir();
                if (cacheDir != null) {
                    logDir = new File(cacheDir, "crash_logs");
                    if (!logDir.exists()) {
                        logDir.mkdirs();
                    }
                }
            }
            
            if (logDir == null || !logDir.exists() || !logDir.canWrite()) {
                Log.e(TAG, "无法创建崩溃日志目录");
                return;
            }
            
            // 创建日志文件
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault());
            String fileName = "crash_" + sdf.format(new Date()) + ".log";
            File logFile = new File(logDir, fileName);
            
            // 使用同步写入，确保数据不丢失
            FileWriter writer = new FileWriter(logFile);
            PrintWriter pw = new PrintWriter(writer);
            
            pw.println("=== LocationTracker 崩溃日志 ===");
            pw.println("时间: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
            pw.println("线程: " + thread.getName() + " (ID: " + thread.getId() + ")");
            pw.println("异常类型: " + ex.getClass().getSimpleName());
            pw.println("异常消息: " + ex.getMessage());
            pw.println();
            pw.println("堆栈跟踪:");
            ex.printStackTrace(pw);
            pw.println();
            
            // 添加系统信息
            pw.println("=== 系统信息 ===");
            pw.println("Android版本: " + android.os.Build.VERSION.RELEASE);
            pw.println("API级别: " + android.os.Build.VERSION.SDK_INT);
            pw.println("设备型号: " + android.os.Build.MODEL);
            pw.println("制造商: " + android.os.Build.MANUFACTURER);
            pw.println("品牌: " + android.os.Build.BRAND);
            pw.println("硬件: " + android.os.Build.HARDWARE);
            pw.println("产品: " + android.os.Build.PRODUCT);
            pw.println();
            
            // 添加内存信息
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            long maxMemory = runtime.maxMemory();
            
            pw.println("=== 内存信息 ===");
            pw.println("总内存: " + (totalMemory / 1024 / 1024) + " MB");
            pw.println("已用内存: " + (usedMemory / 1024 / 1024) + " MB");
            pw.println("空闲内存: " + (freeMemory / 1024 / 1024) + " MB");
            pw.println("最大内存: " + (maxMemory / 1024 / 1024) + " MB");
            pw.println("内存使用率: " + (usedMemory * 100 / totalMemory) + "%");
            pw.println();
            
            // 强制刷新并关闭
            pw.flush();
            pw.close();
            writer.flush();
            writer.close();
            
            Log.d(TAG, "崩溃日志已保存到: " + logFile.getAbsolutePath());
            
        } catch (IOException e) {
            Log.e(TAG, "写入崩溃日志失败", e);
        } catch (Exception e) {
            Log.e(TAG, "处理崩溃日志时发生异常", e);
        }
    }

    /**
     * 获取Application实例
     */
    public static LocationTrackerApplication getInstance() {
        return instance;
    }

    /**
     * 获取应用上下文
     */
    public static Context getAppContext() {
        return instance.getApplicationContext();
    }

    /**
     * 记录自定义崩溃信息
     */
    public static void logCrash(String message, Throwable ex) {
        if (instance != null) {
            Log.e(TAG, "自定义崩溃: " + message, ex);
            instance.writeCrashLogToFile(Thread.currentThread(), ex);
        }
    }

    /**
     * 记录错误信息
     */
    public static void logError(String message, Throwable ex) {
        Log.e(TAG, message, ex);
    }

    /**
     * 记录警告信息
     */
    public static void logWarning(String message) {
        Log.w(TAG, message);
    }

    /**
     * 记录信息
     */
    public static void logInfo(String message) {
        Log.i(TAG, message);
    }

    /**
     * 清理旧的崩溃日志
     */
    private void cleanOldCrashLogs() {
        try {
            // 延迟5秒执行清理，避免清理刚生成的崩溃日志
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    performCleanOldCrashLogs();
                }
            }, 5000);
            
        } catch (Exception e) {
            Log.e(TAG, "设置清理任务失败", e);
        }
    }
    
    /**
     * 执行清理旧的崩溃日志
     */
    private void performCleanOldCrashLogs() {
        try {
            // 尝试多个存储位置
            File logDir = null;
            
            // 首先尝试外部存储
            File externalDir = getExternalFilesDir(null);
            if (externalDir != null) {
                logDir = new File(externalDir, "crash_logs");
            }
            
            // 如果外部存储不可用，尝试内部存储
            if (logDir == null || !logDir.exists()) {
                File internalDir = getFilesDir();
                if (internalDir != null) {
                    logDir = new File(internalDir, "crash_logs");
                }
            }
            
            // 如果仍然无法找到目录，使用缓存目录
            if (logDir == null || !logDir.exists()) {
                File cacheDir = getCacheDir();
                if (cacheDir != null) {
                    logDir = new File(cacheDir, "crash_logs");
                }
            }
            
            if (logDir == null || !logDir.exists()) {
                return;
            }
            
            File[] logFiles = logDir.listFiles();
            if (logFiles == null || logFiles.length == 0) {
                return;
            }
            
            long currentTime = System.currentTimeMillis();
            long maxAge = 7 * 24 * 60 * 60 * 1000L; // 7天
            int maxFiles = 20; // 最多保留20个文件
            int deletedCount = 0;
            
            // 按修改时间排序，最新的在前面
            java.util.Arrays.sort(logFiles, new java.util.Comparator<File>() {
                @Override
                public int compare(File f1, File f2) {
                    return Long.compare(f2.lastModified(), f1.lastModified());
                }
            });
            
            for (int i = 0; i < logFiles.length; i++) {
                File file = logFiles[i];
                
                // 检查文件年龄（排除最近1分钟内的文件）
                long fileAge = currentTime - file.lastModified();
                boolean isOldFile = fileAge > maxAge;
                boolean isRecentFile = fileAge < 60000; // 1分钟内的文件不删除
                
                // 检查文件数量
                boolean isExcessFile = i >= maxFiles;
                
                if ((isOldFile || isExcessFile) && !isRecentFile) {
                    if (file.delete()) {
                        deletedCount++;
                        Log.d(TAG, "已删除旧崩溃日志: " + file.getName());
                    } else {
                        Log.w(TAG, "删除崩溃日志失败: " + file.getName());
                    }
                }
            }
            
            if (deletedCount > 0) {
                Log.i(TAG, "清理崩溃日志完成，删除了 " + deletedCount + " 个文件");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "清理崩溃日志失败", e);
        }
    }

    /**
     * 手动清理崩溃日志
     */
    public static void cleanCrashLogs() {
        if (instance != null) {
            instance.performCleanOldCrashLogs();
        }
    }
    
    /**
     * 立即清理所有崩溃日志（用于手动清理）
     */
    public static void cleanAllCrashLogs() {
        if (instance == null) {
            return;
        }
        
        try {
            // 尝试多个存储位置
            File logDir = null;
            
            // 首先尝试外部存储
            File externalDir = instance.getExternalFilesDir(null);
            if (externalDir != null) {
                logDir = new File(externalDir, "crash_logs");
            }
            
            // 如果外部存储不可用，尝试内部存储
            if (logDir == null || !logDir.exists()) {
                File internalDir = instance.getFilesDir();
                if (internalDir != null) {
                    logDir = new File(internalDir, "crash_logs");
                }
            }
            
            // 如果仍然无法找到目录，使用缓存目录
            if (logDir == null || !logDir.exists()) {
                File cacheDir = instance.getCacheDir();
                if (cacheDir != null) {
                    logDir = new File(cacheDir, "crash_logs");
                }
            }
            
            if (logDir == null || !logDir.exists()) {
                return;
            }
            
            File[] logFiles = logDir.listFiles();
            if (logFiles == null || logFiles.length == 0) {
                return;
            }
            
            int deletedCount = 0;
            
            for (File file : logFiles) {
                if (file.isFile()) {
                    if (file.delete()) {
                        deletedCount++;
                        Log.d(TAG, "已删除崩溃日志: " + file.getName());
                    } else {
                        Log.w(TAG, "删除崩溃日志失败: " + file.getName());
                    }
                }
            }
            
            if (deletedCount > 0) {
                Log.i(TAG, "清理崩溃日志完成，删除了 " + deletedCount + " 个文件");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "清理崩溃日志失败", e);
        }
    }
    
    /**
     * 获取崩溃日志文件数量
     */
    public static int getCrashLogCount() {
        if (instance == null) {
            return 0;
        }
        
        try {
            // 尝试多个存储位置
            File logDir = null;
            
            // 首先尝试外部存储
            File externalDir = instance.getExternalFilesDir(null);
            if (externalDir != null) {
                logDir = new File(externalDir, "crash_logs");
            }
            
            // 如果外部存储不可用，尝试内部存储
            if (logDir == null || !logDir.exists()) {
                File internalDir = instance.getFilesDir();
                if (internalDir != null) {
                    logDir = new File(internalDir, "crash_logs");
                }
            }
            
            // 如果仍然无法找到目录，使用缓存目录
            if (logDir == null || !logDir.exists()) {
                File cacheDir = instance.getCacheDir();
                if (cacheDir != null) {
                    logDir = new File(cacheDir, "crash_logs");
                }
            }
            
            if (logDir == null || !logDir.exists()) {
                return 0;
            }
            
            File[] logFiles = logDir.listFiles();
            return logFiles != null ? logFiles.length : 0;
        } catch (Exception e) {
            Log.e(TAG, "获取崩溃日志数量失败", e);
            return 0;
        }
    }
    
    /**
     * 获取崩溃日志目录大小（MB）
     */
    public static long getCrashLogSize() {
        if (instance == null) {
            return 0;
        }
        
        try {
            // 尝试多个存储位置
            File logDir = null;
            
            // 首先尝试外部存储
            File externalDir = instance.getExternalFilesDir(null);
            if (externalDir != null) {
                logDir = new File(externalDir, "crash_logs");
            }
            
            // 如果外部存储不可用，尝试内部存储
            if (logDir == null || !logDir.exists()) {
                File internalDir = instance.getFilesDir();
                if (internalDir != null) {
                    logDir = new File(internalDir, "crash_logs");
                }
            }
            
            // 如果仍然无法找到目录，使用缓存目录
            if (logDir == null || !logDir.exists()) {
                File cacheDir = instance.getCacheDir();
                if (cacheDir != null) {
                    logDir = new File(cacheDir, "crash_logs");
                }
            }
            
            if (logDir == null || !logDir.exists()) {
                return 0;
            }
            
            return getDirectorySize(logDir) / (1024 * 1024); // 转换为MB
        } catch (Exception e) {
            Log.e(TAG, "获取崩溃日志大小失败", e);
            return 0;
        }
    }
    
    /**
     * 获取崩溃日志存储位置
     */
    public static String getCrashLogDirectory() {
        if (instance == null) {
            return "未知";
        }
        
        try {
            // 尝试多个存储位置
            File logDir = null;
            String location = "未知";
            
            // 首先尝试外部存储
            File externalDir = instance.getExternalFilesDir(null);
            if (externalDir != null) {
                logDir = new File(externalDir, "crash_logs");
                if (logDir.exists()) {
                    location = "外部存储";
                }
            }
            
            // 如果外部存储不可用，尝试内部存储
            if (logDir == null || !logDir.exists()) {
                File internalDir = instance.getFilesDir();
                if (internalDir != null) {
                    logDir = new File(internalDir, "crash_logs");
                    if (logDir.exists()) {
                        location = "内部存储";
                    }
                }
            }
            
            // 如果仍然无法找到目录，使用缓存目录
            if (logDir == null || !logDir.exists()) {
                File cacheDir = instance.getCacheDir();
                if (cacheDir != null) {
                    logDir = new File(cacheDir, "crash_logs");
                    if (logDir.exists()) {
                        location = "缓存目录";
                    }
                }
            }
            
            if (logDir != null && logDir.exists()) {
                return location + ": " + logDir.getAbsolutePath();
            } else {
                return "未找到崩溃日志目录";
            }
            
        } catch (Exception e) {
            Log.e(TAG, "获取崩溃日志目录失败", e);
            return "获取失败";
        }
    }
    
    /**
     * 获取最近3个崩溃日志内容摘要
     */
    public static String getRecentCrashLogsSummary() {
        if (instance == null) {
            return "无法获取崩溃日志";
        }
        
        try {
            // 尝试多个存储位置
            File logDir = null;
            
            // 首先尝试外部存储
            File externalDir = instance.getExternalFilesDir(null);
            if (externalDir != null) {
                logDir = new File(externalDir, "crash_logs");
            }
            
            // 如果外部存储不可用，尝试内部存储
            if (logDir == null || !logDir.exists()) {
                File internalDir = instance.getFilesDir();
                if (internalDir != null) {
                    logDir = new File(internalDir, "crash_logs");
                }
            }
            
            // 如果仍然无法找到目录，使用缓存目录
            if (logDir == null || !logDir.exists()) {
                File cacheDir = instance.getCacheDir();
                if (cacheDir != null) {
                    logDir = new File(cacheDir, "crash_logs");
                }
            }
            
            if (logDir == null || !logDir.exists()) {
                return "未找到崩溃日志文件";
            }
            
            File[] logFiles = logDir.listFiles();
            if (logFiles == null || logFiles.length == 0) {
                return "暂无崩溃日志";
            }
            
            // 按修改时间排序，最新的在前面
            java.util.Arrays.sort(logFiles, new java.util.Comparator<File>() {
                @Override
                public int compare(File f1, File f2) {
                    return Long.compare(f2.lastModified(), f1.lastModified());
                }
            });
            
            StringBuilder summary = new StringBuilder();
            int count = 0;
            int maxFiles = Math.min(3, logFiles.length);
            
            for (int i = 0; i < maxFiles; i++) {
                File file = logFiles[i];
                try {
                    java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file));
                    String line;
                    StringBuilder fileContent = new StringBuilder();
                    
                    // 读取文件内容
                    while ((line = reader.readLine()) != null) {
                        fileContent.append(line).append("\n");
                    }
                    reader.close();
                    
                    String content = fileContent.toString();
                    
                    // 提取关键信息
                    String fileName = file.getName();
                    String timeInfo = "";
                    String exceptionInfo = "";
                    
                    // 查找时间信息
                    java.util.regex.Pattern timePattern = java.util.regex.Pattern.compile("时间: (.+)");
                    java.util.regex.Matcher timeMatcher = timePattern.matcher(content);
                    if (timeMatcher.find()) {
                        timeInfo = timeMatcher.group(1);
                    }
                    
                    // 查找异常信息
                    java.util.regex.Pattern exceptionPattern = java.util.regex.Pattern.compile("异常类型: (.+)\\n异常消息: (.+)");
                    java.util.regex.Matcher exceptionMatcher = exceptionPattern.matcher(content);
                    if (exceptionMatcher.find()) {
                        exceptionInfo = exceptionMatcher.group(1) + ": " + exceptionMatcher.group(2);
                    }
                    
                    // 构建摘要
                    summary.append("【崩溃 ").append(count + 1).append("】\n");
                    summary.append("文件: ").append(fileName).append("\n");
                    summary.append("时间: ").append(timeInfo).append("\n");
                    summary.append("异常: ").append(exceptionInfo).append("\n\n");
                    
                    count++;
                    
                } catch (Exception e) {
                    Log.e(TAG, "读取崩溃日志文件失败: " + file.getName(), e);
                }
            }
            
            if (count == 0) {
                return "无法读取崩溃日志内容";
            }
            
            String result = summary.toString().trim();
            // 限制在300字以内
            if (result.length() > 300) {
                result = result.substring(0, 297) + "...";
            }
            
            return result;
            
        } catch (Exception e) {
            Log.e(TAG, "获取崩溃日志摘要失败", e);
            return "获取崩溃日志摘要失败";
        }
    }
    
    /**
     * 计算目录大小
     */
    private static long getDirectorySize(File dir) {
        long size = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    size += file.length();
                } else if (file.isDirectory()) {
                    size += getDirectorySize(file);
                }
            }
        }
        return size;
    }

    /**
     * 公共方法：检查和修复主题兼容性问题
     * 供其他Activity在onCreate开始时调用
     */
    public static void ensureCompatibleTheme(android.app.Activity activity) {
        if (activity == null) return;
        
        try {
            Log.d(TAG, "开始Activity智能主题设置");
            
            // 1. 获取Activity当前主题信息
            android.content.res.Resources.Theme currentTheme = activity.getTheme();
            String themeInfo = currentTheme != null ? currentTheme.toString() : "null";
            Log.d(TAG, "Activity当前主题: " + themeInfo);
            
            // 2. 检查系统状态
            boolean isPowerSaveMode = checkPowerSaveMode(activity);
            boolean isNightMode = checkNightMode(activity);
            boolean isHighContrast = checkHighContrast(activity);
            
            Log.d(TAG, String.format("Activity系统状态 - 省电模式: %s, 夜间模式: %s, 高对比度: %s", 
                isPowerSaveMode, isNightMode, isHighContrast));
            
            // 3. 根据系统状态选择最合适的主题
            int selectedTheme = selectOptimalThemeForActivity(isPowerSaveMode, isNightMode, isHighContrast);
            
            // 4. 应用选定的主题
            activity.setTheme(selectedTheme);
            
            // 5. 验证主题是否设置成功
            if (validateActivityThemeSet(activity, selectedTheme)) {
                Log.d(TAG, "Activity智能主题设置完成，使用主题ID: " + selectedTheme);
            } else {
                Log.w(TAG, "Activity主题设置可能失败，使用兜底方案");
                setFallbackThemeForActivity(activity);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Activity智能主题设置失败，使用兜底方案", e);
            setFallbackThemeForActivity(activity);
        }
    }
    
    /**
     * 验证Activity主题是否设置成功
     */
    private static boolean validateActivityThemeSet(android.app.Activity activity, int expectedThemeId) {
        try {
            // 获取当前主题
            android.content.res.Resources.Theme currentTheme = activity.getTheme();
            if (currentTheme == null) {
                Log.w(TAG, "Activity主题验证失败：当前主题为null");
                return false;
            }
            
            // 检查主题是否包含AppCompat相关标识
            String themeString = currentTheme.toString();
            if (!themeString.contains("AppCompat")) {
                Log.w(TAG, "Activity主题验证失败：当前主题不是AppCompat主题: " + themeString);
                return false;
            }
            
            Log.d(TAG, "Activity主题验证成功: " + themeString);
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Activity主题验证过程中发生异常", e);
            return false;
        }
    }

    /**
     * 检查Activity的省电模式状态
     */
    private static boolean checkPowerSaveMode(android.app.Activity activity) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 21) {
                android.os.PowerManager pm = (android.os.PowerManager) activity.getSystemService(android.content.Context.POWER_SERVICE);
                return pm != null && pm.isPowerSaveMode();
            }
        } catch (Exception e) {
            Log.e(TAG, "检查Activity省电模式失败", e);
        }
        return false;
    }
    
    /**
     * 检查Activity的夜间模式状态
     */
    private static boolean checkNightMode(android.app.Activity activity) {
        try {
            android.content.res.Configuration config = activity.getResources().getConfiguration();
            int nightMode = config.uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
            return nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        } catch (Exception e) {
            Log.e(TAG, "检查Activity夜间模式失败", e);
        }
        return false;
    }
    
    /**
     * 检查Activity的高对比度模式状态
     */
    private static boolean checkHighContrast(android.app.Activity activity) {
        try {
            android.content.res.Configuration config = activity.getResources().getConfiguration();
            // 修复：正确检测高对比度模式
            int uiModeType = config.uiMode & android.content.res.Configuration.UI_MODE_TYPE_MASK;
            return uiModeType != android.content.res.Configuration.UI_MODE_TYPE_NORMAL;
        } catch (Exception e) {
            Log.e(TAG, "检查Activity高对比度模式失败", e);
        }
        return false;
    }
    
    /**
     * 为Activity选择最优主题
     */
    private static int selectOptimalThemeForActivity(boolean isPowerSaveMode, boolean isNightMode, boolean isHighContrast) {
        try {
            // 优先级1：省电模式 - 使用最基础的AppCompat主题
            if (isPowerSaveMode) {
                Log.d(TAG, "Activity省电模式检测，使用最基础AppCompat主题");
                return R.style.Theme_AppCompat;
            }
            
            // 优先级2：夜间模式 - 使用DayNight主题
            if (isNightMode) {
                Log.d(TAG, "Activity夜间模式检测，使用DayNight主题");
                return R.style.Theme_AppCompat_DayNight_NoActionBar;
            }
            
            // 优先级3：高对比度模式 - 使用基础AppCompat主题
            if (isHighContrast) {
                Log.d(TAG, "Activity高对比度模式检测，使用基础AppCompat主题");
                return R.style.Theme_AppCompat_NoActionBar;
            }
            
            // 优先级4：正常模式 - 使用标准DayNight主题
            Log.d(TAG, "Activity正常模式，使用标准DayNight主题");
            return R.style.Theme_AppCompat_DayNight_NoActionBar;
            
        } catch (Exception e) {
            Log.e(TAG, "Activity选择主题失败，使用兜底主题", e);
            return R.style.Theme_AppCompat;
        }
    }
    
    /**
     * Activity兜底主题设置
     */
    private static void setFallbackThemeForActivity(android.app.Activity activity) {
        try {
            Log.w(TAG, "Activity使用兜底主题设置");
            
            // 尝试设置最兼容的主题
            activity.setTheme(R.style.Theme_AppCompat_DayNight_NoActionBar);
            Log.d(TAG, "Activity成功设置Theme_AppCompat_DayNight_NoActionBar主题");
        } catch (Exception e) {
            Log.e(TAG, "Activity设置Theme_AppCompat_DayNight_NoActionBar主题失败", e);
            try {
                // 如果失败，尝试基础AppCompat主题
                activity.setTheme(R.style.Theme_AppCompat_NoActionBar);
                Log.d(TAG, "Activity成功设置Theme_AppCompat_NoActionBar主题");
            } catch (Exception ex) {
                Log.e(TAG, "Activity设置Theme_AppCompat_NoActionBar主题失败", ex);
                try {
                    // 最后尝试最基础的AppCompat主题
                    activity.setTheme(R.style.Theme_AppCompat);
                    Log.d(TAG, "Activity成功设置Theme_AppCompat主题");
                } catch (Exception exc) {
                    Log.e(TAG, "Activity所有主题设置都失败", exc);
                }
            }
        }
    }

    /**
     * 全局主题安全检查方法
     * 在应用启动时和Activity创建时调用
     */
    public static void performGlobalThemeSafetyCheck() {
        try {
            Log.d(TAG, "执行全局主题安全检查");
            
            // 检查当前应用主题
            android.content.res.Resources.Theme appTheme = getInstance().getTheme();
            if (appTheme == null) {
                Log.w(TAG, "应用主题为null，将在Activity创建时设置");
            } else {
                Log.d(TAG, "应用主题: " + appTheme.toString());
            }
            
            // 检查系统是否处于省电模式
            boolean isPowerSaveMode = false;
            if (android.os.Build.VERSION.SDK_INT >= 21) {
                android.os.PowerManager pm = (android.os.PowerManager) getInstance().getSystemService(android.content.Context.POWER_SERVICE);
                if (pm != null) {
                    isPowerSaveMode = pm.isPowerSaveMode();
                    if (isPowerSaveMode) {
                        Log.d(TAG, "检测到省电模式，Activity将使用最兼容的主题");
                    }
                }
            }
            
            // 检查夜间模式
            boolean isNightMode = false;
            try {
                int nightMode = getInstance().getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
                isNightMode = (nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES);
                if (isNightMode) {
                    Log.d(TAG, "检测到夜间模式，Activity将使用兼容的夜间主题");
                }
            } catch (Exception e) {
                Log.e(TAG, "检查夜间模式失败", e);
            }
            
            // 检查高对比度模式
            boolean isHighContrast = false;
            try {
                android.content.res.Configuration config = getInstance().getResources().getConfiguration();
                int uiModeType = config.uiMode & android.content.res.Configuration.UI_MODE_TYPE_MASK;
                isHighContrast = (uiModeType != android.content.res.Configuration.UI_MODE_TYPE_NORMAL);
                if (isHighContrast) {
                    Log.d(TAG, "检测到高对比度模式，Activity将使用基础主题");
                }
            } catch (Exception e) {
                Log.e(TAG, "检查高对比度模式失败", e);
            }
            
            Log.d(TAG, String.format("全局主题安全检查完成 - 省电模式: %s, 夜间模式: %s, 高对比度: %s", 
                isPowerSaveMode, isNightMode, isHighContrast));
            
        } catch (Exception e) {
            Log.e(TAG, "全局主题安全检查失败", e);
            // 不抛出异常，让应用继续运行
        }
    }

    /**
     * 验证主题是否设置成功
     */
    private boolean validateThemeSet(int expectedThemeId) {
        try {
            // 获取当前主题
            android.content.res.Resources.Theme currentTheme = getTheme();
            if (currentTheme == null) {
                Log.w(TAG, "主题验证失败：当前主题为null");
                return false;
            }
            
            // 检查主题是否包含AppCompat相关标识
            String themeString = currentTheme.toString();
            if (!themeString.contains("AppCompat")) {
                Log.w(TAG, "主题验证失败：当前主题不是AppCompat主题: " + themeString);
                return false;
            }
            
            Log.d(TAG, "主题验证成功: " + themeString);
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "主题验证过程中发生异常", e);
            return false;
        }
    }
} 