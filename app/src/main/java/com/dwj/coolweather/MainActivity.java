package com.dwj.coolweather;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * 获得省 市 县的名称信息 weather id
 * 注册天气查询的key:http://console.heweather.com/register
 * 个人认证key: a51a0df067ff48fd98aa27b1324594e7
 * */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
