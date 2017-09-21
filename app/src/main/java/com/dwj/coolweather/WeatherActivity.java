package com.dwj.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.TimeUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.Target;
import com.dwj.coolweather.db.County;
import com.dwj.coolweather.gson.Weather;
import com.dwj.coolweather.util.DataUtil;
import com.dwj.coolweather.util.HttpUtil;
import com.dwj.coolweather.util.ToolUtil;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import okhttp3.Call;
import okhttp3.Response;

import static com.dwj.coolweather.Contacts.BING;
import static com.dwj.coolweather.Contacts.BING_ICON;
import static com.dwj.coolweather.Contacts.LAST_COUNTY_NAME;
import static com.dwj.coolweather.Contacts.WEATHER_DATA;

public class WeatherActivity extends AppCompatActivity {

    private static final String TAG = "WeatherActivity";
    private String mWeatherUrl;
    private DrawerLayout mDraw;
    private updateWeatherUrlListener mListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        Log.d(TAG, "onCreate: ");
        mWeatherUrl = getIntent().getStringExtra("weatherUrl");
        mDraw = ((DrawerLayout) findViewById(R.id.draw_layout));
        ToolUtil.fitStatusBar(WeatherActivity.this);

        FragmentManager supportFragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        WeatherFragment weatherFragment = WeatherFragment.newInstance(mWeatherUrl);
        fragmentTransaction.replace(R.id.container, weatherFragment).commit();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();
    }

    public DrawerLayout getDrawLayout() {
        if (mDraw != null) {
            return mDraw;
        }
        return null;
    }

    public void openDrawLayout() {
        if (mDraw != null) {
            mDraw.openDrawer(GravityCompat.START);
        }
    }

    //仅仅更新数据
    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        //从侧划页跳转过来
        mWeatherUrl = getIntent().getStringExtra("weatherUrl");
        mListener.callBackup(mWeatherUrl);
        super.onNewIntent(intent);
    }

    public void registerListener(updateWeatherUrlListener listener) {
        this.mListener = listener;
    }

    public interface updateWeatherUrlListener {
        void callBackup(String string);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mListener != null) {
            mListener = null;
        }
    }
}
