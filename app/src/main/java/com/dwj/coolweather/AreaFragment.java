package com.dwj.coolweather;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dwj.coolweather.db.City;
import com.dwj.coolweather.db.County;
import com.dwj.coolweather.db.Province;
import com.dwj.coolweather.util.DataUtil;
import com.dwj.coolweather.util.HttpUtil;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by duWenJun on 17-9-2.
 * 个人认证key: a51a0df067ff48fd98aa27b1324594e7
 */

public class AreaFragment extends Fragment {

    private static final String TAG = "AreaFragment";
    private ListView mListView;
    private ImageView mBack;
    private TextView mText;
    private FragmentActivity mContext;

    private ArrayList<String> mDataList = new ArrayList<String>();
    private List<Province> mProvincesList;
    private List<City> mCityList;
    private List<County> mCountyList;


    private static final int QUERY_PROVINCE = 0;
    private static final int QUERY_CITY = 1;
    private static final int QUERY_COUNTY = 2;

    private int mQueryNumber = QUERY_PROVINCE;
    private ArrayAdapter<String> mAdapter;
    private ProgressDialog mProgressDialog;
    private Province mSelectProvince;
    private City mSelectCity;

    public AreaFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.area_fragment, container, false);
        mContext = getActivity();
        mListView = ((ListView) view.findViewById(R.id.list_view));
        mBack = ((ImageView) view.findViewById(R.id.back));
        mText = ((TextView) view.findViewById(R.id.title));
        mAdapter = new ArrayAdapter<>(mContext,
                android.R.layout.simple_dropdown_item_1line, mDataList);
        mListView.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (mQueryNumber) {
                    case QUERY_PROVINCE:
                        mQueryNumber = QUERY_CITY;
                        mSelectProvince = mProvincesList.get(i);
                        queryCity();
                        break;
                    case QUERY_CITY:
                        mQueryNumber = QUERY_COUNTY;
                        mSelectCity = mCityList.get(i);
                        queryCounty();
                        break;
                }
            }
        });

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mQueryNumber == QUERY_COUNTY) {
                    mQueryNumber = QUERY_CITY;
                    queryCity();
                } else if (mQueryNumber == QUERY_CITY) {
                    mQueryNumber = QUERY_PROVINCE;
                    queryProvince();
                }
            }
        });
        queryProvince();
    }

    private void queryCounty() {
        mBack.setVisibility(View.VISIBLE);
        mText.setText(mSelectCity.getCityName());
        mCountyList = DataSupport.where("cityId= ?", String.valueOf(mSelectCity.getCityCode())).find(County.class);
        if (mCountyList.size() > 0) {
            mDataList.clear();
            for (County county : mCountyList) {
                mDataList.add(county.getCountyName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
        } else {
            int provinceCode = mSelectCity.getProvinceId();
            int cityCode = mSelectCity.getCityCode();
            String countyUrl = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            Log.d(TAG, "queryCounty: " + provinceCode + "cityCode " + cityCode);
            queryFromService(countyUrl);
        }
    }

    private void queryCity() {
        mBack.setVisibility(View.VISIBLE);
        mText.setText(mSelectProvince.getProvinceName());
        mCityList = DataSupport.where("provinceId= ?", String.valueOf(mSelectProvince.getProvinceCode())).find(City.class);
        if (mCityList.size() > 0) {
            mDataList.clear();
            for (City city : mCityList) {
                mDataList.add(city.getCityName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
        } else {
            int provinceCode = mSelectProvince.getProvinceCode();
            Log.d(TAG, "queryCity: " + provinceCode);
            String cityUrl = "http://guolin.tech/api/china/" + provinceCode;
            queryFromService(cityUrl);
        }

    }

    private void queryProvince() {
        mBack.setVisibility(View.GONE);
        mText.setText(R.string.china);
        mProvincesList = DataSupport.findAll(Province.class);
        if (mProvincesList.size() > 0) {
            //直接更新数据
            mDataList.clear();
            for (Province province : mProvincesList) {
                mDataList.add(province.getProvinceName());
            }
            mAdapter.notifyDataSetChanged();
            //定位到list的第一个
            mListView.setSelection(0);
        } else {
            String provinceUrl = "http://guolin.tech/api/china";
            queryFromService(provinceUrl);
        }
    }

    private void queryFromService(String url) {
        showProgressDialog();
        HttpUtil.handleHttpRequest(url, new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, " 网络请求失败", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onFailure: " + "网络请求失败");
                        dismissProgressDialog();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String data = response.body().string();
                boolean isSuccess = false;
                if (mQueryNumber == QUERY_PROVINCE) {
                    isSuccess = DataUtil.saveProvinceData(data);
                } else if (mQueryNumber == QUERY_CITY) {
                    isSuccess = DataUtil.saveCityData(data, mSelectProvince.getProvinceCode());
                } else if (mQueryNumber == QUERY_COUNTY) {
                    isSuccess = DataUtil.saveCountyData(data, mSelectCity.getCityCode());
                }

                if (isSuccess) {
                    mContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissProgressDialog();
                            if (mQueryNumber == QUERY_PROVINCE) {
                                queryProvince();
                            } else if (mQueryNumber == QUERY_CITY) {
                                queryCity();
                            } else if (mQueryNumber == QUERY_COUNTY) {
                                queryCounty();
                            }
                        }
                    });
                } else {
                    mContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissProgressDialog();
                            Toast.makeText(mContext, " 数据库存储失败 ", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "onResponse: " + "数据库存储失败");

                        }
                    });
                }
            }
        });
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setTitle("网络请求");
            mProgressDialog.setMessage("waiting. . .");
        }
        mProgressDialog.show();
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

}
