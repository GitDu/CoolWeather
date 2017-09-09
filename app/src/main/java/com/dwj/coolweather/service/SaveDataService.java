package com.dwj.coolweather.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.dwj.coolweather.db.CountyForSearch;
import com.dwj.coolweather.db.Province;
import com.dwj.coolweather.util.DataUtil;
import com.dwj.coolweather.util.HttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.dwj.coolweather.util.DataUtil.JSON_DATA_ID;
import static com.dwj.coolweather.util.DataUtil.JSON_DATA_NAME;
import static com.dwj.coolweather.util.DataUtil.WEATHER_ID;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * 初始化本地数据库
 * helper methods.
 */
public class SaveDataService extends IntentService {
    private static final String TAG = "SaveDataService";
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "com.dwj.coolweather.service.action.FOO";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "CountyDataList";

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Toast.makeText(SaveDataService.this, " 数据加载存储完成 " , Toast.LENGTH_SHORT).show();
        }
    };

    public SaveDataService() {
        super("SaveDataService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, List<Province> list) {
        Log.d(TAG, "startActionFoo: ");
        final ArrayList<Province> arrayList = ((ArrayList<Province>) list);
        Intent intent = new Intent(context, SaveDataService.class);
        intent.setAction(ACTION_FOO);
        intent.putParcelableArrayListExtra(EXTRA_PARAM1, arrayList);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            ArrayList<Province> list = intent.getParcelableArrayListExtra(EXTRA_PARAM1);
            handleActionFoo(list);
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(ArrayList<Province> list) {
        // TODO: Handle action Foo
        for (Province province : list) {
            Log.d(TAG, "handleActionFoo: " + province.getProvinceName() + " id " + province.getProvinceCode());
            //遍历查询各省的城市
            final int provinceCode = province.getProvinceCode();
            String cityUrl = "http://guolin.tech/api/china/" + provinceCode;
            HttpUtil.handleHttpRequest(cityUrl, new okhttp3.Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, "onFailure: city " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String data = response.body().string();
                    Log.d(TAG, "onResponse: data " + data);
                    JSONArray jsonArray = null;
                    try {
                        jsonArray = new JSONArray(data);
                        int items = jsonArray.length();
                        for (int i = 0; i < items; i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            int cityCode = jsonObject.getInt(JSON_DATA_ID);
                            Log.d(TAG, "onResponse: city " + cityCode);
                            saveDataToLocal(provinceCode, cityCode);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

    }

    private void saveDataToLocal(int provinceCode, int cityCode) {
        String countyUrl = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
        Log.d(TAG, "saveDataToLocal: " + " province code " + provinceCode + " city code " + cityCode);
        //查询到数据库中
        HttpUtil.handleHttpRequest(countyUrl, new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: county " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String data = response.body().string();
                putInToSQLite(data);
            }
        });
    }


    private void putInToSQLite(String data) {
        try {
            JSONArray jsonArray = new JSONArray(data);
            int items = jsonArray.length();
            for (int i = 0; i < items; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                CountyForSearch countyForSearch = new CountyForSearch();
                countyForSearch.setCountyName(jsonObject.getString(JSON_DATA_NAME));
                countyForSearch.setCountyCode(jsonObject.getInt(JSON_DATA_ID));
                countyForSearch.setWeatherId(jsonObject.getString(WEATHER_ID));
                countyForSearch.save();
            }
                Log.d(TAG, "putInToSQLite: " + " hans done ");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //暂时不知道什么时候数据库加载结束
         mHandler.sendEmptyMessage(0);
    }
}