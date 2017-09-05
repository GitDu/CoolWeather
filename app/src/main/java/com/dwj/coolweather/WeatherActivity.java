package com.dwj.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.dwj.coolweather.db.County;
import com.dwj.coolweather.gson.Weather;
import com.dwj.coolweather.util.DataUtil;
import com.dwj.coolweather.util.HttpUtil;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private static final String TAG = "WeatherActivity";
    private static final String BING_ICON = "http://guolin.tech/api/bing_pic";
    private static final String BING = "bing";
    public static final String LAST_COUNTY_NAME = "lastCountyName";
    private boolean mInflate = false;
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
    private ImageView mBackground;
    private DrawerLayout mDraw;
    private ImageView mImage;
    private SwipeRefreshLayout mSwap;
    private ArrayList<ViewHolder> holders = new ArrayList<ViewHolder>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        mWeatherUrl = getIntent().getStringExtra("weatherUrl");
        fitStatusBar();
        initView();
        initData();
        initBackground();
        mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDraw.openDrawer(GravityCompat.START);
            }
        });
    }

    public DrawerLayout getDrawLayout() {
        if (mDraw != null) {
            return mDraw;
        }
        return null;
    }

    //仅仅更新数据
    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        //从侧划页跳转过来
        mWeatherUrl = getIntent().getStringExtra("weatherUrl");
        initData();
        super.onNewIntent(intent);
    }

//    private void refreshViews() {
//        if (mForeCastLayout != null) {
//            int childCount = mForeCastLayout.getChildCount();
//            Log.d(TAG, "onNewIntent: " + childCount);
//            for (int count = mForeCastLayout.getChildCount(); count > 0; count--) {
//                View childAt = mForeCastLayout.getChildAt(count);
//                if (childAt != null && childAt instanceof LinearLayout) {
//                    Log.d(TAG, "onNewIntent: " + childAt.getClass().getSimpleName());
//                    mForeCastLayout.removeView(childAt);
//                }
//            }
//        }
//    }

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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, " 必应背景图片获取失败", Toast.LENGTH_SHORT).show();
                        mSwap.setRefreshing(false);
                    }
                });
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
                        mSwap.setRefreshing(false);
                    }
                });
            }
        });

    }

    private void initView() {
        mDraw = ((DrawerLayout) findViewById(R.id.draw_layout));
        mImage = ((ImageView) findViewById(R.id.home));
        mBackground = ((ImageView) findViewById(R.id.bing_background));
        mCountyName = ((TextView) findViewById(R.id.county_name));
        mUpdateDate = ((TextView) findViewById(R.id.update_time));
        mTemNow = ((TextView) findViewById(R.id.tem_now));
        mWeatherNow = ((TextView) findViewById(R.id.weather_now));
        mAirState = ((TextView) findViewById(R.id.air_state));
        mAqiNumber = ((TextView) findViewById(R.id.aqi_number));
        mForeCastLayout = ((LinearLayout) findViewById(R.id.foreCast));
        mPmNumber = ((TextView) findViewById(R.id.pm_number));
        mAir_suggestion = ((TextView) findViewById(R.id.air_suggestion));
        mCom_suggestion = ((TextView) findViewById(R.id.comf_suggestion));
        mCw_suggestion = ((TextView) findViewById(R.id.cw_suggestion));
        mSwap = ((SwipeRefreshLayout) findViewById(R.id.swipe_layout));
        mSwap.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
        //增加手动更新的功能 再次访问服务器
        mSwap.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mWeatherUrl == null) {
                    //其中有view是动态加载的 需要再次清除view
                    String string = mDefaultPreferences.getString(MainActivity.WEATHER_DATA, null);
                    Weather weather = DataUtil.handleWeatherData(string);
                    if (weather != null) {
                        Weather.BasicBean basic = weather.getBasic();
                        if (basic != null) {
                            String city = basic.getCity();
                            List<County> counties = DataSupport.where("countyName= ?", city).find(County.class);
                            mWeatherUrl = "http://guolin.tech/api/weather?cityid=" +
                                    counties.get(0).getWeatherId() + "&key=a51a0df067ff48fd98aa27b1324594e7";

                        }
                    }
                }
                //访问网络请求天气数据
                queryDataFromService();
                //访问必应图片
                loadIconFromService();
            }
        });
        mDefaultPreferences = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
    }

    private void initData() {
        String string = mDefaultPreferences.getString(MainActivity.WEATHER_DATA, null);
        if (string == null || string.length() == 0) {
            //更新的时候直接再次初始化这个控件
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
                        mSwap.setRefreshing(false);
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
                            mSwap.setRefreshing(false);
                        }
                    });
                }
            }
        });
    }

    private void updateData(Weather weather) {
        Weather.BasicBean basic = weather.getBasic();
        if (basic != null) {
            String titleName = basic.getCity();
            mDefaultPreferences.edit().putString(LAST_COUNTY_NAME, titleName).apply();
            mCountyName.setText(titleName);
            String updateTime = basic.getUpdate().getLoc().split(" ")[1];
            mUpdateDate.setText(updateTime);
        }

        Weather.NowBean now = weather.getNow();
        if (now != null) {
            String weather_now = now.getCond().getTxt();
            String tmp = now.getTmp() + getString(R.string.tem);
            mTemNow.setText(tmp);
            mWeatherNow.setText(weather_now);
        }

        List<Weather.DailyForecastBean> daily_forecast = weather.getDaily_forecast();
        if (daily_forecast != null && daily_forecast.size() > 0) {
            int index = 0;
            for (Weather.DailyForecastBean dailyForecastBean : daily_forecast) {
                String dataNumber = dailyForecastBean.getDate();
                SimpleDateFormat myFmt2 = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
                String chineseWeek = TimeUtils.getChineseWeek(dataNumber, myFmt2);
                String txt_d = dailyForecastBean.getCond().getTxt_d();
                if (txt_d.contains("/")) {
                    txt_d = txt_d.split("/")[0];
                }
                String low = dailyForecastBean.getTmp().getMin() + getString(R.string.tem);
                String high = dailyForecastBean.getTmp().getMax() + getString(R.string.tem);
                //采用listView的缓存机制 只是缓存控件的引用 更新数据的时候只是更新控件内容
                //不是暴力的删掉控件再加载 提供用户体验
                ViewHolder viewHolder = null;
                if (!mInflate) {
                    viewHolder = new ViewHolder();
                    View inflate = LayoutInflater.from(WeatherActivity.this).inflate(R.layout.froecast_item, mForeCastLayout, false);
                    viewHolder.date = (TextView) inflate.findViewById(R.id.date);
                    viewHolder.wea = (TextView) inflate.findViewById(R.id.weather);
                    viewHolder.high_tem = (TextView) inflate.findViewById(R.id.high_tem);
                    viewHolder.low_tem = (TextView) inflate.findViewById(R.id.low_tem);
                    holders.add(viewHolder);
                    mForeCastLayout.addView(inflate);
                } else {
                    viewHolder = holders.get(index++);
                }
                viewHolder.date.setText(chineseWeek);
                viewHolder.wea.setText(txt_d);
                viewHolder.high_tem.setText(high);
                viewHolder.low_tem.setText(low);
            }
            mInflate = true;
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

    private static class ViewHolder {
        TextView date;
        TextView wea;
        TextView high_tem;
        TextView low_tem;
    }
}
