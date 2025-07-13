package com.hx.cationtracke;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import org.json.JSONObject;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import java.util.ArrayList;
import com.hx.cationtracke.DataBaseOpenHelper;

public class LocationWorker extends Worker {
    public LocationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    private boolean performSingleWebhookRequest(Context context, String data, String url) {
        try {
            okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                    .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .retryOnConnectionFailure(false)
                    .build();
            okhttp3.MediaType mediaType = okhttp3.MediaType.parse("application/json");
            okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, data);
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("User-Agent", "LocationTracker/1.0")
                    .build();
            try (okhttp3.Response response = client.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (Exception e) {
            return false;
        }
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Context context = getApplicationContext();
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            Location location = null;
            if (locationManager != null) {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
                if (location == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
            }
            JSONObject jsonObject = new JSONObject();
            if (location != null) {
                jsonObject.put("latitude", location.getLatitude());
                jsonObject.put("longitude", location.getLongitude());
                jsonObject.put("altitude", location.getAltitude());
                jsonObject.put("accuracy", location.getAccuracy());
                jsonObject.put("timestamp", System.currentTimeMillis());
            }
            String data = jsonObject.toString();
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            boolean hasNetwork = netInfo != null && netInfo.isConnected();
            DataBaseOpenHelper dbHelper = new DataBaseOpenHelper(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            String webhookUrl = context.getSharedPreferences("config", Context.MODE_PRIVATE).getString("webhook_url", "");
            if (hasNetwork && webhookUrl != null && !webhookUrl.isEmpty()) {
                // 有网时，批量读取并上报所有缓存
                Cursor cursor = db.rawQuery("SELECT id, data FROM location_cache ORDER BY id ASC LIMIT 10", null);
                ArrayList<Long> successRowIds = new ArrayList<>();
                while (cursor.moveToNext()) {
                    long rowId = cursor.getLong(0);
                    String cachedData = cursor.getString(1);
                    if (performSingleWebhookRequest(context, cachedData, webhookUrl)) {
                        successRowIds.add(rowId);
                    }
                }
                cursor.close();
                // 只删除成功上报的数据
                for (Long rowId : successRowIds) {
                    db.delete("location_cache", "id=?", new String[]{String.valueOf(rowId)});
                }
                // 上报本次数据
                if (performSingleWebhookRequest(context, data, webhookUrl)) {
                    // 不缓存
                } else {
                    // 失败则缓存，保证最多10条
                    db.execSQL("INSERT INTO location_cache (data, created_at) VALUES(?, ?)", new Object[]{data, System.currentTimeMillis()});
                    db.execSQL("DELETE FROM location_cache WHERE id NOT IN (SELECT id FROM location_cache ORDER BY id DESC LIMIT 10)");
                }
            } else {
                // 无网时缓存，保证最多10条
                db.execSQL("INSERT INTO location_cache (data, created_at) VALUES(?, ?)", new Object[]{data, System.currentTimeMillis()});
                db.execSQL("DELETE FROM location_cache WHERE id NOT IN (SELECT id FROM location_cache ORDER BY id DESC LIMIT 10)");
            }
            db.close();
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure();
        }
    }
} 