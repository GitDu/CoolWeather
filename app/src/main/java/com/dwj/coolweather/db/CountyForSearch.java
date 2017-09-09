package com.dwj.coolweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by duWenJun on 17-9-9.
 * 创建本地数据库 为了本地查询
 */

public class CountyForSearch extends DataSupport{
    private int id;
    private String countyName;
    private String weatherId;
    private int countyCode;

    public int getId() {
        return id;
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

    public int getCountyCode() {
        return countyCode;
    }

    public void setCountyCode(int countyCode) {
        this.countyCode = countyCode;
    }
}
