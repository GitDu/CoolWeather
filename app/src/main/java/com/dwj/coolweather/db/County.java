package com.dwj.coolweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by duWenJun on 17-9-2.
 */

public class County extends DataSupport {

    private int id;
    private String countyName;
    private String weatherId;
    private int countyCode;
    private int cityId;

    public County() {
    }

    public int getId() {
        return id;
    }

    public int getCountyCode() {
        return countyCode;
    }

    public void setCountyCode(int countyCode) {
        this.countyCode = countyCode;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public String getWeatherId() {
        return weatherId;
    }

    public void setWeatherId(String weatherId) {
        this.weatherId = weatherId;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }
}
