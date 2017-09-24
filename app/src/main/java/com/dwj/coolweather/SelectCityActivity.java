package com.dwj.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.blankj.utilcode.util.TimeUtils;
import com.dwj.coolweather.bean.SelectCityItem;
import com.dwj.coolweather.db.CountyForSearch;
import com.dwj.coolweather.db.SelectCityWeatherData;
import com.dwj.coolweather.gson.Weather;
import com.dwj.coolweather.util.DataUtil;
import com.dwj.coolweather.util.HttpUtil;
import com.dwj.coolweather.util.ToolUtil;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

import static com.dwj.coolweather.Contacts.WEATHER_DATA;

public class SelectCityActivity extends AppCompatActivity {

    private static final String TAG = "SelectCityActivity";
    private RecyclerView mRecycle;
    private List<SelectCityItem> mList = new ArrayList<SelectCityItem>();
    private SelectCityAdapter mAdapter;
    public static final int REQUEST_CODE = 1;
    private SharedPreferences mShared;

    //初始化itemTouchHelpCallBack对象
    private DefaultItemTouchHelpCallBack mHelpCallBack = new DefaultItemTouchHelpCallBack(new DefaultItemTouchHelpCallBack.OnItemTouchCallBackListener() {
        @Override
        public void onSwiped(int position) {
            if (mList != null && mList.size() > 0) {
                //删除数据  更新ui
                //判断删除数据库还是SharedPreference里的数据
                SelectCityItem selectCityItem = mList.get(position);
                DataSupport.deleteAll(SelectCityWeatherData.class, "cityName = ?", selectCityItem.getCityName());
                Weather weather = null;
                if (mShared != null) {
                    String string = mShared.getString(WEATHER_DATA, null);
                    if (string != null) {
                        weather = DataUtil.handleWeatherData(string);
                    }
                }
                if (weather != null && weather.getBasic() != null) {
                    if (weather.getBasic().getCity().equals(selectCityItem.getCityName())) {
                        //如果相等的话 删除
                        mShared.edit().putString(WEATHER_DATA, null).apply();
                    }
                }
                Log.d(TAG, "onSwiped: " + position + " city name " + selectCityItem.getCityName());
                mList.remove(position);
                mAdapter.notifyItemRemoved(position);
                //重新更新显示列表的顺序
                if (mList.size() == 0) {
                    //如果列表删光了 退出程序
                    ActivityController.finishAll();
                }
            }
        }

        @Override
        public boolean onMove(int srcPosition, int targetPosition) {
            if (mList != null && mList.size() > 0) {
                Log.d(TAG, "onMove: " + "src position " + srcPosition + "target " + targetPosition);
                Collections.swap(mList, srcPosition, targetPosition);
                mAdapter.notifyItemMoved(srcPosition, targetPosition);
                return true;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_city);
        ActivityController.addActivity(SelectCityActivity.this);
        ToolUtil.fitStatusBar(SelectCityActivity.this);
        initBackupGround();
        mRecycle = ((RecyclerView) findViewById(R.id.select_city_list));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(SelectCityActivity.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        SelectCityItem last = new SelectCityItem();
        last.setLast(true);
        mList.add(last);
        //添加从主界面选出的城市
        mRecycle.setLayoutManager(linearLayoutManager);
        mAdapter = new SelectCityAdapter(mList);
        addWeatherFromMainActivity();
        addWeatherFromSQLite();
        mRecycle.setAdapter(mAdapter);
        //设置recycleView的拖拽和滑动删除事件
        mHelpCallBack.setLongPressDragEnabled(true);
        mHelpCallBack.setSwipeEnabled(true);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(mHelpCallBack);
        itemTouchHelper.attachToRecyclerView(mRecycle);
    }

    //添加从MainActivity中选中的城市
    private void addWeatherFromMainActivity() {
        mShared = PreferenceManager.getDefaultSharedPreferences(SelectCityActivity.this);
        String string = mShared.getString(WEATHER_DATA, null);
        add(string);
        //并且从数据库中读取选择过的城市天气信息
    }

    //添加数据库中的选中的天气信息
    private void addWeatherFromSQLite() {
        List<SelectCityWeatherData> weatherDataList = DataSupport.findAll(SelectCityWeatherData.class);
        Log.d(TAG, "addWeatherFromSQLite: " + weatherDataList.size());
        for (SelectCityWeatherData data : weatherDataList) {
            Log.d(TAG, "addWeatherFromSQLite: " + data.getCityName());
            add(data.getWeatherData());
        }
    }

    private void add(String string) {
        if (string != null && string.length() > 0) {
            //解析生成weather对象
            Weather weather = DataUtil.handleWeatherData(string);
            SelectCityItem selectCityItem = getSelectCityItem(weather);
            addToList(selectCityItem);
        }
    }

    private void initBackupGround() {
        ToolUtil.fitStatusBar(SelectCityActivity.this);
        ImageView background = (ImageView) findViewById(R.id.background);
        ToolUtil.initBackupGround(SelectCityActivity.this, background);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            final CountyForSearch county = (CountyForSearch) data.getParcelableExtra(SearchCityActivity.CITY_CODE);
            final String weatherUrl = "http://guolin.tech/api/weather?cityid=" +
                    county.getWeatherId() + "&key=a51a0df067ff48fd98aa27b1324594e7";
            HttpUtil.handleHttpRequest(weatherUrl, new okhttp3.Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, "onFailure: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String string = response.body().string();
                    //将选择的天气信息存储到数据库中持久化
                    //如果数据库中存在了 替换天气数据就可以了
                    List<SelectCityWeatherData> dataList = DataSupport.select("cityName").find(SelectCityWeatherData.class);
                    Log.d(TAG, "onResponse: name 1" + county.getCountyName());
                    boolean isInSQLite = false;
                    for (SelectCityWeatherData weatherData : dataList) {
                        Log.d(TAG, "onResponse: sqlite name " + weatherData.getCityName());
                        Log.d(TAG, "onResponse: whether is " + county.getCountyName().equals(weatherData.getCityName()));
                        if (county.getCountyName().equals(weatherData.getCityName())) {
                            //如果数据库中已经存在了这个城市信息,只需要更新天气信息
                            isInSQLite = true;
                        } else {
                            isInSQLite = false;
                        }
                    }
                    SelectCityWeatherData selectCityWeatherData = new SelectCityWeatherData();
                    if (isInSQLite) {
                        Log.d(TAG, "onResponse: update data");
                        selectCityWeatherData.setWeatherData(string);
                        selectCityWeatherData.updateAll("cityName = ?", county.getCountyName());
                    } else {
                        Log.d(TAG, "onResponse: add");
                        selectCityWeatherData.setWeatherData(string);
                        selectCityWeatherData.setWeatherUrl(weatherUrl);
                        selectCityWeatherData.setCityName(county.getCountyName());
                        selectCityWeatherData.save();
                    }
                    Weather weather = DataUtil.handleWeatherData(string);
                    final SelectCityItem selectCityItem = getSelectCityItem(weather);
                    SelectCityActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //防止重复添加
                            addToList(selectCityItem);

                        }
                    });
                }
            });
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void addToList(SelectCityItem selectCityItem) {
        if (mList.size() > 1) {
            forceAddAgain(selectCityItem);
        }
        mList.add(mList.size() - 1, selectCityItem);
        mAdapter.notifyItemInserted(mList.size() - 2);
        //mAdapter.notifyDataSetChanged();
        //将recycle定位到最后一行
        mRecycle.scrollToPosition(mList.size() - 1);
    }

    @NonNull
    private SelectCityItem getSelectCityItem(Weather weather) {
        final SelectCityItem selectCityItem = new SelectCityItem();
        String nowTime = getNowTime();
        if (weather != null) {
            String cityName = weather.getBasic().getCity();
            String tmp = weather.getNow().getTmp();
            selectCityItem.setCityName(cityName);
            selectCityItem.setTem(tmp + SelectCityActivity.this.getResources().getString(R.string.tem));
            selectCityItem.setTime(nowTime);
        }
        return selectCityItem;
    }

    //防止重复添加
    private void forceAddAgain(SelectCityItem item) {
        //如果有相等的 先删掉里面的 在添加最新的信息
        //不能在遍历的时候删除数据 recycle正确删除方式
        Log.d(TAG, "forceAddAgain: " + mList.size());
        for (int i = 0; i < mList.size(); i++) {
            Log.d(TAG, "forceAddAgain: " + item.getCityName());
            if (item.getCityName() != null) {
                if (item.getCityName().equals(mList.get(i).getCityName())) {
                    mAdapter.notifyItemRemoved(i);
                    mList.remove(i);
                    mAdapter.notifyItemRangeRemoved(0, mAdapter.getItemCount());
                    break;
                }
            }
        }
    }

    private String getNowTime() {
        String nowString = TimeUtils.getNowString();
        String[] split = nowString.split(" ");
        String[] times = split[1].split(":");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < times.length; i++) {
            if (i == 2) {
                break;
            } else {
                builder.append(times[i]);
            }
            builder.append(":");
        }
        return builder.delete(builder.length() - 1, builder.length()).toString();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        //重写了返回事件  当删除列表城市信息的时候 只能通过点击事件跳转
        //do nothing ....
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityController.removeActivity(SelectCityActivity.this);
    }
}
