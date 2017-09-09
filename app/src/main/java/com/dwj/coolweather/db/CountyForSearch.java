package com.dwj.coolweather.db;

import android.os.Parcel;
import android.os.Parcelable;

import org.litepal.crud.DataSupport;

/**
 * Created by duWenJun on 17-9-9.
 * 创建本地数据库 为了本地查询
 */

public class CountyForSearch extends DataSupport implements Parcelable {
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.countyName);
        dest.writeString(this.weatherId);
        dest.writeInt(this.countyCode);
    }

    public CountyForSearch() {
    }

    protected CountyForSearch(Parcel in) {
        this.id = in.readInt();
        this.countyName = in.readString();
        this.weatherId = in.readString();
        this.countyCode = in.readInt();
    }

    public static final Parcelable.Creator<CountyForSearch> CREATOR = new Parcelable.Creator<CountyForSearch>() {
        @Override
        public CountyForSearch createFromParcel(Parcel source) {
            return new CountyForSearch(source);
        }

        @Override
        public CountyForSearch[] newArray(int size) {
            return new CountyForSearch[size];
        }
    };
}
