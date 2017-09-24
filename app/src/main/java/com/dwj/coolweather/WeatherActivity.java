package com.dwj.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.blankj.utilcode.util.ConvertUtils;
import com.dwj.coolweather.db.SelectCityWeatherData;
import com.dwj.coolweather.util.DataUtil;
import com.dwj.coolweather.util.ToolUtil;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import static com.dwj.coolweather.Contacts.CHOSE_CITY;
import static com.dwj.coolweather.Contacts.CITY_LIST;
import static com.dwj.coolweather.Contacts.FROM_SELECT_ACTIVITY;
import static com.dwj.coolweather.Contacts.WEATHER_DATA;
import static com.dwj.coolweather.Contacts.WEATHER_URL;

public class WeatherActivity extends AppCompatActivity {

    private static final String TAG = "WeatherActivity";
    private DrawerLayout mDraw;
    private List<WeatherFragment> mFragments = new ArrayList<WeatherFragment>();
    private List<String> mCityStrings = new ArrayList<String>();
    private int index = -1;
    private ViewPager mContain;
    private MyAdapter mAdapter;
    private SharedPreferences mShared;
    private LinearLayout mWeatherIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        Log.d(TAG, "onCreate: ");
        ActivityController.addActivity(WeatherActivity.this);
        mDraw = ((DrawerLayout) findViewById(R.id.draw_layout));
        mContain = ((ViewPager) findViewById(R.id.pager_container));
        mAdapter = new MyAdapter(getSupportFragmentManager(), mFragments);
        ImageView choose = (ImageView) findViewById(R.id.choose_weathers);
        mWeatherIndex = ((LinearLayout) findViewById(R.id.weather_index));
        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(WeatherActivity.this, SelectCityActivity.class));
                (WeatherActivity.this).finish();
            }
        });
        mContain.setAdapter(mAdapter);
        mContain.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.d(TAG, "onPageScrolled: ------------------");
            }

            @Override
            public void onPageSelected(int position) {
                //滑动的过程中动态设置圆点的位置
                Log.d(TAG, "onPageSelected: ++++++++++");
                int childCount = mWeatherIndex.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    ImageView childAt = (ImageView) mWeatherIndex.getChildAt(i);
                    if (i == position) {
                        childAt.setImageResource(R.drawable.gray_shape);
                    } else {
                        childAt.setImageResource(R.drawable.while_shape);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                Log.d(TAG, "onPageScrollStateChanged: ");
            }
        });
        ToolUtil.fitStatusBar(WeatherActivity.this);
        mShared = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
        initData();
        initDotViews();

    }

    private void initDotViews() {
        //数据初始化之后 根据数据的多少添加引导圈的个数
        float indexWidth = 8;
        mWeatherIndex.removeAllViews();
        for (int i = 0; i < mFragments.size(); i++) {
            Log.d(TAG, "onResume: add");
            ImageView imageView = new ImageView(WeatherActivity.this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER;
            if (i != (mFragments.size() - 1)) {
                //设置间隔
                layoutParams.rightMargin = ConvertUtils.dp2px(indexWidth);
            }

            if (i == index) {
                //选中的设置为灰色
                imageView.setImageResource(R.drawable.gray_shape);
            } else {
                //没有选中的设置成白色
                imageView.setImageResource(R.drawable.while_shape);
            }
            imageView.setLayoutParams(layoutParams);
            mWeatherIndex.addView(imageView);
        }
    }

    private void initData() {
        //判断是否是从选择页过来的
        mFragments.clear();
        String urlString = null;
        String dataString = null;
        String chooseCity = null;
        ArrayList<String> cityList;
        if (getIntent().getBooleanExtra(FROM_SELECT_ACTIVITY, false)) {
            //从选择城市列表页跳转过来
            Log.d(TAG, "initData: " + "from selectActivity");
            //跳转的时候直接选中点击的城市页
            chooseCity = getIntent().getStringExtra(CHOSE_CITY);
            cityList = getIntent().getStringArrayListExtra(CITY_LIST);
            //获得存储的天气数据
            Log.d(TAG, "initData: " + cityList.size());
            if (cityList.size() > 0) {
                for (String s : cityList) {
                    Log.d(TAG, "initData: " + s);
                    if (s != null) {
                        mCityStrings.add(s);
                        //SelectCityWeatherData 查询这个数据库的地址和天气信息 数据只有在数据库中或SharedPreferences中
                        List<SelectCityWeatherData> weatherDataList = DataSupport.where("cityName = ?", s).find(SelectCityWeatherData.class);
                        if (weatherDataList != null && weatherDataList.size() > 0) {
                            urlString = weatherDataList.get(0).getWeatherUrl();
                            dataString = weatherDataList.get(0).getWeatherData();
                        } else {
                            SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
                            dataString = shared.getString(WEATHER_DATA, null);
                            urlString = DataUtil.getWeatherUrlPath(WeatherActivity.this);
                        }
                        //初始化保存fragment的容器
                        mFragments.add(WeatherFragment.newInstance(urlString, dataString));
                    }
                }
                index = mCityStrings.indexOf(chooseCity);
            }
        } else {
            //从主页边跳转过来,默认选择的是sharedPreference里的城市项
            Log.d(TAG, "initData: shared");
            urlString = getIntent().getStringExtra(WEATHER_URL);
            dataString = mShared.getString(WEATHER_DATA, null);
            index = 0;
            mFragments.add(WeatherFragment.newInstance(urlString, dataString));
            //并且查询之前保存的数据库
            List<SelectCityWeatherData> cityWeatherData = DataSupport.findAll(SelectCityWeatherData.class);
            Log.d(TAG, "initData: " + cityWeatherData.size());
            for (SelectCityWeatherData cityWeatherDatum : cityWeatherData) {
                Log.d(TAG, "initData: "  + cityWeatherDatum.getCityName());
                urlString = cityWeatherDatum.getWeatherUrl();
                dataString = cityWeatherDatum.getWeatherData();
                mFragments.add(WeatherFragment.newInstance(urlString, dataString));
            }
        }
        mAdapter.notifyDataSetChanged();
        //指定选择点击页
        if (index != -1) {
            mContain.setCurrentItem(index, true);
        }
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
        //从侧划页跳转过来 更新当前viewPager对应的页面 过来之前删掉了shared里的数据
        //拿到当前的viewPager页
        Log.d(TAG, "onNewIntent: " + index);
//        String urlString = getIntent().getStringExtra(WEATHER_URL);
//        mListener.callBackup(urlString);
        super.onNewIntent(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityController.removeActivity(WeatherActivity.this);
    }

    private static class MyAdapter extends FragmentStatePagerAdapter {
        private List<WeatherFragment> mList;

        public MyAdapter(FragmentManager fm, List<WeatherFragment> list) {
            super(fm);
            this.mList = list;
        }

        @Override
        public Fragment getItem(int position) {
            return mList.get(position);
        }

        @Override
        public int getCount() {
            return mList.size();
        }
    }
}
