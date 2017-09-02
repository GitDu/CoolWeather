package com.dwj.coolweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by duWenJun on 17-9-2.
 */

public class City extends DataSupport{

    private int id;
    private String cityName;
    private int cityCode;
    private int provinceId;

    public City() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getCityCode() {
        return cityCode;
    }

    public void setCityCode(int cityCode) {
        this.cityCode = cityCode;
    }

    public int getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId;
    }
}
