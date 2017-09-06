package com.dwj.coolweather;

/**
 * Created by duWenJun on 17-9-6.
 */

public class HourlyForecastItem {
    private String hour;
    private int iconId;
    private String tem;

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public int getIconId() {
        return iconId;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }

    public String getTem() {
        return tem;
    }

    public void setTem(String tem) {
        this.tem = tem;
    }
}
