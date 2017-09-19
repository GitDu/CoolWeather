package com.dwj.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.dwj.coolweather.util.DataUtil;
import com.dwj.coolweather.util.HttpUtil;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

import static com.dwj.coolweather.Contacts.BING;
import static com.dwj.coolweather.Contacts.BING_ICON;
import static com.dwj.coolweather.Contacts.CHOSE_INDEX;
import static com.dwj.coolweather.Contacts.WEATHER_DATA;

public class AutoUpdateService extends Service {
    private static final String TAG = "AutoUpdateService";
    private String mWeatherUrlPath;
    private SharedPreferences mShare;
    private int mDefaultTime = 4 * 60 * 60 * 1000;
    private AlarmManager mAlarm;
    private PendingIntent mIntent;

    public AutoUpdateService() {

    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: ");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        //设置计时任务 自动更新天气数据
        int index = intent.getIntExtra(CHOSE_INDEX, -1);
        if (index != -1) {
            switch (index) {
                case 0:
                    mDefaultTime = 4 * 60 * 60 * 1000;
                    break;
                case 1:
                    mDefaultTime = 6 * 60 * 60 * 1000;
                    break;
                case 2:
                    mDefaultTime = 8 * 60 * 60 * 1000;
                    break;
                default:
                    throw new IllegalArgumentException(" intent send wrong ");
            }
        }
        mAlarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent selfIntent = new Intent(AutoUpdateService.this, AutoUpdateService.class);
        mIntent = PendingIntent.getService(AutoUpdateService.this, 0, selfIntent, 0);
        //RTC_WAKEUP 对应System.currentTimeMillis(1970.1.1)
        //ELAPSED_REALTIME_WAKEUP 对应于 SystemClock.elapsedRealtime() (系统开机时间)
        mAlarm.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + mDefaultTime, mIntent);
        mWeatherUrlPath = DataUtil.getWeatherUrlPath(AutoUpdateService.this);
        if (mWeatherUrlPath != null) {
            updateSharedPreferences();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateSharedPreferences() {
        HttpUtil.handleHttpRequest(mWeatherUrlPath, new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                if (mShare == null) {
                    mShare = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this);
                }
                mShare.edit().putString(WEATHER_DATA, string).apply();
            }
        });
        HttpUtil.handleHttpRequest(BING_ICON, new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                if (mShare == null) {
                    mShare = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this);
                }
                mShare.edit().putString(BING, string).apply();
                //同时将图片下载到本地
                Glide.with(AutoUpdateService.this).load(string).downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        if (mAlarm != null && mIntent != null) {
            mAlarm.cancel(mIntent);
            mAlarm = null;
        }
        super.onDestroy();
    }
}
