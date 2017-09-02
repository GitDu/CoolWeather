package com.dwj.coolweather.util;

import android.text.TextUtils;
import android.util.Log;

import com.dwj.coolweather.db.City;
import com.dwj.coolweather.db.County;
import com.dwj.coolweather.db.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by duWenJun on 17-9-2.
 */

public class DataUtil {

    private static final String TAG = "DataUtil";
    private static final String JSON_DATA_NAME = "name";
    private static final String JSON_DATA_ID = "id";
    private static final String WEATHER_ID = "weather_id";

    public static boolean saveProvinceData(String data) {
        //String data = "[{\"id\":1,\"name\":\"北京\"},{\"id\":2,\"name\":\"上海\"},{\"id\":3,\"name\":\"天津\"},{\"id\":4,\"name\":\"重庆\"},{\"id\":5,\"name\":\"香港\"},{\"id\":6,\"name\":\"澳门\"},{\"id\":7,\"name\":\"台湾\"},{\"id\":8,\"name\":\"黑龙江\"},{\"id\":9,\"name\":\"吉林\"},{\"id\":10,\"name\":\"辽宁\"},{\"id\":11,\"name\":\"内蒙古\"},{\"id\":12,\"name\":\"河北\"},{\"id\":13,\"name\":\"河南\"},{\"id\":14,\"name\":\"山西\"},{\"id\":15,\"name\":\"山东\"},{\"id\":16,\"name\":\"江苏\"},{\"id\":17,\"name\":\"浙江\"},{\"id\":18,\"name\":\"福建\"},{\"id\":19,\"name\":\"江西\"},{\"id\":20,\"name\":\"安徽\"},{\"id\":21,\"name\":\"湖北\"},{\"id\":22,\"name\":\"湖南\"},{\"id\":23,\"name\":\"广东\"},{\"id\":24,\"name\":\"广西\"},{\"id\":25,\"name\":\"海南\"},{\"id\":26,\"name\":\"贵州\"},{\"id\":27,\"name\":\"云南\"},{\"id\":28,\"name\":\"四川\"},{\"id\":29,\"name\":\"西藏\"},{\"id\":30,\"name\":\"陕西\"},{\"id\":31,\"name\":\"宁夏\"},{\"id\":32,\"name\":\"甘肃\"},{\"id\":33,\"name\":\"青海\"},{\"id\":34,\"name\":\"新疆\"}]";
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
}
