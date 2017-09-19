package com.dwj.coolweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by duWenJun on 17-9-19.
 * 创建用来存储选择的城市天气数据库
 */

public class SelectCityWeatherData extends DataSupport {
    private int id;
    private String weatherData;
    private String cityName;

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getWeatherData() {
        return weatherData;
    }

    public void setWeatherData(String weatherData) {
        this.weatherData = weatherData;
    }

    public int getId() {

        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
