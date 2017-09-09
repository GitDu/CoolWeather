package com.dwj.coolweather.util;

import android.text.TextUtils;
import android.util.Log;

import com.dwj.coolweather.db.City;
import com.dwj.coolweather.db.County;
import com.dwj.coolweather.db.Province;
import com.dwj.coolweather.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by duWenJun on 17-9-2.
 */

public class DataUtil {

    private static final String TAG = "DataUtil";
    public static final String JSON_DATA_NAME = "name";
    public static final String JSON_DATA_ID = "id";
    public static final String WEATHER_ID = "weather_id";

    public static boolean saveProvinceData(String data) {
        Log.d(TAG, "saveProvinceData: " + data);
        if (!TextUtils.isEmpty(data)) {
            try {
                JSONArray jsonArray = new JSONArray(data);
                int items = jsonArray.length();
                for (int i = 0; i < items; i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(jsonObject.getString(JSON_DATA_NAME));
                    province.setProvinceCode(jsonObject.getInt(JSON_DATA_ID));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean saveCityData(String data, int provinceId) {
        Log.d(TAG, "saveCityData: " + data + " provinceId " + provinceId);
        if (!TextUtils.isEmpty(data)) {
            try {
                JSONArray jsonArray = new JSONArray(data);
                int items = jsonArray.length();
                for (int i = 0; i < items; i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    City city = new City();
                    city.setCityName(jsonObject.getString(JSON_DATA_NAME));
                    city.setCityCode(jsonObject.getInt(JSON_DATA_ID));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public static boolean saveCountyData(String data, int cityId) {
        Log.d(TAG, "saveCountyData: " + data);
        if (!TextUtils.isEmpty(data)) {
            JSONArray jsonArray = null;
            try {
                jsonArray = new JSONArray(data);
                int items = jsonArray.length();
                for (int i = 0; i < items; i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(jsonObject.getString(JSON_DATA_NAME));
                    county.setCountyCode(jsonObject.getInt(JSON_DATA_ID));
                    county.setWeatherId(jsonObject.getString(WEATHER_ID));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static Weather handleWeatherData(String weatherData) {
        try {
            JSONObject jsonObject = new JSONObject(weatherData);
            JSONArray heWeather = jsonObject.getJSONArray("HeWeather");
            Log.d(TAG, "handleWeatherData: heWeather" + heWeather.get(0).toString());
            Gson gson = new Gson();
           return gson.fromJson(heWeather.get(0).toString(), Weather.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
