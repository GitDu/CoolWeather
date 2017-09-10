package com.dwj.coolweather;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.dwj.coolweather.service.AutoUpdateService;

public class SettingActivity extends AppCompatActivity {
    private static final String TAG = "SettingActivity";

    private SettingLayoutItem mVersion_up;
    private SettingLayoutItem mAuto_update;
    private SettingLayoutItem mAlarm;
    private SettingLayoutItem mLocation;
    private SettingLayoutItem mAbout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initView();
    }

    private void initView() {
        mAuto_update = ((SettingLayoutItem) findViewById(R.id.auto_update));
        mVersion_up = ((SettingLayoutItem) findViewById(R.id.version_up));
        mAlarm = ((SettingLayoutItem) findViewById(R.id.alarm));
        mLocation = ((SettingLayoutItem) findViewById(R.id.location));
        mAbout = ((SettingLayoutItem) findViewById(R.id.about));
    }

    /**功能 1.开启后台服务,自动更新数据
     *
     * */
    @Override
    protected void onResume() {
        super.onResume();
        mAuto_update.registerCallBack(new SettingLayoutItem.EventCallBack() {
            @Override
            public void callBack(boolean isCheck) {
                if (isCheck) {
                    //开启自动更新天气信息的服务
                    startService(new Intent(SettingActivity.this, AutoUpdateService.class));
                    Toast.makeText(SettingActivity.this, " 自动更新服务已开启 ", Toast.LENGTH_SHORT).show();
                } else {
                    //关闭自动更新天气信息的服务
                    stopService(new Intent(SettingActivity.this, AutoUpdateService.class));
                    Toast.makeText(SettingActivity.this, " 自动更新服务已关闭 ", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        //解回调注册
        mAuto_update.unRegisterCallBack();
        super.onDestroy();
    }
}
