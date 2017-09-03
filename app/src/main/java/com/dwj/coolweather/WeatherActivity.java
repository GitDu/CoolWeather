package com.dwj.coolweather;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.TimeUtils;
import com.bumptech.glide.Glide;
import com.dwj.coolweather.gson.Weather;
import com.dwj.coolweather.util.DataUtil;
import com.dwj.coolweather.util.HttpUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private static final String TAG = "WeatherActivity";
    private static final String BING_ICON = "http://guolin.tech/api/bing_pic";
    private static final String BING = "bing";
    private String mWeatherUrl;
    private TextView mCountyName;
    private TextView mUpdateDate;
    private TextView mTemNow;
    private TextView mWeatherNow;
    private LinearLayout mForeCastLayout;
    private TextView mAirState;
    private TextView mAqiNumber;
    private TextView mPmNumber;
    private TextView mAir_suggestion;
    private TextView mCom_suggestion;
    private TextView mCw_suggestion;
    private SharedPreferences mDefaultPreferences;
    private String mTitleName;
    private ImageView mBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        mWeatherUrl = getIntent().getStringExtra("weatherUrl");
        fitStatusBar();
        initView();
        initData();
        initBackground();
    }

    private void fitStatusBar() {
        //在5.0系统上的应用
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }

    private void initBackground() {
        String icon_path = mDefaultPreferences.getString(BING, null);
        if (icon_path != null && icon_path.length() > 0) {
            Glide.with(WeatherActivity.this).load(icon_path).into(mBackground);
        } else {
            loadIconFromService();
        }
    }

    private void loadIconFromService() {
        HttpUtil.handleHttpRequest(BING_ICON, new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(WeatherActivity.this, " 必应背景图片获取失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String icon_path = response.body().string();
                mDefaultPreferences.edit().putString(BING, icon_path).apply();
                Log.d(TAG, "onResponse: icon path" + icon_path);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(icon_path).into(mBackground);
                    }
                });
            }
        });

    }

    private void initView() {
        mBackground = ((ImageView) findViewById(R.id.bing_background));
        mCountyName = ((TextView) findViewById(R.id.county_name));
        mUpdateDate = ((TextView) findViewById(R.id.update_time));
        mTemNow = ((TextView) findViewById(R.id.tem_now));
        mWeatherNow = ((TextView) findViewById(R.id.weather_now));
        mForeCastLayout = ((LinearLayout) findViewById(R.id.foreCast));
        mAirState = ((TextView) findViewById(R.id.air_state));
        mAqiNumber = ((TextView) findViewById(R.id.aqi_number));
        mPmNumber = ((TextView) findViewById(R.id.pm_number));
        mAir_suggestion = ((TextView) findViewById(R.id.air_suggestion));
        mCom_suggestion = ((TextView) findViewById(R.id.comf_suggestion));
        mCw_suggestion = ((TextView) findViewById(R.id.cw_suggestion));
        mDefaultPreferences = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
    }

    private void initData() {
        String string = mDefaultPreferences.getString(MainActivity.WEATHER_DATA, null);
        if (string == null || string.length() == 0) {
            queryDataFromService();
        } else {
            //直接解析生成weather对象
            Weather weather = DataUtil.handleWeatherData(string);
            updateData(weather);

        }
    }

    private void queryDataFromService() {
        HttpUtil.handleHttpRequest(mWeatherUrl, new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, " 天气数据网络请求失败 ", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "run: fail ");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                mDefaultPreferences.edit().putString(MainActivity.WEATHER_DATA, string).apply();
                final Weather weather = DataUtil.handleWeatherData(string);
                if (weather != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateData(weather);
                        }
                    });
                }
            }
        });
    }

    private void updateData(Weather weather) {
        mTitleName = weather.getBasic().getCity();
        mCountyName.setText(mTitleName);
        String updateTime = weather.getBasic().getUpdate().getLoc().split(" ")[1];
        mUpdateDate.setText(updateTime);

        String weather_now = weather.getNow().getCond().getTxt();
        String tmp = weather.getNow().getTmp() + getString(R.string.tem);
        mTemNow.setText(tmp);
        mWeatherNow.setText(weather_now);

        List<Weather.DailyForecastBean> daily_forecast = weather.getDaily_forecast();
        if (daily_forecast != null && daily_forecast.size() > 0) {
            for (Weather.DailyForecastBean dailyForecastBean : daily_forecast) {
                View inflate = LayoutInflater.from(WeatherActivity.this).inflate(R.layout.froecast_item, null);
                TextView date = (TextView) inflate.findViewById(R.id.date);
                TextView wea = (TextView) inflate.findViewById(R.id.weather);
                TextView high_tem = (TextView) inflate.findViewById(R.id.high_tem);
                TextView low_tem = (TextView) inflate.findViewById(R.id.low_tem);
                String dataNumber = dailyForecastBean.getDate();
                SimpleDateFormat myFmt2 = new SimpleDateFormat("yyyy-MM-dd");
                String chineseWeek = TimeUtils.getChineseWeek(dataNumber, myFmt2);
                date.setText(chineseWeek);
                wea.setText(dailyForecastBean.getCond().getTxt_d());
                String low = dailyForecastBean.getTmp().getMin() + getString(R.string.tem);
                String high = dailyForecastBean.getTmp().getMax() + getString(R.string.tem);
                high_tem.setText(high);
                low_tem.setText(low);
                mForeCastLayout.addView(inflate);
            }
        }

        Weather.AqiBean aqi = weather.getAqi();
        if (aqi != null) {
            mAqiNumber.setText(aqi.getCity().getAqi());
            mPmNumber.setText(aqi.getCity().getPm25());
            mAirState.setText(aqi.getCity().getQlty());
        }

        Weather.SuggestionBean suggestion = weather.getSuggestion();
        if (suggestion != null) {
            mAir_suggestion.setText("空气指数: " + suggestion.getAir().getTxt());
            mCom_suggestion.setText("舒适指数: " + suggestion.getComf().getTxt());
            mCw_suggestion.setText("洗车指数: " + suggestion.getCw().getTxt());
        }
    }
}