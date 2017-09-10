package com.dwj.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
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

public class WeatherActivity extends AppCompatActivity {

    private static final String TAG = "WeatherActivity";
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
    private List<HourlyForecastItem> items = new ArrayList<HourlyForecastItem>();
    private WeatherAdapter mAdapter;
    private RelativeLayout mFooter;
    private ImageView mChoose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        mWeatherUrl = getIntent().getStringExtra("weatherUrl");
        ToolUtil.fitStatusBar(WeatherActivity.this);
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
    private void initBackground() {
        final String icon_path = mDefaultPreferences.getString(BING, null);
        if (icon_path != null && icon_path.length() > 0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        setBitmap(icon_path);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            //  Glide.with(WeatherActivity.this).load(icon_path).into(mBackground);
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
                //获得Glide下载图片的地址
                try {
                    setBitmap(icon_path);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void setBitmap(String icon_path) throws InterruptedException, ExecutionException {
        FutureTarget<File> fileFutureTarget = Glide.with(WeatherActivity.this).load(icon_path).downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
        final File file = fileFutureTarget.get();
        if (file != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Glide.with(WeatherActivity.this).load(file).into(mBackground);
                    mSwap.setRefreshing(false);
                    //设置footer的背景颜色
                    setFooterBackground(file);
                }
            });
        }
    }

    private void setFooterBackground(File file) {
        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
        // 创建一个 Pallette 对象
        Palette palette = Palette.from(bitmap).generate();
        // 使用 Palette 设置背景颜色
        int color = palette.getDominantColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        mFooter.setBackgroundColor(color);
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
        mFooter = ((RelativeLayout) findViewById(R.id.footer));
        mChoose = (ImageView) findViewById(R.id.choose_weathers);
        mChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(WeatherActivity.this, SelectCityActivity.class));
            }
        });
        RecyclerView recyclerView = ((RecyclerView) findViewById(R.id.hourly_forecast));
        //设置横向的小时预测结果
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(WeatherActivity.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new WeatherAdapter(items);
        recyclerView.setAdapter(mAdapter);
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

        if (weather == null || !weather.getStatus().equals("ok")) {
            return;
        }
        initAdapterData(weather);
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
                //不是暴力的删掉控件再加载,提高用户体验
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

    boolean isBegin = true;

    private void initAdapterData(Weather weather) {
        items.clear();
        Weather.NowBean now = weather.getNow();
        if (now != null) {
            HourlyForecastItem item = new HourlyForecastItem();
            String tmp = now.getTmp();
            String txt = now.getCond().getTxt();
            int i = chooseIcon(txt);
            item.setIconId(i);
            item.setTem(tmp + getResources().getString(R.string.tem));
            item.setHour("现在");
            isBegin = false;
            items.add(item);
        }
        String nowString = TimeUtils.getNowString();
        String[] split = nowString.split(" ");
        String s = split[1];
        String hour = s.split(":")[0];
        String[] strings = new String[]{"大雨", "小雨", "晴", "雷", "多云"};
        Random random = new Random();
        //暂时没有数据
//        List<Weather.HourlyForecastBean> hourly_forecast = weather.getHourly_forecast();
//        Log.d(TAG, "initAdapterData: " + hourly_forecast.size());
        //拿到当前的时间 模拟每小时预报的天气数据
        int foreCastHourly = Integer.parseInt(hour);
        Log.d(TAG, "initAdapterData: " + foreCastHourly);
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < strings.length; j++) {
                foreCastHourly = foreCastHourly + 1;
                HourlyForecastItem item = new HourlyForecastItem();
                int icon = chooseIcon(strings[random.nextInt(strings.length)]);
                String tmp = String.valueOf(20 + random.nextInt(10));
                item.setTem(tmp + getResources().getString(R.string.tem));
                item.setIconId(icon);
                hour = String.valueOf(foreCastHourly % 24);
                item.setHour(hour + "时");
                items.add(item);
            }
        }
        //更新Adapter里的数据显示
        mAdapter.notifyDataSetChanged();
    }

    private int chooseIcon(String string) {
        int id = 0;
        if (string.contains("大雨")) {
            id = R.drawable.heary_rain;
        } else if (string.contains("小雨")) {
            id = R.drawable.small_rain;
        } else if (string.contains("阴")) {
            id = R.drawable.cloud;
        } else if (string.contains("晴")) {
            id = R.drawable.sun;
        } else if (string.contains("雷")) {
            id = R.drawable.thunder;
        } else if (string.contains("大雪")) {
            id = R.drawable.heary_snow;
        } else {
            id = R.drawable.sun;
        }
        return id;
    }

    private static class ViewHolder {
        TextView date;
        TextView wea;
        TextView high_tem;
        TextView low_tem;
    }
}
