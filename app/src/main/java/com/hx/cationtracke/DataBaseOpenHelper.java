package com.hx.cationtracke;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class DataBaseOpenHelper extends SQLiteOpenHelper {
    private String TAG="LJSTAG";
    public DataBaseOpenHelper(@Nullable Context context) {
        super(context, Contant.DATABASE, null, Contant.VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate: create");
        // 配置表
        String sqlConfig = "CREATE TABLE IF NOT EXISTS config (id INTEGER PRIMARY KEY AUTOINCREMENT, url VARCHAR, time INTEGER, notification_enable INTEGER DEFAULT 0)";
        db.execSQL(sqlConfig);
        // 定位缓存表
        String sqlCache = "CREATE TABLE IF NOT EXISTS location_cache (id INTEGER PRIMARY KEY AUTOINCREMENT, data TEXT, created_at INTEGER)";
        db.execSQL(sqlCache);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 兼容老表结构
        if (oldVersion < 2) {
            // 老版本升级到新结构
            db.execSQL("CREATE TABLE IF NOT EXISTS config (id INTEGER PRIMARY KEY AUTOINCREMENT, url VARCHAR, time INTEGER, notification_enable INTEGER DEFAULT 0)");
            db.execSQL("CREATE TABLE IF NOT EXISTS location_cache (id INTEGER PRIMARY KEY AUTOINCREMENT, data TEXT, created_at INTEGER)");
        }
        // 其他升级逻辑可按需补充
    }
}
