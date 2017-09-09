package com.dwj.coolweather;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.blankj.utilcode.util.TimeUtils;
import com.dwj.coolweather.bean.SelectCityItem;
import com.dwj.coolweather.db.CountyForSearch;
import com.dwj.coolweather.gson.Weather;
import com.dwj.coolweather.util.DataUtil;
import com.dwj.coolweather.util.HttpUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

public class SelectCityActivity extends AppCompatActivity {

    private static final String TAG = "SelectCityActivity";
    private ImageView mImageView;
    private RecyclerView mRecycle;
    private List<SelectCityItem> mList = new ArrayList<SelectCityItem>();
    private SelectCityAdapter mAdapter;
    private static final int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_city);
        mImageView = ((ImageView) findViewById(R.id.add));
        mRecycle = ((RecyclerView) findViewById(R.id.select_city_list));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(SelectCityActivity.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecycle.setLayoutManager(linearLayoutManager);
        mAdapter = new SelectCityAdapter(mList);
        mRecycle.setAdapter(mAdapter);
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(SelectCityActivity.this, SearchCityActivity.class), REQUEST_CODE);
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            CountyForSearch county = (CountyForSearch) data.getParcelableExtra(SearchCityActivity.CITY_CODE);
            String weatherUrl = "http://guolin.tech/api/weather?cityid=" +
                    county.getWeatherId() + "&key=a51a0df067ff48fd98aa27b1324594e7";
            HttpUtil.handleHttpRequest(weatherUrl, new okhttp3.Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, "onFailure: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final SelectCityItem selectCityItem = new SelectCityItem();
                    String string = response.body().string();
                    Weather weather = DataUtil.handleWeatherData(string);
                    String nowTime = getNowTime();
                    if (weather != null) {
                        String cityName = weather.getBasic().getCity();
                        String tmp = weather.getNow().getTmp();
                        selectCityItem.setCityName(cityName);
                        selectCityItem.setTem(tmp + SelectCityActivity.this.getResources().getString(R.string.tem));
                        selectCityItem.setTime(nowTime);
                    }
                    SelectCityActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mList.add(selectCityItem);
                            mAdapter.notifyDataSetChanged();
                        }
                    });
                }
            });
        }
        super.onActivityResult(requestCode, resultCode, data);
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
}
