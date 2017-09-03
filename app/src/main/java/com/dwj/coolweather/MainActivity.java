package com.dwj.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.blankj.utilcode.util.Utils;

/**
 * 获得省 市 县的名称信息 weather id
 * 注册天气查询的key:http://console.heweather.com/register
 * 个人认证key: a51a0df067ff48fd98aa27b1324594e7
 * */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public static final String WEATHER_DATA = "weatherData";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Utils.init(MainActivity.this.getApplication());
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        if (defaultSharedPreferences.getString(WEATHER_DATA, null) != null) {
            startActivity(new Intent(MainActivity.this, WeatherActivity.class));
            finish();
        }
    }
}
